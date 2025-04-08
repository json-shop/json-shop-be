package deepdive.jsonstore.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeliveryException extends RuntimeException {
  private final JsonStoreErrorCode errorCode;
  private final String customMessage;

  public DeliveryException(JsonStoreErrorCode errorCode) {
    this(errorCode, errorCode.getMessage());
  }

  public String getMessage() {
    return customMessage;
  }

  public static class DeliveryAccessDeniedException extends DeliveryException {
    public DeliveryAccessDeniedException() {
      super(JsonStoreErrorCode.ACCESS_DENIED);
    }
  }

  public static class DeliveryNotFoundException extends DeliveryException {
    public DeliveryNotFoundException() {
      super(JsonStoreErrorCode.DELIVERY_NOT_FOUND);
    }
  }

  public static class AddressNotFoundException extends DeliveryException {
    public AddressNotFoundException() {
      super(JsonStoreErrorCode.ZIPCODE_NOT_VALID);
    }
  }

  public static class AddressAPIException extends DeliveryException {
    public AddressAPIException() {
      super(JsonStoreErrorCode.ADDRESS_API_ERROR);
    }
  }

}
