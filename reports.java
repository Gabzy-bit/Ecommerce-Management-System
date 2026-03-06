/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class reports {

    static class user {
        String user_id,  full_name, email, phone, role;

        user(String user_id, String full_name, String email, String phone, String role) {
            this.user_id = user_id;
            this.full_name = full_name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            
        }
    }

    public static void generateReportWithChooser(String status) {
        List<user> user = fetchUsersFromDB(status);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Users Report");
        fileChooser.setSelectedFile(new File(status+"UsersInformationReport.pdf"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            generatePDFReport(user, fileToSave.getAbsolutePath(), status);
        } else {
            JOptionPane.showMessageDialog(null, "PDF generation cancelled.");
        }
    }

    public static List<user> fetchUsersFromDB(String userStatusFilter) {
    List<user> list = new ArrayList<>();
    String dblink = "jdbc:mysql://localhost:3306/ecommercesystem";
    String user = "root";
    String password = "";

    String query = "SELECT * FROM users WHERE role = ? ORDER BY user_id"; // Grouping by Terminal

    try (Connection conn = DriverManager.getConnection(dblink, user, password);
         PreparedStatement stmt = conn.prepareStatement(query)) {

        stmt.setString(1, userStatusFilter);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            list.add(new user(
                rs.getString("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("role")
                          ));
            
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
    }

    return list;
}

    public static void generatePDFReport(List<user> user, String pdfPath, String status) {
    try {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // Logo
        Image logo = Image.getInstance("ecom.jpg");
        logo.scaleToFit(300, 150);
        logo.setAlignment(Image.ALIGN_RIGHT);
        document.add(logo);

        // Company Info
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        Paragraph companyName = new Paragraph("ECOMMERCE SYSTEM Ltd", companyFont);
        companyName.setAlignment(Element.ALIGN_LEFT);

        Paragraph address = new Paragraph("P.O. Box 12345, Mbarara, Uganda", infoFont);
        address.setAlignment(Element.ALIGN_LEFT);

        Paragraph timestamp = new Paragraph("Generated on: " + new java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm:ss").format(new java.util.Date()), infoFont);
        timestamp.setAlignment(Element.ALIGN_LEFT);

        document.add(companyName);
        document.add(address);
        document.add(timestamp);
        document.add(Chunk.NEWLINE);

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Users Report\nShowing "+status+" Users", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Group by Terminal
        String currentGroup = "";
        Font groupFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPTable table = null;

        for (user u : user) {
            if (!u.user_id.equals(currentGroup)) {
                if (table != null) {
                    document.add(table);
                    document.add(Chunk.NEWLINE);
                }

                currentGroup = u.user_id;
                Paragraph groupHeader = new Paragraph("Users: " + currentGroup, groupFont);
                groupHeader.setSpacingBefore(10);
                groupHeader.setSpacingAfter(10);
                document.add(groupHeader);

                table = new PdfPTable(5);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1.5f, 2f, 2f, 1f, 1f});

                String[] headers = {
                    "user_id", "full_name", "email", "phone",
                     "role"
                };

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            table.addCell(new PdfPCell(new Phrase(u.user_id, rowFont)));
            table.addCell(new PdfPCell(new Phrase(u.full_name, rowFont)));
            table.addCell(new PdfPCell(new Phrase(u.email, rowFont)));
            table.addCell(new PdfPCell(new Phrase(u.phone, rowFont)));
            table.addCell(new PdfPCell(new Phrase(u.role, rowFont)));
            
        }

        if (table != null) document.add(table);
        document.close();

        JOptionPane.showMessageDialog(null, "PDF saved to: " + pdfPath);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error generating PDF: " + e.getMessage());
    }
}

}
