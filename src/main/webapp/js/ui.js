// ═══════════════════════════════════════════════════════
// UI MODULE — Shared UI utilities
// ═══════════════════════════════════════════════════════

// ── Dark/Light Mode Toggle ──
function initTheme() {
    const saved = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', saved);
    updateThemeIcon(saved);
}

function toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme');
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    localStorage.setItem('theme', next);
    updateThemeIcon(next);
}

function updateThemeIcon(theme) {
    const btn = document.getElementById('themeToggle');
    if (btn) btn.textContent = theme === 'dark' ? '☀️' : '🌙';
}

// ── Sidebar Toggle (Mobile) ──
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    if (sidebar) sidebar.classList.toggle('open');
}

// ── Toast Notifications ──
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    
    const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${icons[type] || ''}</span><span>${message}</span>`;
    container.appendChild(toast);
    
    setTimeout(() => toast.remove(), 4000);
}

// ── Loading Spinner ──
function showLoader() {
    let overlay = document.querySelector('.spinner-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.className = 'spinner-overlay';
        overlay.innerHTML = '<div class="spinner"></div>';
        document.body.appendChild(overlay);
    }
    // Fix race condition: add class synchronously so hideLoader can remove it properly if called immediately
    overlay.classList.add('active');
}

function hideLoader() {
    const overlay = document.querySelector('.spinner-overlay');
    if (overlay) overlay.classList.remove('active');
}

// ── Modal Helpers ──
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
}

// ── Status Badge Helper ──
function getStatusBadge(status) {
    const map = {
        'Pending': 'badge-pending',
        'In Progress': 'badge-progress',
        'Resolved': 'badge-resolved',
        'Rejected': 'badge-rejected'
    };
    return `<span class="badge ${map[status] || 'badge-pending'}">${status}</span>`;
}

function getInsuranceBadge(status) {
    if (status === 'Valid') return '<span class="badge badge-valid">✓ Valid</span>';
    return '<span class="badge badge-expired">✗ Expired</span>';
}

// ── Format Date ──
function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    try {
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' });
    } catch { return dateStr; }
}

// ── User Avatar Initial ──
function getInitials(name) {
    if (!name) return '?';
    return name.trim().split(' ').map(w => w[0]).join('').toUpperCase().substring(0, 2);
}

function updateHeaderAvatar(name) {
    const avatar = document.querySelector('.user-avatar');
    if (avatar && name) {
        avatar.textContent = getInitials(name);
        // Deterministic color based on name
        const colors = [
            'linear-gradient(135deg, #ef4444, #dc2626)',
            'linear-gradient(135deg, #3b82f6, #2563eb)',
            'linear-gradient(135deg, #10b981, #059669)',
            'linear-gradient(135deg, #f59e0b, #d97706)',
            'linear-gradient(135deg, #8b5cf6, #7c3aed)',
            'linear-gradient(135deg, #06b6d4, #0891b2)'
        ];
        let hash = 0;
        for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
        avatar.style.background = colors[Math.abs(hash) % colors.length];
    }
}

// ── Form Validation ──
function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validateVehicleNo(vno) {
    // Indian vehicle number format: XX00XX0000
    return /^[A-Z]{2}\d{2}[A-Z]{1,2}\d{4}$/.test(vno.toUpperCase().replace(/\s/g, ''));
}

// ── Password Strength ──
function getPasswordStrength(password) {
    let score = 0;
    if (password.length >= 6) score++;
    if (password.length >= 10) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    return score; // 0-5
}

function updatePasswordStrengthBar(password) {
    const bar = document.querySelector('.password-strength-bar');
    if (!bar) return;
    const strength = getPasswordStrength(password);
    const widths = ['0%', '20%', '40%', '60%', '80%', '100%'];
    const colors = ['#ef4444', '#ef4444', '#eab308', '#eab308', '#22c55e', '#22c55e'];
    bar.style.width = widths[strength];
    bar.style.background = colors[strength];
}

// ── Initialize Theme on Load ──
document.addEventListener('DOMContentLoaded', initTheme);

// ── Notifications UI ──
function toggleNotifications() {
    const dropdown = document.getElementById('notifDropdown');
    if (dropdown) dropdown.style.display = dropdown.style.display === 'none' ? 'block' : 'none';
}

async function loadNotifications() {
    try {
        const notifs = await apiGet('/api/notification');
        renderNotifications(notifs);
    } catch (e) { console.error('Failed to load notifications:', e); }
}

function renderNotifications(notifs) {
    const list = document.getElementById('notifList');
    const badge = document.getElementById('notifBadge');
    if (!list) return;

    const unreadCount = notifs.filter(n => !n.isRead).length;
    if (unreadCount > 0) {
        badge.textContent = unreadCount;
        badge.style.display = 'block';
    } else {
        badge.style.display = 'none';
    }

    if (notifs.length === 0) {
        list.innerHTML = '<div style="padding:var(--space-8); text-align:center; color:var(--text-muted);">No new messages</div>';
        return;
    }

    list.innerHTML = notifs.map(n => `
        <div class="notif-item ${n.isRead ? '' : 'unread'}" onclick="handleNotifClick('${n.id}', '${n.type}', ${n.fineAmount})">
            <div class="notif-title">${n.title}</div>
            <div class="notif-msg">${n.message}</div>
            <div class="notif-time">${formatDate(n.createdAt)}</div>
        </div>
    `).join('');
}

async function markAllRead() {
    try {
        const notifs = await apiGet('/api/notification');
        const unreadIds = notifs.filter(n => !n.isRead).map(n => n.id);
        
        for (const id of unreadIds) {
            await apiPost('/api/notification/read', { id });
        }
        
        loadNotifications();
        showToast('All notifications marked as read', 'info');
    } catch (e) {
        console.error('Failed to mark all read:', e);
    }
}

// ── Account Frozen / Penalty Logic ──
function showFrozenModal(fineAmount) {
    let overlay = document.getElementById('penaltyModalOverlay');
    if (overlay) return;

    overlay = document.createElement('div');
    overlay.id = 'penaltyModalOverlay';
    overlay.style.cssText = `
        position:fixed; top:0; left:0; right:0; bottom:0;
        background:rgba(15, 23, 42, 0.95); backdrop-filter:blur(20px);
        display:flex; align-items:center; justify-content:center;
        z-index:20000; animation:fadeIn 0.5s ease-out;
    `;

    const modal = document.createElement('div');
    modal.style.cssText = `
        background:var(--surface); border:1px solid rgba(239, 68, 68, 0.3);
        border-radius:32px; padding:60px 40px;
        max-width:550px; width:90%; text-align:center;
        box-shadow:0 50px 100px rgba(0,0,0,0.8);
        animation:scaleIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
        position:relative; overflow:hidden;
    `;

    modal.innerHTML = `
        <div style="font-size:5rem;margin-bottom:24px;filter:drop-shadow(0 0 20px rgba(239, 68, 68, 0.4));">🚫</div>
        <h2 style="font-size:2.25rem;font-weight:900;margin-bottom:16px;color:#fff;letter-spacing:-0.04em;">Account Suspended</h2>
        <p style="color:#94a3b8;margin-bottom:32px;line-height:1.7;font-size:1.1rem;">
            Your account has been restricted due to a verified traffic violation. 
            To restore access, you must acknowledge the incident and clear the penalty.
        </p>
        
        <div style="background:rgba(239, 68, 68, 0.05); border:1px solid rgba(239, 68, 68, 0.2); border-radius:20px; padding:24px; margin-bottom:32px; display:flex; flex-direction:column; align-items:center; gap:16px;">
            <div style="font-size:0.9rem; color:#fca5a5; font-weight:700; text-transform:uppercase; letter-spacing:0.1em;">Fine Amount Due</div>
            <div style="font-size:3rem; font-weight:900; color:#fff;">₹${fineAmount.toFixed(2)}</div>
            <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=PAYMENT_GATEWAY_STUB" alt="QR Code" style="border-radius:12px; border:8px solid #fff;">
            <div style="font-size:0.8rem; color:#94a3b8;">Scan to pay using any UPI app</div>
        </div>

        <button id="payFineBtn" onclick="handleFinePayment()" style="
            width:100%; padding:20px; font-size:1.1rem; font-weight:800;
            border:none; border-radius:18px;
            background:linear-gradient(135deg, #ef4444, #b91c1c);
            color:white; cursor:pointer;
            transition:all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow:0 10px 30px rgba(239, 68, 68, 0.3);
        " onmouseover="this.style.transform='translateY(-2px)';this.style.boxShadow='0 15px 40px rgba(239, 68, 68, 0.4)'"
           onmouseout="this.style.transform='';this.style.boxShadow='0 10px 30px rgba(239, 68, 68, 0.3)'">
            I understand and I've paid the fine
        </button>
        <p style="color:#64748b;font-size:0.85rem;margin-top:24px;">
            Access will be restored immediately after acknowledgement.
        </p>
    `;

    overlay.appendChild(modal);
    document.body.appendChild(overlay);

    if (!document.querySelector('#penaltyStyles')) {
        const s = document.createElement('style');
        s.id = 'penaltyStyles';
        s.textContent = `
            @keyframes fadeIn { from { opacity:0; } to { opacity:1; } }
            @keyframes scaleIn { from { opacity:0; transform:scale(0.8) translateY(40px); } to { opacity:1; transform:scale(1) translateY(0); } }
        `;
        document.head.appendChild(s);
    }
}

async function handleFinePayment() {
    const btn = document.getElementById('payFineBtn');
    btn.disabled = true;
    btn.textContent = 'Restoring account...';
    
    try {
        await apiPost('/api/notification/pay-fine', {});
        showToast('Account restored successfully!', 'success');
        setTimeout(() => window.location.reload(), 1500);
    } catch (e) {
        showToast('Verification failed. Please try again.', 'error');
        btn.disabled = false;
        btn.textContent = "I understand and I've paid the fine";
    }
}

function handleNotifClick(id, type, fine) {
    if (type === 'Penalty') {
        showFrozenModal(fine);
    }
    // Mark as read
    apiPost('/api/notification/read', { id }).then(() => loadNotifications());
}
