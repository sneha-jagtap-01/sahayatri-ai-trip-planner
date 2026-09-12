package com.sahayatri.sahayatribackend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class SavedTripController {

    private final TripRepository tripRepository;

    public SavedTripController(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    // Save a new trip
    @PostMapping("/save")
    public ResponseEntity<?> saveTrip(@RequestBody Trip trip) {

        Trip savedTrip = tripRepository.save(trip);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Trip saved successfully!",
                        "id", savedTrip.getId()
                )
        );
    }

    // Get all trips of a user
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Trip>> getUserTrips(
            @PathVariable String email) {

        return ResponseEntity.ok(
                tripRepository.findByEmail(email)
        );
    }

    // Get one specific trip by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTripById(
            @PathVariable Long id) {

        return tripRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a trip
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(
            @PathVariable Long id) {

        if (!tripRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        tripRepository.deleteById(id);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Trip deleted successfully!"
                )
        );
    }
}