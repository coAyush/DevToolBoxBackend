package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.common.ToolsUsageLogger;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.FileValidationException;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.ServiceBusyException;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.WordToPdfResult;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WordToPdfService {

    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};
    private static final byte[] OLE_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};

    private final ToolsUsageLogger toolsUsageLogger;
    private final long maxFileBytes;
    private final Semaphore permits;

    public WordToPdfService(ToolsUsageLogger toolsUsageLogger,
                            @Value("${tools.word-to-pdf.max-file-bytes:10485760}") long maxFileBytes,
                            @Value("${tools.word-to-pdf.max-concurrent:2}") int maxConcurrent) {
        this.toolsUsageLogger = toolsUsageLogger;
        this.maxFileBytes = maxFileBytes;
        this.permits = new Semaphore(maxConcurrent);
    }

    public ResponseEntity<byte[]> convert(CurrentUserPrincipal user, Integer toolId, Integer subToolId,
                                  MultipartFile file) {

        validate(file);

        byte[] pdf = convertToPdf(file, user);

        ToolUsage saved = toolsUsageLogger.logUsage(user.userId, toolId, subToolId);
        if (saved == null) {
            log.error("ToolUsage could not be saved for user {}", user.userId);
            throw new IllegalStateException("ToolUsage could not be saved for user " + user.userId);
        }

        WordToPdfResult result= new WordToPdfResult(pdf, buildPdfName(file.getOriginalFilename()));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(result.fileName(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(result.pdf());
    }

    private byte[] convertToPdf(MultipartFile file, CurrentUserPrincipal user) {
        try {
            if (!permits.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new ServiceBusyException("Server is busy, please try again shortly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a conversion slot", e);
        }

        try (XWPFDocument docx = openDocx(file);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfConverter.getInstance().convert(docx, out, PdfOptions.create());
            return out.toByteArray();

        } catch (IOException | RuntimeException e) {
            if (e instanceof FileValidationException fve) throw fve;
            log.error("Word to PDF failed for user {}", user.userId, e);
            throw new IllegalStateException("Could not convert the document", e);
        } finally {
            permits.release();
        }
    }

    private XWPFDocument openDocx(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return new XWPFDocument(in);
        } catch (IOException | RuntimeException e) {
            log.warn("Could not open uploaded docx: {}", e.getMessage());
            throw new FileValidationException("The file is corrupted or is not a valid .docx document");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
        if (file.getSize() > maxFileBytes) {
            throw new FileValidationException("File must be under " + (maxFileBytes / 1024 / 1024) + "MB");
        }

        byte[] header;
        try (InputStream in = file.getInputStream()) {
            header = in.readNBytes(4);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the uploaded file", e);
        }

        if (startsWith(header, OLE_MAGIC)) {
            throw new FileValidationException(
                    "Old .doc or password-protected files are not supported. Please upload an unprotected .docx");
        }
        if (!startsWith(header, ZIP_MAGIC)) {
            throw new FileValidationException("Not a valid .docx file");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        return data.length >= prefix.length && Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }

    /** report.docx -> report.pdf (path aur special characters hata ke) */
    private String buildPdfName(String original) {
        String base = original == null ? "" : original.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        base = base.replaceAll("[^\\p{L}\\p{N}_\\- ]", "_").trim();
        if (base.length() > 80) base = base.substring(0, 80);
        if (base.isBlank()) base = "document";
        return base + ".pdf";
    }
}