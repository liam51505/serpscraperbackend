package com.liamcashman.serpscrapper.dto;

import java.time.LocalDateTime;

public class ResultDto {
    private Integer organicPosition;
    private Integer mapsPosition;
    private LocalDateTime timestamp;

    // Constructors
    public ResultDto() {}

    public ResultDto(Integer organicPosition, Integer mapsPosition, LocalDateTime timestamp) {
        this.organicPosition = organicPosition;
        this.mapsPosition = mapsPosition;
        this.timestamp = timestamp;
    }

    // Getters and Setters
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
