// cart.js
window.addEventListener('DOMContentLoaded', () => {
  const cartTableBody = document.querySelector('#cart-table tbody');
  const totalElem     = document.getElementById('cart-total');
  const checkoutBtn   = document.getElementById('proceed-checkout');
  const continueBtn   = document.querySelector('.continue-btn');

  // 1) Figure out cartId from URL or localStorage
  const params = new URLSearchParams(window.location.search);
  let cartId = params.get('cartId') || localStorage.getItem('cartId');
  if (cartId) localStorage.setItem('cartId', cartId);
  if (!cartId) {
    alert('No cart ID provided. Redirecting to home.');
    return void (window.location.href = '/');
  }

  let cartItems = [];

  // 2) Formatting helper
  function fmt(n) {
    return Number(n).toLocaleString(undefined, {
      style:    'currency',
      currency: 'USD'
    });
  }

  // 3) Load and render the cart
  async function loadCart() {
    try {
      const res = await fetch(`/cart/${cartId}`);
      if (!res.ok) throw new Error(res.statusText);
      cartItems = await res.json();
      renderTable();
    } catch (e) {
      console.error('Failed to fetch cart:', e);
      alert('Failed to load your cart.');
    }
  }

  function renderTable() {
    cartTableBody.innerHTML = '';
    let total = 0;

    cartItems.forEach(item => {
      const tr = document.createElement('tr');

      // Product name
      tr.innerHTML += `<td>${item.productName}</td>`;

      // Variant description
      tr.innerHTML += `<td>${
        item.variantId
          ? `${item.siz} / ${item.color}`
          : '-'
      }</td>`;

      // Unit price
      tr.innerHTML += `<td>${fmt(item.variantPrice)}</td>`;

      // Quantity input
      const qtyTd = document.createElement('td');
      const inp   = document.createElement('input');
      inp.type    = 'number';
      inp.min     = '1';
      inp.style= "display : none;";
      qtyTd.style= "display : none;";
      inp.value   = item.quantity;

      inp.addEventListener('change', () =>
        updateQuantity(item.cartVariantId, inp.value)
      );
      qtyTd.appendChild(inp);
      tr.appendChild(qtyTd);

      // Subtotal
      const sub = item.variantPrice * item.quantity;
      total += sub;
      tr.innerHTML += `<td>${fmt(sub)}</td>`;

      // Remove button
      const delTd = document.createElement('td');
      const delBtn= document.createElement('button');
      delBtn.textContent = '✕';
      delBtn.addEventListener('click', () =>
        removeItem(item.cartVariantId)
      );
      delTd.appendChild(delBtn);
      tr.appendChild(delTd);

      cartTableBody.appendChild(tr);
    });

    totalElem.textContent = `Total: ${fmt(total)}`;
  }

  // 4) Update quantity
  async function updateQuantity(cartVariantId, newQty) {
    try {
      const res = await fetch(`/cart/${cartId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ cartVariantId, quantity: Number(newQty) })
      });
      if (!res.ok) throw new Error(res.statusText);
      await loadCart();
    } catch (e) {
      console.error('Quantity update failed:', e);
      alert('Could not update quantity.');
    }
  }

  // 5) Remove item
  async function removeItem(cartVariantId) {
    if (!confirm('Remove this item from your cart?')) return;
    try {
      const res = await fetch(`/cart/${cartId}/${cartVariantId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json'  // ensure Spring maps it correctly
        }
      });
      if (!res.ok) throw new Error(res.statusText);
      // reload entire cart so the totals stay accurate
      await loadCart();
    } catch (e) {
      console.error('Remove failed:', e);
      alert('Could not remove item.');
    }
  }

  // 6) Button handlers
  checkoutBtn.addEventListener('click', () => {
    window.location.href = `/checkout?cartId=${cartId}`;
  });
  continueBtn.addEventListener('click', () => {
    window.location.href = '/';
  });

  // Kick things off
  loadCart();
});
