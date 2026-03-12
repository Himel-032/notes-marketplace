package com.notes.notesmarketplace.service;

import org.springframework.web.multipart.MultipartFile;

public interface PdfPreviewService {

    byte[] generateFirstPagePreview(MultipartFile pdfFile);

}
