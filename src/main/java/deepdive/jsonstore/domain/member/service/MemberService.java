package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.AuthException;
import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import deepdive.jsonstore.domain.member.util.MemberUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberUtil memberUtil;

    @Transactional
    public void deleteCurrentMember() {
        Member member = memberUtil.getCurrentMember();

        member.deleteMember();
    }

    @Transactional
    public void resetPW(String email, ResetPasswordRequestDTO dto) {
        Member member = memberRepository.findByEmail(email).orElseThrow(AuthException.UserNotFoundException::new);

        if (!dto.newPassword().equals(dto.newPasswordConfirm())) {
            throw new MemberException.PasswordMismatchException();
        }
        if (!passwordEncoder.matches(dto.currentPassword(), member.getPassword())) {
            throw new MemberException.CurrentPasswordIncorrectException();
        }

        member.resetPassword(passwordEncoder.encode(dto.newPassword()));

    }

    @Transactional
    public void updateMember(String email, UpdateMemberRequestDTO dto) {
        Member member = memberRepository.findByEmail(email).orElseThrow(AuthException.UserNotFoundException::new);

        member.setUsername(dto.username());
        member.setPhone(dto.phone());
    }
}
