package com.notes.notesmarketplace.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.notes.notesmarketplace.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    @SuppressWarnings("unchecked")
    public String uploadPdf(MultipartFile file) {

        try {

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", "notes_pdf"
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (Exception e) {

            throw new RuntimeException("Failed to upload PDF: " + e.getMessage());

        }
    }

    @Override
    public void deletePdf(String pdfUrl) {
        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/{cloud_name}/raw/upload/v{version}/{folder}/{public_id}.pdf
            String publicId = extractPublicIdFromUrl(pdfUrl);

            if (publicId != null) {
                cloudinary.uploader().destroy(publicId,
                        ObjectUtils.asMap("resource_type", "raw"));
            }
        } catch (Exception e) {
            // Log error but don't throw exception to allow database deletion
            System.err.println("Failed to delete PDF from Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // Extract the public_id from Cloudinary URL
            // Example: https://res.cloudinary.com/xxx/raw/upload/v123456789/notes_pdf/filename.pdf
            // We need: notes_pdf/filename

            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = url.substring(uploadIndex + 8); // +8 for "/upload/"

            // Skip version number (v123456789/)
            int firstSlash = afterUpload.indexOf('/');
            if (firstSlash == -1) {
                return null;
            }
            
            String pathWithExtension = afterUpload.substring(firstSlash + 1);

            // Remove file extension
            int lastDot = pathWithExtension.lastIndexOf('.');
            if (lastDot != -1) {
                return pathWithExtension.substring(0, lastDot);
            }

            return pathWithExtension;
        } catch (Exception e) {
            System.err.println("Failed to extract public_id: " + e.getMessage());
            return null;
        }
    }
}
