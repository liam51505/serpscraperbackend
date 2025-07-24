package com.liamcashman.serpscrapper.repository;

import com.liamcashman.serpscrapper.model.Result;
import com.liamcashman.serpscrapper.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByKeyword(Keyword keyword);

    Optional<Result> findTopByKeywordOrderByTimestampDesc(Keyword keyword);
}
