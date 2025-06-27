document.addEventListener("DOMContentLoaded", () => {
    const titleFromSession = sessionStorage.getItem('title');
    const contentFromSession = sessionStorage.getItem('content');

    if (titleFromSession) {
        document.getElementById('title').value = titleFromSession;
    }
    if (contentFromSession) {
        document.getElementById('content').value = contentFromSession;
    }

    // ✅ 폼 제출 시 PUT 요청
    document.getElementById('edit-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const title = document.getElementById('title').value.trim();
        const content = document.getElementById('content').value.trim();

        if (!title || !content) {
            alert("제목과 내용을 모두 입력해주세요.");
            return;
        }

        try {
            const response = await fetchWithAuth(`/api/v1/admin/posts/${postId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ title, content }),
            });

            if (response.ok) {
                alert("수정이 완료되었습니다.");
                sessionStorage.removeItem('title');
                sessionStorage.removeItem('content');
                window.location.href = `/admin/posts/${postId}`;
            } else {
                const err = await response.json();
                alert(err.message || "수정 중 오류가 발생했습니다.");
            }
        } catch (error) {
            alert("서버 통신 오류가 발생했습니다.");
        }
    });

    document.getElementById('cancel-button').addEventListener('click', () => {
        window.history.back();
    });
});