package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.common.config.FirebaseConfig;
import deepdive.jsonstore.common.config.RedisTestService;
import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Rollback(false)
@DisplayName("DeliveryService 기본배송지 관련 테스트")
class DeliveryServiceTest {

    @Autowired
    private DeliveryService deliveryService;

    @MockitoBean
    private FirebaseConfig firebaseConfig;

    @MockitoBean
    private RedisTestService redisTestService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @BeforeEach
    void clean() { //해당 테이블 데이터 삭제하고 시작
        deliveryRepository.deleteAll();
        memberRepository.deleteAll();
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

    private Delivery createDelivery(Member member) {
        Delivery delivery = Delivery.builder()
                .uid(UUID.randomUUID())
                .address("서울")
                .zipCode("12345")
                .phone("010-1234-5678")
                .recipient("이인구")
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
                Member member = createMember("user1@example.com", "user1");
                Delivery delivery = createDelivery(member);

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
                Member owner = createMember("owner@example.com", "owner");
                Delivery delivery = createDelivery(owner);

                Member attacker = createMember("unknown@example.com", "unknown");

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.setDeliveryDefault(attacker.getEmail(), delivery.getUid())
                ).isInstanceOf(DeliveryException.DeliveryAccessDeniedException.class);
            }

            @Test
            @DisplayName("존재하지 않는 배송지 UID일 경우 예외 발생")
            void setDefault_invalidDelivery() {
                // given
                Member member = createMember("user2@example.com", "tester");
                UUID nonExistentUid = UUID.randomUUID();

                // when & then
                assertThatThrownBy(() ->
                        deliveryService.setDeliveryDefault(member.getEmail(), nonExistentUid)
                ).isInstanceOf(EntityNotFoundException.class);
            }
        }
    }
}
