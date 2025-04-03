package deepdive.jsonstore.domain.member.controller;

import deepdive.jsonstore.common.dto.ResponseDto;
import deepdive.jsonstore.domain.member.dto.JoinResponse;
import deepdive.jsonstore.domain.member.service.JoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public ResponseEntity<ResponseDto<String>> join(@Valid @RequestBody JoinResponse joinResponse) {
        joinService.joinProcess(joinResponse);
        return ResponseEntity.ok(new ResponseDto<>(HttpStatus.OK, "회원가입이 완료되었습니다."));
    }
}
