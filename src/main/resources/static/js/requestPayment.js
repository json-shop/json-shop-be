document.addEventListener("DOMContentLoaded", main);

async function main() {
  const button = document.getElementById("payment-button");
  const coupon = document.getElementById("coupon-box");
  const orderIdInput = document.getElementById("order-id");
  const orderPriceInput = document.getElementById("order-price");
  const couponPriceInput = document.getElementById("coupon-price");
  const successUrlInput = document.getElementById("success-url");
  const failUrlInput = document.getElementById("fail-url");

  const clientKey = "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm";
  const tossPayments = TossPayments(clientKey);
  const customerKey = "nZ4IJvwmV-l6l07qmn977";

  const widgets = tossPayments.widgets({ customerKey });

  // 초기 금액 세팅
  await widgets.setAmount({
    currency: "KRW",
    value: parseInt(orderPriceInput.value),
  });

  await Promise.all([
    widgets.renderPaymentMethods({
      selector: "#payment-method",
      variantKey: "DEFAULT",
    }),
    widgets.renderAgreement({
      selector: "#agreement",
      variantKey: "AGREEMENT",
    }),
  ]);

  coupon.addEventListener("change", async () => {
    const base = parseInt(orderPriceInput.value);
    const discount = coupon.checked ? parseInt(couponPriceInput.value) : 0;

    await widgets.setAmount({
      currency: "KRW",
      value: base - discount,
    });
  });

  button.addEventListener("click", async () => {
    const base = parseInt(orderPriceInput.value);
    const discount = coupon.checked ? parseInt(couponPriceInput.value) : 0;
    const amount = base - discount;

    await widgets.setAmount({ currency: "KRW", value: amount });

    await widgets.requestPayment({
      orderId: orderIdInput.value,
      orderName: "토스 티셔츠 외 2건",
      successUrl: window.location.origin + successUrlInput.value,
      failUrl: window.location.origin + failUrlInput.value,
      customerEmail: "customer123@gmail.com",
      customerName: "김토스",
      customerMobilePhone: "01012341234",
    });
  });
}
