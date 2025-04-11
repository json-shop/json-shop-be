document.getElementById('sendButton').addEventListener('click', async () => {
  const memberUid = document.getElementById('notifMemberUid').value.trim();
  const title = document.getElementById('title').value.trim();
  const message = document.getElementById('message').value.trim();
  const sendStatus = document.getElementById('sendStatus');

  if (!memberUid || !title || !message) {
    sendStatus.textContent = '모든 항목을 입력해주세요.';
    return;
  }

  try {
    const response = await fetch('/api/v1/notifications', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memberUid, title, message }),
    });

    sendStatus.textContent = response.ok
        ? '알림이 성공적으로 전송되었습니다.'
        : '알림 전송 실패: ' + await response.text();
  } catch (err) {
    console.error('알림 전송 오류:', err);
    sendStatus.textContent = '알림 전송 중 오류가 발생했습니다: ' + err.message;
  }
});
