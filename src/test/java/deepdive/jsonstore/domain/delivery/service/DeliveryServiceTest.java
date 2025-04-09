package deepdive.jsonstore.domain.delivery.service;

import com.google.firebase.messaging.FirebaseMessaging;
import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Rollback
@DisplayName("DeliveryService 테스트")
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @MockitoBean
    private FirebaseConfig firebaseConfig;

    @MockitoBean
    private RedisTestService redisTestService;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private DeliveryValidationService deliveryValidationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    private Member member;

    @BeforeEach
    void init() { //해당 테이블 데이터 삭제하고 시작
        deliveryRepository.deleteAll();
        memberRepository.deleteAll();
        member = createMember("test" + UUID.randomUUID() + "@example.com", "test" + UUID.randomUUID());

    }

    private Member createMember(String email, String username) {
        Member member = Member.builder()
                .email(email)
                .username(username)
                .password("pw")
                .uid(UUID.randomUUID())
                .isDeleted(false)
                .build();
        return memberRepository.save(member);
    }

    private Delivery createDelivery(Member member, String recipient) {
        Delivery delivery = Delivery.builder()
                .uid(UUID.randomUUID())
                .address("서울")
                .zipCode("12345")
                .phone("010-1234-5678")
                .recipient(recipient)
                .member(member)
                .build();
        return deliveryRepository.save(delivery);
    }

    @Nested
    @DisplayName("기본 배송지 설정")
    class DefaultDeliverySetting {

        @Nested
        @DisplayName("성공 케이스")
        class Success {

            @Test
            @DisplayName("기본 배송지 정상 설정")
            void setDefaultDelivery() {
                // given
                Delivery delivery = createDelivery(member, "기본 배송지 정상 설정");

                // when
                deliveryService.setDeliveryDefault(member.getEmail(), delivery.getUid());

                // then
                Member result = memberRepository.findByEmail(member.getEmail()).orElseThrow();
                assertThat(result.getDefaultDelivery().getUid()).isEqualTo(delivery.getUid());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {

            @Test
            @DisplayName("자신의 배송지가 아닐 경우 예외 발생")
            void setDefault_notOwner() {
                // given
                Delivery delivery = createDelivery(member, "자신의 배송지가 아닐 경우 예외 발생");

                Member unknown = createMember("unknown@example.com", "unknown");

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.setDeliveryDefault(unknown.getEmail(), delivery.getUid())
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);
            }

            @Test
            @DisplayName("존재하지 않는 배송지 UID일 경우 예외 발생")
            void setDefault_invalidDelivery() {
                // given
                UUID nonExistentUid = UUID.randomUUID();

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.setDeliveryDefault(member.getEmail(), nonExistentUid)
                ).isInstanceOf(EntityNotFoundException.class);
            }
        }
    }

    @Nested
    @DisplayName("배송지 등록")
    class CreateDelivery {
        @Nested
        @DisplayName("성공 케이스")
        class Success {
            @Test
            @DisplayName("배송지 정상 등록")
            void createNewDelivery() {
                // given
                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "서울",
                        "12345",
                        "010-1234-5678",
                        "배송지 정상 등록"
                );

                // mock: zipCode 유효성 검사 성공하도록 처리
                Mockito.when(deliveryValidationService.validateZipCode("12345")).thenReturn(true);

                // when
                deliveryService.createDelivery(member.getEmail(), dto);

                // then
                List<Delivery> deliveries = deliveryRepository.findAll();

                assertThat(deliveries).hasSize(1);
                Delivery saved = deliveries.get(0);
                assertThat(saved.getAddress()).isEqualTo(dto.address());
                assertThat(saved.getZipCode()).isEqualTo(dto.zipCode());
                assertThat(saved.getPhone()).isEqualTo(dto.phone());
                assertThat(saved.getRecipient()).isEqualTo(dto.recipient());
                assertThat(saved.getMember().getEmail()).isEqualTo(member.getEmail());
            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {
            @Test
            @DisplayName("존재하지 않는 회원의 경우 예외 발생")
            void createDelivery_notExistingMember() {
                // given
                String notExistingEmail = "test123@example.com";

                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "서울",
                        "12345",
                        "010-1234-5678",
                        "존재하지 않는 회원 예외 발생"
                );

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.createDelivery(notExistingEmail, dto)
                ).isInstanceOf(EntityNotFoundException.class);
            }

            @Test
            @DisplayName("존재하지 않는 주소일 경우 예외 발생")
            void createDelivery_invalidZipCode() {
                // given
                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "서울",
                        "12345",
                        "010-1234-5678",
                        "존재하지 않는 주소 예외 발생"
                );

                // mock: zipCode 유효성 검사 성공하도록 처리
                Mockito.when(deliveryValidationService.validateZipCode("12345")).thenReturn(false);

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.createDelivery(member.getEmail(), dto)
                ).isInstanceOf(DeliveryException.AddressNotFoundException.class);
            }

        }
    }

    @Nested
    @DisplayName("배송지 삭제")
    class DeleteDelivery {

        @Nested
        @DisplayName("성공 케이스")
        class Success {
            @Test
            @DisplayName("배송지 삭제 성공")
            void deleteDelivery() {
                //given
                Delivery delivery = createDelivery(member, "배송지 삭제 성공");

                //when
                deliveryService.deleteDelivery(member.getEmail(), delivery.getUid());

                //then
                boolean exists = deliveryRepository.existsByUid(delivery.getUid());
                assertThat(exists).isFalse();

            }

        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {
            @Test
            @DisplayName("등록자가 일치하지 않을 경우 예외 발생")
            void deleteDelivery_AccessDenied() {
                //given
                Member member2 = createMember("test2@example.com", "tester2");
                Delivery delivery = createDelivery(member, "등록자 불일치 예외 발생");

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.deleteDelivery(member2.getEmail(), delivery.getUid())
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);

            }

            @Test
            @DisplayName("배송지가 존재하지 않을 경우 예외 발생")
            void deleteDelivery_DeliveryNotFound() {

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.deleteDelivery(member.getEmail(), UUID.randomUUID())
                ).isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
            }

        }
    }

    @Nested
    @DisplayName("배송지 조회")
    class GetDelivery {

        @Nested
        @DisplayName("성공 케이스")
        class Success {
            @Test
            @DisplayName("배송지 정상 조회")
            void getDelivery() {
                //given
                Delivery delivery1 = createDelivery(member, "배송지 조회1");
                Delivery delivery2 = createDelivery(member, "배송지 조회2");

                //when
                List<DeliveryResponseDTO> result = deliveryService.getDelivery(member.getEmail());

                //then
                assertThat(result).hasSize(2);
                assertThat(result)
                        .extracting("recipient")
                        .containsExactlyInAnyOrder("배송지 조회1", "배송지 조회2");
            }
        }


        @Nested
        @DisplayName("실패 케이스")
        class Failure {
            @Test
            @DisplayName("존재하지 않는 회원의 경우 예외 발생")
            void getDelivery_MemberNotFound() {
                //given
                String invalidEmail = "test12345@example.com";

                //when & then
                assertThatThrownBy(()->
                        deliveryService.getDelivery(invalidEmail))
                        .isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);
            }
        }
    }

    @Nested
    @DisplayName("배송지 수정")
    class UpdateDelivery {

        @Nested
        @DisplayName("성공 케이스")
        class Success {
            @Test
            @DisplayName("배송지 정상 수정")
            void updateDelivery() {
                //given
                Delivery delivery = createDelivery(member,"배송지 정상 등록");
                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "부산",
                        "00000",
                        "010-0000-0000",
                        "수정됨"
                );

                // mock: zipCode 유효성 검사 성공하도록 처리
                Mockito.when(deliveryValidationService.validateZipCode("00000")).thenReturn(true);

                //when
                deliveryService.updateDelivery(member.getEmail(),delivery.getUid(),dto);

                //then
                Delivery updated = deliveryRepository.findById(delivery.getId()).orElseThrow();

                assertThat(updated.getAddress()).isEqualTo(dto.address());
                assertThat(updated.getZipCode()).isEqualTo(dto.zipCode());
                assertThat(updated.getPhone()).isEqualTo(dto.phone());
                assertThat(updated.getRecipient()).isEqualTo(dto.recipient());

            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class Failure {
            @Test
            @DisplayName("등록자가 일치하지 않을 경우 예외 발생")
            void updateDelivery_MemberNotFound() {
                //given
                Member member2 = createMember("test2@example.com", "tester2");
                Delivery delivery = createDelivery(member, "등록자 불일치 수정");
                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "부산",
                        "00000",
                        "010-0000-0000",
                        "수정됨"
                );

                // mock: zipCode 유효성 검사 성공하도록 처리
                Mockito.when(deliveryValidationService.validateZipCode("00000")).thenReturn(false);

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.updateDelivery(member2.getEmail(), delivery.getUid(), dto)
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);

            }

            @Test
            @DisplayName("배송지가 존재하지 않을 경우 예외 발생")
            void updateDelivery_invalidZipCode() {
                //given
                UUID uuid = UUID.randomUUID();
                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "서울",
                        "00000",
                        "010-0000-0000",
                        "없는배송지"
                );

                // mock: zipCode 유효성 검사 성공하도록 처리
                Mockito.when(deliveryValidationService.validateZipCode("00000")).thenReturn(true);


                //when & then
                assertThatThrownBy(() ->
                        deliveryService.updateDelivery(member.getEmail(), uuid, dto)
                ).isInstanceOf(DeliveryException.DeliveryNotFoundException.class);

            }
        }
    }
}


