// Fabricore Core Application Frame: Router, Auth, Client, Toast, and Dashboard Loader

// Default Mock Sandbox Data storage
const mockStore = {
    suppliers: [
        { id: "s1", name: "Steel Corp LLC", contactPerson: "Rudra Jain", phone: "+919876543211", email: "jr@steel.com" },
        { id: "s2", name: "Apex Polymers Ltd", contactPerson: "Sarah Sah", phone: "+919876543222", email: "sarah@apex.com" }
    ],
    materials: [
        { id: "m1", sku: "STL-REBAR-12", name: "12mm Steel Rebar", unit: "TONS", quantity: 25.00, reorderLevel: 8.00, supplierId: "s1", supplierName: "Steel Corp LLC", lowStock: false },
        { id: "m2", sku: "PLM-NLYN-09", name: "Nylon 66 Polymer Pellets", unit: "KGS", quantity: 4.50, reorderLevel: 10.00, supplierId: "s2", supplierName: "Apex Polymers Ltd", lowStock: true }
    ],
    transactions: [
        { id: "t1", materialId: "m1", quantity: 25.00, type: "STOCK_IN", notes: "Received initial loading", timestamp: "2026-06-01T08:30:00", employeeName: "SYSTEM" },
        { id: "t2", materialId: "m2", quantity: 4.50, type: "STOCK_IN", notes: "First production batch entry", timestamp: "2026-06-01T09:15:00", employeeName: "SYSTEM" }
    ],
    shifts: [
        { id: "sh1", name: "Morning Shift", startTime: "08:00:00", endTime: "16:00:00", gracePeriodMinutes: 15 },
        { id: "sh2", name: "Night Shift", startTime: "16:00:00", endTime: "00:00:00", gracePeriodMinutes: 15 }
    ],
    employees: [
        { id: "e1", firstName: "Nitin", lastName: "Agarwal", email: "nitin@mfg.com", phone: "9876543210", salaryModel: "FIXED", baseSalary: 45000.00, shiftId: "sh1", shiftName: "Morning Shift" },
        { id: "e2", firstName: "Mohit", lastName: "Gupta", email: "mohit@mfg.com", phone: "9876543220", salaryModel: "DAILY", baseSalary: 1200.00, shiftId: "sh1", shiftName: "Morning Shift" }
    ],
    attendance: [
        { id: "a1", employeeId: "e1", employeeName: "Nitin Agarwal", shiftId: "sh1", checkInTime: "2026-06-01T08:05:00", checkOutTime: "2026-06-01T16:00:00", durationHours: 7.92, isLate: false, notes: "Logged card punch" },
        { id: "a2", employeeId: "e2", employeeName: "Mohit Gupta", shiftId: "sh1", checkInTime: "2026-06-01T08:22:00", checkOutTime: "2026-06-01T16:05:00", durationHours: 7.71, isLate: true, notes: "Heavy rain delay" }
    ],
    advances: [
        { id: "ad1", employeeId: "e2", employeeName: "Mohit Gupta", amount: 5000.00, reason: "Family emergency expenses", repaymentMonth: "2026-06", status: "PENDING" }
    ],
    payroll: [
        { id: "p1", employeeId: "e1", employeeName: "Nitin Agarwal", payrollMonth: "2026-06", baseSalary: 45000.00, absenceDeductions: 0.00, loanRepayments: 0.00, netSalary: 45000.00, status: "DRAFT" }
    ],
    expenses: [
        { id: "ex1", description: "High-voltage line utility bill", amount: 8450.00, category: "UTILITIES", department: "Processing", project: "Phase 2 Core", status: "SUBMITTED" }
    ]
};

// REST Core Client Configuration
const client = {
    get isSyncMode() {
        return document.getElementById('sync-mode-toggle').checked;
    },
    getHeaders() {
        const token = localStorage.getItem('fabricore_token');
        const tenantId = localStorage.getItem('fabricore_tenant_id');
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers['Authorization'] = `Bearer ${token}`;
        if (tenantId) headers['X-Tenant-ID'] = tenantId;
        return headers;
    },
    async fetch(path, options = {}) {
        if (!this.isSyncMode) {
            return this.mockFetch(path, options);
        }
        options.headers = { ...this.getHeaders(), ...options.headers };
        try {
            const res = await fetch(path, options);
            if (res.status === 401 || res.status === 403) {
                showToast("SESSION EXPIRED", "Operator context lost. Redirecting...", "error");
                logout();
                throw new Error("Unauthorized");
            }
            if (!res.ok) {
                const data = await res.json().catch(() => ({}));
                const errMsg = (data.errors && data.errors.join(', ')) || data.message || `API Error: ${res.status}`;
                throw new Error(errMsg);
            }
            if (options.binary) return res.blob();
            return await res.json();
        } catch (e) {
            console.warn(`Sync Fetch failed, falling back to mock sandbox: ${e.message}`);
            showToast("SERVER REST FAULT", e.message || "Cannot communicate with Spring endpoint.", "error");
            throw e;
        }
    },
    mockFetch(path, options = {}) {
        return new Promise((resolve) => {
            setTimeout(() => {
                console.log(`Mock Route: ${options.method || 'GET'} ${path}`);
                
                // /api/auth/register-tenant
                if (path === '/api/auth/register-tenant') {
                    const body = JSON.parse(options.body);
                    const newTenantId = crypto.randomUUID();
                    resolve({ success: true, message: "Mock Provision successful", data: { token: "mock_jwt_token", username: body.username, role: "OWNER", tenantId: newTenantId } });
                }
                // /api/auth/login
                else if (path === '/api/auth/login') {
                    const body = JSON.parse(options.body);
                    resolve({ success: true, message: "Mock Sign-In successful", data: { token: "mock_jwt_token", username: body.username, role: "OWNER", tenantId: "85fe3021-ef20-410a-b333-8a30f14ba021" } });
                }
                // GET /api/suppliers
                else if (path.startsWith('/api/suppliers') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.suppliers } });
                }
                // POST /api/suppliers
                else if (path === '/api/suppliers' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const item = { id: 's' + (mockStore.suppliers.length + 1), ...body };
                    mockStore.suppliers.push(item);
                    resolve({ success: true, message: "Supplier saved", data: item });
                }
                // GET /api/materials
                else if (path.startsWith('/api/materials') && (!options.method || options.method === 'GET')) {
                    if (path.includes('/low-stock')) {
                        resolve({ success: true, data: { content: mockStore.materials.filter(m => m.quantity <= m.reorderLevel) } });
                    } else {
                        resolve({ success: true, data: { content: mockStore.materials } });
                    }
                }
                // POST /api/materials
                else if (path === '/api/materials' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const supp = mockStore.suppliers.find(s => s.id === body.supplierId);
                    const item = { 
                        id: 'm' + (mockStore.materials.length + 1), 
                        sku: body.sku,
                        name: body.name,
                        unit: body.unit,
                        quantity: 0.00,
                        reorderLevel: parseFloat(body.reorderLevel),
                        supplierId: body.supplierId,
                        supplierName: supp ? supp.name : 'NO SUPPLIER ASSIGNED',
                        lowStock: parseFloat(body.reorderLevel) >= 0
                    };
                    mockStore.materials.push(item);
                    resolve({ success: true, message: "SKU provisioning registered", data: item });
                }
                // POST /api/transactions
                else if (path === '/api/transactions' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const mat = mockStore.materials.find(m => m.id === body.materialId);
                    if (!mat) {
                        resolve({ success: false, message: "Material reference invalid" });
                        return;
                    }
                    const qtyVal = parseFloat(body.quantity);
                    if (body.type === 'STOCK_OUT' && mat.quantity < qtyVal) {
                        resolve({ success: false, message: "Out of Stock limit guard locks movement. Insufficient quantities!" });
                        return;
                    }
                    
                    mat.quantity = body.type === 'STOCK_IN' ? (mat.quantity + qtyVal) : (mat.quantity - qtyVal);
                    mat.lowStock = mat.quantity <= mat.reorderLevel;

                    const log = {
                        id: 't' + (mockStore.transactions.length + 1),
                        materialId: body.materialId,
                        quantity: qtyVal,
                        type: body.type,
                        notes: body.notes,
                        timestamp: new Date().toISOString(),
                        employeeName: "Admin Owner"
                    };
                    mockStore.transactions.push(log);
                    resolve({ success: true, message: "Ledger stock punch committed successfully", data: log });
                }
                // GET /api/transactions/material
                else if (path.startsWith('/api/transactions/material')) {
                    const matId = path.split('/').pop().split('?')[0];
                    resolve({ success: true, data: { content: mockStore.transactions.filter(t => t.materialId === matId) } });
                }
                // GET /api/employees
                else if (path.startsWith('/api/employees') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.employees } });
                }
                // POST /api/employees
                else if (path === '/api/employees' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const sh = mockStore.shifts.find(s => s.id === body.shiftId);
                    const item = {
                        id: 'e' + (mockStore.employees.length + 1),
                        firstName: body.firstName,
                        lastName: body.lastName,
                        email: body.email,
                        phone: body.phone,
                        salaryModel: body.salaryModel,
                        baseSalary: parseFloat(body.baseSalary),
                        shiftId: body.shiftId,
                        shiftName: sh ? sh.name : 'Morning Shift'
                    };
                    mockStore.employees.push(item);
                    resolve({ success: true, message: "Employee roster registered", data: item });
                }
                // GET /api/shifts
                else if (path.startsWith('/api/shifts') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.shifts } });
                }
                // POST /api/shifts
                else if (path === '/api/shifts' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const item = { id: 'sh' + (mockStore.shifts.length + 1), ...body };
                    mockStore.shifts.push(item);
                    resolve({ success: true, message: "Timetable rule added", data: item });
                }
                // GET /api/attendance
                else if (path.startsWith('/api/attendance') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.attendance } });
                }
                // POST /api/attendance/check-in
                else if (path === '/api/attendance/check-in' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const emp = mockStore.employees.find(e => e.id === body.employeeId);
                    const sh = mockStore.shifts.find(s => s.id === body.shiftId);
                    const item = {
                        id: 'a' + (mockStore.attendance.length + 1),
                        employeeId: body.employeeId,
                        employeeName: emp ? `${emp.firstName} ${emp.lastName}` : 'Staff Roster',
                        shiftId: body.shiftId,
                        checkInTime: body.checkInTime,
                        checkOutTime: null,
                        durationHours: null,
                        isLate: body.checkInTime.includes('T08:2') || body.checkInTime.includes('T08:3') || body.checkInTime.includes('T08:4'),
                        notes: "Live clock punch"
                    };
                    mockStore.attendance.push(item);
                    resolve({ success: true, message: "Check-in card punch registered successfully.", data: item });
                }
                // POST /api/attendance/check-out
                else if (path.startsWith('/api/attendance/check-out') && options.method === 'POST') {
                    const empId = path.split('/').pop();
                    const log = mockStore.attendance.find(a => a.employeeId === empId && !a.checkOutTime);
                    if (log) {
                        log.checkOutTime = new Date().toISOString().substring(0, 19);
                        log.durationHours = 8.00;
                        resolve({ success: true, message: "Check-out card punch committed.", data: log });
                    } else {
                        resolve({ success: false, message: "No active uncommitted check-in found for this employee roster." });
                    }
                }
                // PUT /api/attendance/correct
                else if (path.startsWith('/api/attendance/correct') && options.method === 'PUT') {
                    const attId = path.split('/').pop();
                    const log = mockStore.attendance.find(a => a.id === attId);
                    if (log) {
                        const body = JSON.parse(options.body);
                        log.checkInTime = body.correctedCheckInTime;
                        log.checkOutTime = body.correctedCheckOutTime;
                        log.notes = "OVERRIDE: " + body.notes;
                        resolve({ success: true, message: "Attendance timeline correction log authorized.", data: log });
                    } else {
                        resolve({ success: false, message: "Log reference invalid" });
                    }
                }
                // GET /api/advances
                else if (path.startsWith('/api/advances') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.advances } });
                }
                // POST /api/advances
                else if (path === '/api/advances' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const emp = mockStore.employees.find(e => e.id === body.employeeId);
                    const valAmt = parseFloat(body.amount);
                    if (emp && valAmt > emp.baseSalary * 0.5) {
                        resolve({ success: false, message: "Safety Limit Blocked: Advance loan exceeds the 50% limit bounds of base monthly salary!" });
                        return;
                    }
                    const item = {
                        id: 'ad' + (mockStore.advances.length + 1),
                        employeeId: body.employeeId,
                        employeeName: emp ? `${emp.firstName} ${emp.lastName}` : 'Staff Roster',
                        amount: valAmt,
                        reason: body.reason,
                        repaymentMonth: body.repaymentMonth,
                        status: "PENDING"
                    };
                    mockStore.advances.push(item);
                    resolve({ success: true, message: "Cash loan punch registered, awaiting approval", data: item });
                }
                // PUT /api/advances/approve
                else if (path.includes('/approve') && path.startsWith('/api/advances') && options.method === 'PUT') {
                    const parts = path.split('/');
                    const advId = parts[3];
                    const status = new URLSearchParams(parts[parts.length - 1] || '').get('status') || 'APPROVED';
                    const adv = mockStore.advances.find(a => a.id === advId);
                    if (adv) {
                        adv.status = status;
                        resolve({ success: true, message: `Advance loan successfully ${status.toLowerCase()} by Supervisor.` });
                    } else {
                        resolve({ success: false, message: "Loan reference invalid" });
                    }
                }
                // GET /api/payroll
                else if (path.startsWith('/api/payroll') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.payroll } });
                }
                // POST /api/payroll/calculate
                else if (path === '/api/payroll/calculate' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const emp = mockStore.employees.find(e => e.id === body.employeeId);
                    if (!emp) {
                        resolve({ success: false, message: "Employee reference invalid" });
                        return;
                    }
                    const activeLoans = mockStore.advances.filter(a => a.employeeId === body.employeeId && a.repaymentMonth === body.payrollMonth && a.status === 'APPROVED');
                    const totalRepay = activeLoans.reduce((sum, current) => sum + current.amount, 0);
                    const deductionsAbsence = emp.salaryModel === 'FIXED' ? 0.00 : 0.00;
                    const net = Math.max(0, emp.baseSalary - totalRepay - deductionsAbsence);
                    
                    const item = {
                        id: 'p' + (mockStore.payroll.length + 1),
                        employeeId: body.employeeId,
                        employeeName: `${emp.firstName} ${emp.lastName}`,
                        payrollMonth: body.payrollMonth,
                        baseSalary: emp.baseSalary,
                        absenceDeductions: deductionsAbsence,
                        loanRepayments: totalRepay,
                        netSalary: net,
                        status: "DRAFT"
                    };
                    mockStore.payroll.push(item);
                    resolve({ success: true, message: "Draft monthly payroll compiled", data: item });
                }
                // PUT /api/payroll/{id}/approve
                else if (path.includes('/approve') && path.startsWith('/api/payroll')) {
                    const payId = path.split('/')[3];
                    const pay = mockStore.payroll.find(p => p.id === payId);
                    if (pay) {
                        pay.status = "APPROVED";
                        resolve({ success: true, message: "Draft payroll calculation locked and approved." });
                    } else {
                        resolve({ success: false, message: "Payroll reference invalid" });
                    }
                }
                // PUT /api/payroll/{id}/pay
                else if (path.includes('/pay') && path.startsWith('/api/payroll')) {
                    const payId = path.split('/')[3];
                    const pay = mockStore.payroll.find(p => p.id === payId);
                    if (pay) {
                        pay.status = "PAID";
                        resolve({ success: true, message: "Disbursal releasing authorized. Paid successfully." });
                    } else {
                        resolve({ success: false, message: "Payroll reference invalid" });
                    }
                }
                // GET /api/expenses
                else if (path.startsWith('/api/expenses') && (!options.method || options.method === 'GET')) {
                    resolve({ success: true, data: { content: mockStore.expenses } });
                }
                // POST /api/expenses
                else if (path === '/api/expenses' && options.method === 'POST') {
                    const body = JSON.parse(options.body);
                    const item = {
                        id: 'ex' + (mockStore.expenses.length + 1),
                        description: body.description,
                        amount: parseFloat(body.amount),
                        category: body.category,
                        department: body.department,
                        project: body.project,
                        status: "SUBMITTED"
                    };
                    mockStore.expenses.push(item);
                    resolve({ success: true, message: "Expense logged", data: item });
                }
                // PUT /api/expenses/{id}/approve
                else if (path.includes('/approve') && path.startsWith('/api/expenses')) {
                    const expId = path.split('/')[3];
                    const exp = mockStore.expenses.find(e => e.id === expId);
                    if (exp) {
                        exp.status = "APPROVED";
                        resolve({ success: true, message: "Expense verified and approved." });
                    } else {
                        resolve({ success: false, message: "Expense reference invalid" });
                    }
                }
                // PUT /api/expenses/{id}/review
                else if (path.includes('/review') && path.startsWith('/api/expenses')) {
                    const expId = path.split('/')[3];
                    const exp = mockStore.expenses.find(e => e.id === expId);
                    if (exp) {
                        exp.status = "COMPLETED";
                        resolve({ success: true, message: "Expense finalized and archived to ledger." });
                    } else {
                        resolve({ success: false, message: "Expense reference invalid" });
                    }
                }
                // PUT /api/expenses/{id}/reject
                else if (path.includes('/reject') && path.startsWith('/api/expenses')) {
                    const expId = path.split('/')[3];
                    const exp = mockStore.expenses.find(e => e.id === expId);
                    if (exp) {
                        exp.status = "REJECTED";
                        resolve({ success: true, message: "Expense rejected." });
                    } else {
                        resolve({ success: false, message: "Expense reference invalid" });
                    }
                }
                // GET /api/dashboard
                else if (path.startsWith('/api/dashboard') && (!options.method || options.method === 'GET')) {
                    const activeCount = mockStore.employees.length;
                    const criticalStock = mockStore.materials.filter(m => m.quantity <= m.reorderLevel).length;
                    const totalPaid = mockStore.payroll.reduce((sum, p) => sum + p.netSalary, 0);
                    const totalExpenses = mockStore.expenses.filter(e => e.status !== 'REJECTED').reduce((sum, e) => sum + e.amount, 0);
                    resolve({
                        success: true,
                        data: {
                            activeWorkforce: activeCount,
                            monthlyPayrollCosts: totalPaid,
                            criticalLowStockItems: criticalStock,
                            approvedExpenseBurn: totalExpenses
                        }
                    });
                }
                else {
                    resolve({ success: true, message: "Mock fallback endpoint satisfied" });
                }
            }, 500);
        });
    }
};

// Application State & Navigation Router
const state = {
    activeView: 'auth',
    currentMonth: '2026-06',
    user: null,
    suppliers: [],
    materials: [],
    shifts: [],
    employees: [],
    attendance: [],
    advances: [],
    payrolls: [],
    expenses: []
};

// Global Routing Engine
function handleRouting() {
    const hash = window.location.hash || '#auth';
    const token = localStorage.getItem('fabricore_token');
    
    if (hash === '#auth' || !token) {
        state.activeView = 'auth';
        document.getElementById('workspace').classList.add('hidden');
        document.getElementById('view-auth').classList.remove('hidden');
        return;
    }

    state.activeView = hash.substring(1);
    document.getElementById('view-auth').classList.add('hidden');
    document.getElementById('workspace').classList.remove('hidden');

    const navLinks = ['dashboard', 'inventory', 'workforce', 'payroll', 'expenses'];
    navLinks.forEach(l => {
        const el = document.getElementById(`nav-${l}`);
        if (l === state.activeView) {
            el.className = "flex items-center gap-3 px-6 py-3 border-l-2 border-primary text-primary bg-surface-container-high transition-colors";
        } else {
            el.className = "flex items-center gap-3 px-6 py-3 border-l-2 border-transparent text-on-surface-variant hover:bg-surface-variant transition-colors";
        }
    });

    const headerTitles = {
        dashboard: 'OPERATIONS DASHBOARD',
        inventory: 'INVENTORY & SUPPLIERS LEDGER',
        workforce: 'WORKFORCE ROSTER & ATTENDANCE PUNCH',
        payroll: 'LOANS & PAYROLL ENGINE',
        expenses: 'OPERATIONAL EXPENSE CONTROL PIPELINE'
    };
    document.getElementById('workspace-view-title').innerText = headerTitles[state.activeView] || 'OPERATIONS CENTER';

    navLinks.forEach(l => {
        const block = document.getElementById(`view-${l}`);
        if (l === state.activeView) {
            block.classList.remove('hidden');
        } else {
            block.classList.add('hidden');
        }
    });

    if (state.activeView === 'dashboard') loadDashboardData();
    if (state.activeView === 'inventory') loadInventoryData();
    if (state.activeView === 'workforce') loadWorkforceData();
    if (state.activeView === 'payroll') loadPayrollData();
    if (state.activeView === 'expenses') loadExpensesData();
}

window.addEventListener('load', () => {
    const cachedToken = localStorage.getItem('fabricore_token');
    const cachedUser = localStorage.getItem('fabricore_user');
    const cachedRole = localStorage.getItem('fabricore_role');
    const cachedTenant = localStorage.getItem('fabricore_tenant');

    if (cachedToken) {
        state.user = { username: cachedUser, role: cachedRole, tenantId: cachedTenant };
        document.getElementById('current-user-name').innerText = cachedUser;
        document.getElementById('current-user-role').innerText = cachedRole + " ROLE";
        document.getElementById('tenant-display-name').innerText = "TENANT: " + (cachedTenant ? cachedTenant.substring(0, 8) : 'OMEGA');
    }

    const syncMode = localStorage.getItem('fabricore_sync_mode') !== 'false';
    document.getElementById('sync-mode-toggle').checked = syncMode;
    updateSyncStatusUI();

    const now = new Date();
    const dateStr = now.toISOString().substring(0, 10);
    document.getElementById('att-time-stamp').value = `${dateStr}T08:05:00`;
    document.getElementById('corr-in').value = `${dateStr}T08:00:00`;
    document.getElementById('corr-out').value = `${dateStr}T16:00:00`;

    window.addEventListener('hashchange', handleRouting);
    handleRouting();
});

function switchAuthTab(tab) {
    const formIn = document.getElementById('form-signin');
    const formReg = document.getElementById('form-register');
    const btnIn = document.getElementById('tab-btn-signin');
    const btnReg = document.getElementById('tab-btn-register');

    if (tab === 'signin') {
        formIn.classList.remove('hidden');
        formReg.classList.add('hidden');
        btnIn.className = "flex-1 py-4 font-mono text-xs uppercase transition-colors text-primary border-b-2 border-primary bg-surface-container";
        btnReg.className = "flex-1 py-4 font-mono text-xs uppercase transition-colors text-on-surface-variant hover:bg-surface-container-high";
    } else {
        formReg.classList.remove('hidden');
        formIn.classList.add('hidden');
        btnReg.className = "flex-1 py-4 font-mono text-xs uppercase transition-colors text-primary border-b-2 border-primary bg-surface-container";
        btnIn.className = "flex-1 py-4 font-mono text-xs uppercase transition-colors text-on-surface-variant hover:bg-surface-container-high";
    }
}

async function handleAuthSubmit(event, action) {
    event.preventDefault();
    const btn = event.submitter;
    const originalHTML = btn.innerHTML;
    btn.innerHTML = `<span class="material-symbols-outlined animate-spin text-sm">sync</span> SYNCING MATRIX...`;
    btn.disabled = true;

    try {
        let res;
        if (action === 'signin') {
            const username = document.getElementById('signin-username').value;
            const password = document.getElementById('signin-password').value;
            res = await client.fetch('/api/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
        } else {
            const companyName = document.getElementById('reg-company').value;
            const username = document.getElementById('reg-username').value;
            const email = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            res = await client.fetch('/api/auth/register-tenant', {
                method: 'POST',
                body: JSON.stringify({ username, email, password, companyName })
            });
        }

        if (res.success && res.data) {
            const d = res.data;
            localStorage.setItem('fabricore_token', d.token);
            localStorage.setItem('fabricore_user', d.username);
            localStorage.setItem('fabricore_role', d.role);
            localStorage.setItem('fabricore_tenant_id', d.tenantId);
            localStorage.setItem('fabricore_tenant', d.tenantId);

            state.user = { username: d.username, role: d.role, tenantId: d.tenantId };
            
            document.getElementById('current-user-name').innerText = d.username;
            document.getElementById('current-user-role').innerText = d.role + " ROLE";
            document.getElementById('tenant-display-name').innerText = "TENANT: " + d.tenantId.substring(0, 8);

            showToast("PROVISION PROTOCOL OK", "Access authorization validated. Opening Operations node.", "success");
            
            setTimeout(() => {
                window.location.hash = '#dashboard';
            }, 1000);
        } else {
            showToast("ACCESS DENIED", res.message || "Failed authentication.", "error");
        }
    } catch (err) {
        showToast("NODE CONNECTION FAULT", err.message || "Failed to authenticate with backend.", "error");
    } finally {
        btn.innerHTML = originalHTML;
        btn.disabled = false;
    }
}

function toggleSyncMode() {
    const syncMode = document.getElementById('sync-mode-toggle').checked;
    localStorage.setItem('fabricore_sync_mode', syncMode);
    updateSyncStatusUI();
    showToast("SYNC MODE RECONFIGURED", syncMode ? "Server Real-time Sync Active" : "Local Sandbox Offline Mode Active", "info");
    handleRouting();
}

function updateSyncStatusUI() {
    const syncMode = client.isSyncMode;
    const ind = document.getElementById('sync-status-indicator');
    if (syncMode) {
        ind.className = "w-2.5 h-2.5 rounded-full bg-primary animate-pulse";
        ind.title = "Server Synchronized Database active";
    } else {
        ind.className = "w-2.5 h-2.5 rounded-full bg-emerald-500";
        ind.title = "Sandbox Local storage active (mock)";
    }
}

function toggleMockModeHint() {
    document.getElementById('sync-mode-toggle').checked = false;
    localStorage.setItem('fabricore_sync_mode', false);
    updateSyncStatusUI();
    document.getElementById('signin-username').value = "chief1";
    document.getElementById('signin-password').value = "mypassword";
    showToast("SANDBOX INITIALIZED", "Loaded demo credentials. Click INITIALIZE PROTOCOLS to browse.", "info");
}

function logout() {
    localStorage.removeItem('fabricore_token');
    localStorage.removeItem('fabricore_user');
    localStorage.removeItem('fabricore_role');
    localStorage.removeItem('fabricore_tenant_id');
    localStorage.removeItem('fabricore_tenant');
    state.user = null;
    window.location.hash = '#auth';
}

function handleMonthChange() {
    state.currentMonth = document.getElementById('global-month-select').value;
    showToast("DATE CYCLE CHANGED", `Operational scopes adjusted to: ${state.currentMonth}`, "info");
    handleRouting();
}

async function loadDashboardData() {
    try {
        const res = await client.fetch(`/api/dashboard?month=${state.currentMonth}`);
        if (res.success && res.data) {
            const d = res.data;
            document.getElementById('stat-headcount').innerText = d.activeWorkforce || 0;
            document.getElementById('stat-payroll').innerText = `₹${((d.monthlyPayrollCosts || 0) / 1000).toFixed(1)}K`;
            document.getElementById('stat-alarms').innerText = String(d.criticalLowStockItems).padStart(2, '0');
            document.getElementById('stat-opex').innerText = `₹${((d.approvedExpenseBurn || 0) / 1000).toFixed(1)}K`;

            const countLow = d.criticalLowStockItems || 0;
            const alarmBadge = document.getElementById('stat-alarms-badge');
            if (countLow > 0) {
                alarmBadge.innerText = "ACTION REQUIRED";
                alarmBadge.className = "text-xs text-error font-mono font-bold tracking-widest animate-pulse";
            } else {
                alarmBadge.innerText = "OK";
                alarmBadge.className = "text-xs text-emerald-500 font-mono font-bold tracking-widest";
            }
        }
    } catch (e) {}

    try {
        const res = await client.fetch('/api/materials/low-stock');
        if (res.success && res.data) {
            const list = res.data.content || [];
            document.getElementById('dashboard-critical-count').innerText = `${list.length} LOW`;
            const container = document.getElementById('dashboard-critical-list');
            if (list.length === 0) {
                container.innerHTML = `<p class="text-xs text-on-surface-variant text-center font-mono py-8">NO LOW STOCK DETECTED</p>`;
            } else {
                container.innerHTML = list.map(m => `
                    <div class="border border-outline-variant p-3 space-y-2 relative overflow-hidden bg-surface">
                        <div class="absolute right-0 top-0 w-1 h-full bg-error"></div>
                        <div class="flex justify-between items-start">
                            <p class="font-mono text-xs font-bold">${m.name}</p>
                            <span class="text-error font-mono text-[10px]">${m.quantity} ${m.unit}</span>
                        </div>
                        <div class="flex justify-between items-end">
                            <span class="font-mono text-[9px] text-on-surface-variant">Min. Alert: ${m.reorderLevel}</span>
                            <button class="bg-on-surface text-background font-mono font-black text-[9px] px-2 py-0.5 hover:bg-primary hover:text-background transition-colors" onclick="window.location.hash='#inventory'">ORDER NOW</button>
                        </div>
                    </div>
                `).join('');
            }
        }
    } catch (e) {}
}

function toggleChecklistStrike(checkbox) {
    const textContainer = checkbox.nextElementSibling.querySelector('p:first-child');
    if (checkbox.checked) {
        textContainer.classList.add('line-through', 'opacity-40');
    } else {
        textContainer.classList.remove('line-through', 'opacity-40');
    }
    showToast("CHECKLIST MODIFIED", "Operational status log checked.", "info");
}

function showToast(title, message, type = "info") {
    const toast = document.getElementById('toast');
    const icon = document.getElementById('toast-icon');
    const tTitle = document.getElementById('toast-title');
    const tMsg = document.getElementById('toast-message');

    tTitle.innerText = title;
    tMsg.innerText = message;

    if (type === "success") {
        icon.innerText = "check_circle";
        icon.className = "material-symbols-outlined text-emerald-500";
    } else if (type === "error") {
        icon.innerText = "error";
        icon.className = "material-symbols-outlined text-error animate-pulse";
    } else {
        icon.innerText = "info";
        icon.className = "material-symbols-outlined text-primary";
    }

    toast.classList.remove('translate-x-full');
    
    if (window.toastTimeout) clearTimeout(window.toastTimeout);
    window.toastTimeout = setTimeout(hideToast, 4000);
}

function hideToast() {
    const toast = document.getElementById('toast');
    toast.classList.add('translate-x-full');
}
