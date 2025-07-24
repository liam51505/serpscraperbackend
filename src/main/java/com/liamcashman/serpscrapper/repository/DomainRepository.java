package com.liamcashman.serpscrapper.repository;

import com.liamcashman.serpscrapper.model.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByUrl(String url);
}
