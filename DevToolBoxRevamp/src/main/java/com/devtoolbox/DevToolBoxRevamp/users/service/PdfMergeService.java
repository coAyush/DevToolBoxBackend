package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.common.ToolsUsageLogger;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.FileValidationException;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.ServiceBusyException;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.PdfMergeResult;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PdfMergeService {

    private static final byte[] PDF_MAGIC = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final ToolsUsageLogger toolsUsageLogger;
    private final long maxFileBytes;
    private final long maxTotalBytes;
    private final int maxTotalPages;
    private final Semaphore permits;

    public PdfMergeService(ToolsUsageLogger toolsUsageLogger,
                           @Value("${tools.pdf-merge.max-file-bytes:10485760}") long maxFileBytes,
                           @Value("${tools.pdf-merge.max-total-bytes:31457280}") long maxTotalBytes,
                           @Value("${tools.pdf-merge.max-total-pages:500}") int maxTotalPages,
                           @Value("${tools.pdf-merge.max-concurrent:2}") int maxConcurrent) {
        this.toolsUsageLogger = toolsUsageLogger;
        this.maxFileBytes = maxFileBytes;
        this.maxTotalBytes = maxTotalBytes;
        this.maxTotalPages = maxTotalPages;
        this.permits = new Semaphore(maxConcurrent);
    }

    public ResponseEntity<byte[]> merge(CurrentUserPrincipal user, Integer toolId, Integer subToolId,
                                List<MultipartFile> files) {

        validateBatch(files);

        PdfMergeResult result = mergeAll(files, user);

        ToolUsage saved = toolsUsageLogger.logUsage(user.userId, toolId, subToolId);
        if (saved == null) {
            log.error("ToolUsage could not be saved for user {}", user.userId);
            throw new IllegalStateException("ToolUsage could not be saved for user " + user.userId);
        }
         return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("merged.pdf").build().toString())
                .header("X-Page-Count", String.valueOf(result.pageCount()))
                .body(result.pdf());
    }

    private PdfMergeResult mergeAll(List<MultipartFile> files, CurrentUserPrincipal user) {
        try {
            if (!permits.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new ServiceBusyException("Server is busy, please try again shortly");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a merge slot", e);
        }
        List<PDDocument> sources = new ArrayList<>();
        try (PDDocument destination = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDFMergerUtility merger = new PDFMergerUtility();
            int totalPages = 0;

            for (int i = 0; i < files.size(); i++) {
                PDDocument source = openPdf(files.get(i), i + 1);
                sources.add(source);

                totalPages += source.getNumberOfPages();
                if (totalPages > maxTotalPages) {
                    throw new FileValidationException(
                            "Combined PDF cannot exceed " + maxTotalPages + " pages");
                }
                merger.appendDocument(destination, source);
            }

            destination.save(out);
            return new PdfMergeResult(out.toByteArray(), totalPages);

        } catch (IOException e) {
            log.error("PDF merge failed for user {}", user.userId, e);
            throw new IllegalStateException("Could not merge the PDFs", e);
        } finally {
            sources.forEach(this::closeQuietly);
            permits.release();
        }
    }
    private PDDocument openPdf(MultipartFile file, int number) {
        try (InputStream in = file.getInputStream()) {
            PDDocument doc = Loader.loadPDF(new RandomAccessReadBuffer(in));
            if (doc.getNumberOfPages() == 0) {
                doc.close();
                throw new FileValidationException("PDF " + number + " has no pages");
            }
            return doc;
        } catch (InvalidPasswordException e) {
            throw new FileValidationException(
                    "PDF " + number + " is password-protected. Please remove the password first");
        } catch (IOException e) {
            log.warn("Could not open uploaded PDF {}: {}", number, e.getMessage());
            throw new FileValidationException("PDF " + number + " is corrupted or not a valid PDF");
        }
    }

    private void validateBatch(List<MultipartFile> files) {
        long total = 0;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            int number = i + 1;

            if (f == null || f.isEmpty()) {
                throw new FileValidationException("PDF " + number + " is empty");
            }
            if (f.getSize() > maxFileBytes) {
                throw new FileValidationException(
                        "PDF " + number + " must be under " + (maxFileBytes / 1024 / 1024) + "MB");
            }
            total += f.getSize();

            try (InputStream in = f.getInputStream()) {
                if (!Arrays.equals(in.readNBytes(PDF_MAGIC.length), PDF_MAGIC)) {
                    throw new FileValidationException("File " + number + " is not a PDF");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not read the uploaded file", e);
            }
        }
        if (total > maxTotalBytes) {
            throw new FileValidationException(
                    "Total upload must be under " + (maxTotalBytes / 1024 / 1024) + "MB");
        }
    }

    private void closeQuietly(PDDocument doc) {
        try {
            doc.close();
        } catch (IOException e) {
            log.warn("Could not close PDF document: {}", e.getMessage());
        }
    }
}