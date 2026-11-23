package com.devtoolbox.DevToolBoxRevamp.common;


import com.devtoolbox.DevToolBoxRevamp.events.event.ReceiptData;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ReceiptPdfGenerator {

    private static final Color NAVY   = new Color(0x0B, 0x12, 0x20);
    private static final Color CYAN   = new Color(0x06, 0xB6, 0xD4);
    private static final Color INK    = new Color(0x1E, 0x29, 0x3B);
    private static final Color MUTED  = new Color(0x64, 0x74, 0x8B);
    private static final Color LINE   = new Color(0xE2, 0xE8, 0xF0);
    private static final Color SOFT   = new Color(0xF1, 0xF5, 0xF9);
    private static final Color GREEN  = new Color(0x05, 0x96, 0x69);

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a").withZone(ZoneId.of("Asia/Kolkata"));

    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
    private static final Font VALUE = FontFactory.getFont(FontFactory.HELVETICA, 10.5f, INK);
    private static final Font VALUE_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f, INK);

    public byte[] generate(ReceiptData d) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 0, 0, 0, 36);   // header band goes edge to edge
            PdfWriter.getInstance(doc, out);
            doc.addTitle("DevToolBox Receipt " + d.orderId());
            doc.addAuthor("DevToolBox");
            doc.open();

            doc.add(headerBand());

            // Body is a table with side margins, because the page itself has 0 margin
            PdfPTable body = new PdfPTable(1);
            body.setWidthPercentage(100);
            PdfPCell wrap = new PdfPCell();
            wrap.setBorder(Rectangle.NO_BORDER);
            wrap.setPaddingLeft(40);
            wrap.setPaddingRight(40);
            wrap.setPaddingTop(26);

            wrap.addElement(statusRow(d));
            wrap.addElement(spacer(18));
            wrap.addElement(partiesTable(d));
            wrap.addElement(spacer(22));
            wrap.addElement(itemsTable(d));
            wrap.addElement(spacer(6));
            wrap.addElement(totalTable(d));
            wrap.addElement(spacer(26));
            wrap.addElement(referenceTable(d));
            wrap.addElement(spacer(30));
            wrap.addElement(footerNote(d));

            body.addCell(wrap);
            doc.add(body);

            doc.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Could not generate receipt PDF", e);
        }
    }

    // ---------- header: navy band, logo on the left, title on the right ----------
    private PdfPTable headerBand() throws IOException, DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3f, 2f});
        t.setWidthPercentage(100);

        PdfPCell left = new PdfPCell();
        left.setBackgroundColor(NAVY);
        left.setBorder(Rectangle.NO_BORDER);
        left.setPaddingLeft(40);
        left.setPaddingTop(26);
        left.setPaddingBottom(26);
        left.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logo = loadLogo();
        if (logo != null) {
            logo.scaleToFit(190, 40);
            left.addElement(new Chunk(logo, 0, 0, true));
        } else {
            left.addElement(new Phrase("DevToolBox",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE)));
        }
        t.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBackgroundColor(NAVY);
        right.setBorder(Rectangle.NO_BORDER);
        right.setPaddingRight(40);
        right.setPaddingTop(26);
        right.setPaddingBottom(26);
        right.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph title = new Paragraph("PAYMENT RECEIPT",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, CYAN));
        title.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(title);
        t.addCell(right);

        // thin cyan accent line under the band
        PdfPCell accent = new PdfPCell(new Phrase(" ", FontFactory.getFont(FontFactory.HELVETICA, 2)));
        accent.setColspan(2);
        accent.setBackgroundColor(CYAN);
        accent.setBorder(Rectangle.NO_BORDER);
        accent.setFixedHeight(3);
        t.addCell(accent);
        return t;
    }

    // Logo comes from src/main/resources/receipt/logo.png (copied into the jar)
    private Image loadLogo() {
        try (InputStream in = new ClassPathResource("receipt/logo.png").getInputStream()) {
            return Image.getInstance(in.readAllBytes());
        } catch (Exception e) {
            return null;   // receipt still works with a text title if the logo is missing
        }
    }

    // ---------- "PAID" badge + test mode label ----------
    private PdfPTable statusRow(ReceiptData d) {
        PdfPTable t = new PdfPTable(new float[]{1f, 2f});
        t.setWidthPercentage(100);

        // A Chunk with a background colour gives a compact pill-style badge
        Chunk paid = new Chunk("  PAID  ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, GREEN));
        paid.setBackground(new Color(0xD1, 0xFA, 0xE5), 2, 3, 2, 3);
        PdfPCell l = new PdfPCell(new Phrase(paid));
        l.setBorder(Rectangle.NO_BORDER);
        l.setPadding(0);
        t.addCell(l);

        Paragraph mode = new Paragraph(d.testMode() ? "TEST MODE - no real money charged" : "",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(0xB4, 0x53, 0x09)));
        mode.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell r = new PdfPCell(mode);
        r.setBorder(Rectangle.NO_BORDER);
        r.setPadding(0);
        r.setHorizontalAlignment(Element.ALIGN_RIGHT);
        r.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(r);
        return t;
    }

    // ---------- billed to / receipt info ----------
    private PdfPTable partiesTable(ReceiptData d) {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.addCell(infoBlock("BILLED TO", d.customerEmail()));
        t.addCell(infoBlock("DATE OF PAYMENT", DATE_FMT.format(d.paidAt())));
        return t;
    }

    private PdfPCell infoBlock(String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        c.addElement(new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, MUTED)));
        Paragraph v = new Paragraph(value, VALUE_BOLD);
        v.setSpacingBefore(3);
        c.addElement(v);
        return c;
    }

    // ---------- line item ----------
    private PdfPTable itemsTable(ReceiptData d) {
        PdfPTable t = new PdfPTable(new float[]{4f, 1.4f, 1.6f});
        t.setWidthPercentage(100);

        t.addCell(headCell("DESCRIPTION", Element.ALIGN_LEFT));
        t.addCell(headCell("DURATION", Element.ALIGN_CENTER));
        t.addCell(headCell("AMOUNT", Element.ALIGN_RIGHT));

        PdfPCell desc = bodyCell(Element.ALIGN_LEFT);
        desc.addElement(new Paragraph("DevToolBox " + d.packageName() + " plan", VALUE_BOLD));
        Paragraph sub = new Paragraph("Valid until " + DATE_FMT.format(d.validUntil()), LABEL);
        sub.setSpacingBefore(2);
        desc.addElement(sub);
        t.addCell(desc);

        t.addCell(textCell(d.durationDays() + " days", Element.ALIGN_CENTER, VALUE));
        t.addCell(textCell(inr(d.amount()), Element.ALIGN_RIGHT, VALUE));
        return t;
    }

    private PdfPTable totalTable(ReceiptData d) {
        PdfPTable t = new PdfPTable(new float[]{3f, 2f});
        t.setWidthPercentage(100);

        PdfPCell blank = new PdfPCell(new Phrase(" "));
        blank.setBorder(Rectangle.NO_BORDER);
        t.addCell(blank);

        PdfPCell total = new PdfPCell();
        total.setBackgroundColor(SOFT);
        total.setBorder(Rectangle.NO_BORDER);
        total.setPadding(10);
        PdfPTable inner = new PdfPTable(new float[]{1f, 1f});
        inner.setWidthPercentage(100);
        PdfPCell a = new PdfPCell(new Phrase("Total paid", VALUE_BOLD));
        a.setBorder(Rectangle.NO_BORDER);
        PdfPCell b = new PdfPCell(new Phrase(inr(d.amount()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, NAVY)));
        b.setBorder(Rectangle.NO_BORDER);
        b.setHorizontalAlignment(Element.ALIGN_RIGHT);
        inner.addCell(a);
        inner.addCell(b);
        total.addElement(inner);
        t.addCell(total);
        return t;
    }

    // ---------- order id / payment id ----------
    private PdfPTable referenceTable(ReceiptData d) {
        PdfPTable t = new PdfPTable(new float[]{1.3f, 4f});
        t.setWidthPercentage(100);
        refRow(t, "Order ID", d.orderId());
        refRow(t, "Payment ID", d.paymentId());
        refRow(t, "Payment status", "Paid");
        refRow(t, "Currency", "INR");
        return t;
    }

    private void refRow(PdfPTable t, String label, String value) {
        PdfPCell l = new PdfPCell(new Phrase(label, LABEL));
        l.setBorder(Rectangle.BOTTOM);
        l.setBorderColor(LINE);
        l.setPaddingTop(7);
        l.setPaddingBottom(7);
        t.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value, VALUE));
        v.setBorder(Rectangle.BOTTOM);
        v.setBorderColor(LINE);
        v.setPaddingTop(7);
        v.setPaddingBottom(7);
        t.addCell(v);
    }

    private Paragraph footerNote(ReceiptData d) {
        String text = "This is a computer-generated receipt and does not need a signature."
                + (d.testMode() ? " This payment was made in Razorpay test mode, so no real money was charged." : "")
                + " Questions? Reply to the email this receipt came with.";
        Paragraph p = new Paragraph(text, FontFactory.getFont(FontFactory.HELVETICA, 8.5f, MUTED));
        p.setLeading(12);
        return p;
    }

    // ---------- small helpers ----------
    private PdfPCell headCell(String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, MUTED)));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(NAVY);
        c.setBorderWidth(1f);
        c.setHorizontalAlignment(align);
        c.setPaddingBottom(7);
        return c;
    }

    private PdfPCell bodyCell(int align) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(LINE);
        c.setHorizontalAlignment(align);
        c.setPaddingTop(12);
        c.setPaddingBottom(12);
        return c;
    }

    private PdfPCell textCell(String text, int align, Font font) {
        PdfPCell c = bodyCell(align);
        c.setPhrase(new Phrase(text, font));
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(height);
        return p;
    }

    private String inr(BigDecimal amount) {
        // Built-in PDF fonts have no rupee glyph, so use "INR" instead of the symbol
        return "INR " + amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
