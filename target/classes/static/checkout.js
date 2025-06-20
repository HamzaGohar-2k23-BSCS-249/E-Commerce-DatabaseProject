window.addEventListener('DOMContentLoaded', () => {
  const params       = new URLSearchParams(window.location.search);
  const cartId       = params.get('cartId');
  const summaryBody  = document.querySelector('#order-summary tbody');
  const grandTotalEl = document.getElementById('grand-total');

  if (!cartId) {
    alert('No cart ID provided. Please add items to your cart first.');
    return window.location.href = '/';
  }

  // Fetch cart items and render summary
  fetch(`/cart/${cartId}`)
    .then(res => {
      if (!res.ok) throw new Error(`Fetch failed: ${res.status}`);
      return res.json();
    })
    .then(items => {
      let grandTotal = 0;
      summaryBody.innerHTML = '';

      items.forEach(item => {
        const desc = item.siz
          ? `${item.productName} (${item.siz}, ${item.color})`
          : item.productName;
        const lineTotal = item.variantPrice * item.quantity;

        const row = document.createElement('tr');
        row.innerHTML = `
          <td>${desc}</td>
          <td>${item.quantity}</td>
          <td>$${item.variantPrice.toFixed(2)}</td>
          <td>$${lineTotal.toFixed(2)}</td>
        `;
        summaryBody.appendChild(row);
        grandTotal += lineTotal;
      });

      grandTotalEl.textContent = `$${grandTotal.toFixed(2)}`;
    })
    .catch(err => {
      console.error('Error loading order summary:', err);
      alert('Could not load order summary. Please try again.');
    });

  // Payment method toggle
  const paymentMethod = document.getElementById('paymentMethod');
  const cardDetails   = document.getElementById('card-details');
  function toggleCard() {
    cardDetails.style.display = paymentMethod.value === 'card' ? 'block' : 'none';
  }
  paymentMethod.addEventListener('change', toggleCard);
  toggleCard();

  // Place order
  document.getElementById('place-order').addEventListener('click', () => {
    const shipping = {
      firstName: document.getElementById('firstName').value.trim(),
      lastName:  document.getElementById('lastName').value.trim(),
      phone:     document.getElementById('phone').value.trim(),
      address:   document.getElementById('address').value.trim(),
      province:  document.getElementById('province').value.trim(),
      city:      document.getElementById('city').value.trim(),
      area:      document.getElementById('area').value.trim(),
    };

    const payment = { method: paymentMethod.value };
    if (paymentMethod.value === 'card') {
      payment.cardNumber = document.getElementById('cardNumber').value.trim();
    }

    const payload = {
      customerId:       Number(localStorage.getItem('customerId')),
      cartId:           parseInt(cartId, 10),
      shippingAddress:  shipping,
      payment:          payment
    };

    fetch('/order', {
      method:  'POST',
      credentials: 'include',      // ← this line makes the browser send JSESSIONID
      headers: {'Content-Type': 'application/json'},
      body:    JSON.stringify(payload)
    })
    .then(res => { if (!res.ok) throw new Error(res.statusText); return res.json(); })
    .then(data => {
      alert('Order placed! Order ID: ' + data.orderId);
      window.location.href = `/order-confirmation?orderId=${data.orderId}`;
    })
    .catch(err => {
      console.error('Place order error:', err);
      alert('Order Placed Successfully.');
    });
  });
});
