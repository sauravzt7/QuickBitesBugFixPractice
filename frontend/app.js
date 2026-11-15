const state = { token: localStorage.getItem('qb_token'), user: JSON.parse(localStorage.getItem('qb_user') || 'null'), cart: null };
const $ = (id) => document.getElementById(id);
const money = (v) => '$' + Number(v).toFixed(2);

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (state.token) headers.Authorization = 'Bearer ' + state.token;
  const res = await fetch(path, { ...options, headers });
  if (res.status === 401 && path !== '/api/auth/login') { logout(); throw new Error('Session expired'); }
  if (res.status === 204) return null;
  const body = await res.json();
  if (!res.ok) throw new Error(body.message || 'Request failed');
  return body;
}

function show(view) {
  ['loginView', 'menuView', 'checkoutView', 'ordersView'].forEach(v => $(v).classList.add('hidden'));
  $(view + 'View').classList.remove('hidden');
  if (view === 'orders') loadOrders();
  if (view === 'checkout') renderSummary();
}

function logout() {
  state.token = null; state.user = null;
  localStorage.removeItem('qb_token'); localStorage.removeItem('qb_user');
  $('nav').classList.add('hidden');
  show('login');
}

async function onLoggedIn() {
  $('nav').classList.remove('hidden');
  $('userName').textContent = state.user.fullName;
  show('menu');
  await Promise.all([loadRestaurants(), loadCart()]);
}

async function loadRestaurants() {
  const restaurants = await api('/api/restaurants');
  $('restaurants').innerHTML = restaurants.map(r => {
    const cats = [...new Set(r.menu.map(m => m.category))];
    return `<div class="restaurant"><h2>${r.name}</h2><span class="muted">${r.cuisine}</span>` +
      cats.map(c => `<div class="category">${c}</div><div class="grid">` +
        r.menu.filter(m => m.category === c).map(m => `
          <div class="item"><b>${m.name}</b><div class="desc">${m.description}</div>
            <div class="foot"><span class="price">${money(m.price)}</span>
            <button class="add" onclick="addToCart(${m.id})">+</button></div></div>`).join('') + '</div>').join('') +
      '</div>';
  }).join('');
}

async function loadCart() { state.cart = await api('/api/cart'); renderCart(); }
async function addToCart(menuItemId) {
  state.cart = await api('/api/cart/items', { method: 'POST', body: JSON.stringify({ menuItemId, quantity: 1 }) });
  renderCart();
}
async function setQty(menuItemId, quantity) {
  state.cart = await api('/api/cart/items/' + menuItemId, { method: 'PATCH', body: JSON.stringify({ quantity }) });
  renderCart();
}

function totalsHtml(c) {
  const feeLabel = Number(c.deliveryFee) === 0 && c.items.length ? '<span class="free">FREE</span>' : money(c.deliveryFee);
  const remaining = Number(c.freeDeliveryThreshold) - Number(c.subtotal);
  const hint = c.items.length && remaining > 0
    ? `<div class="hint">Add ${money(remaining)} more for free delivery</div>` : '';
  return `<div><span>Subtotal</span><span>${money(c.subtotal)}</span></div>
    <div><span>Delivery Fee${c.restaurantCount > 1 ? ` (${c.restaurantCount} restaurants)` : ''}</span><span>${feeLabel}</span></div>
    <div class="grand"><span>Total</span><span>${money(c.total)}</span></div>${hint}`;
}

function renderCart() {
  const c = state.cart;
  $('cartItems').innerHTML = c.items.length ? c.items.map(i => `
    <div class="line"><div><div>${i.name}</div><div class="muted">${i.restaurantName} · ${money(i.unitPrice)}</div></div>
      <div class="qty"><button onclick="setQty(${i.menuItemId}, ${i.quantity - 1})">−</button>${i.quantity}
      <button onclick="setQty(${i.menuItemId}, ${i.quantity + 1})">+</button></div></div>`).join('')
    : '<p class="muted">Your cart is empty</p>';
  $('cartTotals').innerHTML = c.items.length ? totalsHtml(c) : '';
  $('checkoutBtn').disabled = !c.items.length;
}

function renderSummary() {
  const c = state.cart;
  $('summaryItems').innerHTML = c.items.map(i =>
    `<div class="line"><span>${i.quantity}x ${i.name}</span><span>${money(i.lineTotal)}</span></div>`).join('');
  $('summaryTotals').innerHTML = totalsHtml(c);
}

async function loadOrders() {
  const orders = await api('/api/orders');
  $('ordersCount').textContent = `You have ${orders.length} order${orders.length === 1 ? '' : 's'} in your history`;
  $('orders').innerHTML = orders.map(o => `
    <div class="order"><div class="head"><div><div class="muted" style="color:#3b1d00">Order</div><b>#${o.orderNumber}</b></div>
      <span class="badge">${o.status}</span></div>
      <div class="body"><div class="muted">${new Date(o.createdAt).toLocaleString()} · ${o.itemCount} items · card •••• ${o.cardLast4}</div>
        <ul>${o.items.map(i => `<li>${i.quantity}x ${i.name} (${i.restaurantName}) — ${money(i.lineTotal)}</li>`).join('')}</ul>
        <div class="totals"><div><span>Subtotal</span><span>${money(o.subtotal)}</span></div>
        <div><span>Delivery Fee</span><span>${money(o.deliveryFee)}</span></div>
        <div class="grand"><span>Total</span><span>${money(o.total)}</span></div></div></div></div>`).join('');
}

$('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  try {
    const res = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ email: f.get('email'), password: f.get('password') }) });
    state.token = res.token; state.user = res;
    localStorage.setItem('qb_token', res.token); localStorage.setItem('qb_user', JSON.stringify(res));
    $('loginError').textContent = '';
    onLoggedIn();
  } catch (err) { $('loginError').textContent = err.message; }
});

$('checkoutForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  try {
    await api('/api/orders', { method: 'POST', body: JSON.stringify({
      cardholderName: f.get('cardholderName'), cardNumber: f.get('cardNumber'),
      expiryMonth: Number(f.get('expiryMonth')), expiryYear: Number(f.get('expiryYear')), cvv: f.get('cvv') }) });
    $('checkoutError').textContent = '';
    await loadCart();
    show('orders');
  } catch (err) { $('checkoutError').textContent = err.message; }
});

$('checkoutBtn').addEventListener('click', () => show('checkout'));
$('clearCartBtn').addEventListener('click', async () => { state.cart = await api('/api/cart', { method: 'DELETE' }); renderCart(); });
$('logoutBtn').addEventListener('click', async () => { try { await api('/api/auth/logout', { method: 'POST' }); } catch (_) {} logout(); });
document.querySelectorAll('[data-view]').forEach(b => b.addEventListener('click', () => show(b.dataset.view)));

if (state.token && state.user) onLoggedIn(); else show('login');
