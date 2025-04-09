package deepdive.jsonstore.domain.delivery.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeliveryException extends RuntimeException {
  private final DeliveryErrorCode errorCode;
  private final String customMessage;

  public DeliveryException(DeliveryErrorCode errorCode) {
    this(errorCode, errorCode.getMessage());
  }

  public String getMessage() {
    return customMessage;
  }

  public static class DeliveryAccessDeniedException extends DeliveryException {
    public DeliveryAccessDeniedException() {
      super(DeliveryErrorCode.ACCESS_DENIED);
    }
  }

  public static class DeliveryNotFoundException extends DeliveryException {
    public DeliveryNotFoundException() {
      super(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }
  }

  public static class AddressNotFoundException extends DeliveryException {
    public AddressNotFoundException() {
      super(DeliveryErrorCode.ZIPCODE_NOT_VALID);
    }
  }

  public static class AddressAPIException extends DeliveryException {
    public AddressAPIException() {
      super(DeliveryErrorCode.ADDRESS_API_ERROR);
    }
  }

}
