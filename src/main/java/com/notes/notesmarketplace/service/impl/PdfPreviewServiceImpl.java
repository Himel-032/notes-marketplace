package com.notes.notesmarketplace.service.impl;

import com.notes.notesmarketplace.service.PdfPreviewService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;

@Service
public class PdfPreviewServiceImpl implements PdfPreviewService {

    @Override
    public byte[] generateFirstPagePreview(MultipartFile pdfFile) {

        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {

            PDFRenderer renderer = new PDFRenderer(document);

            BufferedImage image = renderer.renderImageWithDPI(0, 200);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            ImageIO.write(image, "png", output);

            return output.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to generate preview");

        }
    }
} 
