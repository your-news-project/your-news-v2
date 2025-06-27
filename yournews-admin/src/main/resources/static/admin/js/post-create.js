document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('create-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const title = document.getElementById('title').value.trim();
        const content = document.getElementById('content').value.trim();

        if (!title || !content) {
            alert("제목과 내용을 모두 입력해주세요.");
            return;
        }

        try {
            const response = await fetchWithAuth('/api/v1/admin/posts', {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ title, content }),
            });

            if (response.ok) {
                const result = await response.json();
                alert("게시글이 생성되었습니다.");
                window.location.href = `/admin/posts/${result.data.id}`;
            } else {
                const err = await response.json();
                alert(err.message || "게시글 생성 중 오류가 발생했습니다.");
            }
        } catch (error) {
            alert("서버 통신 오류가 발생했습니다.");
        }
    });

    document.getElementById('cancel-button').addEventListener('click', () => {
        window.history.back();
    });
});

