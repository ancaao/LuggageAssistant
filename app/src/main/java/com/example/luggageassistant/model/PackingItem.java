package com.example.luggageassistant.model;

public class PackingItem {
    private String category;
    private String item;
    private int quantity;
    private boolean checked;

    public PackingItem(String category, String item, int quantity) {
        this.category = category;
        this.item = item;
        this.quantity = quantity;
        this.checked = false;
    }

    public String getCategory() {
        return category;
    }

    public String getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
