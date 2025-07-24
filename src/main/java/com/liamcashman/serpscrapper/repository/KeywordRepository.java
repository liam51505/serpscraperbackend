package com.liamcashman.serpscrapper.repository;

import com.liamcashman.serpscrapper.model.Keyword;
import com.liamcashman.serpscrapper.model.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByKeyword(String keyword);
    List<Keyword> findByDomain(Domain domain);
}
