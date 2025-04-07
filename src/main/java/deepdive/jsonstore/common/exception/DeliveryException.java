package deepdive.jsonstore.common.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class DeliveryException extends RuntimeException {
  private final JsonStoreErrorCode errorCode;
  private final String customMessage;

  public DeliveryException(JsonStoreErrorCode errorCode) {
    this(errorCode, errorCode.getMessage());
  }

  public DeliveryException(JsonStoreErrorCode errorCode, String customMessage) {
    super(customMessage);
    this.errorCode = errorCode;
    this.customMessage = customMessage;
  }

  public String getMessage() {
    return customMessage;
  }

  public static class DeliveryAccessDeniedException extends DeliveryException {
    public DeliveryAccessDeniedException() {super(JsonStoreErrorCode.ACCESS_DENIED);}
  }

  public static class DeliveryNotFoundException extends DeliveryException {
    public DeliveryNotFoundException(UUID uuid) {
      super(JsonStoreErrorCode.DELIVERY_NOT_FOUND, "존재하지 않는 배송지입니다: " + uuid);
    }
  }

}
