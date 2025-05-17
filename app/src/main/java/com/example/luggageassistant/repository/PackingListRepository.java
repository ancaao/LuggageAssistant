package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.PackingItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PackingListRepository {

    private static PackingListRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PackingListRepository() {}

    public static synchronized PackingListRepository getInstance() {
        if (instance == null) {
            instance = new PackingListRepository();
        }
        return instance;
    }

    public void savePackingItem(String userId, String tripId, String personName, PackingItem item) {
        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("category", item.getCategory());
        itemMap.put("item", item.getItem());
        itemMap.put("quantity", item.getQuantity());
        itemMap.put("checked", item.isChecked());

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("packingLists")
                .document(personName)
                .collection("items")
                .document(itemId)
                .set(itemMap)
                .addOnSuccessListener(unused -> Log.d("FIREBASE", "Packing item saved"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to save packing item", e));
    }
}

