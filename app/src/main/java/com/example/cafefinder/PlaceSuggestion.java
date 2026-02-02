package com.example.cafefinder;

public class PlaceSuggestion {
    public final String placeId;
    public final String fullText;

    public PlaceSuggestion(String placeId, String fullText) {
        this.placeId = placeId;
        this.fullText = fullText;
    }

    @Override
    public String toString() {
        // AutoCompleteTextView uses toString() to show text in dropdown
        return fullText;
    }
}

