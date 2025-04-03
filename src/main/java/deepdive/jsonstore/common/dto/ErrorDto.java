package deepdive.jsonstore.common.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorDto extends BaseDto {

    private final String message;

    public ErrorDto(HttpStatus httpStatus, String message) {
        super(httpStatus);
        this.message = message;
    }
}
