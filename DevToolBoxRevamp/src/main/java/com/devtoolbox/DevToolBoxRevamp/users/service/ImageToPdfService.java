package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.common.ToolsUsageLogger;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.FileValidationException;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.ImageToPdfRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.PageMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageToPdfService {
    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "png", "gif", "bmp");
    @Value("${tools.image-to-pdf.max-file-bytes:10485760}")
    private long maxFileBytes;
    @Value("${tools.image-to-pdf.max-total-bytes:31457280}")
    private long maxTotalBytes;
    @Value("${tools.image-to-pdf.max-pixels:25000000}")
    private long maxPixels;
    private final ToolsUsageLogger toolsUsageLogger;
    private record ImageInfo(int width, int height) {}

    public ResponseEntity<byte[]> convert(CurrentUserPrincipal user,
                          ImageToPdfRequestDto dto) {
        Integer toolId = dto.getToolId();
        Integer subToolId = dto.getSubToolId();
        List<MultipartFile> files = dto.getFiles();
        validateBatch(files);

        byte[] pdf;
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            for (int i = 0; i < files.size(); i++) {
                addPage(document, files.get(i), i + 1, dto.getPageMode());
            }
            document.save(out);
            pdf = out.toByteArray();

        } catch (IOException e) {
            log.error("Image to PDF failed for user {}", user.userId, e);
            throw new IllegalStateException("Could not create the PDF", e);
        }

        ToolUsage saved = toolsUsageLogger.logUsage(user.userId, toolId, subToolId);
        if (saved == null) {
            log.error("ToolUsage could not be saved for user {}", user.userId);
            throw new IllegalStateException("ToolUsage could not be saved for user " + user.userId);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("images.pdf").build().toString())
                .header("X-Page-Count", String.valueOf(dto.getFiles().size()))
                .body(pdf);
    }

    private void validateBatch(List<MultipartFile> files) {
        long total = 0;
        for (int i = 0; i < files.size(); i++) {
            MultipartFile f = files.get(i);
            if (f == null || f.isEmpty()) {
                throw new FileValidationException("Image " + (i + 1) + " is empty");
            }
            if (f.getSize() > maxFileBytes) {
                throw new FileValidationException(
                        "Image " + (i + 1) + " must be under " + (maxFileBytes / 1024 / 1024) + "MB");
            }
            total += f.getSize();
        }
        if (total > maxTotalBytes) {
            throw new FileValidationException(
                    "Total upload must be under " + (maxTotalBytes / 1024 / 1024) + "MB");
        }
    }

    private void addPage(PDDocument doc, MultipartFile file, int number, PageMode mode) throws IOException {
        byte[] bytes = file.getBytes();                 // ek time pe ek hi image heap me
        ImageInfo info = inspect(bytes, number);        // decode se PEHLE size/type check

        PDImageXObject image = PDImageXObject.createFromByteArray(doc, bytes, "image-" + number);

        float w = info.width();
        float h = info.height();

        PDRectangle pageSize;
        float scale = 1f;
        if (mode == PageMode.A4) {
            PDRectangle a4 = PDRectangle.A4;
            // landscape image ho to landscape page
            pageSize = w > h ? new PDRectangle(a4.getHeight(), a4.getWidth()) : a4;
            scale = Math.min(pageSize.getWidth() / w, pageSize.getHeight() / h);
        } else {
            pageSize = new PDRectangle(w, h);
        }

        float drawW = w * scale;
        float drawH = h * scale;
        float x = (pageSize.getWidth() - drawW) / 2;
        float y = (pageSize.getHeight() - drawH) / 2;

        PDPage page = new PDPage(pageSize);
        doc.addPage(page);
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            cs.drawImage(image, x, y, drawW, drawH);
        }
    }

    /** Image ko poora decode kiye bina uska type aur dimensions padhta hai. */
    private ImageInfo inspect(byte[] bytes, int number) throws IOException {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (in == null) {
                throw new FileValidationException("Image " + number + " is not a valid image");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw new FileValidationException("Image " + number + " is not a supported image (use JPG, PNG, GIF or BMP)");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!ALLOWED_FORMATS.contains(format)) {
                    throw new FileValidationException("Image " + number + " has unsupported format: " + format);
                }
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                if ((long) w * h > maxPixels) {
                    throw new FileValidationException("Image " + number + " resolution is too large");
                }
                return new ImageInfo(w, h);
            } finally {
                reader.dispose();
            }
        }
    }
}