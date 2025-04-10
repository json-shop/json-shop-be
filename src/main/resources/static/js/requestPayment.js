document.addEventListener("DOMContentLoaded", () => {
  const payBtn = document.getElementById("payBtn");

  payBtn.addEventListener("click", async () => {
    const storeId = document.getElementById("storeId").value;
    const channelKey = document.getElementById("channelKey").value;
    const paymentId = document.getElementById("paymentId").value || "order-" + Date.now();
    const orderName = document.getElementById("orderName").value;
    const totalAmount = Number(document.getElementById("totalAmount").value);
    const confirmUrl = document.getElementById("confirmUrl").value;

    try {
      const response = await PortOne.requestPayment({
        storeId: storeId,
        channelKey: channelKey,
        paymentId: paymentId,
        orderName: orderName,
        totalAmount: totalAmount,
        currency: 'CURRENCY_KRW',
        payMethod: 'CARD',
        confirmUrl: confirmUrl,
      });

      // ✅ 성공 처리
      console.log('✅ 결제 성공 응답:', response);
      alert(`✅ 결제 성공!\n\n주문번호: ${response.paymentId}\n상태: ${response.status}`);

    } catch (error) {
      // ❌ 실패 처리
      console.error('❌ 결제 실패:', error);
      alert(`❌ 결제 실패: ${error.message}`);
    }
  });
});
