package com.liamcashman.serpscrapper.dto;

import java.util.List;

public class KeywordDto {
    private Long id;
    private String keyword;
    private String location;
    private ResultDto latest;

    public KeywordDto() {}

    public KeywordDto(Long id, String keyword, String location, ResultDto latest) {
        this.id = id;
        this.keyword = keyword;
        this.location = location;
        this.latest = latest;
    }

    // Getters and Setters
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

    public ResultDto getLatest() {
        return latest;
    }

    public void setLatest(ResultDto latest) {
        this.latest = latest;
    }
}
