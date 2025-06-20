// AddProducts.js
document.addEventListener('DOMContentLoaded', () => {
  const hasVariants = document.getElementById('checkBox');
  const defaultFields = document.getElementById('defaultFields');
  const variantsContainer = document.getElementById('variantsContainer');
  const addVariantBtn = document.getElementById('addVariantBtn');
  const variantInputs = document.getElementById('variantInputs');

  // 1) Toggle between default and variants
  hasVariants.addEventListener('change', () => {
    const showingVariants = hasVariants.checked;
    defaultFields.style.display    = showingVariants ? 'none' : 'block';
    variantsContainer.style.display = showingVariants ? 'block' : 'none';
  });

  // 2) Add one variant row
  addVariantBtn.addEventListener('click', () => {
    const idx = variantInputs.children.length;
    const row = document.createElement('div');
    row.className = 'variant-row';

    row.innerHTML = `
      <label>Variant ${idx + 1}:</label>
      <select name="variants[${idx}].siz" required>
        <option value="">Size</option>
        <option value="Small">Small</option>
        <option value="Medium">Medium</option>
        <option value="Large">Large</option>
      </select>
      <select name="variants[${idx}].color" required>
        <option value="">Color</option>
        <option value="Red">Red</option>
        <option value="Green">Green</option>
        <option value="Blue">Blue</option>
      </select>
      <input type="number" name="variants[${idx}].variantWeight"
             placeholder="Weight" step="0.01" required>
      <input type="number" name="variants[${idx}].variantPrice"
             placeholder="Price" step="0.01" required>
      <button type="button" class="removeVariantBtn">– Remove</button>
    `;

    // Remove handler
    row.querySelector('.removeVariantBtn').addEventListener('click', () => {
      variantInputs.removeChild(row);
      if (variantInputs.children.length === 0) {
        // if no variants left, uncheck and show default fields
        hasVariants.checked = false;
        defaultFields.style.display    = 'block';
        variantsContainer.style.display = 'none';
      }
    });

    variantInputs.appendChild(row);
  });
});
