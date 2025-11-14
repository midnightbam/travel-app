package com.techup.travel_app.repository;

import com.techup.travel_app.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    
    // Find trips ordered by creation date (newest first)
    List<Trip> findAllByOrderByCreatedAtDesc();
    
    // Paginated trips
    Page<Trip> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Search for PostgreSQL with string handling
    @Query("SELECT t FROM Trip t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.tags) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Trip> searchByTitleOrTags(@Param("query") String query);
    
    // Find trips by author
    List<Trip> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}