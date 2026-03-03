package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.entity.Booking;
import com.busbooking.bus_booking_system.entity.Passenger;
import com.busbooking.bus_booking_system.exception.ResourceNotFoundException;
import com.busbooking.bus_booking_system.exception.UnauthorizedActionException;
import com.busbooking.bus_booking_system.repository.BookingRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final BookingRepository bookingRepository;

    // Change GST if needed (India bus often ~5%)
    private static final double GST_RATE = 0.05;

    public TicketService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public byte[] generateTicket(Long bookingId, String username, boolean isAdmin) {

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isAdmin && !booking.getUser().getEmail().equals(username)) {
            throw new UnauthorizedActionException("Not authorized to access this ticket");
        }

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ================= HEADER =================

            document.add(new Paragraph("Sarathi")
                    .setBold()
                    .setFontSize(24)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Heritage Travel Network of Bharat")
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\n"));

            // ================= BOOKING DETAILS =================

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

            document.add(new Paragraph("Booking ID: " + booking.getId()));
            document.add(new Paragraph("Status: " + booking.getStatus()));
            document.add(new Paragraph("Route: " +
                    booking.getBus().getFromLocation() + " → " +
                    booking.getBus().getToLocation()));

            document.add(new Paragraph("Departure: " +
                    booking.getBus().getDepartureTime().format(formatter)));

            document.add(new Paragraph("\nPassengers:"));

            for (Passenger p : booking.getPassengers()) {
                document.add(new Paragraph(
                        p.getName() + "  |  Seat: " + p.getSeatNumber()
                ));
            }

            document.add(new Paragraph("\n"));

            // ================= PRICE BREAKUP =================

            int seats = booking.getPassengers() == null ? 0 : booking.getPassengers().size();
            double pricePerSeat = booking.getBus().getPrice();

            double subtotal = pricePerSeat * seats;
            double gst = subtotal * GST_RATE;
            double total = subtotal + gst;

            document.add(new Paragraph("Fare Details").setBold());

            document.add(new Paragraph("Seats: " + seats));
            document.add(new Paragraph("Price per Seat: ₹" + pricePerSeat));
            document.add(new Paragraph("Subtotal: ₹" + String.format("%.2f", subtotal)));
            document.add(new Paragraph("GST (5%): ₹" + String.format("%.2f", gst)));
            document.add(new Paragraph("Total Amount Paid: ₹" + String.format("%.2f", total))
                    .setBold());

            document.add(new Paragraph("\n"));

            // ================= QR CODE =================

            String qrContent = "Sarathi Booking#" + booking.getId() +
                    "|Route:" + booking.getBus().getFromLocation() + "-" + booking.getBus().getToLocation() +
                    "|Seats:" +
                    booking.getPassengers()
                            .stream()
                            .map(Passenger::getSeatNumber)
                            .collect(Collectors.joining(","));

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            var bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 160, 160);

            ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG", qrOut);

            Image qrImage = new Image(ImageDataFactory.create(qrOut.toByteArray()));
            qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(new Paragraph("Scan for Verification")
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(qrImage);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating ticket", e);
        }
    }
}
