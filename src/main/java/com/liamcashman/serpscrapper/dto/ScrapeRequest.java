package com.liamcashman.serpscrapper.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ScrapeRequest {
    private String keyword;
    private String domain;
    private String businessName;
    private String location;

    // Default constructor
    public ScrapeRequest() {}

    // Getters and setters
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getBusinessName() {
        return businessName;
    }
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}
