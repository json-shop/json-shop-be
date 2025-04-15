package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.MemberException;
import deepdive.jsonstore.domain.member.dto.ResetPasswordRequestDTO;
import deepdive.jsonstore.domain.member.dto.UpdateMemberRequestDTO;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.util.MemberUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberUtil memberUtil;
    private final MemberValidationService memberValidationService;

    @Transactional
    public void deleteCurrentMember() {
        Member member = memberUtil.getCurrentMember();

        member.deleteMember();
    }

    @Transactional
    public void resetPW(UUID memberUid, ResetPasswordRequestDTO dto) {
        Member member = memberValidationService.findByUid(memberUid);

        if (!dto.newPassword().equals(dto.newPasswordConfirm())) {
            throw new MemberException.PasswordMismatchException();
        }
        memberValidationService.newPasswordConfirm(dto.newPassword(),dto.newPasswordConfirm());

        if (!passwordEncoder.matches(dto.currentPassword(), member.getPassword())) {
            throw new MemberException.CurrentPasswordIncorrectException();
        }

        member.resetPassword(passwordEncoder.encode(dto.newPassword()));

    }

    @Transactional
    public void updateMember(UUID memberUid, UpdateMemberRequestDTO dto) {
        Member member = memberValidationService.findByUid(memberUid);

        member.setUsername(dto.username());
        member.setPhone(dto.phone());
    }
}
