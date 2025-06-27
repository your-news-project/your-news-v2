let currentPage = 0;
const size = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadUserList(currentPage);
});

async function loadUserList(page) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/users?page=${page}&size=${size}`);
        const result = await res.json();

        if (result.code !== 200) throw new Error(result.message);

        const users = result.data.content;
        const totalPages = result.data.page.totalPages;

        renderUserTable(users);
        renderPagination(totalPages, page);
    } catch (e) {
        alert('유저 목록 로딩 실패: ' + e.message);
    }
}

function renderUserTable(users) {
    const tableBody = document.getElementById('user-table-body');
    tableBody.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.nickname}</td>
            <td>${user.email}</td>
            <td>${user.isBanned ? 'O' : 'X'}</td>
            <td>${user.bannedAt ? user.bannedAt.replace('T', ' ') : '-'}</td>
            <td><button onclick="viewUserDetail(${user.id})">보기</button></td>
        `;
        tableBody.appendChild(row);
    });
}

function renderPagination(totalPages, current) {
    const paginationDiv = document.getElementById('pagination');
    paginationDiv.innerHTML = '';

    const groupSize = 10;
    const currentGroup = Math.floor(current / groupSize);
    const start = currentGroup * groupSize;
    const end = Math.min(start + groupSize, totalPages);

    if (start > 0) {
        const prev = document.createElement('button');
        prev.innerText = '◀';
        prev.onclick = () => {
            currentPage = start - 1;
            loadUserList(currentPage);
        };
        paginationDiv.appendChild(prev);
    }

    for (let i = start; i < end; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.disabled = i === current;
        btn.onclick = () => {
            currentPage = i;
            loadUserList(i);
        };
        paginationDiv.appendChild(btn);
    }

    if (end < totalPages) {
        const next = document.createElement('button');
        next.innerText = '▶';
        next.onclick = () => {
            currentPage = end;
            loadUserList(currentPage);
        };
        paginationDiv.appendChild(next);
    }
}

function viewUserDetail(userId) {
    window.location.href = `/admin/users/${userId}`;
}