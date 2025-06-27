document.addEventListener('DOMContentLoaded', () => {
    loadNewsDetail(newsId);

    document.getElementById('delete-button').addEventListener('click', async () => {
        const confirmDelete = confirm('정말로 이 소식을 삭제하시겠습니까?');
        if (!confirmDelete) return;

        try {
            const res = await fetchWithAuth(`/api/v1/admin/news/${newsId}`, {
                method: 'DELETE'
            });
            const result = await res.json();

            if (result.code !== 200) throw new Error(result.message);

            alert('소식이 성공적으로 삭제되었습니다.');
            window.location.href = '/admin/news';
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    });

    document.getElementById('back-button').addEventListener('click', () => {
        window.location.href = '/admin/news';
    });
});

async function loadNewsDetail(id) {
    try {
        const res = await fetchWithAuth(`/api/v1/admin/news/${id}`);
        const result = await res.json();

        if (result.code !== 200) throw new Error(result.message);

        const news = result.data;
        document.getElementById('detail-id').textContent = news.id;
        document.getElementById('detail-name').textContent = news.name;
        document.getElementById('detail-url').innerHTML = `<a href="${news.url}" target="_blank">${news.url}</a>`;
        document.getElementById('detail-college').textContent = news.college;
        document.getElementById('detail-subMember').textContent = news.subMember;
    } catch (e) {
        alert('소식 정보를 불러오지 못했습니다: ' + e.message);
    }
}