async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("accessToken");
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/admin/login";
        return;
    }

    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
        ...options.headers
    };

    const finalOptions = { ...options, headers };
    return await fetch(url, finalOptions);
}

window.fetchWithAuth = fetchWithAuth;