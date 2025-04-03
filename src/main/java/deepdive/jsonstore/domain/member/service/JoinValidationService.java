package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.CustomException;
import deepdive.jsonstore.domain.member.dto.JoinResponse;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinValidationService {

    private final MemberRepository memberRepository;

    public void validateJoinRequest(JoinResponse joinResponse) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(joinResponse.email())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다.");
        }

        // 비밀번호 확인 검사
        if (!joinResponse.password().equals(joinResponse.confirmPassword())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        }
    }
}
