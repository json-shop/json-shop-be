package deepdive.jsonstore.common.dto;

import lombok.Getter;

@Getter
public class ErrorExtraResponse<T> extends ErrorResponse{
    private final T extra;

    public ErrorExtraResponse(String code, String message, T extra) {
        super(code, message);
        this.extra = extra;
    }
}
