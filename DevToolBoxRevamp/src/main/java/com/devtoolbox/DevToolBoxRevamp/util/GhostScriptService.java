package com.devtoolbox.DevToolBoxRevamp.util;

import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.CompressionLevels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GhostScriptService {

    @Value("${app.ghostscript.executable:gs}")
    private String gsExecutable;

    @Value("${app.ghostscript.timeout-minutes:5}")
    private long timeoutMinutes;

    private final Semaphore permits;

    public GhostScriptService(@Value("${app.ghostscript.max-concurrent:2}") int maxConcurrent) {
        this.permits = new Semaphore(maxConcurrent);
    }

    public byte[] compressedPdf(MultipartFile file, CompressionLevels level) {
        Path inputPath = null;
        Path outputPath = null;
        Path logPath = null;

        try {
            inputPath = Files.createTempFile("original-", ".pdf");
            outputPath = Files.createTempFile("compressed-", ".pdf");
            logPath = Files.createTempFile("ghostscript-", ".log");

            file.transferTo(inputPath);

            List<String> command = List.of(gsExecutable,
                    "-dSAFER",
                    "-sDEVICE=pdfwrite",
                    "-dCompatibilityLevel=1.4",
                    "-dPDFSETTINGS=" + level.getCompressedLevel(),
                    "-dNOPAUSE", "-dBATCH", "-dQUIET",
                    "-sOutputFile=" + outputPath,
                    inputPath.toString());
            log.info("Ghostscript executable: {}", gsExecutable);
            log.info("Input PDF size: {} MB",
                    Files.size(inputPath) / (1024.0 * 1024.0));
            log.info("Compression level: {}", level);
            runGhostscript(command, logPath);

            long originalSize = Files.size(inputPath);
            long compressedSize = Files.size(outputPath);

            if (compressedSize == 0) {
                throw new IllegalStateException("Ghostscript produced an empty file");
            }
            log.info("Original PDF size: {} MB",
                    originalSize / (1024.0 * 1024));

            log.info("Ghostscript output size: {} MB",
                    compressedSize / (1024.0 * 1024));
            if (compressedSize >= originalSize) {
                log.info("Compression gave no benefit ({} -> {}), returning original", originalSize, compressedSize);
                return Files.readAllBytes(inputPath);
            }

            log.info("PDF compressed {} -> {} bytes", originalSize, compressedSize);
            return Files.readAllBytes(outputPath);

        } catch (IOException e) {
            throw new IllegalStateException("PDF compression failed", e);
        } finally {
            deleteQuietly(inputPath);
            deleteQuietly(outputPath);
            deleteQuietly(logPath);
        }
    }

    private void runGhostscript(List<String> command, Path logPath) throws IOException {
        try {
            permits.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for compression slot", e);
        }

        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .redirectOutput(logPath.toFile())
                    .start();

            if (!process.waitFor(timeoutMinutes, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                log.error("Ghostscript timed out after {} min", timeoutMinutes);
                throw new IllegalStateException("Compression timed out");
            }

            if (process.exitValue() != 0) {
                String gsLog = Files.readString(logPath, StandardCharsets.UTF_8);
                log.error("Ghostscript exited with code {}: {}", process.exitValue(), gsLog);
                throw new IllegalStateException("Compression failed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Compression interrupted", e);
        } finally {
            permits.release();
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Could not delete temp file {}", path, e);
        }
    }
}