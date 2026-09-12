package com.sahayatri.sahayatribackend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TripGroupRepository extends JpaRepository<TripGroup, Long> {

    Optional<TripGroup> findByTripId(Long tripId);
}