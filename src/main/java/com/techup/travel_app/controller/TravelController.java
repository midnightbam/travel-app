package com.techup.travel_app.controller;

import com.techup.travel_app.entity.Trip;
import com.techup.travel_app.entity.User;
import com.techup.travel_app.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TravelController {

    @Autowired
    private TripRepository tripRepository;

    @GetMapping("/trips")
    public ResponseEntity<Map<String, Object>> getAllTrips(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        try {
            List<Trip> trips;
            
            if (query != null && !query.trim().isEmpty()) {
                // Search functionality
                trips = tripRepository.searchByTitleOrTags(query.trim());
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
            
            // Convert trips to response format
            List<Map<String, Object>> tripList = trips.stream()
                    .map(this::convertTripToMap)
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
        tripMap.put("duration", trip.getDuration());
        tripMap.put("price", trip.getPrice());
        tripMap.put("rating", trip.getRating());
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
        tripMap.put("duration", trip.getDuration());
        tripMap.put("price", trip.getPrice());
        tripMap.put("rating", trip.getRating());
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
            if (tripData.get("duration") != null) {
                trip.setDuration((String) tripData.get("duration"));
            }
            if (tripData.get("price") != null) {
                trip.setPrice(Double.parseDouble(tripData.get("price").toString()));
            }
            if (tripData.get("rating") != null) {
                trip.setRating(Double.parseDouble(tripData.get("rating").toString()));
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
            if (tripData.containsKey("duration")) {
                trip.setDuration((String) tripData.get("duration"));
            }
            if (tripData.containsKey("price")) {
                trip.setPrice(Double.parseDouble(tripData.get("price").toString()));
            }
            if (tripData.containsKey("rating")) {
                trip.setRating(Double.parseDouble(tripData.get("rating").toString()));
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
            return null;
        }
        
        String token = authHeader.substring(7);
        
        // Demo implementation: extract user ID from token format "jwt-token-userId-timestamp"
        // or "demo-jwt-token-timestamp" for demo user (ID = 1)
        if (token.startsWith("demo-jwt-token")) {
            return 1L;
        }
        
        // For real JWT tokens, parse the token and extract user ID
        // In production, use JWT library to decode and validate token
        try {
            String[] parts = token.split("-");
            if (parts.length >= 3) {
                // Try to parse user ID from token format
                return Long.parseLong(parts[2]);
            }
        } catch (Exception e) {
            // Invalid token format
        }
        
        return null;
    }
}
