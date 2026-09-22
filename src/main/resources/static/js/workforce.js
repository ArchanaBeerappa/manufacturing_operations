// Fabricore Workforce Roster & Attendance Clocking Module
async function loadWorkforceData() {
    const joiningInput = document.getElementById('emp-joiningdate');
    if (joiningInput && !joiningInput.value) {
        joiningInput.value = new Date().toISOString().substring(0, 10);
    }

    try {
        const res = await client.fetch('/api/shifts');
        if (res.success && res.data) {
            state.shifts = res.data.content || [];
            
            // Render Shifts Table
            const tbody = document.getElementById('shifts-table-body');
            if (state.shifts.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" class="p-4 text-center text-on-surface-variant">NO DEPLOYED TIMETABLES</td></tr>`;
            } else {
                tbody.innerHTML = state.shifts.map(s => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3 font-bold">${s.name}</td>
                        <td class="p-3 font-mono">${s.startTime}</td>
                        <td class="p-3 font-mono">${s.endTime}</td>
                        <td class="p-3 font-mono">${s.gracePeriodMinutes} MINS</td>
                        <td class="p-3 text-right">
                            <button class="text-on-surface-variant hover:text-error" onclick="deleteShift('${s.id}')">
                                <span class="material-symbols-outlined text-sm">delete</span>
                            </button>
                        </td>
                    </tr>
                `).join('');
            }

            // Update employee shift option dropdown
            const select = document.getElementById('emp-shift');
            select.innerHTML = `<option value="">SELECT ACTIVE SHIFT</option>` + 
                state.shifts.map(s => `<option value="${s.id}">${s.name}</option>`).join('');

            // Update attendance form shift option
            const aSelect = document.getElementById('att-shift-id');
            aSelect.innerHTML = `<option value="">SELECT SHIFT</option>` + 
                state.shifts.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        }
    } catch(e) {}

    // Load Employees
    await loadEmployeesList();

    // Load Attendance logs
    await loadAttendanceLogsList();
}

async function loadEmployeesList() {
    try {
        const res = await client.fetch('/api/employees');
        if (res.success && res.data) {
            state.employees = res.data.content || [];
            
            // Render Employees Table
            const tbody = document.getElementById('employees-table-body');
            if (state.employees.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-on-surface-variant">NO ACTIVE ROSTER PROFILES</td></tr>`;
            } else {
                tbody.innerHTML = state.employees.map(e => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors">
                        <td class="p-3">
                            <p class="font-bold">${e.firstName} ${e.lastName}</p>
                            <p class="text-[10px] text-on-surface-variant truncate">${e.email}</p>
                        </td>
                        <td class="p-3 font-mono">${e.phone}</td>
                        <td class="p-3 font-bold text-primary">${e.shiftName || 'Day Shift'}</td>
                        <td class="p-3 font-mono uppercase text-[10px]">${e.salaryType || e.salaryModel || 'FIXED'}</td>
                        <td class="p-3 font-mono font-bold">₹${e.salaryType === 'DAILY_WAGE' ? e.dailyWageRate : (e.monthlySalaryRate || e.baseSalary || 0)}</td>
                        <td class="p-3 text-right">
                            <button class="text-on-surface-variant hover:text-error" onclick="deleteEmployee('${e.id}')">
                                <span class="material-symbols-outlined text-sm">delete_sweep</span>
                            </button>
                        </td>
                    </tr>
                `).join('');
            }

            // Update attendance borrowers dropdown
            const attEmpSelect = document.getElementById('att-emp-id');
            attEmpSelect.innerHTML = `<option value="">SELECT STAFF MEMBER</option>` + 
                state.employees.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName}</option>`).join('');

            // Update advances borrower dropdown
            const loanEmpSelect = document.getElementById('adv-emp-id');
            loanEmpSelect.innerHTML = `<option value="">SELECT ROSTER EMPLOYEE</option>` + 
                state.employees.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName}</option>`).join('');

            // Update payroll target dropdown
            const payEmpSelect = document.getElementById('pay-emp-id');
            payEmpSelect.innerHTML = `<option value="">SELECT ROSTER EMPLOYEE</option>` + 
                state.employees.map(e => `<option value="${e.id}">${e.firstName} ${e.lastName}</option>`).join('');
        }
    } catch(e) {}
}

async function loadAttendanceLogsList() {
    try {
        const res = await client.fetch('/api/attendance');
        if (res.success && res.data) {
            state.attendance = res.data.content || [];
            
            // Render Attendance Table
            const tbody = document.getElementById('attendance-table-body');
            if (state.attendance.length === 0) {
                tbody.innerHTML = `<tr><td colspan="6" class="p-4 text-center text-on-surface-variant">NO ATTENDANCE CARDS PUNCHED</td></tr>`;
            } else {
                tbody.innerHTML = state.attendance.map(a => `
                    <tr class="border-b border-outline-variant hover:bg-surface-container transition-colors ${a.isLate ? 'bg-error-container/5' : ''}">
                        <td class="p-3 font-bold">${a.employeeName || 'Staff Member'}</td>
                        <td class="p-3 font-mono">${a.checkInTime ? new Date(a.checkInTime).toLocaleString() : 'N/A'}</td>
                        <td class="p-3 font-mono">${a.checkOutTime ? new Date(a.checkOutTime).toLocaleString() : 'PENDING CHECK_OUT'}</td>
                        <td class="p-3 font-mono font-bold">${a.durationHours ? a.durationHours + ' hrs' : 'N/A'}</td>
                        <td class="p-3 font-bold uppercase text-[10px] ${a.isLate ? 'text-error animate-pulse' : 'text-emerald-500'}">${a.isLate ? 'LATE WARNING' : 'ON-TIME'}</td>
                        <td class="p-3 text-on-surface-variant text-[11px] font-mono">${a.notes || ''}</td>
                    </tr>
                `).join('');
            }

            // Update supervisor correction dropdown
            const corrSelect = document.getElementById('corr-punch-id');
            corrSelect.innerHTML = `<option value="">SELECT LOG EVENT</option>` + 
                state.attendance.map(a => `<option value="${a.id}">${a.employeeName} (${a.checkInTime ? a.checkInTime.substring(5,16) : 'Empty'})</option>`).join('');
        }
    } catch(e) {}
}

function handleAttEmployeeChange() {
    const empId = document.getElementById('att-emp-id').value;
    const emp = state.employees.find(e => e.id === empId);
    if (emp && emp.shiftId) {
        document.getElementById('att-shift-id').value = emp.shiftId;
    }
}

async function submitShift(e) {
    e.preventDefault();
    const name = document.getElementById('shift-name').value;
    const startTime = document.getElementById('shift-start').value;
    const endTime = document.getElementById('shift-end').value;
    const gracePeriodMinutes = document.getElementById('shift-grace').value;

    try {
        const res = await client.fetch('/api/shifts', {
            method: 'POST',
            body: JSON.stringify({ name, startTime, endTime, gracePeriodMinutes })
        });
        if (res.success) {
            showToast("SHIFT CONFIGURED", `Shift rule ${name} provisioned in timetable database.`, "success");
            document.getElementById('form-shift').reset();
            loadWorkforceData();
        }
    } catch(err) {}
}

async function submitEmployee(e) {
    e.preventDefault();
    const firstName = document.getElementById('emp-firstname').value;
    const lastName = document.getElementById('emp-lastname').value;
    const email = document.getElementById('emp-email').value;
    const phone = document.getElementById('emp-phone').value;
    const salaryModel = document.getElementById('emp-salmodel').value;
    const baseSalary = parseFloat(document.getElementById('emp-basesalary').value);
    const shiftId = document.getElementById('emp-shift').value;

    const role = document.getElementById('emp-role').value;
    const department = document.getElementById('emp-dept').value;
    const joiningDate = document.getElementById('emp-joiningdate').value;

    const salaryType = salaryModel === 'DAILY' ? 'DAILY_WAGE' : 'FIXED_MONTHLY';
    const monthlySalaryRate = salaryType === 'FIXED_MONTHLY' ? baseSalary : 0;
    const dailyWageRate = salaryType === 'DAILY_WAGE' ? baseSalary : 0;

    try {
        const res = await client.fetch('/api/employees', {
            method: 'POST',
            body: JSON.stringify({ 
                firstName, 
                lastName, 
                email, 
                phone, 
                shiftId,
                role,
                department,
                joiningDate,
                salaryType,
                monthlySalaryRate,
                dailyWageRate,
                salaryModel,      // Preserve for offline mock
                baseSalary        // Preserve for offline mock
            })
        });
        if (res.success) {
            showToast("EMPLOYEE ONBOARDED", `${firstName} ${lastName} added to roster ledger.`, "success");
            document.getElementById('form-employee').reset();
            loadWorkforceData();
        }
    } catch(err) {}
}

async function submitAttendancePunch(e) {
    e.preventDefault();
    const employeeId = document.getElementById('att-emp-id').value;
    const shiftId = document.getElementById('att-shift-id').value;
    const type = document.getElementById('att-type').value;
    const checkInTime = document.getElementById('att-time-stamp').value;

    try {
        let res;
        if (type === 'CHECK_IN') {
            res = await client.fetch('/api/attendance/check-in', {
                method: 'POST',
                body: JSON.stringify({ employeeId, shiftId, checkInTime })
            });
        } else {
            res = await client.fetch(`/api/attendance/check-out/employee/${employeeId}`, {
                method: 'POST'
            });
        }

        if (res.success) {
            showToast("CARD PUNCH OK", "Attendance clock movement punched successfully.", "success");
            document.getElementById('form-attendance').reset();
            
            const now = new Date();
            document.getElementById('att-time-stamp').value = now.toISOString().substring(0, 19);

            loadWorkforceData();
        } else {
            showToast("LOCK CONSTRAINTS BLOCKED", res.message || "Attendance validation failed.", "error");
        }
    } catch(err) {}
}

async function submitCorrectionOverride(e) {
    e.preventDefault();
    const id = document.getElementById('corr-punch-id').value;
    const correctedCheckInTime = document.getElementById('corr-in').value;
    const correctedCheckOutTime = document.getElementById('corr-out').value;
    const notes = document.getElementById('corr-notes').value;
    const status = document.getElementById('corr-status').value;

    try {
        const res = await client.fetch(`/api/attendance/correct/${id}`, {
            method: 'PUT',
            body: JSON.stringify({ 
                checkInTime: correctedCheckInTime,
                checkOutTime: correctedCheckOutTime || null,
                reason: notes,
                status,
                correctedCheckInTime, // support for local sandbox mode
                correctedCheckOutTime, // support for local sandbox mode
                notes // support for local sandbox mode
            })
        });
        if (res.success) {
            showToast("CORRECTION COMMITTED", "Supervisor override authorized inside Audit Logs.", "success");
            document.getElementById('form-correction').reset();
            loadWorkforceData();
        }
    } catch(err) {}
}

async function deleteEmployee(id) {
    if (!confirm("Deactivate this employee roster profile? historical summaries will remain.")) return;
    try {
        const res = await client.fetch(`/api/employees/${id}`, { method: 'DELETE' });
        if (res.success) {
            showToast("ROSTER ARCHIVED", "Employee soft-deleted.", "success");
            loadEmployeesList();
        }
    } catch(e) {}
}

async function deleteShift(id) {
    if (!confirm("Are you sure you want to purge this Shift Rule from the system?")) return;
    try {
        const res = await client.fetch(`/api/shifts/${id}`, { method: 'DELETE' });
        if (res.success) {
            showToast("RULE PURGED", "Shift deleted successfully.", "success");
            loadWorkforceData();
        }
    } catch(e) {}
}
