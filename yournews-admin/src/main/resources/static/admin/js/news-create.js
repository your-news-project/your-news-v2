const colleges = {
    HUMANITIES: "인문대학",
    NATURAL_SCIENCE: "자연과학대학",
    ENGINEERING: "공과대학",
    DIGITAL_CONVERGENCE: "디지털융합대학",
    SOCIAL_SCIENCE: "사회과학대학",
    BUSINESS: "경영대학",
    MEDICINE: "의과대학",
    PHARMACY: "약학대학",
    LIFE_APPLIED_SCIENCE: "생명응용과학대학",
    HUMAN_ECOLOGY: "생활과학대학",
    LAW: "사범대학",
    ARTS: "예술대학",
    GLOBAL_HUMANITIES: "글로벌인재대학",
    CHUNMA_COLLEGE: "천마학부대학",
    ETC: "기타"
};

document.addEventListener('DOMContentLoaded', () => {
    const collegeSelect = document.getElementById('college');

    Object.entries(colleges).forEach(([key, label]) => {
        const option = document.createElement('option');
        option.value = key;
        option.textContent = label;
        collegeSelect.appendChild(option);
    });

    document.getElementById('create-form').addEventListener('submit', async (e) => {
        e.preventDefault();

        const name = document.getElementById('name').value.trim();
        const url = document.getElementById('url').value.trim();
        const college = document.getElementById('college').value;

        try {
            const res = await fetchWithAuth('/api/v1/admin/news', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, url, college })
            });

            const result = await res.json();
            if (result.code !== 200) throw new Error(result.message);

            alert('소식이 성공적으로 생성되었습니다.');
            window.location.href = '/admin/news';
        } catch (e) {
            alert('생성 실패: ' + e.message);
        }
    });

    document.getElementById('cancel-button').addEventListener('click', () => {
        window.location.href = '/admin/news';
    });
});