package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.dto.MemberDto;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinService {
    private final MeterRegistry meterRegistry;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JoinValidationService joinValidationService;

    public void joinProcess(@Valid JoinRequest joinRequest) {
        // 회원 가입 전 검증 수행
        joinValidationService.validateJoinRequest(joinRequest);

        // 회원 정보 저장
        MemberDto memberDto = new MemberDto(
                null,
                joinRequest.username(),
                joinRequest.email(),
                joinRequest.phone(),
                false
        );

        Member member = memberDto.toEntity(bCryptPasswordEncoder.encode(joinRequest.password()));
        memberRepository.save(member);
        meterRegistry.counter("business.member.signup.success").increment();
        log.info("회원가입 완료: {}", member.getUsername());
    }
}