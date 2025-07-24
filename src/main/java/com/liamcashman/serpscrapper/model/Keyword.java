package com.liamcashman.serpscrapper.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Keyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String location;

    private String keyword;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Result> results;

    @ManyToOne
    @JoinColumn(name = "domain_id")
    private Domain domain;

    // Constructors
    public Keyword() {
    }

    public Keyword(String keyword) {
        this.keyword = keyword;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

}
