package com.techup.travel_app.controller;

import com.techup.travel_app.entity.Trip;
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
        tripMap.put("duration", trip.getDuration());
        tripMap.put("price", trip.getPrice());
        tripMap.put("rating", trip.getRating());
        tripMap.put("createdAt", trip.getCreatedAt());
        
        // For now, set a default author name since we're using existing Supabase data
        tripMap.put("authorName", "Travel Blogger");
        
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
        
        // For now, set a default author since we're using existing Supabase data
        Map<String, Object> author = new HashMap<>();
        author.put("id", 1L);
        author.put("displayName", "Travel Blogger");
        author.put("email", "blogger@example.com");
        tripMap.put("author", author);
        
        return tripMap;
    }
}
