package deepdive.jsonstore.common.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class BaseDto {

    private final HttpStatus httpStatus;

}
