-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================================
-- 1. Tenant & Security Layer
-- =========================================================================

-- Tenants (Factories) Table
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    subdomain VARCHAR(100) UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, SUSPENDED, DELETED
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Users (Accounts) Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL, -- OWNER, MANAGER, FLOOR_STAFF
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT uq_users_tenant_username UNIQUE(tenant_id, username),
    CONSTRAINT uq_users_tenant_email UNIQUE(tenant_id, email)
);

-- Create Indexes for Security Layer
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);

-- =========================================================================
-- 2. Audit Logging
-- =========================================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, RESTORE
    old_value TEXT, -- JSON content before change
    new_value TEXT, -- JSON content after change
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_tenant ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- =========================================================================
-- 3. Raw Material Inventory Layer
-- =========================================================================

CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(150),
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT uq_suppliers_tenant_name UNIQUE(tenant_id, name)
);

CREATE TABLE raw_materials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    unit VARCHAR(50) NOT NULL, -- KG, LTR, METERS, PCS, etc.
    quantity NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    reorder_level NUMERIC(15, 4) NOT NULL DEFAULT 0.0000,
    supplier_id UUID REFERENCES suppliers(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_raw_materials_tenant_sku UNIQUE(tenant_id, sku)
);

CREATE TABLE stock_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    material_id UUID NOT NULL REFERENCES raw_materials(id) ON DELETE CASCADE,
    transaction_type VARCHAR(50) NOT NULL, -- STOCK_IN, STOCK_OUT
    quantity NUMERIC(15, 4) NOT NULL,
    remarks TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Indexes for Inventory Layer
CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);
CREATE INDEX idx_raw_materials_tenant ON raw_materials(tenant_id);
CREATE INDEX idx_raw_materials_sku ON raw_materials(sku);
CREATE INDEX idx_stock_transactions_material ON stock_transactions(material_id);
CREATE INDEX idx_stock_transactions_tenant ON stock_transactions(tenant_id);

-- =========================================================================
-- 4. Workforce Layer
-- =========================================================================

CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    first_name VARCHAR(150) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(100) NOT NULL, -- OPERATOR, SUPERVISOR, HELPER, PACKER, etc.
    department VARCHAR(100) NOT NULL, -- PRODUCTION, ASSEMBLY, LOGISTICS, QUALITY, etc.
    joining_date DATE NOT NULL,
    salary_type VARCHAR(50) NOT NULL, -- FIXED_MONTHLY, DAILY_WAGE
    monthly_salary_rate NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    daily_wage_rate NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, TERMINATED, ON_LEAVE
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE shifts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- MORNING, EVENING, NIGHT
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE attendances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    check_in_time TIMESTAMP WITH TIME ZONE NOT NULL,
    check_out_time TIMESTAMP WITH TIME ZONE,
    shift_id UUID REFERENCES shifts(id) ON DELETE SET NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, HALF_DAY, LATE
    corrected_by UUID REFERENCES users(id),
    correction_reason TEXT,
    corrected_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT uq_attendance_employee_date UNIQUE(tenant_id, employee_id, date)
);

-- Indexes for Workforce Layer
CREATE INDEX idx_employees_tenant ON employees(tenant_id);
CREATE INDEX idx_shifts_tenant ON shifts(tenant_id);
CREATE INDEX idx_attendances_tenant ON attendances(tenant_id);
CREATE INDEX idx_attendances_employee ON attendances(employee_id);
CREATE INDEX idx_attendances_date ON attendances(date);

-- =========================================================================
-- 5. Payroll Layer
-- =========================================================================

CREATE TABLE advance_salaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    request_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, DEDUCTED
    approved_by UUID REFERENCES users(id),
    deduction_month VARCHAR(7) NOT NULL, -- YYYY-MM
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE payroll_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    payroll_month VARCHAR(7) NOT NULL, -- YYYY-MM
    base_salary NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    daily_wage_earnings NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    overtime_earnings NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    advance_deductions NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    other_deductions NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    net_salary NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT', -- DRAFT, APPROVED, PAID
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    CONSTRAINT uq_payroll_employee_month UNIQUE(tenant_id, employee_id, payroll_month)
);

-- Indexes for Payroll
CREATE INDEX idx_advance_salaries_tenant ON advance_salaries(tenant_id);
CREATE INDEX idx_payroll_records_tenant ON payroll_records(tenant_id);
CREATE INDEX idx_payroll_records_employee ON payroll_records(employee_id);

-- =========================================================================
-- 6. Expense Layer
-- =========================================================================

CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category VARCHAR(100) NOT NULL, -- UTILITIES, MAINTENANCE, TRANSPORT, MISC
    department VARCHAR(100),
    project VARCHAR(150),
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED', -- SUBMITTED, APPROVED, REVIEWED, REJECTED
    submitted_by UUID NOT NULL REFERENCES users(id),
    approved_by UUID REFERENCES users(id),
    reviewed_by UUID REFERENCES users(id),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Indexes for Expenses
CREATE INDEX idx_expenses_tenant ON expenses(tenant_id);
CREATE INDEX idx_expenses_status ON expenses(status);
