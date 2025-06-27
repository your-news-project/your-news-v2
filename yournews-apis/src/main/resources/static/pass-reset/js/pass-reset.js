document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("reset-form");
    const passwordInput = document.getElementById("password");
    const passwordCheckInput = document.getElementById("password-check");
    const usernameInput = document.getElementById("username");
    const errorMsg = document.getElementById("error-message");
    const successMsg = document.getElementById("success-message");

    const queryParams = new URLSearchParams(window.location.search);
    const uuid = queryParams.get("code");

    if (!uuid) {
        errorMsg.textContent = "잘못된 접근입니다.";
        form.style.display = "none";
        return;
    }

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        errorMsg.textContent = "";
        successMsg.textContent = "";

        const username = usernameInput.value.trim();
        const password = passwordInput.value.trim();
        const passwordCheck = passwordCheckInput.value.trim();

        if (!username || !password || !passwordCheck) {
            errorMsg.textContent = "모든 항목을 입력해주세요.";
            return;
        }

        if (password !== passwordCheck) {
            errorMsg.textContent = "비밀번호가 일치하지 않습니다.";
            return;
        }

        const pattern = /(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\W).{8,16}/;
        if (!pattern.test(password)) {
            errorMsg.textContent = "비밀번호는 8~16자 영문, 숫자, 특수문자를 포함해야 합니다.";
            return;
        }

        try {
            const res = await fetch("/api/v1/auth/password/reset", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, uuid, password }),
            });

            if (!res.ok) {
                const json = await res.json();
                throw new Error(json?.message || "비밀번호 재설정 실패");
            }

            successMsg.textContent = "✅ 비밀번호가 성공적으로 변경되었습니다.";
            form.reset();
        } catch (err) {
            errorMsg.textContent = err.message;
        }
    });
});