/* ==========================================================================
   Dineflow Formatting & Security Utilities
   ========================================================================== */

const IndianCurrencyFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0
});

function money(amount) {
  if (amount == null || isNaN(amount)) return '₹0';
  return IndianCurrencyFormatter.format(Number(amount));
}

// XSS Sanitizer: Escape dangerous HTML entities
function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatPhone(phone) {
  if (!phone) return '—';
  const clean = String(phone).replace(/\D/g, '');
  if (clean.length === 10) {
    return `${clean.slice(0, 5)} ${clean.slice(5)}`;
  }
  return clean;
}
