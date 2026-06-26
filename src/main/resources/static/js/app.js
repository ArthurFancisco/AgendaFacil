document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert.ok').forEach(el => {
    setTimeout(() => { el.style.transition='opacity .35s ease'; el.style.opacity='0'; }, 4200);
  });

  document.querySelectorAll('[data-modal-open]').forEach(button => {
    button.addEventListener('click', () => {
      const modal = document.getElementById(button.dataset.modalOpen);
      if (modal && typeof modal.showModal === 'function') {
        modal.showModal();
      }
    });
  });

  document.querySelectorAll('[data-modal-close]').forEach(button => {
    button.addEventListener('click', () => {
      button.closest('dialog')?.close();
    });
  });

  document.querySelectorAll('dialog.modal').forEach(dialog => {
    dialog.addEventListener('click', event => {
      if (event.target === dialog) {
        dialog.close();
      }
    });
  });
});
