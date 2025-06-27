function navigateTo(page) {
    window.location.href = `/admin/${page}`;
}

document.getElementById("logoutBtn").addEventListener("click", async () => {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("이미 로그아웃 상태입니다.");
        return;
    }

    try {
        await fetch("/api/v1/admin/auth/sign-out", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });

        localStorage.removeItem("accessToken");
        window.location.href = "/admin/login";
    } catch (e) {
        console.error("로그아웃 실패", e);
        alert("로그아웃 중 문제가 발생했습니다.");
    }
});