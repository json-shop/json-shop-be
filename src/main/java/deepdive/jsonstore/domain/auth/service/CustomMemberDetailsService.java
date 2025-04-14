package deepdive.jsonstore.domain.auth.service;

import deepdive.jsonstore.domain.auth.entity.CustomMemberDetails;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일을 기반으로 Member 객체를 찾음
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 찾을 수 없습니다."));

        // 삭제된 회원은 인증되지 않음
        if (Boolean.TRUE.equals(member.getIsDeleted())) {
            throw new DisabledException("삭제된 회원입니다.");
        }

        // CustomMemberDetails 객체에 비밀번호와 권한 포함하여 반환
        return new CustomMemberDetails(
                member.getUid(),
                member.getPassword(), // 비밀번호 추가
                Collections.singleton(new SimpleGrantedAuthority("MEMBER")) // 권한 하드코딩
        );
    }

    public CustomMemberDetails loadUserByUuid(UUID uuid) {
        // UUID를 사용하여 Member 객체를 조회
        Member member = memberRepository.findByUid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("해당 UUID를 찾을 수 없습니다."));

        // CustomMemberDetails 객체에 비밀번호와 권한 포함하여 반환
        return new CustomMemberDetails(
                member.getUid(),
                member.getPassword(), // 비밀번호 추가
                Collections.singleton(new SimpleGrantedAuthority("MEMBER")) // 권한 하드코딩
        );
    }
}