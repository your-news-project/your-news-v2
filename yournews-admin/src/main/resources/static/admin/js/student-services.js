let currentPage = 0;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('load-button').addEventListener('click', () => {
        currentPage = 0;
        loadStudentServices(currentPage);
    });
    document.getElementById('dashboard-button').addEventListener('click', () => {
        window.location.href = '/admin/dashboard';
    });

    loadStudentServices(currentPage);
});

async function loadStudentServices(page) {
    const status = document.getElementById('status').value;
    const query = new URLSearchParams({ page, size: pageSize });
    if (status) query.set('status', status);

    try {
        const response = await fetchWithAuth(`/api/v1/admin/student-services?${query}`);
        const result = await response.json();
        if (!response.ok || result.code !== 200) throw new Error(result.message);

        renderStudentServices(result.data.content ?? []);
        renderPagination(result.data.page?.totalPages ?? result.data.totalPages ?? 0, page);
    } catch (error) {
        alert('홍보 게시물 목록을 불러오지 못했습니다: ' + error.message);
    }
}

function renderStudentServices(studentServices) {
    const tableBody = document.getElementById('student-service-table-body');
    tableBody.replaceChildren();

    if (studentServices.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 7;
        cell.className = 'empty-row';
        cell.textContent = '조회된 홍보 게시물이 없습니다.';
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
    }

    studentServices.forEach(studentService => {
        const row = document.createElement('tr');
        row.appendChild(createCell(studentService.id));

        const nameCell = createCell(studentService.name);
        nameCell.className = 'service-name';
        row.appendChild(nameCell);
        row.appendChild(createCell(contentTypeLabel(studentService.contentType)));
        row.appendChild(createCell(statusLabel(studentService.status)));
        row.appendChild(createCell(`${studentService.imageUrls?.length ?? 0}장`));
        row.appendChild(createCell(formatDateTime(studentService.createdAt)));

        const actionCell = document.createElement('td');
        const detailButton = document.createElement('button');
        detailButton.type = 'button';
        detailButton.textContent = '검토';
        detailButton.addEventListener('click', () => {
            window.location.href = `/admin/student-services/${studentService.id}`;
        });
        actionCell.appendChild(detailButton);
        row.appendChild(actionCell);

        tableBody.appendChild(row);
    });
}

function renderPagination(totalPages, current) {
    const pagination = document.getElementById('pagination');
    pagination.replaceChildren();

    for (let page = 0; page < totalPages; page++) {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = page + 1;
        button.disabled = page === current;
        button.addEventListener('click', () => {
            currentPage = page;
            loadStudentServices(page);
        });
        pagination.appendChild(button);
    }
}

function createCell(value) {
    const cell = document.createElement('td');
    cell.textContent = value ?? '-';
    return cell;
}

function statusLabel(status) {
    return {
        PENDING: '승인 대기',
        APPROVED: '승인',
        REJECTED: '거절',
        HIDDEN: '숨김'
    }[status] ?? status;
}

function contentTypeLabel(type) {
    return { SERVICE: '서비스', CAMPUS_PROMOTION: '학교 홍보' }[type] ?? type;
}

function formatDateTime(value) {
    return value ? value.replace('T', ' ') : '-';
}
