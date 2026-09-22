// Fabricore Operational Expense Control Module
async function loadExpensesData() {
    try {
        const res = await client.fetch('/api/expenses');
        if (res.success && res.data) {
            state.expenses = res.data.content || [];
            
            // Render Table
            const tbody = document.getElementById('expenses-table-body');
            if (state.expenses.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-on-surface-variant">NO LOGGED PIPELINE OUTLAYS</td></tr>`;
            } else {
                tbody.innerHTML = state.expenses.map(e => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3 font-bold">${e.description}</td>
                        <td class="p-3 font-mono font-bold text-primary">₹${e.amount}</td>
                        <td class="p-3 font-mono text-[10px] text-on-surface-variant uppercase">${e.category}</td>
                        <td class="p-3">
                            <p class="font-bold">${e.department}</p>
                            <p class="text-[9px] text-on-surface-variant uppercase font-mono">${e.project}</p>
                        </td>
                        <td class="p-3 font-mono">
                            <span class="px-1.5 py-0.5 rounded font-bold text-[10px] uppercase ${e.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-500' : e.status === 'APPROVED' ? 'bg-primary/10 text-primary' : e.status === 'REJECTED' ? 'bg-error-container/20 text-error' : 'bg-surface-container-high text-on-surface-variant animate-pulse'}">${e.status}</span>
                        </td>
                        <td class="p-3 text-right">
                            ${e.status === 'SUBMITTED' ? `
                                <button class="bg-primary text-background px-2 py-0.5 text-[10px] font-mono hover:brightness-110 mr-1" onclick="auditExpense('${e.id}', 'approve')">APPROVE</button>
                                <button class="border border-error text-error px-2 py-0.5 text-[10px] font-mono hover:bg-error hover:text-background" onclick="auditExpense('${e.id}', 'reject')">REJ</button>
                            ` : ''}
                            ${e.status === 'APPROVED' ? `
                                <button class="bg-emerald-500 text-background px-2.5 py-0.5 text-[10px] font-mono font-bold hover:brightness-110 mr-1" onclick="auditExpense('${e.id}', 'review')">COMPLETE</button>
                                <button class="border border-error text-error px-2 py-0.5 text-[10px] font-mono hover:bg-error hover:text-background" onclick="auditExpense('${e.id}', 'reject')">REJ</button>
                            ` : ''}
                            ${e.status === 'COMPLETED' || e.status === 'REJECTED' ? '<span class="text-[10px] font-mono text-on-surface-variant">ARCHIVED</span>' : ''}
                        </td>
                    </tr>
                `).join('');
            }

            // Render allocation summary charts
            renderExpenseAllocationSummary();
        }
    } catch(e) {}
}

function renderExpenseAllocationSummary() {
    const summary = {};
    let total = 0;
    state.expenses.forEach(e => {
        if (e.status !== 'REJECTED') {
            summary[e.category] = (summary[e.category] || 0) + e.amount;
            total += e.amount;
        }
    });

    const container = document.getElementById('expenses-allocation-list');
    if (total === 0) {
        container.innerHTML = `<p class="text-xs text-on-surface-variant text-center font-mono py-8">NO BURN DATA COMPREHENDED</p>`;
    } else {
        container.innerHTML = Object.keys(summary).map(cat => {
            const amt = summary[cat];
            const pct = ((amt / total) * 100).toFixed(0);
            return `
                <div class="space-y-1">
                    <div class="flex justify-between uppercase">
                        <span class="font-bold text-[10px]">${cat}</span>
                        <span>₹${amt} (${pct}%)</span>
                    </div>
                    <div class="w-full bg-surface-container-high h-1.5">
                        <div class="bg-primary h-full" style="width: ${pct}%"></div>
                    </div>
                </div>
            `;
        }).join('');
    }
}

async function submitExpense(e) {
    e.preventDefault();
    const description = document.getElementById('exp-desc').value;
    const amount = document.getElementById('exp-amount').value;
    const category = document.getElementById('exp-cat').value;
    const department = document.getElementById('exp-dept').value;
    const project = document.getElementById('exp-proj').value;

    try {
        const res = await client.fetch('/api/expenses', {
            method: 'POST',
            body: JSON.stringify({ description, amount, category, department, project })
        });
        if (res.success) {
            showToast("EXPENSE SUBMITTED", "Expense logged inside operational pipelines.", "success");
            document.getElementById('form-expense').reset();
            loadExpensesData();
        }
    } catch(err) {}
}

async function auditExpense(id, action) {
    try {
        const res = await client.fetch(`/api/expenses/${id}/${action}`, { method: 'PUT' });
        if (res.success) {
            showToast("AUDIT TRANSITION OK", `Expense marked as ${action}d successfully.`, "success");
            loadExpensesData();
        }
    } catch(e) {}
}
