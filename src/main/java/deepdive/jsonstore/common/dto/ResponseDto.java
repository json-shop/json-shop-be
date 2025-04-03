package deepdive.jsonstore.common.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseDto<T> extends BaseDto {

    private final T data;

    public ResponseDto(HttpStatus httpStatus, T data) {
        super(httpStatus);
        this.data = data;
    }
}
