package com.example.luggageassistant.model;

public class PackingListEntry {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String category;
    private PackingItem item;

    public PackingListEntry(int type, String category, PackingItem item) {
        this.type = type;
        this.category = category;
        this.item = item;
    }

    public int getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public PackingItem getItem() {
        return item;
    }
}
