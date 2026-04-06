function toggleProcess(row) {
    const processId = row.getAttribute('data-process-id');
    const sign = row.querySelector('.toggle-sign');
    const stepRows = document.querySelectorAll(`tr[data-parent-process='${processId}']`);

    const shouldShow = Array.from(stepRows).some(r => r.classList.contains('hidden'));

    stepRows.forEach(stepRow => {
        if (shouldShow) {
            stepRow.classList.remove('hidden');
        } else {
            stepRow.classList.add('hidden');

            const stepId = stepRow.getAttribute('data-step-id');
            const stepSign = stepRow.querySelector('.toggle-sign');
            const transitionRows = document.querySelectorAll(`tr[data-parent-step='${stepId}']`);

            transitionRows.forEach(tr => tr.classList.add('hidden'));
            if (stepSign) {
                stepSign.textContent = '+';
            }
        }
    });

    sign.textContent = shouldShow ? '-' : '+';
}

function toggleStep(event, row) {
    event.stopPropagation();

    const stepId = row.getAttribute('data-step-id');
    const sign = row.querySelector('.toggle-sign');
    const transitionRows = document.querySelectorAll(`tr[data-parent-step='${stepId}']`);

    const shouldShow = Array.from(transitionRows).some(r => r.classList.contains('hidden'));

    transitionRows.forEach(tr => {
        if (shouldShow) {
            tr.classList.remove('hidden');
        } else {
            tr.classList.add('hidden');
        }
    });

    sign.textContent = shouldShow ? '-' : '+';
}