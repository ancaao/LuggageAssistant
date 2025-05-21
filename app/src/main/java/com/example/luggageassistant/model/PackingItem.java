package com.example.luggageassistant.model;

import java.io.Serializable;

public class PackingItem implements Serializable {
    private String category;
    private String item;
    private int quantity;
    private boolean checked;
    private String personName;

    public PackingItem() {}

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

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }
}
