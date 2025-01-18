package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRetailerId(Long retailerId);
}