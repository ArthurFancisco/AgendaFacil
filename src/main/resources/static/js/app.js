document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.alert.ok').forEach(el => {
    setTimeout(() => { el.style.transition='opacity .35s ease'; el.style.opacity='0'; }, 4200);
  });
});
