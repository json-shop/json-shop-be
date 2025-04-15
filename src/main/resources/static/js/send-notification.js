document.getElementById('sendButton').addEventListener('click', async () => {
  const title = document.getElementById('title').value.trim();
  const message = document.getElementById('message').value.trim();
  const sendStatus = document.getElementById('sendStatus');
  const jwtToken = localStorage.getItem('jwtToken');

  if (!title || !message) {
    sendStatus.textContent = '모든 항목을 입력해주세요.';
    return;
  }

  if (!jwtToken) {
    sendStatus.textContent = '❌ JWT 토큰이 저장되어 있지 않습니다. 먼저 입력하고 저장해주세요.';
    return;
  }

  try {
    const response = await fetch('/api/v1/notifications', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${jwtToken}`
      },
      body: JSON.stringify({ title, message }),
    });

    sendStatus.textContent = response.ok
        ? '✅ 알림이 성공적으로 전송되었습니다.'
        : '❌ 알림 전송 실패: ' + await response.text();
  } catch (err) {
    console.error('알림 전송 오류:', err);
    sendStatus.textContent = '❌ 알림 전송 중 오류가 발생했습니다: ' + err.message;
  }
});
