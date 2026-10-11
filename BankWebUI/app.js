/**
 * app.js
 * 
 * Purpose:
 * Client-side JavaScript connecting the Bank Management Web UI to the
 * Java HttpServer REST API (/api/login, /api/register, /api/deposit, /api/withdraw, /api/transfer, /api/statement).
 * Manages user session state in sessionStorage and dynamically renders live data.
 * 
 * Target Audience: 2nd-Year Engineering Students learning Asynchronous JavaScript & REST APIs.
 */

// Global API Base URL (Relative path works automatically with BankWebServer)
const API_BASE = '/api';

// ==================== SESSION MANAGEMENT ====================

function getCurrentUser() {
    const userStr = sessionStorage.getItem('apex_bank_user');
    if (!userStr) return null;
    try {
        return JSON.parse(userStr);
    } catch (e) {
        return null;
    }
}

function setCurrentUser(userData) {
    sessionStorage.setItem('apex_bank_user', JSON.stringify(userData));
}

function clearSession() {
    sessionStorage.removeItem('apex_bank_user');
}

function requireAuth() {
    const user = getCurrentUser();
    if (!user || !user.accountNumber) {
        window.location.href = 'login.html';
        return null;
    }
    return user;
}

// ==================== UI NOTIFICATIONS (TOASTS) ====================

function showToast(message, type = 'success') {
    let toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 9999;
            display: flex;
            flex-direction: column;
            gap: 10px;
        `;
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.style.cssText = `
        background-color: ${type === 'success' ? '#16a34a' : '#dc2626'};
        color: white;
        padding: 12px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        font-size: 0.95rem;
        font-weight: 500;
        min-width: 250px;
        max-width: 380px;
        opacity: 0;
        transform: translateY(-10px);
        transition: all 0.3s ease;
    `;
    toast.innerText = message;
    toastContainer.appendChild(toast);

    // Fade in
    setTimeout(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    }, 10);

    // Auto dismiss
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ==================== GENERIC API HELPER ====================

async function apiRequest(endpoint, method = 'GET', bodyData = null) {
    const options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };

    if (bodyData && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(bodyData);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, options);
        const data = await response.json();
        return { status: response.status, data: data };
    } catch (err) {
        console.error('API Error:', err);
        return { 
            status: 500, 
            data: { success: false, message: 'Server is offline or unreachable. Please ensure BankWebServer is running.' } 
        };
    }
}

// ==================== PAGE-SPECIFIC INITIALIZERS ====================

document.addEventListener('DOMContentLoaded', () => {
    // 1. LOGIN PAGE
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const accNo = document.getElementById('accountNumber').value.trim();
            const pin = document.getElementById('pin').value.trim();
            const submitBtn = loginForm.querySelector('button[type="submit"]');

            submitBtn.disabled = true;
            submitBtn.innerText = 'Verifying...';

            const res = await apiRequest('/login', 'POST', { accountNumber: accNo, pin: pin });
            submitBtn.disabled = false;
            submitBtn.innerText = 'Sign In to Account';

            if (res.data && res.data.success) {
                // Store user session including PIN for authenticating subsequent actions
                setCurrentUser({
                    accountNumber: res.data.accountNumber,
                    holderName: res.data.holderName,
                    balance: res.data.balance,
                    pin: pin
                });
                showToast(`Welcome back, ${res.data.holderName}!`, 'success');
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1000);
            } else {
                showToast(res.data.message || 'Login failed. Please check credentials.', 'error');
            }
        });
    }

    // 2. REGISTRATION PAGE
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const name = document.getElementById('holderName').value.trim();
            const accNo = document.getElementById('accountNumber').value.trim();
            const initialDeposit = parseFloat(document.getElementById('initialDeposit').value);
            const pin = document.getElementById('pin').value.trim();
            const submitBtn = registerForm.querySelector('button[type="submit"]');

            submitBtn.disabled = true;
            submitBtn.innerText = 'Opening Account...';

            const res = await apiRequest('/register', 'POST', {
                holderName: name,
                accountNumber: accNo,
                initialDeposit: initialDeposit,
                pin: pin
            });

            submitBtn.disabled = false;
            submitBtn.innerText = 'Create Account';

            if (res.data && res.data.success) {
                showToast('Account created successfully! Redirecting to login...', 'success');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 1500);
            } else {
                showToast(res.data.message || 'Registration failed.', 'error');
            }
        });
    }

    // 3. DASHBOARD PAGE
    const dashboardContainer = document.getElementById('dashboardView');
    if (dashboardContainer) {
        initDashboard();
    }

    // 4. TRANSFER PAGE
    const transferForm = document.getElementById('transferForm');
    if (transferForm) {
        initTransferPage();
    }

    // 5. LOGOUT LINKS
    const logoutLinks = document.querySelectorAll('.logout-btn');
    logoutLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            clearSession();
            window.location.href = 'login.html';
        });
    });
});

// ==================== DASHBOARD FUNCTIONS ====================

async function initDashboard() {
    const user = requireAuth();
    if (!user) return;

    // Display user profile info
    const nameElements = document.querySelectorAll('.user-name');
    nameElements.forEach(el => el.innerText = user.holderName);

    const accElements = document.querySelectorAll('.user-acc');
    accElements.forEach(el => el.innerText = user.accountNumber);

    await refreshDashboardData(user);
}

async function refreshDashboardData(user) {
    // 1. Fetch live balance
    const accRes = await apiRequest(`/account?accountNumber=${user.accountNumber}&pin=${user.pin}`, 'GET');
    if (accRes.data && accRes.data.success) {
        const balElement = document.getElementById('accountBalance');
        if (balElement) {
            balElement.innerText = `$${parseFloat(accRes.data.balance).toFixed(2)}`;
        }
        // Update session
        user.balance = accRes.data.balance;
        setCurrentUser(user);
    }

    // 2. Fetch mini statement
    const stmtRes = await apiRequest(`/statement?accountNumber=${user.accountNumber}`, 'GET');
    const tableBody = document.getElementById('statementTableBody');
    if (tableBody) {
        tableBody.innerHTML = '';

        if (stmtRes.data && stmtRes.data.transactions && stmtRes.data.transactions.length > 0) {
            // Reverse so recent are first
            const txns = [...stmtRes.data.transactions].reverse();
            txns.forEach(txn => {
                const tr = document.createElement('tr');
                let badgeClass = 'badge-deposit';
                let amountPrefix = '+$';
                let amountColor = 'var(--success-color)';

                if (txn.type.toLowerCase() === 'withdraw') {
                    badgeClass = 'badge-withdraw';
                    amountPrefix = '-$';
                    amountColor = 'var(--danger-color)';
                } else if (txn.type.toLowerCase() === 'transfer') {
                    badgeClass = 'badge-transfer';
                    amountPrefix = '-$';
                    amountColor = 'var(--primary-color)';
                }

                tr.innerHTML = `
                    <td><code>${txn.id}</code></td>
                    <td>${txn.date}</td>
                    <td><span class="badge ${badgeClass}">${txn.type}</span></td>
                    <td style="color: ${amountColor}; font-weight: 600;">${amountPrefix}${parseFloat(txn.amount).toFixed(2)}</td>
                    <td>${txn.remarks}</td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 2rem;">No transaction activity recorded yet.</td></tr>`;
        }
    }
}

// Modal / Prompt Deposit
async function handleQuickDeposit() {
    const user = requireAuth();
    if (!user) return;

    const amountStr = prompt(`Enter amount to deposit into Account ${user.accountNumber} ($):`);
    if (!amountStr) return;

    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount <= 0) {
        showToast('Please enter a valid positive deposit amount.', 'error');
        return;
    }

    const res = await apiRequest('/deposit', 'POST', {
        accountNumber: user.accountNumber,
        amount: amount
    });

    if (res.data && res.data.success) {
        showToast(res.data.message || `Successfully deposited $${amount.toFixed(2)}!`, 'success');
        await refreshDashboardData(user);
    } else {
        showToast(res.data.message || 'Deposit failed.', 'error');
    }
}

// Modal / Prompt Withdraw
async function handleQuickWithdraw() {
    const user = requireAuth();
    if (!user) return;

    const amountStr = prompt(`Enter amount to withdraw from Account ${user.accountNumber} ($):`);
    if (!amountStr) return;

    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount <= 0) {
        showToast('Please enter a valid positive withdrawal amount.', 'error');
        return;
    }

    const pin = prompt('Enter your 4-digit PIN to authorize withdrawal:');
    if (!pin) return;

    const res = await apiRequest('/withdraw', 'POST', {
        accountNumber: user.accountNumber,
        pin: pin,
        amount: amount
    });

    if (res.data && res.data.success) {
        showToast(res.data.message || `Successfully withdrew $${amount.toFixed(2)}!`, 'success');
        await refreshDashboardData(user);
    } else {
        showToast(res.data.message || 'Withdrawal failed.', 'error');
    }
}

// ==================== TRANSFER PAGE FUNCTIONS ====================

function initTransferPage() {
    const user = requireAuth();
    if (!user) return;

    const fromAccInput = document.getElementById('fromAccount');
    if (fromAccInput) {
        fromAccInput.value = `${user.accountNumber} (${user.holderName} - Balance: $${parseFloat(user.balance).toFixed(2)})`;
    }

    const transferForm = document.getElementById('transferForm');
    transferForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const toAcc = document.getElementById('toAccount').value.trim();
        const amount = parseFloat(document.getElementById('amount').value);
        const remarks = document.getElementById('remarks').value.trim();
        const pin = document.getElementById('pin').value.trim();
        const submitBtn = transferForm.querySelector('button[type="submit"]');

        submitBtn.disabled = true;
        submitBtn.innerText = 'Processing Transfer...';

        const res = await apiRequest('/transfer', 'POST', {
            fromAccount: user.accountNumber,
            pin: pin,
            toAccount: toAcc,
            amount: amount,
            remarks: remarks
        });

        submitBtn.disabled = false;
        submitBtn.innerText = 'Confirm Transfer';

        if (res.data && res.data.success) {
            showToast(res.data.message || 'Transfer completed successfully!', 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1500);
        } else {
            showToast(res.data.message || 'Transfer failed. Check details.', 'error');
        }
    });
}
