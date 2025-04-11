document.getElementById('historyButton').addEventListener('click', async () => {
  const memberUid = document.getElementById('historyMemberUid').value.trim();
  const notificationHistory = document.getElementById('notificationHistory');

  if (!memberUid) {
    notificationHistory.innerHTML = '<p style="color:red;">사용자 UID를 입력해주세요.</p>';
    return;
  }

  try {
    const response = await fetch(`/api/v1/notifications?memberUid=${memberUid}`);
    if (!response.ok) {
      notificationHistory.innerHTML = `<p style="color:red;">조회 실패: ${await response.text()}</p>`;
      return;
    }

    const data = await response.json();
    if (data.length === 0) {
      notificationHistory.innerHTML = '<p>알림 내역이 없습니다.</p>';
      return;
    }

    let tableHtml = `<table style="width:100%; border-collapse:collapse; margin-top:10px;">
      <thead>
        <tr style="background:#f0f0f0;">
          <th style="padding:8px; border:1px solid #ddd;">제목</th>
          <th style="padding:8px; border:1px solid #ddd;">내용</th>
          <th style="padding:8px; border:1px solid #ddd;">분류</th>
          <th style="padding:8px; border:1px solid #ddd;">시간</th>
        </tr>
      </thead><tbody>`;

    data.forEach(n => {
      tableHtml += `<tr>
        <td style="padding:8px; border:1px solid #ddd;">${n.title}</td>
        <td style="padding:8px; border:1px solid #ddd;">${n.body}</td>
        <td style="padding:8px; border:1px solid #ddd;">${n.category}</td>
        <td style="padding:8px; border:1px solid #ddd;">${new Date(n.createdAt).toLocaleString()}</td>
      </tr>`;
    });

    tableHtml += '</tbody></table>';
    notificationHistory.innerHTML = tableHtml;

  } catch (err) {
    console.error('내역 조회 오류:', err);
    notificationHistory.innerHTML = `<p style="color:red;">조회 중 오류 발생: ${err.message}</p>`;
  }
});
