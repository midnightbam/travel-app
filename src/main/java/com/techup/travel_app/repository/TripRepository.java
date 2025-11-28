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
    
    // Search for PostgreSQL with Unicode and multilingual support
    @Query("SELECT t FROM Trip t WHERE " +
           "UPPER(t.title) LIKE UPPER(CONCAT('%', :query, '%')) OR " +
           "UPPER(t.description) LIKE UPPER(CONCAT('%', :query, '%')) OR " +
           "UPPER(t.location) LIKE UPPER(CONCAT('%', :query, '%')) OR " +
           "UPPER(t.province) LIKE UPPER(CONCAT('%', :query, '%')) OR " +
           "UPPER(t.tags) LIKE UPPER(CONCAT('%', :query, '%'))")
    List<Trip> searchByTitleOrTags(@Param("query") String query);
    
    // Find trips by author
    List<Trip> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}