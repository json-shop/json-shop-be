package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MemberDeleteServiceTest {

    @Test
    @DisplayName("성공 - 6개월 이상 Soft Delete된 회원을 삭제한다")
    void removeOldSoftDeletedMembers_6개월이전회원삭제() {
        // given
        MemberRepository mockRepository = mock(MemberRepository.class);
        MemberDeleteService service = new MemberDeleteService(mockRepository);

        // when
        service.removeOldSoftDeletedMembers();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(mockRepository, times(1)).deleteAllSoftDeletedBefore(captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        LocalDateTime expectedTime = LocalDateTime.now().minusMonths(6);

        // 확인: 대략 6개월 전 시점이 전달되었는지
        assertThat(capturedTime).isBeforeOrEqualTo(expectedTime.plusMinutes(1));
        assertThat(capturedTime).isAfter(expectedTime.minusMinutes(1));
    }
}
