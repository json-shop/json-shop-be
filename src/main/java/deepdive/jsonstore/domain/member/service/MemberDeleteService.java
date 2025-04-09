package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberDeleteService {
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 0 * * MON")
    public void removeOldSoftDeletedMembers() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        memberRepository.deleteAllSoftDeletedBefore(sixMonthsAgo);
    }
}

