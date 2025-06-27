(async function checkAdminAuth() {
    try {
        const response = await fetchWithAuth("/api/v1/admin/check", {
            method: "GET"
        });

        const result = await response.json();

        if (!response.ok || result.code !== 200 || result.data !== true) {
            throw new Error("관리자 권한 없음");
        }

    } catch (error) {
        localStorage.removeItem("accessToken");
        alert("접근 권한이 없습니다. 로그인해주세요.");
        window.location.href = "/admin/login";
    }
})();