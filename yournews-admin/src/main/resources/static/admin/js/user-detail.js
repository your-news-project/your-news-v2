document.addEventListener('DOMContentLoaded', () => {
    if (!userId) {
        alert('사용자 ID가 존재하지 않습니다.');
        return;
    }

    loadUserDetail(userId);

    document.getElementById('back-button').addEventListener('click', () => {
        window.location.href = '/admin/users';
    });
});

async function loadUserDetail(userId) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/users/${userId}`);
        const result = await res.json();

        if (result.code !== 200) throw new Error(result.message);

        displayUserDetail(result.data);
    } catch (e) {
        alert('상세 정보 로딩 실패: ' + e.message);
    }
}

function displayUserDetail(user) {
    document.getElementById('detail-id').innerText = user.id;
    document.getElementById('detail-username').innerText = user.username;
    document.getElementById('detail-nickname').innerText = user.nickname;
    document.getElementById('detail-email').innerText = user.email;
    document.getElementById('detail-platform').innerText = user.platform;
    document.getElementById('detail-subscriptions').innerText = user.subscriptions.join(', ') || '-';
    document.getElementById('detail-keywords').innerText = user.keywords.join(', ') || '-';
    document.getElementById('detail-status').innerText = user.isBanned ? 'O' : 'X';

    document.getElementById('ban-reason-group').style.display = user.isBanned ? 'block' : 'none';
    document.getElementById('detail-banReason').innerText = user.banReason || '-';
    document.getElementById('detail-bannedAt').innerText = user.bannedAt || '-';

    const banBtn = document.getElementById('ban-button');
    banBtn.innerText = user.isBanned ? '차단 해제' : '차단';
    banBtn.onclick = () => {
        if (user.isBanned) {
            unbanUser(user.id);
        } else {
            showBanReasonInput(user.id);
        }
    };
}

function showBanReasonInput(userId) {
    const reason = prompt('차단 사유를 입력하세요.');
    if (!reason) return alert('사유는 필수입니다.');
    banUser(userId, reason);
}

async function banUser(userId, reason) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/users/${userId}/ban`, {
            method: 'PATCH',
            body: JSON.stringify({ reason })
        });

        const result = await res.json();
        if (result.code !== 200) throw new Error(result.message);

        alert('차단 완료');
        loadUserDetail(userId);
    } catch (e) {
        alert('차단 실패: ' + e.message);
    }
}

async function unbanUser(userId) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/users/${userId}/unban`, {
            method: 'PATCH'
        });

        const result = await res.json();
        if (result.code !== 200) throw new Error(result.message);

        alert('차단 해제 완료');
        loadUserDetail(userId);
    } catch (e) {
        alert('해제 실패: ' + e.message);
    }
}