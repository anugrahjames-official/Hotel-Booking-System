// ============================================
// API HELPER
// ============================================
async function apiFetch(url, options = {}) {
  try {
    const response = await fetch(url, options);
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

// ============================================
// STATE
// ============================================
let rooms = [];
let guests = [];
let bookings = [];

// ============================================
// NAVIGATION
// ============================================
function showSection(sectionId) {
  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
  const section = document.getElementById(sectionId);
  if (section) section.classList.add('active');

  document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
  const activeBtn = document.querySelector(`.nav-btn[data-section="${sectionId}"]`);
  if (activeBtn) activeBtn.classList.add('active');

  const titleEl = document.getElementById('page-title');
  const actionsEl = document.getElementById('header-actions');
  actionsEl.innerHTML = '';

  if (sectionId === 'dashboard-section') {
    titleEl.textContent = 'Dashboard';
  } else if (sectionId === 'rooms-section') {
    titleEl.textContent = 'Rooms';
    actionsEl.innerHTML = `<button class="btn btn-primary" onclick="openModal('add-room-modal')"><i data-lucide="plus"></i> Add Room</button>`;
  } else if (sectionId === 'guests-section') {
    titleEl.textContent = 'Guests';
    actionsEl.innerHTML = `<button class="btn btn-primary" onclick="openModal('register-guest-modal')"><i data-lucide="user-plus"></i> Register</button>`;
  } else if (sectionId === 'bookings-section') {
    titleEl.textContent = 'Bookings';
    actionsEl.innerHTML = `<button class="btn btn-primary" onclick="openNewBookingModal()"><i data-lucide="calendar-plus"></i> New Booking</button>`;
  }
  if (typeof lucide !== 'undefined') {
    lucide.createIcons();
  }
}

// ============================================
// DATA LOADING
// ============================================
async function loadRooms() {
  try {
    const res = await apiFetch('/api/rooms');
    if (res && res.success) {
      rooms = res.data;
      renderRooms();
    } else {
      showToast(res ? res.message : 'Failed to load rooms', 'error');
    }
  } catch (e) {
    showToast('Error loading rooms', 'error');
  }
}

async function loadGuests() {
  try {
    const res = await apiFetch('/api/guests');
    if (res && res.success) {
      guests = res.data;
      renderGuests();
    } else {
      showToast(res ? res.message : 'Failed to load guests', 'error');
    }
  } catch (e) {
    showToast('Error loading guests', 'error');
  }
}

async function loadBookings() {
  try {
    const res = await apiFetch('/api/bookings');
    if (res && res.success) {
      bookings = res.data;
      renderBookings();
    } else {
      showToast(res ? res.message : 'Failed to load bookings', 'error');
    }
  } catch (e) {
    showToast('Error loading bookings', 'error');
  }
}

async function loadAll() {
  await Promise.all([loadRooms(), loadGuests(), loadBookings()]);
  renderDashboard();
}

// ============================================
// RENDERING
// ============================================
function renderDashboard() {
  document.getElementById('stat-total-rooms').textContent = rooms.length;
  document.getElementById('stat-available-rooms').textContent = rooms.filter(r => r.available).length;
  document.getElementById('stat-total-guests').textContent = guests.length;
  document.getElementById('stat-total-bookings').textContent = bookings.length;

  const recentBody = document.getElementById('recent-bookings-body');
  recentBody.innerHTML = '';
  
  if (bookings.length === 0) {
    recentBody.innerHTML = `<tr><td colspan="6" class="empty-state">No recent bookings</td></tr>`;
    return;
  }

  const recent = bookings.slice(0, 5);
  recent.forEach(b => {
    const tr = document.createElement('tr');
    
    const outDate = new Date(b.checkOut);
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    const isActive = outDate >= now;
    const badgeClass = isActive ? 'badge-success' : 'badge-neutral';
    const statusText = isActive ? 'Active' : 'Completed';

    tr.innerHTML = `
      <td>${escapeHtml(b.bookingId.toString())}</td>
      <td>${escapeHtml(b.guestId.toString())}</td>
      <td>${escapeHtml(b.roomNo.toString())}</td>
      <td>${formatDate(b.checkIn)}</td>
      <td>${formatDate(b.checkOut)}</td>
      <td><span class="badge ${badgeClass}">${statusText}</span></td>
    `;
    recentBody.appendChild(tr);
  });
}

function renderRooms() {
  const tbody = document.getElementById('rooms-table-body');
  tbody.innerHTML = '';
  
  const searchEl = document.getElementById('search-rooms');
  const filterEl = document.getElementById('filter-rooms');
  const search = searchEl ? searchEl.value.toLowerCase() : '';
  const filter = filterEl ? filterEl.value : 'all';

  const filtered = rooms.filter(r => {
    const matchSearch = r.roomNo.toString().includes(search) || r.roomType.toLowerCase().includes(search);
    let matchFilter = true;
    if (filter === 'available') matchFilter = r.available;
    if (filter === 'booked') matchFilter = !r.available;
    return matchSearch && matchFilter;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="empty-state">No rooms found.</td></tr>`;
    return;
  }

  filtered.forEach(r => {
    const tr = document.createElement('tr');
    const badgeClass = r.available ? 'badge-success' : 'badge-danger';
    const badgeText = r.available ? 'Available' : 'Booked';

    tr.innerHTML = `
      <td>${escapeHtml(r.roomNo.toString())}</td>
      <td>${escapeHtml(r.roomType)}</td>
      <td>${formatCurrency(r.basePrice)}</td>
      <td><span class="badge ${badgeClass}">${badgeText}</span></td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openEditRoom(${r.roomNo})">Edit</button>
        <button class="btn btn-danger btn-sm" onclick="handleDeleteRoom(${r.roomNo})">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function renderGuests() {
  const tbody = document.getElementById('guests-table-body');
  tbody.innerHTML = '';
  
  const searchEl = document.getElementById('search-guests');
  const search = searchEl ? searchEl.value.toLowerCase() : '';

  const filtered = guests.filter(g => {
    return g.name.toLowerCase().includes(search) || 
           g.guestId.toString().includes(search) ||
           g.contact.includes(search) ||
           g.idProof.toLowerCase().includes(search);
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No guests found.</td></tr>`;
    return;
  }

  filtered.forEach(g => {
    const tr = document.createElement('tr');
    
    let tierClass = 'badge-neutral';
    if (g.loyaltyTier === 'GOLD') tierClass = 'badge-warning';
    if (g.loyaltyTier === 'SILVER') tierClass = 'badge-neutral';

    tr.innerHTML = `
      <td>${escapeHtml(g.guestId.toString())}</td>
      <td>${escapeHtml(g.name)}</td>
      <td>${escapeHtml(g.idProof)}</td>
      <td>${escapeHtml(g.contact)}</td>
      <td><span class="badge ${tierClass}">${escapeHtml(g.loyaltyTier || 'NONE')}</span></td>
      <td>${escapeHtml(g.bookingCount.toString())}</td>
    `;
    tbody.appendChild(tr);
  });
}

function renderBookings() {
  const tbody = document.getElementById('bookings-table-body');
  tbody.innerHTML = '';
  
  const searchEl = document.getElementById('search-bookings');
  const search = searchEl ? searchEl.value.toLowerCase() : '';

  const filtered = bookings.filter(b => {
    return b.bookingId.toString().includes(search) || 
           b.guestId.toString().includes(search) ||
           b.roomNo.toString().includes(search);
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No bookings found.</td></tr>`;
    return;
  }

  filtered.forEach(b => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${escapeHtml(b.bookingId.toString())}</td>
      <td>${escapeHtml(b.guestId.toString())}</td>
      <td>${escapeHtml(b.roomNo.toString())}</td>
      <td>${formatDate(b.checkIn)}</td>
      <td>${formatDate(b.checkOut)}</td>
      <td>${formatCurrency(b.bill)}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openViewBooking(${b.bookingId})">View</button>
        <button class="btn btn-danger btn-sm" onclick="openCancelBookingModal(${b.bookingId})">Cancel</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ============================================
// MODALS
// ============================================
function openModal(id) { 
  const el = document.getElementById(id);
  if(el) el.classList.add('active'); 
}

function closeModal(id) { 
  const el = document.getElementById(id);
  if(el) el.classList.remove('active'); 
}

// ============================================
// ROOM OPERATIONS
// ============================================
async function handleAddRoom(e) {
  e.preventDefault();
  const btn = document.getElementById('add-room-submit');
  btn.disabled = true;
  btn.textContent = 'Adding...';

  const form = e.target;
  const data = new URLSearchParams(new FormData(form));

  try {
    const res = await apiFetch('/api/rooms', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      closeModal('add-room-modal');
      showToast(res.message || 'Room added successfully', 'success');
      form.reset();
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to add room', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Add Room';
  }
}

async function handleEditRoom(e) {
  e.preventDefault();
  const btn = document.getElementById('edit-room-submit');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  const form = e.target;
  const data = new URLSearchParams(new FormData(form));

  try {
    const res = await apiFetch('/api/rooms/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      closeModal('edit-room-modal');
      showToast(res.message || 'Room updated successfully', 'success');
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to update room', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Changes';
  }
}

async function handleDeleteRoom(roomNo) {
  if (!confirm('Delete room ' + roomNo + '?')) return;

  try {
    const data = new URLSearchParams();
    data.append('roomNo', roomNo);

    const res = await apiFetch('/api/rooms/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      showToast(res.message || 'Room deleted successfully', 'success');
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to delete room', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  }
}

function openEditRoom(roomNo) {
  const room = rooms.find(r => r.roomNo === roomNo);
  if (room) {
    document.getElementById('edit-room-no').value = room.roomNo;
    document.getElementById('edit-room-type').value = room.roomType;
    document.getElementById('edit-room-price').value = room.basePrice;
    openModal('edit-room-modal');
  }
}

// ============================================
// GUEST OPERATIONS
// ============================================
async function handleRegisterGuest(e) {
  e.preventDefault();
  const btn = document.getElementById('register-guest-submit');
  btn.disabled = true;
  btn.textContent = 'Registering...';

  const form = e.target;
  const data = new URLSearchParams(new FormData(form));

  try {
    const res = await apiFetch('/api/guests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      closeModal('register-guest-modal');
      showToast(res.message || 'Guest registered successfully', 'success');
      form.reset();
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to register guest', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Register';
  }
}

// ============================================
// BOOKING OPERATIONS
// ============================================
function openNewBookingModal() {
  const guestSelect = document.getElementById('booking-guest');
  guestSelect.innerHTML = '<option value="">Select Guest</option>';
  guests.forEach(g => {
    const opt = document.createElement('option');
    opt.value = g.guestId;
    opt.textContent = `${g.name} (ID: ${g.guestId})`;
    guestSelect.appendChild(opt);
  });

  const roomSelect = document.getElementById('booking-room');
  roomSelect.innerHTML = '<option value="">Select Room</option>';
  const availableRooms = rooms.filter(r => r.available);
  availableRooms.forEach(r => {
    const opt = document.createElement('option');
    opt.value = r.roomNo;
    opt.textContent = `Room ${r.roomNo} - ${r.roomType} (${formatCurrency(r.basePrice)})`;
    roomSelect.appendChild(opt);
  });

  openModal('new-booking-modal');
}

async function handleNewBooking(e) {
  e.preventDefault();
  const btn = document.getElementById('new-booking-submit');
  btn.disabled = true;
  btn.textContent = 'Creating...';

  const form = e.target;
  const data = new URLSearchParams(new FormData(form));

  try {
    const res = await apiFetch('/api/bookings', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      closeModal('new-booking-modal');
      showToast(res.message || 'Booking created successfully', 'success');
      form.reset();
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to create booking', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Create Booking';
  }
}

function openCancelBookingModal(bookingId) {
  document.getElementById('cancel-booking-id-display').textContent = bookingId;
  document.getElementById('cancel-booking-id').value = bookingId;
  openModal('cancel-booking-modal');
}

async function handleCancelBooking() {
  const bookingId = document.getElementById('cancel-booking-id').value;
  const btn = document.getElementById('confirm-cancel-booking');
  btn.disabled = true;
  btn.textContent = 'Canceling...';

  try {
    const data = new URLSearchParams();
    data.append('bookingId', bookingId);

    const res = await apiFetch('/api/bookings/cancel', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: data
    });

    if (res && res.success) {
      closeModal('cancel-booking-modal');
      showToast(res.message || 'Booking cancelled successfully', 'success');
      await loadAll();
    } else {
      showToast(res ? res.message : 'Failed to cancel booking', 'error');
    }
  } catch (error) {
    showToast('An error occurred', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Cancel Booking';
  }
}

function openViewBooking(bookingId) {
  const booking = bookings.find(b => b.bookingId === bookingId);
  if (booking) {
    const guest = guests.find(g => g.guestId === booking.guestId);
    const guestName = guest ? guest.name : 'Unknown';

    document.getElementById('view-b-id').textContent = booking.bookingId;
    document.getElementById('view-b-guest').textContent = `${guestName} (ID: ${booking.guestId})`;
    document.getElementById('view-b-room').textContent = booking.roomNo;
    document.getElementById('view-b-in').textContent = formatDate(booking.checkIn);
    document.getElementById('view-b-out').textContent = formatDate(booking.checkOut);
    document.getElementById('view-b-bill').textContent = formatCurrency(booking.bill);

    openModal('view-booking-modal');
  }
}

// ============================================
// SEARCH & FILTER
// ============================================
function filterRooms() {
  renderRooms();
}

function searchGuests() {
  renderGuests();
}

function searchBookings() {
  renderBookings();
}

// ============================================
// TOAST NOTIFICATIONS
// ============================================
function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  
  container.appendChild(toast);
  
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%) scale(0.9)';
    toast.style.transition = 'all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ============================================
// UTILITIES
// ============================================
function formatCurrency(amount) {
  return '₹' + new Intl.NumberFormat('en-IN').format(amount);
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  let parts = dateStr.split('-');
  if (parts.length === 3) {
      let year, month, day;
      if (parts[2].length === 4) { // DD-MM-YYYY
          year = parseInt(parts[2], 10);
          month = parseInt(parts[1], 10) - 1;
          day = parseInt(parts[0], 10);
      } else { // YYYY-MM-DD
          year = parseInt(parts[0], 10);
          month = parseInt(parts[1], 10) - 1;
          day = parseInt(parts[2], 10);
      }
      const d = new Date(year, month, day);
      if (!isNaN(d)) {
          return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
      }
  }
  
  const d = new Date(dateStr);
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

// ============================================
// INITIALIZATION
// ============================================
document.addEventListener('DOMContentLoaded', () => {
  // Navigation
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      showSection(e.currentTarget.dataset.section);
    });
  });

  // Search and Filter Listeners
  const sr = document.getElementById('search-rooms');
  if(sr) sr.addEventListener('input', filterRooms);
  
  const fr = document.getElementById('filter-rooms');
  if(fr) fr.addEventListener('change', filterRooms);
  
  const sg = document.getElementById('search-guests');
  if(sg) sg.addEventListener('input', searchGuests);
  
  const sb = document.getElementById('search-bookings');
  if(sb) sb.addEventListener('input', searchBookings);

  // Forms
  const addRoomForm = document.getElementById('add-room-form');
  if(addRoomForm) addRoomForm.addEventListener('submit', handleAddRoom);
  
  const editRoomForm = document.getElementById('edit-room-form');
  if(editRoomForm) editRoomForm.addEventListener('submit', handleEditRoom);
  
  const regGuestForm = document.getElementById('register-guest-form');
  if(regGuestForm) regGuestForm.addEventListener('submit', handleRegisterGuest);
  
  const newBookingForm = document.getElementById('new-booking-form');
  if(newBookingForm) newBookingForm.addEventListener('submit', handleNewBooking);

  // Confirmation Modals
  const cancelBtn = document.getElementById('confirm-cancel-booking');
  if(cancelBtn) cancelBtn.addEventListener('click', handleCancelBooking);

  // Modals Close handlers
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      closeModal(e.currentTarget.dataset.closeModal);
    });
  });

  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay && overlay.id !== 'cancel-booking-modal') {
        overlay.classList.remove('active');
      }
    });
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const activeModal = document.querySelector('.modal-overlay.active');
      if (activeModal && activeModal.id !== 'cancel-booking-modal') {
        activeModal.classList.remove('active');
      }
    }
  });

  // Initial load
  showSection('dashboard-section');
  loadAll();
  if (typeof lucide !== 'undefined') {
    lucide.createIcons();
  }
});
