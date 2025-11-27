package com.techup.travel_app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.bucket:trips-photo}")
    private String bucket;

    @Value("${supabase.apiKey}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Upload a file to Supabase Storage and return public URL
     * @param file The file to upload
     * @return The public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file) {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin";
        String fileName = System.currentTimeMillis() + "_" + original;
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, fileName);

        System.out.println("Uploading file to: " + uploadUrl);
        System.out.println("Using bucket: " + bucket);
        System.out.println("File size: " + file.getSize() + " bytes");
        System.out.println("Content type: " + file.getContentType());

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            System.err.println("Cannot read file bytes: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read file bytes", e);
        }

        try {
            webClient.put()
                .uri(uploadUrl)
                .header("Authorization", "Bearer " + apiKey)     // Service Role Key
                .header("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .header("x-upsert", "true") // Allow overwriting files
                .bodyValue(bytes)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res ->
                    res.bodyToMono(String.class).defaultIfEmpty("Upload failed").flatMap(msg -> {
                        System.err.println("Supabase upload error: " + msg);
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Supabase upload failed: " + msg));
                    })
                )
                .toBodilessEntity()
                .block();

            // Return public URL for immediate access
            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucket, fileName);
            System.out.println("Upload successful! Public URL: " + publicUrl);
            return publicUrl;

        } catch (ResponseStatusException ex) {
            System.err.println("ResponseStatusException: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.err.println("Unexpected error during upload: " + ex.getMessage());
            ex.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected error while uploading to Supabase", ex);
        }
    }

    /**
     * Delete a file from Supabase Storage
     * @param fileUrl The public URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            
            // Build delete URL
            String deleteUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, filename);
            
            webClient.delete()
                .uri(deleteUrl)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .toBodilessEntity()
                .block();
                
            System.out.println("File deleted successfully: " + filename);
            
        } catch (Exception e) {
            System.err.println("Failed to delete file: " + e.getMessage());
        }
    }
}
