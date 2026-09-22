// Fabricore Client-side CSV Compiler & Exporter Module
function exportDataCSV() {
    if (!client.isSyncMode) {
        // Compile mock reports client-side using CSV Blob format
        let csvContent = "data:text/csv;charset=utf-8,";
        
        // 1. Overview Indicators
        csvContent += "=== OPERATIONS SUMMARY INDICATORS ===\r\n";
        csvContent += `Active Workforce Headcount, ${mockStore.employees.length} Staff\r\n`;
        const totalPaid = mockStore.payroll.reduce((sum, p) => sum + p.netSalary, 0);
        csvContent += `Monthly Payroll Burn (INR), ₹${totalPaid}\r\n`;
        const totalEx = mockStore.expenses.filter(e => e.status !== 'REJECTED').reduce((sum, e) => sum + e.amount, 0);
        csvContent += `Consolidated OpEx Burn, ₹${totalEx}\r\n`;
        const lowStockCount = mockStore.materials.filter(m => m.quantity <= m.reorderLevel).length;
        csvContent += `Active Critical Stock Alarms, ${lowStockCount}\r\n\r\n`;

        // 2. Raw Material Inventory Table
        csvContent += "=== ACTIVE RAW MATERIAL INVENTORY LEDGER ===\r\n";
        csvContent += "SKU,Material Name,Unit,Current Quantity,Reorder Alert Level,Supplier Name,Low Stock Warning\r\n";
        mockStore.materials.forEach(m => {
            csvContent += `"${m.sku}","${m.name}","${m.unit}",${m.quantity},${m.reorderLevel},"${m.supplierName || 'NONE'}",${m.lowStock ? 'LOW' : 'OK'}\r\n`;
        });
        csvContent += "\r\n";

        // 3. Operational Expenses Outlays
        csvContent += "=== CONSOLIDATED OPERATIONAL EXPENSE RECORD ===\r\n";
        csvContent += "Reference Description,Amount,Category,Department,Project,Approval State\r\n";
        mockStore.expenses.forEach(e => {
            csvContent += `"${e.description}",${e.amount},"${e.category}","${e.department}","${e.project}","${e.status}"\r\n`;
        });

        // Trigger local dynamic browser download
        const encodedUri = encodeURI(csvContent);
        const a = document.createElement("a");
        a.setAttribute("href", encodedUri);
        a.setAttribute("download", `mfg_report_${state.currentMonth}_sandbox.csv`);
        document.body.appendChild(a);
        a.click();
        a.remove();
        showToast("CSV DOWNLOAD OK", "Sandbox operational report (CSV) generated & downloaded.", "success");
        return;
    }

    // Sync Mode: Call the real Spring Boot Dashboard Export REST API
    triggerBackendCSVDownload();
}

async function triggerBackendCSVDownload() {
    try {
        showToast("GENERATING CSV", "Requesting dashboard CSV byte stream...", "info");
        const res = await fetch(`/api/dashboard/export?month=${state.currentMonth}`, {
            headers: client.getHeaders()
        });
        if (!res.ok) throw new Error("CSV Export endpoint failed");
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `mfg_report_${state.currentMonth}.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        showToast("CSV DOWNLOAD OK", "Manufacturing CSV exported successfully.", "success");
    } catch(err) {
        showToast("EXPORT ERROR", err.message || "Failed to download CSV from server.", "error");
    }
}
