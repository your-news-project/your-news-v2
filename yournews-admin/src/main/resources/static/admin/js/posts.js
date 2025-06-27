let currentPage = 0;
const size = 10;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('load-button').addEventListener('click', () => {
        currentPage = 0;
        loadPostList(currentPage);
    });

    loadPostList(currentPage);
});

document.getElementById('create-button').addEventListener('click', () => {
    window.location.href = '/admin/posts/create';
});

function getSelectedCategory() {
    return document.getElementById('category').value;
}

async function loadPostList(page) {
    const category = getSelectedCategory();
    try {
        const res = await fetchWithAuth(`/api/v1/admin/posts?category=${category}&page=${page}&size=${size}`);
        const result = await res.json();

        if (result.code !== 200) throw new Error(result.message);

        const posts = result.data.content;
        const totalPages = result.data.page.totalPages;

        renderPostTable(posts);
        renderPagination(totalPages, page);
    } catch (e) {
        alert('게시글 로딩 실패: ' + e.message);
    }
}

function renderPostTable(posts) {
    const tableBody = document.getElementById('post-table-body');
    tableBody.innerHTML = '';

    posts.forEach(post => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${post.id}</td>
            <td>${post.title}</td>
            <td>${post.nickname}</td>
            <td>${post.createdAt.replace('T', ' ')}</td>
            <td>${post.likeCount}</td>
            <td><button onclick="viewPostDetail(${post.id})">보기</button></td>
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
            loadPostList(currentPage);
        };
        paginationDiv.appendChild(prev);
    }

    for (let i = start; i < end; i++) {
        const btn = document.createElement('button');
        btn.innerText = i + 1;
        btn.disabled = i === current;
        btn.onclick = () => {
            currentPage = i;
            loadPostList(i);
        };
        paginationDiv.appendChild(btn);
    }

    if (end < totalPages) {
        const next = document.createElement('button');
        next.innerText = '▶';
        next.onclick = () => {
            currentPage = end;
            loadPostList(currentPage);
        };
        paginationDiv.appendChild(next);
    }
}

function viewPostDetail(postId) {
    window.location.href = `/admin/posts/${postId}`;
}