// Compatibility shim for scripts/extensions that expect mgt.clearMarks().
// Prevents hard crashes when mgt exists but clearMarks is missing.
(() => {
    const scope = window;
    scope.mgt = scope.mgt || {};

    if (typeof scope.mgt.clearMarks !== 'function') {
        scope.mgt.clearMarks = function clearMarksShim(...args) {
            if (typeof scope.mgt.unmark === 'function') {
                return scope.mgt.unmark(...args);
            }
            return undefined;
        };
    }
})();

(() => {
    const sidebar = document.querySelector('.admin-sidebar');
    const toggleButton = document.getElementById('sidebarToggle');

    if (toggleButton && sidebar) {
        toggleButton.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });
    }

    document.querySelectorAll('form[data-confirm]').forEach((form) => {
        form.addEventListener('submit', (event) => {
            const message = form.getAttribute('data-confirm') || 'Are you sure?';
            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
})();
