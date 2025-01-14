package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRetailerId(Long retailerId);
    Optional<Rating> findByRetailerIdAndCustomerId(Long retailerId, Long customerId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.retailer.id = :retailerId")
    Optional<Double> findAverageRatingByRetailerId(Long retailerId);
}