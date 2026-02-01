package com.smarttask.smarttaskmanager.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.smarttask.smarttaskmanager.model.Task;

import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Stream;

public class PDFExportService {

    public void exportTasksToPDF(List<Task> tasks, String filePath) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // 1. Titre du Document
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED);
            Paragraph title = new Paragraph("Rapport des Tâches - SmartManager", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. Création du Tableau (4 Colonnes)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);

            // 3. Header du Tableau
            addTableHeader(table);

            // 4. Remplir avec les Données
            for (Task task : tasks) {
                table.addCell(task.getTitle());
                table.addCell(task.getPriority());
                table.addCell(task.getStatus());
                table.addCell(task.getDeadline() != null ? task.getDeadline().toString() : "Aucune");
            }

            document.add(table);
            document.close();

            System.out.println("✅ PDF Généré avec succès : " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addTableHeader(PdfPTable table) {
        Stream.of("Titre", "Priorité", "Statut", "Deadline")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setPadding(5);
                    table.addCell(header);
                });
    }
}