package deepdive.jsonstore.domain.member.service;


import deepdive.jsonstore.common.exception.JoinException;
import deepdive.jsonstore.common.exception.JsonStoreErrorCode;
import deepdive.jsonstore.domain.member.dto.JoinRequest;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinValidationService {

    private final MemberRepository memberRepository;

    public void validateJoinRequest(JoinRequest joinRequest) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(joinRequest.email())) {
            throw new JoinException.DuplicateEmailException(JsonStoreErrorCode.DUPLICATE_EMAIL);
        }

        if (!joinRequest.password().equals(joinRequest.confirmPassword())) {
            throw new JoinException.PasswordMismatchException(JsonStoreErrorCode.PASSWORD_MISMATCH);
        }

    }
}
