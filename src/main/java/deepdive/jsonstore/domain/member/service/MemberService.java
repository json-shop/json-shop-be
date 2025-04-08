package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void resetPW(String email, ResetPasswordRequestDTO dto) {
        Member member = memberRepository.findByEmail(email).orElseThrow(AuthException.UserNotFoundException::new);

        if (!dto.newPassword().equals(dto.newPasswordConfirm())) {
            throw new MemberException.PasswordMismatchException();
        }
        if (!passwordEncoder.matches(dto.newPassword(), member.getPassword())) {
            throw new MemberException.CurrentPasswordIncorrectException();
        }

        member.resetPassword(bCryptPasswordEncoder.encode(dto.newPassword()));

    }
}
