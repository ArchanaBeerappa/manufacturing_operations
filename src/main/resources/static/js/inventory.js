// Fabricore Inventory & Supplier Catalog Module
let showLowStockOnly = false;

async function loadInventoryData() {
    // Load Suppliers dropdown & lists
    try {
        const res = await client.fetch('/api/suppliers');
        if (res.success && res.data) {
            state.suppliers = res.data.content || [];
            
            // Render Suppliers
            const tbody = document.getElementById('suppliers-table-body');
            if (state.suppliers.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="p-4 text-center text-on-surface-variant">NO REGISTERED SUPPLIERS</td></tr>`;
            } else {
                tbody.innerHTML = state.suppliers.map(s => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3 font-bold">${s.name}</td>
                        <td class="p-3">${s.contactName || s.contactPerson || ''}</td>
                        <td class="p-3 font-mono">${s.phone}</td>
                        <td class="p-3">${s.email}</td>
                        <td class="p-3 text-right">
                            <button class="text-on-surface-variant hover:text-error" onclick="deleteSupplier('${s.id}')">
                                <span class="material-symbols-outlined text-sm">delete</span>
                            </button>
                        </td>
                    </tr>
                `).join('');
            }

            // Update supplier dropdowns in Material forms
            const select = document.getElementById('mat-supplier');
            select.innerHTML = `<option value="">NO SUPPLIER ASSIGNED</option>` + 
                state.suppliers.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }
    } catch (e) {}

    // Load Materials Table
    await loadMaterialsList();
}

async function loadMaterialsList() {
    try {
        const url = showLowStockOnly ? '/api/materials/low-stock' : '/api/materials';
        const res = await client.fetch(url);
        if (res.success && res.data) {
            state.materials = res.data.content || [];
            renderMaterialsTable();

            // Update Material dropdown in punches
            const select = document.getElementById('punch-mat-id');
            select.innerHTML = `<option value="">SELECT RAW MATERIAL</option>` + 
                state.materials.map(m => `<option value="${m.id}">${m.sku} - ${m.name}</option>`).join('');
            
            // Update timeline filter dropdown
            const tSelect = document.getElementById('timeline-filter-sku');
            tSelect.innerHTML = `<option value="">ALL MOVEMENTS</option>` + 
                state.materials.map(m => `<option value="${m.id}">${m.sku}</option>`).join('');
        }
    } catch (e) {}
}

function renderMaterialsTable() {
    const query = document.getElementById('inventory-search').value.toLowerCase();
    const tbody = document.getElementById('materials-table-body');
    
    let filtered = state.materials;
    if (query) {
        filtered = filtered.filter(m => m.sku.toLowerCase().includes(query) || m.name.toLowerCase().includes(query));
    }

    if (filtered.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-on-surface-variant">NO RAW MATERIAL SKU REGISTERED</td></tr>`;
    } else {
        tbody.innerHTML = filtered.map(m => `
            <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors ${m.lowStock ? 'bg-error-container/5' : ''}">
                <td class="p-3 font-bold ${m.lowStock ? 'text-error' : 'text-primary'}">${m.sku}</td>
                <td class="p-3">
                    <p class="font-bold">${m.name}</p>
                    <p class="text-[10px] text-on-surface-variant uppercase">${m.unit} MEASURE</p>
                </td>
                <td class="p-3 font-mono font-bold">${m.quantity}</td>
                <td class="p-3 font-mono text-on-surface-variant">${m.reorderLevel}</td>
                <td class="p-3">${m.supplierName || 'NONE'}</td>
                <td class="p-3 text-right space-x-2">
                    <button class="text-on-surface-variant hover:text-primary" onclick="inspectMaterialLedger('${m.id}')" title="Inspect Ledger">
                        <span class="material-symbols-outlined text-sm">history</span>
                    </button>
                    <button class="text-on-surface-variant hover:text-error" onclick="deleteMaterial('${m.id}')" title="Soft Delete SKU">
                        <span class="material-symbols-outlined text-sm">delete</span>
                    </button>
                </td>
            </tr>
        `).join('');
    }
}

function toggleLowStockFilter() {
    showLowStockOnly = !showLowStockOnly;
    const btn = document.getElementById('btn-lowstock-filter');
    if (showLowStockOnly) {
        btn.className = "bg-primary border border-primary text-[10px] font-mono px-3 py-1.5 text-background font-bold";
    } else {
        btn.className = "bg-surface border border-outline-variant text-[10px] font-mono px-3 py-1.5 text-on-surface-variant hover:text-primary";
    }
    loadMaterialsList();
}

function inspectMaterialLedger(id) {
    document.getElementById('timeline-filter-sku').value = id;
    loadTransactionLogs();
    showToast("LEDGER LOADED", "Material stock movement logs retrieved.", "info");
}

async function loadTransactionLogs() {
    const matId = document.getElementById('timeline-filter-sku').value;
    const container = document.getElementById('transaction-logs-list');
    
    try {
        let res;
        if (matId) {
            res = await client.fetch(`/api/transactions/material/${matId}`);
        } else {
            if (!client.isSyncMode) {
                res = { success: true, data: { content: mockStore.transactions } };
            } else {
                container.innerHTML = `<p class="text-xs text-on-surface-variant text-center font-mono py-8">Select a SKU to view ledger logs</p>`;
                return;
            }
        }

        if (res.success && res.data) {
            const list = res.data.content || [];
            if (list.length === 0) {
                container.innerHTML = `<p class="text-xs text-on-surface-variant text-center font-mono py-8">NO LEDGER HISTORY PUNCHED</p>`;
            } else {
                list.sort((a,b) => new Date(b.timestamp) - new Date(a.timestamp));
                container.innerHTML = list.map(t => {
                    const type = t.transactionType || t.type;
                    const notes = t.remarks || t.notes || '';
                    const isIn = type === 'STOCK_IN';
                    const mat = state.materials.find(m => m.id === t.materialId);
                    const skuName = mat ? mat.sku : 'SKU';
                    return `
                        <div class="flex items-start gap-3 border-b border-outline-variant/40 pb-2.5">
                            <span class="font-mono text-[9px] px-1.5 py-0.5 rounded font-bold ${isIn ? 'bg-emerald-500/10 text-emerald-500' : 'bg-error-container/20 text-error'}">${type}</span>
                            <div class="flex-1 font-mono text-[11px]">
                                <p class="font-bold text-on-surface">${skuName}: ${isIn ? '+' : '-'}${t.quantity}</p>
                                <p class="text-[9px] text-on-surface-variant mt-0.5">${notes}</p>
                            </div>
                            <div class="text-right font-mono text-[9px] text-on-surface-variant">
                                <p>${new Date(t.timestamp).toLocaleTimeString()}</p>
                                <p>${t.employeeName || 'Operator'}</p>
                            </div>
                        </div>
                    `;
                }).join('');
            }
        }
    } catch(e) {}
}

async function submitSupplier(e) {
    e.preventDefault();
    const name = document.getElementById('supp-name').value;
    const contactPerson = document.getElementById('supp-contact').value;
    const phone = document.getElementById('supp-phone').value;
    const email = document.getElementById('supp-email').value;

    try {
        const res = await client.fetch('/api/suppliers', {
            method: 'POST',
            body: JSON.stringify({ 
                name, 
                contactName: contactPerson, // For backend DTO
                contactPerson,              // For mock sandbox
                phone, 
                email 
            })
        });
        if (res.success) {
            showToast("SUPPLIER SAVED", `Supplier ${name} created inside Tenant partition.`, "success");
            document.getElementById('form-supplier').reset();
            loadInventoryData();
        }
    } catch(err) {}
}

async function submitMaterial(e) {
    e.preventDefault();
    const sku = document.getElementById('mat-sku').value;
    const name = document.getElementById('mat-name').value;
    const unit = document.getElementById('mat-unit').value;
    const reorderLevel = document.getElementById('mat-reorder').value;
    const supplierId = document.getElementById('mat-supplier').value || null;

    try {
        const res = await client.fetch('/api/materials', {
            method: 'POST',
            body: JSON.stringify({ sku, name, unit, reorderLevel, supplierId })
        });
        if (res.success) {
            showToast("SKU CREATED", `Material Ledger code ${sku} provisioned successfully.`, "success");
            document.getElementById('form-material').reset();
            loadInventoryData();
        }
    } catch(err) {}
}

async function submitStockPunch(e) {
    e.preventDefault();
    const materialId = document.getElementById('punch-mat-id').value;
    const quantity = document.getElementById('punch-qty').value;
    const type = document.getElementById('punch-type').value;
    const notes = document.getElementById('punch-notes').value;

    try {
        const res = await client.fetch('/api/transactions', {
            method: 'POST',
            body: JSON.stringify({ 
                materialId, 
                quantity, 
                transactionType: type, 
                type, 
                remarks: notes, 
                notes 
            })
        });
        if (res.success) {
            showToast("LEDGER MOVEMENT OK", `Punch committed successfully. Ledger adjusted.`, "success");
            document.getElementById('form-stock-punch').reset();
            
            const now = new Date();
            document.getElementById('att-time-stamp').value = now.toISOString().substring(0, 19);

            loadInventoryData();
            loadTransactionLogs();
        } else {
            showToast("LEDGER FAULT", res.message || "Failed to commit stock transaction.", "error");
        }
    } catch (err) {}
}

async function deleteSupplier(id) {
    if (!confirm("Are you sure you want to hard purge this Supplier record?")) return;
    try {
        const res = await client.fetch(`/api/suppliers/${id}`, { method: 'DELETE' });
        if (res.success) {
            showToast("PURGED SUCCESSFUL", "Supplier successfully removed.", "success");
            loadInventoryData();
        }
    } catch (e) {}
}

async function deleteMaterial(id) {
    if (!confirm("Soft Purging this material SKU? Transaction logs will be preserved.")) return;
    try {
        const res = await client.fetch(`/api/materials/${id}`, { method: 'DELETE' });
        if (res.success) {
            showToast("SKU DEACTIVATED", "Raw material soft-deleted.", "success");
            loadInventoryData();
        }
    } catch(e) {}
}
