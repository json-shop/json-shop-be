document.getElementById('registerButton').addEventListener('click', async () => {
  const tokenStatus = document.getElementById('tokenStatus');
  const jwtToken = localStorage.getItem('jwtToken');

  if (!jwtToken) {
    tokenStatus.textContent = '❌ JWT 토큰이 저장되어 있지 않습니다. 먼저 입력하고 저장해주세요.';
    return;
  }

  try {
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      tokenStatus.textContent = '알림 권한을 허용해주세요.';
      return;
    }

    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
    const token = await messaging.getToken({
      vapidKey: "BMudtW8mJr24K2EAYNoi2-8G_KYrTgRVWt93aQyYthGuaFEIWWcf1uU7D4s-FxCEWmlTCJJBlgrEHmRl7y8fCUI",
      serviceWorkerRegistration: registration,
    });

    console.log('FCM 토큰:', token);

    const response = await fetch('/api/v1/fcm-tokens', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}`
      },
      body: JSON.stringify({token }),
    });

    tokenStatus.textContent = response.ok
        ? '✅ 푸시 알림이 성공적으로 등록되었습니다.'
        : '❌ 서버에 토큰 등록 실패: ' + await response.text();
  } catch (error) {
    console.error('푸시 등록 오류:', error);
    tokenStatus.textContent = '❌ 푸시 등록 중 오류가 발생했습니다: ' + error.message;
  }

  messaging.onMessage((payload) => {
    console.log('포그라운드 메시지 수신:', payload);
    const notification = new Notification(payload.notification.title, {
      body: payload.notification.body
    });
    notification.onclick = function () {
      window.focus();
      this.close();
    };
  });
});
