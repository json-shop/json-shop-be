package deepdive.jsonstore.domain.member.service;

import deepdive.jsonstore.common.exception.CommonException;
import deepdive.jsonstore.domain.member.entity.Member;
import deepdive.jsonstore.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberValidationService {

    private final MemberRepository memberRepository;

    public Member findByUid(UUID uid) {
        //TODO : 커스텀에러로 변경할 것
        return memberRepository.findByUid(uid).orElseThrow(CommonException.InternalServerException::new);
    }

    public Member findById(Long id) {
        //TODO : 커스텀에러로 변경할 것
        return memberRepository.findById(id).orElseThrow(CommonException.InternalServerException::new);
    }
}
