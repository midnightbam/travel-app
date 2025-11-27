package com.techup.travel_app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;
    
    @Column(name = "tags", columnDefinition = "TEXT") 
    private String tags;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "province")
    private String province;
    
    @Column(name = "location_link", columnDefinition = "TEXT")
    private String locationLink;
    
    @Column
    private Double latitude;
    
    @Column
    private Double longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper method to get the first photo as cover image
    public String getCoverImage() {
        String[] photoArray = parseArrayString(photos);
        return photoArray.length > 0 ? photoArray[0] : null;
    }
    
    // Helper method to get province/region (now uses direct field)
    public String getProvince() {
        if (province != null && !province.trim().isEmpty()) {
            return province;
        }
        
        // Fallback: try to extract from tags for legacy data
        String[] tagArray = parseArrayString(tags);
        if (tagArray.length == 0) return null;
        
        // Look for province-like tags (usually the last tag in Thai locations)
        for (String tag : tagArray) {
            // Thai provinces or countries
            if (tag.matches(".*(?:จังหวัด|ตราด|ชลบุรี|กาญจนบุรี|เชียงใหม่|สตูล|กรุงเทพมหานคร|ไต้หวัน|ญี่ปุ่น|ฝรั่งเศส|ฟินแลนด์).*")) {
                return tag;
            }
        }
        
        // Fallback to last tag
        return tagArray[tagArray.length - 1];
    }
    
    // Helper method to parse PostgreSQL array string format like {item1,item2,item3}
    private String[] parseArrayString(String arrayStr) {
        if (arrayStr == null || arrayStr.trim().isEmpty()) {
            return new String[0];
        }
        
        // Remove curly braces and split by comma
        String cleanStr = arrayStr.trim();
        if (cleanStr.startsWith("{") && cleanStr.endsWith("}")) {
            cleanStr = cleanStr.substring(1, cleanStr.length() - 1);
        }
        
        if (cleanStr.isEmpty()) {
            return new String[0];
        }
        
        return cleanStr.split(",");
    }
    
    // Helper method to get short description (max 120 characters)
    public String getShortDescription() {
        if (description == null) return null;
        if (description.length() <= 120) return description;
        
        String truncated = description.substring(0, 120);
        int lastSpace = truncated.lastIndexOf(' ');
        
        if (lastSpace > 100) {
            return truncated.substring(0, lastSpace) + "...";
        }
        
        return truncated + "...";
    }
    
    // Utility methods for array handling (PostgreSQL array strings)
    public String[] getPhotosArray() {
        return parseArrayString(photos);
    }
    
    // Helper method for String input (convert to PostgreSQL array string)
    public void setPhotos(String photosString) {
        this.photos = photosString;
    }
    
    // Standard array setter (convert array to PostgreSQL string format)
    public void setPhotos(String[] photosArray) {
        this.photos = photosArray != null && photosArray.length > 0 
            ? "{" + String.join(",", photosArray) + "}"
            : "{}";
    }
    
    public String[] getTagsArray() {
        return parseArrayString(tags);
    }
    
    // Helper method for String input (convert to PostgreSQL array string)
    public void setTags(String tagsString) {
        this.tags = tagsString;
    }
    
    // Standard array setter (convert array to PostgreSQL string format)
    public void setTags(String[] tagsArray) {
        this.tags = tagsArray != null && tagsArray.length > 0 
            ? "{" + String.join(",", tagsArray) + "}"
            : "{}";
    }
}