package deepdive.jsonstore.domain.auth.auth;

import deepdive.jsonstore.domain.admin.repository.AdminRepository;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberTypeUtil {

    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;

    public boolean isAdmin(String email) {
        return adminRepository.existsByEmail(email);
    }

    public boolean isMember(String email) {
        return memberRepository.existsByEmail(email);
    }
}