document.getElementById('registerButton').addEventListener('click', async () => {
  const memberUid = document.getElementById('memberUid').value.trim();
  const tokenStatus = document.getElementById('tokenStatus');

  if (!memberUid) {
    tokenStatus.textContent = '사용자 UID를 입력해주세요.';
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
      vapidKey: "BFb0x3jt3JOK6l0eG7iBXKyiiZszUCINd4NgNRqDYNGPM3-Uny9Dch6z7e_Ac2lwUjiJR8MMSP4V2Oxv_LKaSFA",
      serviceWorkerRegistration: registration,
    });

    console.log('FCM 토큰:', token);

    const response = await fetch('/api/v1/fcm-tokens', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memberUid, token }),
    });

    tokenStatus.textContent = response.ok
        ? '푸시 알림이 성공적으로 등록되었습니다.'
        : '서버에 토큰 등록 실패: ' + await response.text();
  } catch (error) {
    console.error('푸시 등록 오류:', error);
    tokenStatus.textContent = '푸시 등록 중 오류가 발생했습니다: ' + error.message;
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
