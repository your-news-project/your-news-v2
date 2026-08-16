document.addEventListener('DOMContentLoaded', () => {
    if (!studentServiceId) {
        alert('홍보 게시물 ID가 존재하지 않습니다.');
        window.location.href = '/admin/student-services';
        return;
    }

    loadStudentServiceDetail();
    document.getElementById('approve-button').addEventListener('click', () => changeStatus('approve'));
    document.getElementById('reject-button').addEventListener('click', () => changeStatus('reject'));
    document.getElementById('back-button').addEventListener('click', () => {
        window.location.href = '/admin/student-services';
    });
});

async function loadStudentServiceDetail() {
    try {
        const response = await fetchWithAuth(`/api/v1/admin/student-services/${studentServiceId}`);
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message);

        renderStudentServiceDetail(result.data);
    } catch (error) {
        alert('홍보 게시물 정보를 불러오지 못했습니다: ' + error.message);
    }
}

function renderStudentServiceDetail(studentService) {
    setText('detail-id', studentService.id);
    setText('detail-name', studentService.name);
    setText('detail-description', studentService.description);
    setText('detail-content-type', contentTypeLabel(studentService.contentType));
    setText('detail-status', statusLabel(studentService.status));
    setText('detail-report-count', studentService.reportCount);
    setText('detail-created-at', formatDateTime(studentService.createdAt));
    setText('detail-updated-at', formatDateTime(studentService.updatedAt));

    const userContainer = document.getElementById('detail-user-id');
    userContainer.replaceChildren();
    const userLink = document.createElement('a');
    userLink.href = `/admin/users/${studentService.userId}`;
    userLink.textContent = studentService.userId;
    userContainer.appendChild(userLink);

    renderExternalLinks(studentService.serviceUrls ?? []);
    renderImageUrls(studentService.imageUrls ?? []);

    const editable = studentService.status === 'PENDING';
    document.getElementById('approve-button').disabled = !editable;
    document.getElementById('reject-button').disabled = !editable;
}

function renderExternalLinks(urls) {
    const list = document.getElementById('detail-service-urls');
    list.replaceChildren();
    if (urls.length === 0) {
        const item = document.createElement('li');
        item.textContent = '등록된 링크가 없습니다.';
        list.appendChild(item);
        return;
    }
    urls.forEach((url, index) => {
        const item = document.createElement('li');
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        link.textContent = `링크 ${index + 1}: ${url}`;
        item.appendChild(link);
        list.appendChild(item);
    });
}

function contentTypeLabel(type) {
    return { SERVICE: '서비스', CAMPUS_PROMOTION: '학교 홍보' }[type] ?? type;
}

function renderImageUrls(imageUrls) {
    const list = document.getElementById('detail-image-urls');
    list.replaceChildren();

    if (imageUrls.length === 0) {
        const item = document.createElement('li');
        item.textContent = '등록된 이미지가 없습니다.';
        list.appendChild(item);
        return;
    }

    imageUrls.forEach((url, index) => {
        const item = document.createElement('li');
        const link = document.createElement('a');
        link.href = url;
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        link.textContent = `이미지 ${index + 1}: ${url}`;
        item.appendChild(link);
        list.appendChild(item);
    });
}

async function changeStatus(action) {
    const actionLabel = action === 'approve' ? '승인' : '거절';
    if (!confirm(`이 홍보 게시물을 ${actionLabel}하시겠습니까?`)) return;

    try {
        const response = await fetchWithAuth(
                `/api/v1/admin/student-services/${studentServiceId}/${action}`,
                { method: 'PATCH' }
        );
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message);

        alert(`${actionLabel} 처리되었습니다.`);
        window.location.href = '/admin/student-services';
    } catch (error) {
        alert(`${actionLabel} 처리에 실패했습니다: ${error.message}`);
    }
}

function setText(id, value) {
    document.getElementById(id).textContent = value ?? '-';
}

function statusLabel(status) {
    return {
        PENDING: '승인 대기',
        APPROVED: '승인',
        REJECTED: '거절',
        HIDDEN: '숨김'
    }[status] ?? status;
}

function formatDateTime(value) {
    return value ? value.replace('T', ' ') : '-';
}
