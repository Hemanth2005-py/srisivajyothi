package com.mymart.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.mymart.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class InvoicePdfService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a").withZone(ZoneId.of("Asia/Kolkata"));

    public byte[] generateOrderInvoice(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Color scheme (Blinkit style fresh green & slate dark)
            Color brandGreen = new Color(12, 131, 31);
            Color lightGray = new Color(245, 247, 250);
            Color darkText = new Color(33, 37, 41);
            Color mutedText = new Color(108, 117, 125);

            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, brandGreen);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, darkText);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, darkText);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkText);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 9, mutedText);

            // Header Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.addElement(new Paragraph("MYMART", brandFont));
            logoCell.addElement(new Paragraph("Supermarket Grocery Delivery", mutedFont));
            logoCell.addElement(new Paragraph("www.mymart.com | support@mymart.com", mutedFont));
            headerTable.addCell(logoCell);

            PdfPCell docTypeCell = new PdfPCell();
            docTypeCell.setBorder(Rectangle.NO_BORDER);
            docTypeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            String docTitle = "PAID".equalsIgnoreCase(order.getPaymentStatus()) ? "TAX INVOICE" : "ORDER RECEIPT";
            Paragraph pDocTitle = new Paragraph(docTitle, titleFont);
            pDocTitle.setAlignment(Element.ALIGN_RIGHT);
            docTypeCell.addElement(pDocTitle);

            Paragraph pOrderNo = new Paragraph("Order #: " + order.getOrderNumber(), boldFont);
            pOrderNo.setAlignment(Element.ALIGN_RIGHT);
            docTypeCell.addElement(pOrderNo);

            Paragraph pDate = new Paragraph("Date: " + DATE_FMT.format(order.getCreatedAt()), mutedFont);
            pDate.setAlignment(Element.ALIGN_RIGHT);
            docTypeCell.addElement(pDate);

            Paragraph pStatus = new Paragraph("Status: " + order.getOrderStatus() + " | " + order.getPaymentStatus(), boldFont);
            pStatus.setAlignment(Element.ALIGN_RIGHT);
            docTypeCell.addElement(pStatus);

            headerTable.addCell(docTypeCell);
            document.add(headerTable);

            document.add(new Paragraph(" "));

            // Customer and Delivery Details Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{50, 50});

            PdfPCell custCell = new PdfPCell();
            custCell.setBackgroundColor(lightGray);
            custCell.setPadding(8);
            custCell.setBorderColor(new Color(220, 224, 230));
            custCell.addElement(new Paragraph("CUSTOMER INFORMATION", boldFont));
            if (order.getCustomerSnapshot() != null) {
                custCell.addElement(new Paragraph("Name: " + order.getCustomerSnapshot().getName(), normalFont));
                custCell.addElement(new Paragraph("Email: " + order.getCustomerSnapshot().getEmail(), normalFont));
                custCell.addElement(new Paragraph("Phone: " + order.getCustomerSnapshot().getPhone(), normalFont));
            }
            infoTable.addCell(custCell);

            PdfPCell delivCell = new PdfPCell();
            delivCell.setBackgroundColor(lightGray);
            delivCell.setPadding(8);
            delivCell.setBorderColor(new Color(220, 224, 230));
            delivCell.addElement(new Paragraph("DELIVERY ADDRESS", boldFont));
            if (order.getDeliveryAddress() != null) {
                delivCell.addElement(new Paragraph(order.getDeliveryAddress().getStreet(), normalFont));
                delivCell.addElement(new Paragraph(order.getDeliveryAddress().getCity() + ", " +
                        order.getDeliveryAddress().getState() + " - " + order.getDeliveryAddress().getPincode(), normalFont));
                if (order.getDeliveryAddress().getLandmark() != null) {
                    delivCell.addElement(new Paragraph("Landmark: " + order.getDeliveryAddress().getLandmark(), mutedFont));
                }
            }
            infoTable.addCell(delivCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Line Items Table
            PdfPTable itemTable = new PdfPTable(6);
            itemTable.setWidthPercentage(100);
            itemTable.setWidths(new float[]{5, 45, 12, 12, 12, 14});

            // Table Headers
            String[] headers = {"#", "Item Description", "Qty", "Price", "Discount", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(brandGreen);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                itemTable.addCell(cell);
            }

            int index = 1;
            for (Order.OrderItem item : order.getItems()) {
                PdfPCell cIndex = new PdfPCell(new Phrase(String.valueOf(index++), normalFont));
                cIndex.setHorizontalAlignment(Element.ALIGN_CENTER);
                cIndex.setPadding(5);
                itemTable.addCell(cIndex);

                String desc = item.getProductName() + (item.getUnit() != null ? " (" + item.getUnit() + ")" : "");
                PdfPCell cDesc = new PdfPCell(new Phrase(desc, normalFont));
                cDesc.setPadding(5);
                itemTable.addCell(cDesc);

                PdfPCell cQty = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), normalFont));
                cQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                cQty.setPadding(5);
                itemTable.addCell(cQty);

                PdfPCell cPrice = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", item.getOriginalUnitPrice()), normalFont));
                cPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cPrice.setPadding(5);
                itemTable.addCell(cPrice);

                double itemDiscount = (item.getOriginalUnitPrice() - item.getDiscountUnitPrice()) * item.getQuantity();
                PdfPCell cDisc = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", Math.max(0, itemDiscount)), normalFont));
                cDisc.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cDisc.setPadding(5);
                itemTable.addCell(cDisc);

                PdfPCell cSub = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", item.getSubtotal()), boldFont));
                cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cSub.setPadding(5);
                itemTable.addCell(cSub);
            }

            document.add(itemTable);
            document.add(new Paragraph(" "));

            // Totals Table
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(50);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{60, 40});

            addTotalRow(totalsTable, "Original Items Total:", "Rs. " + String.format("%.2f", order.getOriginalTotal()), normalFont);
            addTotalRow(totalsTable, "Discounts Saved:", "- Rs. " + String.format("%.2f", order.getDiscountTotal()), normalFont);
            addTotalRow(totalsTable, "Delivery Charge:", order.getDeliveryCharge() == 0 ? "FREE" : "Rs. " + String.format("%.2f", order.getDeliveryCharge()), normalFont);
            addTotalRow(totalsTable, "Taxes (GST 5%):", "Rs. " + String.format("%.2f", order.getTaxTotal()), normalFont);

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Final Payable Total:", boldFont));
            totalLabelCell.setBackgroundColor(lightGray);
            totalLabelCell.setPadding(6);
            totalsTable.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase("Rs. " + String.format("%.2f", order.getFinalTotal()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, brandGreen)));
            totalValCell.setBackgroundColor(lightGray);
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValCell.setPadding(6);
            totalsTable.addCell(totalValCell);

            document.add(totalsTable);

            // Footer Notice
            document.add(new Paragraph(" "));
            Paragraph pPayment = new Paragraph("Payment Method: " + order.getPaymentMethod() + " | Payment Status: " + order.getPaymentStatus(), boldFont);
            document.add(pPayment);

            Paragraph pThankYou = new Paragraph("Thank you for shopping with MYMART! For queries, contact support@mymart.com", mutedFont);
            pThankYou.setSpacingBefore(10);
            document.add(pThankYou);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF invoice for order {}: ", order.getOrderNumber(), e);
            throw new RuntimeException("Error generating invoice PDF: " + e.getMessage());
        }
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, font));
        lCell.setBorder(Rectangle.NO_BORDER);
        lCell.setPadding(3);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, font));
        vCell.setBorder(Rectangle.NO_BORDER);
        vCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        vCell.setPadding(3);
        table.addCell(vCell);
    }
}
