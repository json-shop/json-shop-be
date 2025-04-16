package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberValidationService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Member findByUid(UUID uid) {
        //TODO : 커스텀에러로 변경할 것
        return memberRepository.findByUid(uid).orElseThrow(CommonException.InternalServerException::new);
    }

    public Member findById(Long id) {
        //TODO : 커스텀에러로 변경할 것
        return memberRepository.findById(id).orElseThrow(CommonException.InternalServerException::new);
    }

    public void existsByUid(UUID uid) {
        if (!memberRepository.existsByUid(uid)){
            throw new MemberException.MemberNotFound();
        }
    }

    public void newPasswordConfirm(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new MemberException.PasswordMismatchException();
        }
    }

    public void passwordConfirm(String password, String confirmPassword) {
        if (!passwordEncoder.matches(confirmPassword, password)) {
            throw new MemberException.CurrentPasswordIncorrectException();
        }
    }
}
