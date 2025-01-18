package nl.pkock.brewhub_backend.repositories;

import nl.pkock.brewhub_backend.models.User;
import nl.pkock.brewhub_backend.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRolesContaining(UserRole role);
}