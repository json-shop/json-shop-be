package deepdive.jsonstore.domain.member.service;


import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.dto.JoinResponse;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinValidationService {

    private final MemberRepository memberRepository;

    public void validateJoinRequest(JoinResponse joinResponse) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(joinResponse.email())) {
            throw new JoinException.DuplicateEmailException(JsonStoreErrorCode.DUPLICATE_EMAIL);
        }

        if (!joinResponse.password().equals(joinResponse.confirmPassword())) {
            throw new JoinException.PasswordMismatchException(JsonStoreErrorCode.PASSWORD_MISMATCH);
        }

    }
}
