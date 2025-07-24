package com.liamcashman.serpscrapper.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "keyword_id")
    @JsonManagedReference
    private Keyword keyword;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    @JsonManagedReference
    private Domain domain;

    @Column
    private Integer organicPosition;

    @Column
    private Integer mapsPosition;

    private LocalDateTime timestamp;

    // Constructors
    public Result() {}

    public Result(Keyword keyword, Domain domain, Integer organicPosition, Integer mapsPosition, LocalDateTime timestamp) {
        this.keyword = keyword;
        this.domain = domain;
        this.organicPosition = organicPosition;
        this.mapsPosition = mapsPosition;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Integer getOrganicPosition() {
        return organicPosition;
    }

    public void setOrganicPosition(Integer organicPosition) {
        this.organicPosition = organicPosition;
    }

    public Integer getMapsPosition() {
        return mapsPosition;
    }

    public void setMapsPosition(Integer mapsPosition) {
        this.mapsPosition = mapsPosition;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
