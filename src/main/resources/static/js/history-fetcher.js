document.getElementById('historyButton').addEventListener('click', async () => {
  const notificationHistory = document.getElementById('notificationHistory');
  const jwtToken = localStorage.getItem('jwtToken');

  if (!jwtToken) {
    notificationHistory.innerHTML = '<p style="color:red;">JWT 토큰이 저장되어 있지 않습니다. 먼저 입력하고 저장해주세요.</p>';
    return;
  }

  try {
    const response = await fetch(`/api/v1/notifications`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    });

    if (!response.ok) {
      const errorText = await response.text();
      notificationHistory.innerHTML = `<p style="color:red;">조회 실패: ${errorText}</p>`;
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
