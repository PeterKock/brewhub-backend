package nl.pkock.brewhub_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("nl.pkock.brewhub_backend.models")
@EnableJpaRepositories("nl.pkock.brewhub_backend.repositories")
public class BrewhubBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrewhubBackendApplication.class, args);
    }
}