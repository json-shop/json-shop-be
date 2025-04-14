package deepdive.jsonstore.domain.delivery.service;

import com.google.firebase.messaging.FirebaseMessaging;
import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.delivery.dto.DeliveryRegRequestDTO;
import deepdive.jsonstore.domain.delivery.dto.DeliveryResponseDTO;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Transactional
@Rollback
@DisplayName("DeliveryService 테스트")
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @MockitoBean
    private DeliveryAddressValidationService deliveryAddressValidationService;

    @MockitoBean
    private FirebaseConfig firebaseConfig;

    @MockitoBean
    private RedisTestService redisTestService;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;


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
                deliveryService.setDeliveryDefault(member.getUid(), delivery.getUid());

                // then
                Member result = memberRepository.findByUid(member.getUid()).orElseThrow();
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
                        deliveryService.setDeliveryDefault(unknown.getUid(), delivery.getUid())
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);
            }

            @Test
            @DisplayName("존재하지 않는 배송지 UID일 경우 예외 발생")
            void setDefault_invalidDelivery() {
                // given
                UUID nonExistentUid = UUID.randomUUID();

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.setDeliveryDefault(member.getUid(), nonExistentUid)
                ).isInstanceOf(DeliveryException.DeliveryNotFoundException.class);
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
                doNothing().when(deliveryAddressValidationService).validateZipCode("12345");

                // when
                deliveryService.createDelivery(member.getUid(), dto);

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
                UUID nonExistentUid = UUID.randomUUID();

                DeliveryRegRequestDTO dto = new DeliveryRegRequestDTO(
                        "서울",
                        "12345",
                        "010-1234-5678",
                        "존재하지 않는 회원 예외 발생"
                );

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.createDelivery(nonExistentUid, dto)
                ).isInstanceOf(CommonException.class);
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
                doThrow(new DeliveryException.AddressNotFoundException())
                        .when(deliveryAddressValidationService)
                        .validateZipCode("12345");

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.createDelivery(member.getUid(), dto)
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
                deliveryService.deleteDelivery(member.getUid(), delivery.getUid());

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
                        deliveryService.deleteDelivery(member2.getUid(), delivery.getUid())
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);

            }

            @Test
            @DisplayName("배송지가 존재하지 않을 경우 예외 발생")
            void deleteDelivery_DeliveryNotFound() {

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.deleteDelivery(member.getUid(), UUID.randomUUID())
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
                List<DeliveryResponseDTO> result = deliveryService.getDelivery(member.getUid());

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
                UUID invalidUid = UUID.randomUUID();

                //when & then
                assertThatThrownBy(()->
                        deliveryService.getDelivery(invalidUid))
                        .isInstanceOf(MemberException.MemberNotFound.class);
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
                doNothing().when(deliveryAddressValidationService).validateZipCode("00000");

                //when
                deliveryService.updateDelivery(member.getUid(),delivery.getUid(),dto);

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
//                doThrow(new DeliveryException.AddressNotFoundException())
//                        .when(deliveryValidationService)
//                        .validateZipCode("00000");

                //when & then
                assertThatThrownBy(() ->
                        deliveryService.updateDelivery(member2.getUid(), delivery.getUid(), dto)
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
//                doNothing().when(deliveryValidationService).validateZipCode("00000");


                //when & then
                assertThatThrownBy(() ->
                        deliveryService.updateDelivery(member.getUid(), uuid, dto)
                ).isInstanceOf(DeliveryException.DeliveryNotFoundException.class);

            }
        }
    }
}


