package com.mfgops.payroll.pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mfgops.payroll.record.PayrollRecord;
import com.mfgops.workforce.employee.Employee;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PayslipService {

    public byte[] generatePayslipPdf(PayrollRecord record) {
        Employee employee = record.getEmployee();
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set up Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);

            // Title
            Paragraph title = new Paragraph("FABRICORE PLATFORMS LTD.", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Payslip for the Month of: " + record.getPayrollMonth(), subTitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Employee Details Table
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(20);

            detailsTable.addCell(createCell("Employee ID:", boldFont, false));
            detailsTable.addCell(createCell(employee.getId().toString(), normalFont, false));

            detailsTable.addCell(createCell("Employee Name:", boldFont, false));
            detailsTable.addCell(createCell(employee.getFirstName() + " " + employee.getLastName(), normalFont, false));

            detailsTable.addCell(createCell("Department / Role:", boldFont, false));
            detailsTable.addCell(createCell(employee.getDepartment() + " / " + employee.getRole(), normalFont, false));

            detailsTable.addCell(createCell("Salary Structure:", boldFont, false));
            detailsTable.addCell(createCell(employee.getSalaryType(), normalFont, false));

            document.add(detailsTable);

            // Earnings vs Deductions Table
            PdfPTable salaryTable = new PdfPTable(4);
            salaryTable.setWidthPercentage(100);
            salaryTable.setSpacingAfter(20);
            salaryTable.setWidths(new float[]{25, 25, 25, 25});

            // Headers
            salaryTable.addCell(createCell("Earnings Description", boldFont, true));
            salaryTable.addCell(createCell("Amount", boldFont, true));
            salaryTable.addCell(createCell("Deductions Description", boldFont, true));
            salaryTable.addCell(createCell("Amount", boldFont, true));

            // Row 1
            if ("FIXED_MONTHLY".equalsIgnoreCase(employee.getSalaryType())) {
                salaryTable.addCell(createCell("Base Monthly Salary", normalFont, false));
                salaryTable.addCell(createCell(record.getBaseSalary().toString() + " INR", normalFont, false));
            } else {
                salaryTable.addCell(createCell("Daily Wage Earnings", normalFont, false));
                salaryTable.addCell(createCell(record.getDailyWageEarnings().toString() + " INR", normalFont, false));
            }
            salaryTable.addCell(createCell("Advance Salary Repayment", normalFont, false));
            salaryTable.addCell(createCell(record.getAdvanceDeductions().toString() + " INR", normalFont, false));

            // Row 2
            salaryTable.addCell(createCell("Overtime Earnings", normalFont, false));
            salaryTable.addCell(createCell(record.getOvertimeEarnings().toString() + " INR", normalFont, false));
            salaryTable.addCell(createCell("Other Absent Deductions", normalFont, false));
            salaryTable.addCell(createCell(record.getOtherDeductions().toString() + " INR", normalFont, false));

            document.add(salaryTable);

            // Net Pay Summary Block
            PdfPTable netTable = new PdfPTable(2);
            netTable.setWidthPercentage(50);
            netTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            netTable.setSpacingAfter(30);

            netTable.addCell(createCell("Total Gross Earnings:", boldFont, false));
            BigDecimal gross = "FIXED_MONTHLY".equalsIgnoreCase(employee.getSalaryType()) ? record.getBaseSalary() : record.getDailyWageEarnings();
            netTable.addCell(createCell(gross.toString() + " INR", normalFont, false));

            netTable.addCell(createCell("Total Deductions:", boldFont, false));
            BigDecimal deductions = record.getAdvanceDeductions().add(record.getOtherDeductions());
            netTable.addCell(createCell(deductions.toString() + " INR", normalFont, false));

            netTable.addCell(createCell("NET SALARY DISBURSED:", boldFont, true));
            netTable.addCell(createCell(record.getNetSalary().toString() + " INR", boldFont, true));

            document.add(netTable);

            // Footer
            Paragraph footer = new Paragraph("This is a system-generated document and does not require a physical signature.", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private PdfPCell createCell(String text, Font font, boolean highlight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        if (highlight) {
            cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
        }
        return cell;
    }
}
