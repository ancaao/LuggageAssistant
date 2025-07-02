package com.example.luggageassistant.model;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class PersonPackingList implements Serializable {
    private String personName;
    private Map<String, List<PackingItem>> categorizedItems;

    public PersonPackingList(String personName, Map<String, List<PackingItem>> categorizedItems) {
        this.personName = personName;
        this.categorizedItems = categorizedItems;
    }

    public String getPersonName() {
        return personName;
    }


    public Map<String, List<PackingItem>> getCategorizedItems() {
        return categorizedItems;
    }
}
