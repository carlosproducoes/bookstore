document.querySelectorAll('[data-mask]').forEach(input => {
  const applyMask = () => {
    const digits = input.value.replace(/\D/g, '');
    let masked = digits;

    if (input.dataset.mask === 'cpf') {
      masked = digits.slice(0, 11)
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d)/, '$1.$2')
        .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }

    if (input.dataset.mask === 'phone') {
      masked = digits.slice(0, 9)
        .replace(/(\d{5})(\d)/, '$1-$2');
      if (digits.length <= 8) {
        masked = digits.slice(0, 8).replace(/(\d{4})(\d)/, '$1-$2');
      }
    }

    if (input.dataset.mask === 'cep') {
      masked = digits.slice(0, 8).replace(/(\d{5})(\d)/, '$1-$2');
    }

    input.value = masked;
  };

  input.addEventListener('input', applyMask);
  applyMask();
});
