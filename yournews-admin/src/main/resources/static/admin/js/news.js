let currentPage = 0;
const size = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadNewsList(currentPage);
});

document.getElementById('create-button').addEventListener('click', () => {
    window.location.href = '/admin/news/create';
});

async function loadNewsList(page) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/news?page=${page}&size=${size}`);
        const result = await res.json();

        if (result.code !== 200) throw new Error(result.message);

        const newsList = result.data.content;
        const totalPages = result.data.page.totalPages;

        renderNewsTable(newsList);
        renderPagination(totalPages, page);
    } catch (e) {
        alert('소식 로딩 실패: ' + e.message);
    }
}

function renderNewsTable(newsList) {
    const tableBody = document.getElementById('news-table-body');
    tableBody.innerHTML = '';

    newsList.forEach(news => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${news.id}</td>
            <td>${news.name}</td>
            <td><a href="${news.url}" target="_blank">${news.url}</a></td>
            <td>${news.college}</td>
            <td><button onclick="viewNewsDetail(${news.id})">보기</button></td>
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
            loadNewsList(currentPage);
        };
        paginationDiv.appendChild(prev);
    }

    for (let i = start; i < end; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.disabled = i === current;
        btn.onclick = () => {
            currentPage = i;
            loadNewsList(i);
        };
        paginationDiv.appendChild(btn);
    }

    if (end < totalPages) {
        const next = document.createElement('button');
        next.innerText = '▶';
        next.onclick = () => {
            currentPage = end;
            loadNewsList(currentPage);
        };
        paginationDiv.appendChild(next);
    }
}

function viewNewsDetail(postId) {
    window.location.href = `/admin/news/${postId}`;
}