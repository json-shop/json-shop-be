package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.domain.member.dto.JoinResponse;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JoinValidationService joinValidationService;

    public void joinProcess(@Valid JoinResponse joinResponse) {
        // 회원 가입 전 검증 수행
        joinValidationService.validateJoinRequest(joinResponse);

        // 회원 정보 저장
        Member member = new Member(
                null, // ID는 자동 생성
                null, // UUID 생성
                joinResponse.username(),
                bCryptPasswordEncoder.encode(joinResponse.password()),
                joinResponse.email(),
                joinResponse.phone(),
                false, // isDeleted 초기값 설정
                null // deletedAt 초기값 설정
        );

        memberRepository.save(member);
    }
}
