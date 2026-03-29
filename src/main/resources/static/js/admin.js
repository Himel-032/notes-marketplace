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
