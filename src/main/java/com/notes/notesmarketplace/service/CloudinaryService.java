package com.notes.notesmarketplace.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String uploadPdf(MultipartFile file);

    void deletePdf(String pdfUrl);

}
