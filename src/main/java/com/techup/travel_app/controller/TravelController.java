package com.techup.travel_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techup.travel_app.entity.Trip;
import com.techup.travel_app.entity.User;
import com.techup.travel_app.repository.TripRepository;
import com.techup.travel_app.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TravelController {

    @Autowired
    private TripRepository tripRepository;
    
    @Autowired
    private SupabaseStorageService storageService;

    @GetMapping("/trips")
    public ResponseEntity<Map<String, Object>> getAllTrips(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            List<Trip> trips;
            
            if (query != null && !query.trim().isEmpty()) {
                // Search functionality with proper UTF-8 handling for Thai text
                String decodedQuery = java.net.URLDecoder.decode(query.trim(), "UTF-8");
                System.out.println("Search query: " + decodedQuery); // Debug log
                trips = tripRepository.searchByTitleOrTags(decodedQuery);
                System.out.println("Found " + trips.size() + " trips for query: " + decodedQuery); // Debug log
            } else {
                // Get all trips
                if (page == 0 && size == 10) {
                    // Default - return all without pagination for simplicity
                    trips = tripRepository.findAllByOrderByCreatedAtDesc();
                } else {
                    // Paginated
                    Pageable pageable = PageRequest.of(page, size);
                    Page<Trip> tripPage = tripRepository.findAllByOrderByCreatedAtDesc(pageable);
                    trips = tripPage.getContent();
                }
            }
            
            // Convert trips to response format - handle province column gracefully
            List<Map<String, Object>> tripList = trips.stream()
                    .map(trip -> {
                        try {
                            return convertTripToMap(trip);
                        } catch (Exception e) {
                            // Handle database column issues gracefully
                            System.err.println("Error converting trip " + trip.getId() + ": " + e.getMessage());
                            Map<String, Object> fallbackMap = new HashMap<>();
                            fallbackMap.put("id", trip.getId());
                            fallbackMap.put("title", trip.getTitle());
                            fallbackMap.put("description", trip.getShortDescription());
                            fallbackMap.put("coverImage", trip.getCoverImage());
                            fallbackMap.put("province", "Unknown"); // Fallback value
                            fallbackMap.put("tags", trip.getTagsArray());
                            fallbackMap.put("createdAt", trip.getCreatedAt());
                            return fallbackMap;
                        }
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("trips", tripList);
            response.put("total", tripList.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch trips");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/trips/{id}")
    public ResponseEntity<Map<String, Object>> getTripById(@PathVariable Long id) {
        try {
            Trip trip = tripRepository.findById(id).orElse(null);
            
            if (trip == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(convertTripToDetailMap(trip));
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch trip");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private Map<String, Object> convertTripToMap(Trip trip) {
        Map<String, Object> tripMap = new HashMap<>();
        tripMap.put("id", trip.getId());
        tripMap.put("title", trip.getTitle());
        tripMap.put("description", trip.getShortDescription());
        tripMap.put("coverImage", trip.getCoverImage());
        tripMap.put("province", trip.getProvince());
        tripMap.put("tags", trip.getTagsArray());
        tripMap.put("photos", trip.getPhotosArray()); // Add full photos array for editing
        tripMap.put("location", trip.getLocation()); // Add location field for editing
        tripMap.put("locationLink", trip.getLocationLink()); // Add location link for editing
        tripMap.put("createdAt", trip.getCreatedAt());
        tripMap.put("updatedAt", trip.getUpdatedAt());
        
        // Include author_id for ownership checks
        if (trip.getAuthor() != null) {
            tripMap.put("authorId", trip.getAuthor().getId());
            tripMap.put("authorName", trip.getAuthor().getDisplayName());
        } else {
            // Legacy trips without author
            tripMap.put("authorId", null);
            tripMap.put("authorName", "Travel Blogger");
        }
        
        return tripMap;
    }
    
    private Map<String, Object> convertTripToDetailMap(Trip trip) {
        Map<String, Object> tripMap = new HashMap<>();
        tripMap.put("id", trip.getId());
        tripMap.put("title", trip.getTitle());
        tripMap.put("description", trip.getDescription()); // Full description
        tripMap.put("photos", trip.getPhotosArray());
        tripMap.put("tags", trip.getTagsArray());
        tripMap.put("latitude", trip.getLatitude());
        tripMap.put("longitude", trip.getLongitude());
        tripMap.put("location", trip.getLocation());
        tripMap.put("province", trip.getProvince());
        tripMap.put("locationLink", trip.getLocationLink());
        tripMap.put("createdAt", trip.getCreatedAt());
        tripMap.put("updatedAt", trip.getUpdatedAt());
        
        // Include actual author information for ownership checks
        Map<String, Object> author = new HashMap<>();
        if (trip.getAuthor() != null) {
            author.put("id", trip.getAuthor().getId());
            author.put("displayName", trip.getAuthor().getDisplayName());
            author.put("email", trip.getAuthor().getEmail());
        } else {
            // Legacy trips without author
            author.put("id", null);
            author.put("displayName", "Travel Blogger");
            author.put("email", "blogger@example.com");
        }
        tripMap.put("author", author);
        
        return tripMap;
    }
    
    // Get trips created by the current user
    @GetMapping("/trips/mine")
    public ResponseEntity<Map<String, Object>> getMyTrips(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Require authentication for accessing personal trips
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            // Return only user's trips
            List<Trip> trips = tripRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
            
            List<Map<String, Object>> tripList = trips.stream()
                    .map(this::convertTripToMap)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("trips", tripList);
            response.put("total", tripList.size());
            response.put("userId", userId); // Send userId to frontend for ownership checks
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch your trips");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Create a new trip
    @PostMapping("/trips")
    public ResponseEntity<Map<String, Object>> createTrip(
            @RequestBody Map<String, Object> tripData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Require authentication for creating trips
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Trip trip = new Trip();
            trip.setTitle((String) tripData.get("title"));
            trip.setDescription((String) tripData.get("description"));
            trip.setLocation((String) tripData.get("location"));
            if (tripData.containsKey("province")) {
                trip.setProvince((String) tripData.get("province"));
            }
            if (tripData.containsKey("locationLink")) {
                trip.setLocationLink((String) tripData.get("locationLink"));
            }
            
            // Set author if user is authenticated
            if (userId != null) {
                User author = new User();
                author.setId(userId);
                trip.setAuthor(author);
            }
            
            // Handle photos array
            if (tripData.get("photos") instanceof List) {
                List<String> photosList = (List<String>) tripData.get("photos");
                trip.setPhotos(String.join(",", photosList));
            }
            
            // Handle tags array
            if (tripData.get("tags") instanceof List) {
                List<String> tagsList = (List<String>) tripData.get("tags");
                trip.setTags(String.join(",", tagsList));
            }
            
            if (tripData.get("latitude") != null) {
                trip.setLatitude(Double.parseDouble(tripData.get("latitude").toString()));
            }
            if (tripData.get("longitude") != null) {
                trip.setLongitude(Double.parseDouble(tripData.get("longitude").toString()));
            }
            
            Trip savedTrip = tripRepository.save(trip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trip created successfully");
            response.put("trip", convertTripToDetailMap(savedTrip));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create trip");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Update an existing trip
    @PutMapping("/trips/{id}")
    public ResponseEntity<Map<String, Object>> updateTrip(
            @PathVariable Long id,
            @RequestBody Map<String, Object> tripData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Require authentication for updating trips
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Trip trip = tripRepository.findById(id).orElse(null);
            
            if (trip == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Trip not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // Check ownership
            if (userId != null && trip.getAuthor() != null && !trip.getAuthor().getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "You can only edit or delete your own trips.");
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            // Update fields
            if (tripData.containsKey("title")) {
                trip.setTitle((String) tripData.get("title"));
            }
            if (tripData.containsKey("description")) {
                trip.setDescription((String) tripData.get("description"));
            }
            if (tripData.containsKey("location")) {
                trip.setLocation((String) tripData.get("location"));
            }
            if (tripData.containsKey("province")) {
                trip.setProvince((String) tripData.get("province"));
            }
            if (tripData.containsKey("locationLink")) {
                trip.setLocationLink((String) tripData.get("locationLink"));
            }
            
            // Handle photos array
            if (tripData.containsKey("photos") && tripData.get("photos") instanceof List) {
                List<String> photosList = (List<String>) tripData.get("photos");
                trip.setPhotos(String.join(",", photosList));
            }
            
            // Handle tags array
            if (tripData.containsKey("tags") && tripData.get("tags") instanceof List) {
                List<String> tagsList = (List<String>) tripData.get("tags");
                trip.setTags(String.join(",", tagsList));
            }
            
            if (tripData.containsKey("latitude")) {
                trip.setLatitude(Double.parseDouble(tripData.get("latitude").toString()));
            }
            if (tripData.containsKey("longitude")) {
                trip.setLongitude(Double.parseDouble(tripData.get("longitude").toString()));
            }
            
            Trip updatedTrip = tripRepository.save(trip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trip updated successfully");
            response.put("trip", convertTripToDetailMap(updatedTrip));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update trip");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Delete a trip
    @DeleteMapping("/trips/{id}")
    public ResponseEntity<Map<String, Object>> deleteTrip(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            // Require authentication for deleting trips
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Trip trip = tripRepository.findById(id).orElse(null);
            
            if (trip == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Trip not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // Check ownership
            if (userId != null && trip.getAuthor() != null && !trip.getAuthor().getId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "You can only edit or delete your own trips.");
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            tripRepository.delete(trip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trip deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete trip");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // Helper method to extract user ID from JWT token
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("No valid auth header found");
            return null;
        }
        
        String token = authHeader.substring(7);
        System.out.println("Extracting user ID from token: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // Demo implementation: extract user ID from token format "jwt-token-userId-timestamp"
        // or "demo-jwt-token-timestamp" for demo user (ID = 1)
        if (token.startsWith("demo-jwt-token")) {
            System.out.println("Demo token detected, returning user ID: 1");
            return 1L;
        }
        
        // For real JWT tokens, parse the token and extract user ID
        // In production, use JWT library to decode and validate token
        try {
            String[] parts = token.split("-");
            if (parts.length >= 3) {
                // Try to parse user ID from token format
                Long userId = Long.parseLong(parts[2]);
                System.out.println("Extracted user ID from token: " + userId);
                return userId;
            }
        } catch (Exception e) {
            System.err.println("Failed to parse user ID from token: " + e.getMessage());
        }
        
        System.err.println("Could not extract user ID from token");
        return null;
    }
    
    // ==================== MULTIPART FILE UPLOAD ENDPOINTS ====================
    
    @PostMapping(value = "/trips/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> createTripWithFiles(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) String latitudeStr,
            @RequestParam(value = "longitude", required = false) String longitudeStr,
            @RequestParam(value = "locationLink", required = false) String locationLink,
            @RequestParam(value = "tags", required = false) String tagsStr,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Trip trip = new Trip();
            trip.setTitle(title);
            trip.setDescription(description);
            if (province != null && !province.isEmpty()) {
                trip.setProvince(province);
            }
            trip.setLocation(location);
            trip.setLocationLink(locationLink);
            
            // Parse coordinates
            if (latitudeStr != null && !latitudeStr.isEmpty()) {
                try {
                    trip.setLatitude(Double.parseDouble(latitudeStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid latitude: " + latitudeStr);
                }
            }
            if (longitudeStr != null && !longitudeStr.isEmpty()) {
                try {
                    trip.setLongitude(Double.parseDouble(longitudeStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid longitude: " + longitudeStr);
                }
            }
            
            // Set author
            User author = new User();
            author.setId(userId);
            trip.setAuthor(author);
            
            // Handle tags
            if (tagsStr != null && !tagsStr.isEmpty()) {
                trip.setTags(tagsStr);
            }
            
            // Upload photos to Supabase and get URLs
            List<String> photoUrls = new ArrayList<>();
            if (photos != null && photos.length > 0) {
                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) {
                        try {
                            String url = storageService.uploadFile(photo);
                            photoUrls.add(url);
                            System.out.println("Successfully uploaded photo: " + url);
                        } catch (Exception e) {
                            System.err.println("Failed to upload photo: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            if (!photoUrls.isEmpty()) {
                trip.setPhotos(String.join(",", photoUrls));
            } else {
                trip.setPhotos(""); // Set empty string instead of null
            }
            
            Trip savedTrip = tripRepository.save(trip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trip created successfully");
            response.put("trip", convertTripToDetailMap(savedTrip));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create trip");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PutMapping(value = "/trips/{id}/upload", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> updateTripWithFiles(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "province", required = false) String province,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "latitude", required = false) String latitudeStr,
            @RequestParam(value = "longitude", required = false) String longitudeStr,
            @RequestParam(value = "locationLink", required = false) String locationLink,
            @RequestParam(value = "tags", required = false) String tagsStr,
            @RequestParam(value = "existingPhotos", required = false) String existingPhotosStr,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            
            if (userId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please log in to continue.");
                return ResponseEntity.status(401).body(errorResponse);
            }
            
            Trip trip = tripRepository.findById(id).orElse(null);
            
            if (trip == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Trip not found");
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            // Check ownership - allow if no author or user owns the trip
            if (trip.getAuthor() != null && !trip.getAuthor().getId().equals(userId)) {
                System.err.println("Ownership check failed:");
                System.err.println("- Trip author ID: " + trip.getAuthor().getId());
                System.err.println("- Current user ID: " + userId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "You can only edit your own trips.");
                errorResponse.put("tripAuthorId", trip.getAuthor().getId());
                errorResponse.put("currentUserId", userId);
                return ResponseEntity.status(403).body(errorResponse);
            }
            
            System.out.println("Update allowed for trip " + id + " by user " + userId);
            
            // Update basic fields
            trip.setTitle(title);
            trip.setDescription(description);
            if (province != null && !province.isEmpty()) {
                trip.setProvince(province);
            }
            trip.setLocation(location);
            trip.setLocationLink(locationLink);
            
            // Parse and update coordinates
            if (latitudeStr != null && !latitudeStr.isEmpty()) {
                try {
                    trip.setLatitude(Double.parseDouble(latitudeStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid latitude: " + latitudeStr);
                }
            }
            if (longitudeStr != null && !longitudeStr.isEmpty()) {
                try {
                    trip.setLongitude(Double.parseDouble(longitudeStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid longitude: " + longitudeStr);
                }
            }
            
            // Update tags
            if (tagsStr != null) {
                trip.setTags(tagsStr);
            }
            
            // Handle photos
            List<String> photoUrls = new ArrayList<>();
            
            // Add existing photos
            if (existingPhotosStr != null && !existingPhotosStr.isEmpty()) {
                String[] existingPhotos = existingPhotosStr.split(",");
                for (String photo : existingPhotos) {
                    if (!photo.trim().isEmpty()) {
                        photoUrls.add(photo.trim());
                    }
                }
            }
            
            // Upload new photos
            if (photos != null && photos.length > 0) {
                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) {
                        try {
                            String url = storageService.uploadFile(photo);
                            photoUrls.add(url);
                            System.out.println("Successfully uploaded photo: " + url);
                        } catch (Exception e) {
                            System.err.println("Failed to upload photo: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            if (!photoUrls.isEmpty()) {
                trip.setPhotos(String.join(",", photoUrls));
            } else {
                trip.setPhotos(""); // Set empty string instead of null
            }
            
            Trip updatedTrip = tripRepository.save(trip);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trip updated successfully");
            response.put("trip", convertTripToDetailMap(updatedTrip));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update trip");
            errorResponse.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
