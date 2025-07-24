package com.liamcashman.serpscrapper.dto;

import java.util.List;

public class ClientDto {
    private Long id;
    private String url;
    private List<KeywordDto> keywords;

    public ClientDto() {}

    public ClientDto(Long id, String url, List<KeywordDto> keywords) {
        this.id = id;
        this.url = url;
        this.keywords = keywords;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<KeywordDto> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeywordDto> keywords) {
        this.keywords = keywords;
    }
}
