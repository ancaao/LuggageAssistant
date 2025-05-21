package com.example.luggageassistant.repository;

import android.util.Log;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.TripConfiguration;
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
        // Asociază explicit numele persoanei cu itemul (foarte important)
        item.setPersonName(personName);

        // Creează un ID unic (folosim item + categorie)
        String itemId = item.getItem().replaceAll("\\s+", "_") + "_" + item.getCategory();

        // Convertește obiectul într-un Map pentru Firebase
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("category", item.getCategory());
        itemMap.put("item", item.getItem());
        itemMap.put("quantity", item.getQuantity());
        itemMap.put("checked", false); // toate pornesc nebifate
        itemMap.put("personName", item.getPersonName()); // salvăm și în Firebase

        // Ne asigurăm că documentul persoanei există (poți sări acest pas dacă nu vrei document gol)
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .collection("finalLists")
                .document(personName)
                .set(Collections.singletonMap("name", personName), SetOptions.merge());

        // Salvăm efectiv itemul
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

    public void getAllTripConfigurations(String userId, OnTripConfigurationsLoadedListener listener) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<TripConfiguration> tripList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        TripConfiguration config = doc.toObject(TripConfiguration.class);
                        // Salvează și tripId-ul dacă vrei (se obține cu doc.getId())
                        config.setTripId(doc.getId()); // adaugă un field `tripId` în model
                        tripList.add(config);
                    }
                    listener.onTripsLoaded(tripList);
                })
                .addOnFailureListener(listener::onError);
    }

    public void deleteTripConfiguration(String userId, String tripId, Runnable onSuccess) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d("TRIP_DELETE", "Trip " + tripId + " deleted.");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e("TRIP_DELETE", "Error deleting trip", e));
    }

    public void updateTripPinned(String userId, String tripId, boolean pinned, Runnable onSuccess) {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId)
                .update("pinned", pinned)
                .addOnSuccessListener(unused -> {
                    Log.d("PIN_TRIP", "Trip " + tripId + " updated with pinned=" + pinned);
                    onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Log.e("PIN_TRIP", "Failed to update pinned state for trip " + tripId, e)
                );
    }

}

