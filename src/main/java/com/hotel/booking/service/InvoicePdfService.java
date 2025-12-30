package com.hotel.booking.service;

import com.hotel.booking.entity.Invoice;
import com.hotel.booking.entity.Booking;
import com.hotel.booking.entity.User;
import com.hotel.booking.repository.BookingCancellationRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class InvoicePdfService {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePdfService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.GERMANY);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withLocale(Locale.GERMANY);

    private final BookingCancellationRepository bookingCancellationRepository;

    public InvoicePdfService(BookingCancellationRepository bookingCancellationRepository) {
        this.bookingCancellationRepository = bookingCancellationRepository;
    }

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            logger.info("Generating PDF for invoice: {}", invoice.getInvoiceNumber());
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            DeviceRgb headerColor = new DeviceRgb(25, 45, 85); // Dark blue
            DeviceRgb accentColor = new DeviceRgb(218, 165, 32); // Gold

            // ===== HEADER WITH LOGO (LOGO ON TOP RIGHT) =====
            Table headerTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            
            // Hotel Info (left)
            Cell infoCell = new Cell();
            infoCell.add(new Paragraph("Hotelium").setFontSize(18).setBold().setFontColor(headerColor));
            infoCell.add(new Paragraph("Luxury Hotel Experience").setFontSize(10).setFontColor(accentColor).setItalic());
            infoCell.add(new Paragraph("Interaktion 1, 33602 Bielefeld").setFontSize(9));
            infoCell.add(new Paragraph("Tel: +49 (521) 123-4567 | E-Mail: info@hotelium.de").setFontSize(8));
            infoCell.setBorder(null);
            infoCell.setPadding(0);
            headerTable.addCell(infoCell);
            
            // Logo (right)
            Cell logoCell = new Cell();
            try {
                String logoPath = "src/main/resources/static/images/HoteliumLogo.png";
                File logoFile = new File(logoPath);
                if (logoFile.exists()) {
                    Image logo = new Image(com.itextpdf.io.image.ImageDataFactory.create(logoPath));
                    logo.scaleToFit(100, 100);
                    logoCell.add(logo).setTextAlignment(TextAlignment.RIGHT);
                } else {
                    logoCell.add(new Paragraph(""));
                }
            } catch (Exception e) {
                logger.warn("Logo not found, continuing without logo", e);
                logoCell.add(new Paragraph(""));
            }
            logoCell.setBorder(null);
            logoCell.setPadding(0);
            logoCell.setPaddingLeft(200);
            logoCell.setMarginLeft(10);
            logoCell.setTextAlignment(TextAlignment.RIGHT);
            headerTable.addCell(logoCell);
            
            document.add(headerTable);
            document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2)).setMarginBottom(15));
            
            // ===== INVOICE TITLE =====
            Paragraph title = new Paragraph("INVOICE")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(headerColor)
                .setMarginBottom(15);
            document.add(title);
            
            // ===== INVOICE DETAILS (2-COLUMN) =====
            Table detailsTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            
            // Left column
            Cell leftCol = new Cell();
            leftCol.add(new Paragraph("INVOICE DETAILS").setFontSize(11).setBold().setFontColor(headerColor));
            leftCol.add(new Paragraph("Invoice #: " + (invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")).setFontSize(10));
            leftCol.add(new Paragraph("Issued: " + (invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_TIME_FORMATTER) : "N/A")).setFontSize(10));
            leftCol.setBorder(null);
            leftCol.setPadding(10);
            detailsTable.addCell(leftCol);
            
            // Right column
            Cell rightCol = new Cell();
            rightCol.add(new Paragraph("GUEST INFORMATION").setFontSize(11).setBold().setFontColor(headerColor));
            Booking booking = invoice.getBooking();
            if (booking != null && booking.getGuest() != null) {
                User guest = booking.getGuest();
                String guestName = (guest.getFirstName() != null ? guest.getFirstName() : "") + " " + (guest.getLastName() != null ? guest.getLastName() : "");
                rightCol.add(new Paragraph(guestName.trim().isEmpty() ? "N/A" : guestName).setFontSize(10).setBold());
                rightCol.add(new Paragraph(guest.getEmail() != null ? guest.getEmail() : "N/A").setFontSize(9));
                
                // Add complete address
                if (guest.getAddress() != null) {
                    StringBuilder addressBuilder = new StringBuilder();
                    if (guest.getAddress().getStreet() != null && !guest.getAddress().getStreet().isEmpty()) {
                        addressBuilder.append(guest.getAddress().getStreet());
                    }
                    if (guest.getAddress().getHouseNumber() != null && !guest.getAddress().getHouseNumber().isEmpty()) {
                        addressBuilder.append(" ").append(guest.getAddress().getHouseNumber());
                    }
                    if (addressBuilder.length() > 0) {
                        rightCol.add(new Paragraph(addressBuilder.toString()).setFontSize(9));
                    }
                    
                    StringBuilder cityBuilder = new StringBuilder();
                    if (guest.getAddress().getPostalCode() != null && !guest.getAddress().getPostalCode().isEmpty()) {
                        cityBuilder.append(guest.getAddress().getPostalCode()).append(" ");
                    }
                    if (guest.getAddress().getCity() != null && !guest.getAddress().getCity().isEmpty()) {
                        cityBuilder.append(guest.getAddress().getCity());
                    }
                    if (cityBuilder.length() > 0) {
                        rightCol.add(new Paragraph(cityBuilder.toString()).setFontSize(9));
                    }
                    
                    if (guest.getAddress().getCountry() != null && !guest.getAddress().getCountry().isEmpty()) {
                        rightCol.add(new Paragraph(guest.getAddress().getCountry()).setFontSize(9));
                    }
                }
            }
            rightCol.setBorder(null);
            rightCol.setPadding(10);
            detailsTable.addCell(rightCol);
            
            document.add(detailsTable);
            document.add(new Paragraph("").setMarginBottom(8));
            
            // ===== BOOKING DETAILS =====
            document.add(new Paragraph("BOOKING DETAILS").setFontSize(11).setBold().setFontColor(headerColor).setMarginTop(0));
            
            if (booking != null) {
                Table bookingTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
                
                Cell cell1 = new Cell().add(new Paragraph("Booking Number:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                Cell cell2 = new Cell().add(new Paragraph(booking.getBookingNumber() != null ? booking.getBookingNumber() : "N/A").setFontSize(9)).setBorder(null).setPadding(5);
                bookingTable.addCell(cell1);
                bookingTable.addCell(cell2);
                
                if (booking.getCheckInDate() != null) {
                    Cell cell3 = new Cell().add(new Paragraph("Check-in:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                    Cell cell4 = new Cell().add(new Paragraph(booking.getCheckInDate().format(DATE_FORMATTER)).setFontSize(9)).setBorder(null).setPadding(5);
                    bookingTable.addCell(cell3);
                    bookingTable.addCell(cell4);
                }
                
                if (booking.getCheckOutDate() != null) {
                    Cell cell5 = new Cell().add(new Paragraph("Check-out:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                    Cell cell6 = new Cell().add(new Paragraph(booking.getCheckOutDate().format(DATE_FORMATTER)).setFontSize(9)).setBorder(null).setPadding(5);
                    bookingTable.addCell(cell5);
                    bookingTable.addCell(cell6);
                }
                
                Cell cell7 = new Cell().add(new Paragraph("Total Booking Price:").setBold().setFontSize(9)).setBorder(null).setPadding(5).setFontColor(accentColor);
                Cell cell8 = new Cell().add(new Paragraph("€" + (booking.getTotalPrice() != null ? String.format("%.2f", booking.getTotalPrice()) : "N/A")).setBold().setFontSize(9)).setBorder(null).setPadding(5).setFontColor(accentColor);
                bookingTable.addCell(cell7);
                bookingTable.addCell(cell8);
                
                // Add Extras if available
                if (booking.getExtras() != null && !booking.getExtras().isEmpty()) {
                    Cell extrasLabel = new Cell().add(new Paragraph("Extras:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                    Cell extrasContent = new Cell().setBorder(null).setPadding(5);
                    
                    StringBuilder extrasText = new StringBuilder();
                    for (com.hotel.booking.entity.BookingExtra extra : booking.getExtras()) {
                        if (extrasText.length() > 0) {
                            extrasText.append(", ");
                        }
                        if (extra.getName() != null) {
                            extrasText.append(extra.getName());
                        }
                    }
                    
                    extrasContent.add(new Paragraph(extrasText.toString()).setFontSize(9));
                    bookingTable.addCell(extrasLabel);
                    bookingTable.addCell(extrasContent);
                }
                
                document.add(bookingTable);
            }
            
            document.add(new Paragraph("").setMarginBottom(6));
            
            // ===== PAYMENT SECTION =====
            document.add(new Paragraph("PAYMENT INFORMATION").setFontSize(11).setBold().setFontColor(headerColor).setMarginTop(0));
            
            Table paymentTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            
            Cell payMethod = new Cell().add(new Paragraph("Payment Method:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
            String paymentMethodStr = invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : "N/A";
            Cell payMethodVal = new Cell().add(new Paragraph(paymentMethodStr).setFontSize(9)).setBorder(null).setPadding(5);
            paymentTable.addCell(payMethod);
            paymentTable.addCell(payMethodVal);

            Invoice.PaymentStatus statusEnum = invoice.getInvoiceStatus() != null
                    ? invoice.getInvoiceStatus()
                    : Invoice.PaymentStatus.PENDING;

            DeviceRgb statusColor;
            switch (statusEnum) {
                case PAID:
                    statusColor = new DeviceRgb(34, 139, 34); // Green
                    break;
                case PENDING:
                case PARTIAL:
                    statusColor = new DeviceRgb(218, 165, 32); // Gold/Amber
                    break;
                case REFUNDED:
                    statusColor = new DeviceRgb(30, 144, 255); // Blue
                    break;
                case FAILED:
                default:
                    statusColor = new DeviceRgb(220, 20, 60); // Red
                    break;
            }

            Cell status = new Cell().add(new Paragraph("Status:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
            Cell statusVal = new Cell().add(new Paragraph(statusEnum.name()).setBold().setFontSize(9)).setBorder(null).setPadding(5).setFontColor(statusColor);
            paymentTable.addCell(status);
            paymentTable.addCell(statusVal);

                // If refunded/partial, show the refunded amount as a negative value (credit)
                if ((statusEnum == Invoice.PaymentStatus.REFUNDED || statusEnum == Invoice.PaymentStatus.PARTIAL)
                    && booking != null
                    && booking.getId() != null) {
                bookingCancellationRepository.findTopByBookingIdOrderByCancelledAtDesc(booking.getId())
                    .map(c -> c.getRefundedAmount())
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .ifPresent(refund -> {
                        BigDecimal refundScaled = refund.setScale(2, RoundingMode.HALF_UP);
                        Cell refundLabel = new Cell()
                            .add(new Paragraph("Refund Amount:").setBold().setFontSize(9))
                            .setBorder(null).setPadding(5);
                        Cell refundVal = new Cell()
                            .add(new Paragraph("-€" + String.format(Locale.GERMANY, "%.2f", refundScaled))
                                .setBold().setFontSize(9))
                            .setBorder(null).setPadding(5)
                            .setFontColor(statusColor);
                        paymentTable.addCell(refundLabel);
                        paymentTable.addCell(refundVal);
                    });
                }
            
            boolean showPaidOn = statusEnum != Invoice.PaymentStatus.PENDING && invoice.getPaidAt() != null;
            if (showPaidOn) {
                Cell paidDate = new Cell().add(new Paragraph("Paid On:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                Cell paidDateVal = new Cell().add(new Paragraph(invoice.getPaidAt().format(DATE_TIME_FORMATTER)).setFontSize(9)).setBorder(null).setPadding(5);
                paymentTable.addCell(paidDate);
                paymentTable.addCell(paidDateVal);
            } else if (invoice.getIssuedAt() != null) {
                LocalDate deadline = invoice.getIssuedAt().toLocalDate().plusDays(14);
                Cell deadlineCell = new Cell().add(new Paragraph("Payment Deadline:").setBold().setFontSize(9)).setBorder(null).setPadding(5);
                Cell deadlineVal = new Cell().add(new Paragraph(deadline.format(DATE_FORMATTER)).setFontSize(9)).setBorder(null).setPadding(5).setFontColor(accentColor);
                paymentTable.addCell(deadlineCell);
                paymentTable.addCell(deadlineVal);
            }
            
            document.add(paymentTable);
            
            document.add(new Paragraph("").setMarginBottom(6));
            
            // ===== BANK DETAILS =====
            document.add(new Paragraph("BANK DETAILS").setFontSize(11).setBold().setFontColor(headerColor).setMarginTop(0));
            
            Table bankTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            
            Cell[] bankLabels = {
                new Cell().add(new Paragraph("Account Holder:").setBold().setFontSize(9)),
                new Cell().add(new Paragraph("IBAN:").setBold().setFontSize(9)),
                new Cell().add(new Paragraph("BIC:").setBold().setFontSize(9))
            };
            Cell[] bankValues = {
                new Cell().add(new Paragraph("Hotelium GmbH").setFontSize(9)),
                new Cell().add(new Paragraph("DE44 3564 0114 0002 0188 06").setFontSize(9)),
                new Cell().add(new Paragraph("WELADED1BIE").setFontSize(9))
            };
            
            for (int i = 0; i < bankLabels.length; i++) {
                bankLabels[i].setBorder(null).setPadding(5);
                bankValues[i].setBorder(null).setPadding(5);
                bankTable.addCell(bankLabels[i]);
                bankTable.addCell(bankValues[i]);
            }
            
            document.add(bankTable);
            
            document.add(new Paragraph("").setMarginBottom(6));
            
            // ===== TOTAL AMOUNT (HIGHLIGHTED) =====
            Table totalTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
            Cell totalLabel = new Cell().add(new Paragraph("TOTAL INVOICE AMOUNT").setBold().setFontSize(10)).setPadding(5);
            totalLabel.setBackgroundColor(headerColor).setFontColor(ColorConstants.WHITE);
            totalTable.addCell(totalLabel);
            
            Cell totalAmount = new Cell().add(new Paragraph("€" + (invoice.getAmount() != null ? String.format("%.2f", invoice.getAmount()) : "N/A")).setFontSize(12).setBold()).setPadding(5);
            totalAmount.setBackgroundColor(accentColor);
            totalTable.addCell(totalAmount);
            
            document.add(totalTable);
            
            document.add(new Paragraph("").setMarginBottom(8));
            
            // ===== FOOTER =====
            document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1)).setMarginTop(5).setMarginBottom(5));
            document.add(new Paragraph("Thank you for your stay at Hotelium!").setTextAlignment(TextAlignment.CENTER).setFontSize(10).setItalic().setFontColor(headerColor));
            document.add(new Paragraph("").setMarginBottom(2));
            document.add(new Paragraph("Hotelium GmbH | Managing Director: Max Mustermann | Munich Regional Court HRB 123456").setTextAlignment(TextAlignment.CENTER).setFontSize(7).setFontColor(ColorConstants.GRAY));
            
            document.close();
            logger.info("PDF generated successfully, size: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
}



