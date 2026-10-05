package com.moh.vaxtrack.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Component
public class PdfGenerator {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(59, 111, 232));
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, Color.GRAY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY);

    // A one-page PDF containing the appointment's QR code and its key details
    public byte[] generateQrPdf(byte[] qrPng, String patientName, String hospitalName,
                                 String vaccineName, int doseNumber, String eventDate,
                                 String timeSlot, String bookingCode) throws DocumentException, IOException {

        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.add(new Paragraph("VaxTrack Booking Confirmation", TITLE_FONT));
        document.add(Chunk.NEWLINE);

        Image qrImage = Image.getInstance(qrPng);
        qrImage.setAlignment(Image.ALIGN_CENTER);
        qrImage.scaleToFit(200, 200);
        document.add(qrImage);
        document.add(Chunk.NEWLINE);

        addField(document, "Patient", patientName);
        addField(document, "Hospital", hospitalName);
        addField(document, "Vaccine", vaccineName + " — Dose " + doseNumber);
        addField(document, "Date & Time", eventDate + " • " + timeSlot);
        addField(document, "Booking Code", bookingCode);

        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
                "Present this QR code at your appointment. This booking is only valid while active.",
                FOOTER_FONT);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    public byte[] generateCertificatePdf(String patientName, String idType, String idNumber,
                                          List<String[]> vaccinationRows) throws DocumentException {

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        document.add(new Paragraph("VaxTrack Vaccination Certificate", TITLE_FONT));
        document.add(new Paragraph("Issued by the Ministry of Health", FOOTER_FONT));
        document.add(Chunk.NEWLINE);

        addField(document, "Patient Name", patientName);
        addField(document, idType, idNumber);
        addField(document, "Issued On", java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        document.add(Chunk.NEWLINE);

        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
        table.setWidthPercentage(100);
        addHeaderCell(table, "Vaccine");
        addHeaderCell(table, "Dose");
        addHeaderCell(table, "Hospital");
        addHeaderCell(table, "Date");

        if (vaccinationRows.isEmpty()) {
            com.lowagie.text.pdf.PdfPCell emptyCell = new com.lowagie.text.pdf.PdfPCell(
                    new Phrase("No completed vaccinations on record yet.", VALUE_FONT));
            emptyCell.setColspan(4);
            emptyCell.setPadding(10);
            table.addCell(emptyCell);
        } else {
            for (String[] row : vaccinationRows) {
                for (String cell : row) {
                    com.lowagie.text.pdf.PdfPCell pdfCell = new com.lowagie.text.pdf.PdfPCell(new Phrase(cell, VALUE_FONT));
                    pdfCell.setPadding(8);
                    table.addCell(pdfCell);
                }
            }
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph(
                "This certificate reflects vaccination records held by VaxTrack as of the issue date above.",
                FOOTER_FONT));

        document.close();
        return outputStream.toByteArray();
    }

    private void addField(Document document, String label, String value) throws DocumentException {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + ": ", LABEL_FONT));
        p.add(new Chunk(value, VALUE_FONT));
        p.setSpacingAfter(6);
        document.add(p);
    }

    private void addHeaderCell(com.lowagie.text.pdf.PdfPTable table, String text) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new Phrase(text, new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(new Color(59, 111, 232));
        cell.setPadding(8);
        table.addCell(cell);
    }
}
