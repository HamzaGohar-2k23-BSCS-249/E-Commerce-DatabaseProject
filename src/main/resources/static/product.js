window.addEventListener('DOMContentLoaded', () => {
  const params       = new URLSearchParams(window.location.search);
  const productId    = params.get('id');
  const prodNameEl   = document.getElementById('prod-name');
  const priceEl      = document.getElementById('prod-price');
  const mainImgEl    = document.getElementById('main-img');
  const descEl       = document.getElementById('prod-desc');
  const catEl        = document.getElementById('prod-cat');
  const sizeSelect   = document.getElementById('size-select');
  const colorSelect  = document.getElementById('color-select');
  const qtyInput     = document.getElementById('qty-input');
  const addBtn       = document.getElementById('add-to-cart');
  const buyBtn       = document.getElementById('buy-now');
  const extraImages  = document.getElementById('extra-images');
  const cartLink     = document.getElementById('view-cart');

  if (!productId) {
    console.error("No product ID in URL; aborting script.");
    return;
  }

  // rewrite Cart link on every page load
  const storedCart = localStorage.getItem('cartId');
  if (cartLink) {
    if (storedCart) {
      cartLink.href = `/cart.html?cartId=${storedCart}`;
    } else {
      cartLink.href = '#';
      cartLink.addEventListener('click', e => {
        e.preventDefault();
        alert('Your cart is empty');
      });
    }
  }

  let variants        = [];
  let selectedVariant = null;
  let isVariantProduct= true;

  function updateButtons() {
    const enabled = !isVariantProduct || !!selectedVariant;
    addBtn.disabled = buyBtn.disabled = !enabled;
  }

  fetch(`/product/${productId}`)
    .then(res => { if (!res.ok) throw new Error(`Fetch failed: ${res.status}`); return res.json(); })
    .then(product => {
      prodNameEl.textContent = product.name;
      priceEl.textContent    = product.basePrice.toFixed(2);
      mainImgEl.src          = `/uploads/${product.mainImage}`;
      mainImgEl.alt          = product.name;
      descEl.textContent     = product.description;
      catEl.textContent      = product.categoryName;

      extraImages.innerHTML = '';
      (product.extraImages || []).forEach(imgName => {
        const thumb = document.createElement('img');
        thumb.src = `/uploads/${imgName}`;
        thumb.alt = product.name;
        thumb.addEventListener('click', () => mainImgEl.src = thumb.src);
        extraImages.appendChild(thumb);
      });

      variants        = product.variants || [];
      isVariantProduct= product.hasVariants;

      if (!isVariantProduct) {
        sizeSelect.parentElement.style.display  = 'none';
        colorSelect.parentElement.style.display = 'none';
        addBtn.disabled = buyBtn.disabled = false;
        return;
      }

      const uniqueSizes = [...new Set(variants.map(v => v.siz))];
      sizeSelect.innerHTML = '<option disabled selected>Select size</option>';
      uniqueSizes.forEach(sz => {
        const o = document.createElement('option');
        o.value = o.textContent = sz;
        sizeSelect.appendChild(o);
      });
      sizeSelect.disabled = false;

      sizeSelect.addEventListener('change', () => {
        selectedVariant = null;
        priceEl.textContent = product.basePrice.toFixed(2);
        colorSelect.disabled = false;
        colorSelect.innerHTML = '<option disabled selected>Select color</option>';
        const colors = [...new Set(
          variants
            .filter(v => v.siz === sizeSelect.value)
            .map(v => v.color)
        )];
        colors.forEach(clr => {
          const o = document.createElement('option');
          o.value = o.textContent = clr;
          colorSelect.appendChild(o);
        });
        updateButtons();
      });

      colorSelect.addEventListener('change', () => {
        selectedVariant = variants.find(v =>
          v.siz === sizeSelect.value &&
          v.color === colorSelect.value
        ) || null;
        if (selectedVariant) {
          priceEl.textContent = parseFloat(selectedVariant.variantPrice).toFixed(2);
        }
        updateButtons();
      });

      updateButtons();
    })
    .catch(err => {
      console.error("Error loading product:", err);
      addBtn.disabled = buyBtn.disabled = true;
    });

  function buildCartPayload() {
    const quantity = parseInt(qtyInput.value, 10) || 1;
    if (isVariantProduct) {
      if (!selectedVariant) {
        alert("Please select both size and color.");
        return null;
      }
      return { variantId: selectedVariant.variantId, quantity };
    }
    return { productId: parseInt(productId, 10), quantity };
  }

  function handleAddOrBuy(isCheckout) {
    const payload = buildCartPayload();
    if (!payload) return;
    // include existing cartId so items accumulate
    const existing = localStorage.getItem('cartId');
    if (existing) payload.cartId = Number(existing);

    fetch('/cart', {
      method: 'POST',
      credentials: 'include',      // ← this line makes the browser send JSESSIONID
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(payload)
    })
    .then(r => r.ok ? r.json() : Promise.reject(r.statusText))
    .then(data => {
      localStorage.setItem('cartId', data.cartId);
      if (cartLink) cartLink.href = `/cart.html?cartId=${data.cartId}`;
      if (isCheckout) {
        window.location.href = `/checkout?cartId=${data.cartId}`;
      } else {
        alert(`Added to cart (cartId=${data.cartId})`);
      }
    })
    .catch(e => {
      console.error("Add-to-cart failed:", e);
      alert(isCheckout ? "Could not proceed to checkout." : "Failed to add to cart.");
    });
  }

  addBtn.addEventListener('click', () => handleAddOrBuy(false));
  buyBtn.addEventListener('click', () => handleAddOrBuy(true));
});
