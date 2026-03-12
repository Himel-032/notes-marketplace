package com.notes.notesmarketplace.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String uploadPdf(MultipartFile file);

    String uploadImage(byte[] imageBytes);

    void deletePdf(String pdfUrl);

}
