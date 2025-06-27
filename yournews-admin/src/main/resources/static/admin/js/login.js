document.getElementById("login-button").addEventListener("click", login);

async function login() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();
    const errorDiv = document.getElementById("error");

    if (!username || !password) {
        errorDiv.innerText = "아이디와 비밀번호를 모두 입력해주세요.";
        errorDiv.style.display = "block";
        return;
    }

    try {
        const response = await fetch("/api/v1/admin/auth/sign-in", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username, password })
        });

        const result = await response.json();

        if (!response.ok || result.code !== 200 || !result.data?.accessToken) {
            errorDiv.innerText = result.message || "로그인 실패";
            errorDiv.style.display = "block";
            return;
        }

        localStorage.setItem("accessToken", result.data.accessToken);
        window.location.href = "/admin/dashboard";
    } catch (e) {
        errorDiv.innerText = "서버 오류 발생: " + e.message;
        errorDiv.style.display = "block";
    }
}