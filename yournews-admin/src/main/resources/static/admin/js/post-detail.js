document.addEventListener('DOMContentLoaded', () => {
    if (!postId) {
        alert('게시글 ID가 존재하지 않습니다.');
        return;
    }

    loadPostDetail(postId);

    document.getElementById('edit-button').addEventListener('click', () => {
        sessionStorage.setItem('title', document.getElementById('detail-title').innerText);
        sessionStorage.setItem('content', document.getElementById('detail-content').innerText);
        window.location.href = `/admin/posts/${postId}/edit`;
    });

    document.getElementById('delete-button').addEventListener('click', async () => {
        const confirmed = confirm('정말로 이 게시글을 삭제하시겠습니까?');
        if (!confirmed) return;

        try {
            const res = await fetchWithAuth(`/api/v1/admin/posts/${postId}`, {
                method: 'DELETE'
            });

            const result = await res.json();
            if (result.code !== 200) throw new Error(result.message);

            alert('삭제되었습니다.');
            window.location.href = '/admin/posts';
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    });

    document.getElementById('back-button').addEventListener('click', () => {
        window.location.href = '/admin/posts';
    });
});

async function loadPostDetail(postId) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/posts/${postId}`);
        const result = await res.json();
        if (result.code !== 200) throw new Error(result.message);

        displayPostDetail(result.data);
    } catch (e) {
        alert('상세 정보 로딩 실패: ' + e.message);
    }
}

function displayPostDetail(post) {
    document.getElementById('detail-id').innerText = post.id;
    document.getElementById('detail-title').innerText = post.title;
    document.getElementById('detail-content').innerText = post.content;
    document.getElementById('detail-nickname').innerText = post.nickname;
    document.getElementById('detail-createdAt').innerText = post.createdAt;
    document.getElementById('detail-likeCount').innerText = post.likeCount;
    document.getElementById('detail-userId').innerHTML = `
        <button onclick="viewUserDetail(${post.userId})">${post.userId}</button>
    `;
}

function viewUserDetail(userId) {
    window.location.href = `/admin/users/${userId}`;
}