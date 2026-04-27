package de.dhbw.uno.dto;

import java.util.Objects;

public class CardDataDTO {
    
    private String value;
    private String color;
    
    public CardDataDTO() {}
    
    public CardDataDTO(String value, String color) {
        this.value = value;
        this.color = color;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardDataDTO that = (CardDataDTO) o;
        return Objects.equals(value, that.value) && Objects.equals(color, that.color);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, color);
    }
    
    @Override
    public String toString() {
        return color + "_" + value;
    }
} 