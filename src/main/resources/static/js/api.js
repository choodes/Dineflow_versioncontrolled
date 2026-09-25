/* ==========================================================================
   Dineflow Shared API Client & Toast Dispatcher
   ========================================================================== */

async function api(method, path, body) {
  const headers = {};
  if (body) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(path, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  if (!res.ok) {
    let message = res.statusText;
    try {
      const errBody = await res.json();
      message = errBody.error || errBody.message || message;
    } catch (e) {
      /* Response was not JSON, fallback to statusText */
    }
    throw new Error(message);
  }

  const ct = res.headers.get('content-type') || '';
  return ct.includes('application/json') ? res.json() : null;
}

function showToast(msg, duration = 2400) {
  let toastEl = document.getElementById('app-toast');
  if (!toastEl) {
    toastEl = document.createElement('div');
    toastEl.id = 'app-toast';
    toastEl.className = 'toast-notice';
    document.body.appendChild(toastEl);
  }
  toastEl.textContent = msg;
  toastEl.classList.add('show');
  
  if (window._toastTimeout) clearTimeout(window._toastTimeout);
  window._toastTimeout = setTimeout(() => {
    toastEl.classList.remove('show');
  }, duration);
}
