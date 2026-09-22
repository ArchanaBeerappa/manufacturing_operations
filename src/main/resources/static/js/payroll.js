// Fabricore Advances & Payroll Engine Module
async function loadPayrollData() {
    try {
        const res = await client.fetch('/api/advances');
        if (res.success && res.data) {
            state.advances = res.data.content || [];
            
            const tbody = document.getElementById('advances-table-body');
            if (state.advances.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-on-surface-variant">NO ACTIVE ADVANCE SHEETS</td></tr>`;
            } else {
                tbody.innerHTML = state.advances.map(a => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3 font-bold">${a.employeeName || 'Staff Borrower'}</td>
                        <td class="p-3 font-mono font-bold text-primary">₹${a.amount}</td>
                        <td class="p-3 font-mono">${a.deductionMonth || a.repaymentMonth || ''}</td>
                        <td class="p-3 text-on-surface-variant text-[11px] font-mono">${a.reason}</td>
                        <td class="p-3 font-mono">
                            <span class="px-1.5 py-0.5 rounded font-bold text-[10px] uppercase ${a.status === 'APPROVED' ? 'bg-emerald-500/10 text-emerald-500' : a.status === 'REJECTED' ? 'bg-error-container/20 text-error' : 'bg-surface-container-high text-on-surface-variant animate-pulse'}">${a.status}</span>
                        </td>
                        <td class="p-3 text-right">
                            ${a.status === 'PENDING' ? `
                                <button class="bg-emerald-500 text-background px-2.5 py-1 text-[10px] font-mono font-bold hover:brightness-110 mr-1" onclick="approveAdvance('${a.id}', 'APPROVED')">OK</button>
                                <button class="bg-error text-background px-2.5 py-1 text-[10px] font-mono font-bold hover:brightness-110" onclick="approveAdvance('${a.id}', 'REJECTED')">REJ</button>
                            ` : '<span class="text-[10px] font-mono text-on-surface-variant">LOCKED</span>'}
                        </td>
                    </tr>
                `).join('');
            }
        }
    } catch(e) {}

    // Load Payroll List
    await loadPayrollsList();
}

async function loadPayrollsList() {
    try {
        const res = await client.fetch('/api/payroll');
        if (res.success && res.data) {
            state.payrolls = res.data.content || [];
            
            const tbody = document.getElementById('payrolls-table-body');
            if (state.payrolls.length === 0) {
                tbody.innerHTML = `<tr><td colspan="8" class="p-4 text-center text-on-surface-variant">NO PAYROLL DRAFTS LOADED</td></tr>`;
            } else {
                tbody.innerHTML = state.payrolls.map(p => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3 font-bold">${p.employeeName}</td>
                        <td class="p-3 font-mono">${p.payrollMonth}</td>
                        <td class="p-3 font-mono font-bold">₹${p.baseSalary}</td>
                        <td class="p-3 font-mono text-error">₹${p.otherDeductions !== undefined ? p.otherDeductions : (p.absenceDeductions || 0)}</td>
                        <td class="p-3 font-mono text-error">₹${p.advanceDeductions !== undefined ? p.advanceDeductions : (p.loanRepayments || 0)}</td>
                        <td class="p-3 font-mono font-bold text-emerald-500">₹${p.netSalary}</td>
                        <td class="p-3 font-mono">
                            <span class="px-1.5 py-0.5 rounded font-bold text-[10px] uppercase ${p.status === 'PAID' ? 'bg-emerald-500/10 text-emerald-500' : p.status === 'APPROVED' ? 'bg-primary/10 text-primary' : 'bg-surface-container-high text-on-surface-variant animate-pulse'}">${p.status}</span>
                        </td>
                        <td class="p-3 text-right space-x-2">
                            ${p.status === 'DRAFT' ? `
                                <button class="border border-primary text-primary font-mono text-[10px] px-2 py-0.5 hover:bg-primary hover:text-background" onclick="approvePayroll('${p.id}')">LOCK DRAFT</button>
                            ` : ''}
                            ${p.status === 'APPROVED' ? `
                                <button class="bg-primary text-background font-mono text-[10px] px-2.5 py-0.5 font-bold hover:brightness-110" onclick="disbursePayroll('${p.id}')">DISBURSE CASH</button>
                            ` : ''}
                            <button class="text-on-surface-variant hover:text-primary align-middle" onclick="downloadPayslipPDF('${p.id}')" title="Download payslip PDF">
                                <span class="material-symbols-outlined text-sm">download_for_offline</span>
                            </button>
                        </td>
                    </tr>
                `).join('');
            }
        }
    } catch(e) {}
}

async function submitAdvanceLoan(e) {
    e.preventDefault();
    const employeeId = document.getElementById('adv-emp-id').value;
    const amount = document.getElementById('adv-amount').value;
    const repaymentMonth = document.getElementById('adv-month').value;
    const reason = document.getElementById('adv-reason').value;

    try {
        const res = await client.fetch('/api/advances', {
            method: 'POST',
            body: JSON.stringify({ 
                employeeId, 
                amount, 
                deductionMonth: repaymentMonth, // for backend
                repaymentMonth,                 // for mock sandbox
                reason 
            })
        });
        if (res.success) {
            showToast("LOAN PROVISION OK", "Advance salary cash request logged.", "success");
            document.getElementById('form-advance').reset();
            loadPayrollData();
        } else {
            showToast("SAFETY GUARD BLOCKED", res.message || "Failed to submit loan request.", "error");
        }
    } catch(err) {}
}

async function approveAdvance(id, status) {
    try {
        const res = await client.fetch(`/api/advances/${id}/approve?status=${status}`, {
            method: 'PUT'
        });
        if (res.success) {
            showToast("LOAN STATUS LOCKED", `Loan request successfully ${status.toLowerCase()}.`, "success");
            loadPayrollData();
        }
    } catch(e) {}
}

async function calculatePayroll(e) {
    e.preventDefault();
    const employeeId = document.getElementById('pay-emp-id').value;
    const payrollMonth = document.getElementById('pay-month').value;

    try {
        const res = await client.fetch('/api/payroll/calculate', {
            method: 'POST',
            body: JSON.stringify({ employeeId, payrollMonth })
        });
        if (res.success) {
            showToast("PAYROLL DRAFT READY", "Monthly salary compilation logged as DRAFT.", "success");
            loadPayrollData();
        } else {
            showToast("CALCULATION ERROR", res.message || "Failed to process payroll.", "error");
        }
    } catch(err) {}
}

async function approvePayroll(id) {
    try {
        const res = await client.fetch(`/api/payroll/${id}/approve`, { method: 'PUT' });
        if (res.success) {
            showToast("PAYROLL LOCKED APPROVED", "Draft locks completed. Disbursal protocols unlocked.", "success");
            loadPayrollData();
        }
    } catch(e) {}
}

async function disbursePayroll(id) {
    try {
        const res = await client.fetch(`/api/payroll/${id}/pay`, { method: 'PUT' });
        if (res.success) {
            showToast("DISBURSAL RELEASING OK", "Cash release transactions executed inside general ledgers.", "success");
            loadPayrollData();
        }
    } catch(e) {}
}

async function downloadPayslipPDF(id) {
    if (!client.isSyncMode) {
        showToast("DOWNLOAD COMPLETED", "Simulated payslip compiled & downloaded successfully.", "success");
        return;
    }

    try {
        showToast("COMPILING PAYSLIP", "Calling OpenPDF payslip builder engine...", "info");
        const blob = await client.fetch(`/api/payroll/${id}/payslip`, { binary: true });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `payslip_${id}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showToast("PDF STREAM OK", "Payslip downloaded successfully.", "success");
    } catch (err) {
        showToast("PDF COMPILE FAULT", "Payslip rendering engine failed or timed out.", "error");
    }
}
