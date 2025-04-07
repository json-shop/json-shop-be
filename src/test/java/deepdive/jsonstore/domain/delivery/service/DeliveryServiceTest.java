package deepdive.jsonstore.domain.delivery.service;

import deepdive.jsonstore.common.exception.DeliveryException;
import deepdive.jsonstore.domain.delivery.entity.Delivery;
import deepdive.jsonstore.domain.delivery.repository.DeliveryRepository;
import deepdive.jsonstore.domain.member.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void deleteDelivery_존재하지않는_배송지_예외() {
        UUID uuid = UUID.randomUUID();
        String email = "test@example.com";

        when(deliveryRepository.findByUid(uuid)).thenReturn(Optional.empty());

        assertThrows(DeliveryException.DeliveryNotFoundException.class, () ->
                deliveryService.deleteDelivery(email, uuid));
    }

    @Test
    void deleteDelivery_권한없음_예외() {
        UUID uuid = UUID.randomUUID();
        String email = "user1@example.com";
        Member otherUser = new Member();
        otherUser.setEmail("user2@example.com");

        Delivery delivery = new Delivery();
        delivery.setUuid(uuid);
        delivery.setMember(otherUser);

        when(deliveryRepository.findByUid(uuid)).thenReturn(Optional.of(delivery));

        assertThrows(DeliveryException.DeliveryAccessDeniedException.class, () ->
                deliveryService.deleteDelivery(email, uuid));
    }

    @Test
    void deleteDelivery_정상삭제() {
        UUID uuid = UUID.randomUUID();
        String email = "user@example.com";

        Member member = new Member();
        member.setEmail(email);

        Delivery delivery = new Delivery();
        delivery.setUuid(uuid);
        delivery.setMember(member);

        when(deliveryRepository.findByUid(uuid)).thenReturn(Optional.of(delivery));

        deliveryService.deleteDelivery(email, uuid);

        verify(deliveryRepository).delete(delivery);
    }
}