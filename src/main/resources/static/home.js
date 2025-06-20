window.addEventListener('DOMContentLoaded', () => {
  fetch('/products')
    .then(res => res.json())
    .then(products => {
      console.log('Got products:', products);
      const grid = document.getElementById('product-grid');

      products.forEach(p => {
        const img   = p.mainImage;   // matches DTO getter getMainImage()
        const price = p.basePrice;   // matches getBasePrice()

        if (typeof price !== 'number') {
          console.warn('Missing price on', p);
          return;
        }

        // 1) Create a link wrapper
        const link = document.createElement('a');
        link.href = `product.html?id=${p.productId}`;
        link.className = 'card-link';

        // 2) Create the card div
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
          <img src="/uploads/${img}" alt="${p.name}">
          <div class="card-content">
            <h2 class="card-title">${p.name}</h2>
            <p class="card-price">$${price.toFixed(2)}</p>
          </div>
        `;

        // 3) Nest and append
        link.appendChild(card);
        grid.appendChild(link);
      });
    })
    .catch(err => console.error('Fetch /products error:', err));
});
