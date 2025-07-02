package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.PackingItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    public void savePackingItem(String userId, String tripId, String personName, PackingItem item, Runnable onSuccess) {
        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("category", item.getCategory());
        itemMap.put("item", item.getItem());
        itemMap.put("quantity", item.getQuantity());
        itemMap.put("checked", item.isChecked());

        // ✅ Asigură-te că documentul persoanei apare în Firestore (cu un câmp minim)
        Map<String, Object> personDocMap = new HashMap<>();
        personDocMap.put("name", personName);

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("packingLists")
                .document(personName)
                .set(personDocMap, SetOptions.merge()); // folosim merge pentru a păstra subcolecția 'items'

        // ✅ Salvează efectiv itemul în subcolecția 'items'
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("packingLists")
                .document(personName)
                .collection("items")
                .document(itemId)
                .set(itemMap)
                .addOnSuccessListener(unused -> {
                    Log.d("FIREBASE", "Packing item saved for: " + personName + " - " + item.getItem());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to save packing item", e));
    }

    public void getCheckedItems(String userId, String tripId, OnItemsLoadedListener callback) {
        List<PackingItem> allItems = new ArrayList<>();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("packingLists")
                .get()
                .addOnSuccessListener(persons -> {
                    Log.d("FIREBASE", "Found " + persons.size() + " people in packingLists");
                    int totalPersons = persons.size();
                    if (totalPersons == 0) {
                        callback.onItemsLoaded(allItems);
                        return;
                    }

                    final int[] finishedCount = {0};

                    for (QueryDocumentSnapshot personDoc : persons) {
                        String personName = personDoc.getId();

                        personDoc.getReference().collection("items")
                                .whereEqualTo("checked", true)
                                .get()
                                .addOnSuccessListener(itemSnap -> {
                                    Log.d("FIREBASE", "Found " + itemSnap.size() + " items for " + personDoc.getId());
                                    for (QueryDocumentSnapshot doc : itemSnap) {
                                        PackingItem item = doc.toObject(PackingItem.class);
                                        item.setPersonName(personName);
                                        allItems.add(item);
                                    }

                                    finishedCount[0]++;
                                    if (finishedCount[0] == totalPersons) {
                                        callback.onItemsLoaded(allItems);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FIREBASE", "Failed to get items", e);
                                    finishedCount[0]++;
                                    if (finishedCount[0] == totalPersons) {
                                        callback.onItemsLoaded(allItems);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to get packingLists", e);
                    callback.onError(e);
                });
    }

    public void saveFinalPackingItem(String userId, String tripId, String personName, PackingItem item, Runnable onSuccess) {
        item.setPersonName(personName);

        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("category", item.getCategory());
        itemMap.put("item", item.getItem());
        itemMap.put("quantity", item.getQuantity());
        itemMap.put("checked", false); // toate pornesc nebifate
        itemMap.put("personName", item.getPersonName()); // salvăm și în Firebase

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .document(personName)
                .set(Collections.singletonMap("name", personName), SetOptions.merge());

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .document(personName)
                .collection("items")
                .document(itemId)
                .set(itemMap)
                .addOnSuccessListener(unused -> {
                    Log.d("SAVE_PROCESS", "Final item saved for: " + personName + " - " + item.getItem());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to save final item", e));

        Log.d("SAVE_PROCESS", " firebase Saving to: users/" + userId + "/trips/" + tripId + "/finalLists/" + personName + "/items/" + itemId);
        Log.d("SAVE_PROCESS", "→ " + item.getItem() + " | checked: " + item.isChecked() + " | category: " + item.getCategory());

    }

    public void getFinalPackingItems(String userId, String tripId, OnItemsLoadedListener callback) {
        List<PackingItem> allItems = new ArrayList<>();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .get()
                .addOnSuccessListener(persons -> {
                    int totalPersons = persons.size();
                    if (totalPersons == 0) {
                        callback.onItemsLoaded(allItems);
                        return;
                    }

                    final int[] finishedCount = {0};

                    for (QueryDocumentSnapshot personDoc : persons) {
                        String personName = personDoc.getId();

                        personDoc.getReference().collection("items")
                                .get()
                                .addOnSuccessListener(itemSnap -> {
                                    for (QueryDocumentSnapshot doc : itemSnap) {
                                        PackingItem item = doc.toObject(PackingItem.class);
                                        item.setPersonName(personName); // asociem persoana
                                        allItems.add(item);
                                    }

                                    finishedCount[0]++;
                                    if (finishedCount[0] == totalPersons) {
                                        callback.onItemsLoaded(allItems);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FIREBASE", "Failed to get final items", e);
                                    finishedCount[0]++;
                                    if (finishedCount[0] == totalPersons) {
                                        callback.onItemsLoaded(allItems);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Failed to get finalLists", e);
                    callback.onError(e);
                });
    }

    public void updateFinalItemChecked(String userId, String tripId, String personName, PackingItem item, boolean isChecked) {
        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .document(personName)
                .collection("items")
                .document(itemId)
                .update("checked", isChecked)
                .addOnSuccessListener(unused ->
                        Log.d("FIREBASE", "Checked updated: " + item.getItem() + " = " + isChecked)
                )
                .addOnFailureListener(e ->
                        Log.e("FIREBASE", "Failed to update checked for: " + item.getItem(), e)
                );
    }

    public void deleteFinalPackingItem(String userId, String tripId, String personName, PackingItem item, Runnable onSuccess) {
        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .document(personName)
                .collection("items")
                .document(itemId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d("FIREBASE", "Item deleted: " + item.getItem());
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to delete item", e));
    }

}

