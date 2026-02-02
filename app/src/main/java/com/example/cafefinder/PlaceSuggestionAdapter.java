package com.example.cafefinder;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlaceSuggestionAdapter extends ArrayAdapter<PlaceSuggestion> {

    private final List<PlaceSuggestion> items = new ArrayList<>();

    public PlaceSuggestionAdapter(Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
    }

    public void setItems(List<PlaceSuggestion> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);

        clear();
        addAll(items);
        notifyDataSetChanged();
    }

    public PlaceSuggestion getSuggestion(int position) {
        return items.get(position);
    }
}