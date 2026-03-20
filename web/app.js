const state = {
    userId: null,
    role: null,
    fullName: null,
    currentPage: 'catalogue'
};

// Elements
const loginScreen = document.getElementById('login-screen');
const appShell = document.getElementById('app-shell');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const toastEl = document.getElementById('toast');

const navAdmin = document.getElementById('nav-admin');

// Pages
const pages = {
    catalogue: document.getElementById('page-catalogue'),
    history: document.getElementById('page-history'),
    admin: document.getElementById('page-admin'),
    'book-details': document.getElementById('page-book-details')
};

// Nav buttons
const navBtns = {
    catalogue: document.getElementById('nav-catalogue'),
    history: document.getElementById('nav-history'),
    admin: document.getElementById('nav-admin')
};

const API_URL = 'http://localhost:8080/api';

// --- Login Logic ---
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const btn = loginForm.querySelector('button');
    
    try {
        btn.textContent = 'Authenticating...';
        btn.disabled = true;
        
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        
        if (data.success) {
            state.userId = data.userId;
            state.role = data.role;
            state.fullName = data.fullName;
            
            document.getElementById('user-name').textContent = data.fullName;
            
            // Show/hide admin panel based on role
            if (state.role === 'LIBRARIAN') {
                navAdmin.style.display = 'block';
            } else {
                navAdmin.style.display = 'none';
            }
            
            loginScreen.classList.remove('active');
            appShell.classList.add('active');
            
            // Redirect to Profile (My History) by default upon login
            navigateTo('history');
            loginForm.reset();
        } else {
            loginError.textContent = data.error || 'Invalid credentials';
        }
    } catch (err) {
        loginError.textContent = 'Failed to connect to server. Is it running?';
    } finally {
        btn.textContent = 'Sign In';
        btn.disabled = false;
    }
});

function toggleRegister(showReg) {
    document.getElementById('login-form').style.display = showReg ? 'none' : 'block';
    document.getElementById('register-form').style.display = showReg ? 'block' : 'none';
    document.getElementById('login-error').textContent = '';
    document.getElementById('register-error').textContent = '';
}

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const registerError = document.getElementById('register-error');
    
    const payload = {
        username: document.getElementById('reg-username').value,
        password: document.getElementById('reg-password').value,
        fullName: document.getElementById('reg-fullname').value,
        email: document.getElementById('reg-email').value,
        phone: document.getElementById('reg-phone').value
    };
    
    try {
        btn.textContent = 'Registering...';
        btn.disabled = true;
        
        const response = await fetch(`${API_URL}/register`, {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        
        const data = await response.json();
        
        if (data.success) {
            // Auto-login
            document.getElementById('username').value = payload.username;
            document.getElementById('password').value = payload.password;
            toggleRegister(false);
            loginForm.dispatchEvent(new Event('submit'));
        } else {
            registerError.textContent = data.error || 'Registration failed';
        }
    } catch (err) {
        registerError.textContent = 'Network error.';
    } finally {
        btn.textContent = 'Register & Login';
        btn.disabled = false;
    }
});

document.getElementById('logout-btn').addEventListener('click', () => {
    state.userId = null;
    state.role = null;
    appShell.classList.remove('active');
    loginScreen.classList.add('active');
});

// --- Routing ---
function navigateTo(pageId) {
    state.currentPage = pageId;
    
    // Toggle active classes on pages
    Object.values(pages).forEach(p => p.classList.remove('active'));
    pages[pageId].classList.add('active');
    
    // Toggle active classes on nav
    Object.values(navBtns).forEach(b => b.classList.remove('active'));
    navBtns[pageId].classList.add('active');
    
    // Load Data
    if (pageId === 'catalogue') loadBooks();
    if (pageId === 'history') loadHistory();
    if (pageId === 'admin') loadAdminIssues();
}

// --- Data Fetching ---

let allBooks = [];
let currentCategoryFilter = 'All';

function filterCategory(category) {
    currentCategoryFilter = category;
    
    // Update active class on buttons
    const chips = document.querySelectorAll('.cat-chip');
    chips.forEach(chip => {
        if (chip.textContent === category) chip.classList.add('active');
        else chip.classList.remove('active');
    });
    
    renderBooks();
}

async function loadBooks() {
    try {
        const response = await fetch(`${API_URL}/books`);
        const books = await response.json();
        allBooks = books; // Cache for quick detail view access
        renderBooks();
    } catch (err) {
        showToast('Failed to load catalogue.', 'error');
    }
}

function renderBooks() {
    const tbody = document.getElementById('books-table-body');
    tbody.innerHTML = '';
    
    const displayBooks = currentCategoryFilter === 'All' 
        ? allBooks 
        : allBooks.filter(b => b.genre === currentCategoryFilter);
        
    displayBooks.forEach(book => {
        const isAvailable = book.available > 0;
        const statusClass = isAvailable ? 'status-available' : 'status-unavailable';
        const statusText = isAvailable ? `Available (${book.available}/${book.total})` : 'Out of Stock';
        
        tbody.innerHTML += `
            <tr>
                <td><strong>${book.title}</strong></td>
                <td><span class="status-badge" style="background:#e2e8f0;color:var(--text-secondary);font-size:0.8rem">${book.genre}</span></td>
                <td>${book.author}</td>
                <td>${book.isbn}</td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>
                    <button class="btn-secondary" onclick="viewBook(${book.id})">View Details</button>
                </td>
            </tr>
        `;
    });
}

async function loadHistory() {
    try {
        const response = await fetch(`${API_URL}/history?userId=${state.userId}`);
        if (!response.ok) throw new Error('Network response was not ok');
        const issues = await response.json();
        const tbody = document.getElementById('history-table-body');
        
        tbody.innerHTML = '';
        if (issues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center">No borrowing history found.</td></tr>';
            return;
        }
        
        issues.forEach(issue => {
            let sClass = 'status-issued';
            let canReturn = false;
            if (issue.status === 'RETURNED') sClass = 'status-returned';
            else { 
                canReturn = true; 
                if (issue.status === 'OVERDUE') sClass = 'status-overdue';
            }

            tbody.innerHTML += `
                <tr>
                    <td>Book #${issue.bookId}</td>
                    <td>${issue.issueDate}</td>
                    <td>${issue.dueDate}</td>
                    <td><span class="status-badge ${sClass}">${issue.status}</span></td>
                    <td>${issue.fine > 0 ? '₹' + issue.fine : 'None'}</td>
                    <td>
                        ${canReturn ? `<button class="btn-secondary" onclick="returnBook(${issue.bookId})">Return Book</button>` : ''}
                    </td>
                </tr>
            `;
        });
    } catch (err) {
        showToast('Failed to load history.', 'error');
    }
}

async function loadAdminIssues() {
    try {
        const response = await fetch(`${API_URL}/admin/issues/all`);
        if (!response.ok) throw new Error('Network response was not ok');
        const issues = await response.json();
        const tbody = document.getElementById('admin-table-body');
        
        tbody.innerHTML = '';
        if (issues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center">No active issues found.</td></tr>';
            return;
        }
        
        issues.forEach(issue => {
            let sClass = issue.status === 'OVERDUE' ? 'status-overdue' : 'status-issued';
            tbody.innerHTML += `
                <tr>
                    <td>#${issue.issueId}</td>
                    <td>User #${issue.userId}</td>
                    <td>Book #${issue.bookId}</td>
                    <td>${issue.issueDate}</td>
                    <td>${issue.dueDate}</td>
                    <td><span class="status-badge ${sClass}">${issue.status}</span></td>
                </tr>
            `;
        });
    } catch (err) {
        showToast('Failed to load active issues.', 'error');
    }
}

// --- Actions ---

function viewBook(id) {
    const book = allBooks.find(b => b.id === id);
    if (!book) return;
    
    document.getElementById('detail-title').textContent = book.title;
    document.getElementById('detail-author').textContent = 'by ' + book.author;
    document.getElementById('detail-isbn').textContent = 'ISBN: ' + book.isbn;
    document.getElementById('detail-desc').textContent = book.description;
    
    const borrowBtn = document.getElementById('detail-borrow-btn');
    if (book.available > 0) {
        borrowBtn.disabled = false;
        borrowBtn.textContent = 'Borrow this Book';
        borrowBtn.onclick = () => { issueBook(book.id); };
        borrowBtn.style.background = 'var(--primary)';
        borrowBtn.style.cursor = 'pointer';
    } else {
        borrowBtn.disabled = true;
        borrowBtn.textContent = 'Currently Unavailable';
        borrowBtn.onclick = null;
        borrowBtn.style.background = '#cbd5e1';
        borrowBtn.style.cursor = 'not-allowed';
    }
    
    navigateTo('book-details');
}

async function issueBook(bookId) {
    try {
        const res = await fetch(`${API_URL}/issue`, {
            method: 'POST',
            body: JSON.stringify({ bookId: bookId, userId: state.userId })
        });
        const data = await res.json();
        if (data.success) { showToast(data.message, 'success'); loadBooks(); }
        else { showToast(data.error, 'error'); }
    } catch (err) { showToast('Network error while borrowing.', 'error'); }
}

async function returnBook(bookId) {
    try {
        const res = await fetch(`${API_URL}/return`, {
            method: 'POST',
            body: JSON.stringify({ bookId: bookId, userId: state.userId })
        });
        const data = await res.json();
        if (data.success) { 
            showToast(data.message, 'success'); 
            loadBooks(); 
            if (state.currentPage === 'history') loadHistory();
        } else { showToast(data.error, 'error'); }
    } catch (err) { showToast('Network error while returning.', 'error'); }
}

function showToast(message, type = 'success') {
    toastEl.textContent = message;
    toastEl.className = `toast ${type}`;
    toastEl.classList.remove('hidden');
    setTimeout(() => { toastEl.classList.add('hidden'); }, 3000);
}
