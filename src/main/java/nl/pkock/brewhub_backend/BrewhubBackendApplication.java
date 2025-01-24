package nl.pkock.brewhub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan({
        "nl.pkock.brewhub_backend.auth.models",
        "nl.pkock.brewhub_backend.community.models",
        "nl.pkock.brewhub_backend.inventory.models",
        "nl.pkock.brewhub_backend.order.models",
        "nl.pkock.brewhub_backend.rating.model",
        "nl.pkock.brewhub_backend.guide.models",
        "nl.pkock.brewhub_backend.recipe.models"
})
@EnableJpaRepositories({
        "nl.pkock.brewhub_backend.auth.repository",
        "nl.pkock.brewhub_backend.community.repositories",
        "nl.pkock.brewhub_backend.inventory.repository",
        "nl.pkock.brewhub_backend.order.repository",
        "nl.pkock.brewhub_backend.rating.repository",
        "nl.pkock.brewhub_backend.guide.repository",
        "nl.pkock.brewhub_backend.recipe.repository"
})
public class BrewhubBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrewhubBackendApplication.class, args);
    }
}