package com.example.luggageassistant.view;

import static com.example.luggageassistant.view.adapter.FinalPackingListAdapter.categorizeItems;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.repository.PackingListRepository;
import com.example.luggageassistant.view.adapter.FinalPackingListAdapter;
import com.example.luggageassistant.viewmodel.FinalListViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinalPersonPackingListFragment extends Fragment {

    private static final String ARG_ITEMS = "arg_items";
    private static final String ARG_NAME = "arg_name";
    private List<PackingItem> items;
    private String personName;
    private FinalListViewModel viewModel;
    private FinalPackingListAdapter adapter;
    private List<PackingItem> currentItems = new ArrayList<>();
    private boolean deleteMode = false;
    private PackingItem recentlyDeletedItem;


    public static FinalPersonPackingListFragment newInstance(String personName, List<PackingItem> items) {
        FinalPersonPackingListFragment fragment = new FinalPersonPackingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, personName);
        args.putSerializable(ARG_ITEMS, new ArrayList<>(items));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        personName = getArguments().getString(ARG_NAME);
        items = (List<PackingItem>) getArguments().getSerializable(ARG_ITEMS);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final_person_packing_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFinalList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        MaterialButton btnAdd = view.findViewById(R.id.btnAddItem);
        MaterialButton btnDelete = view.findViewById(R.id.btnDeleteItem);
        MaterialButton btnShare = view.findViewById(R.id.btnShareList);

        btnAdd.setOnClickListener(v -> showAddItemDialog(personName));
        btnShare.setOnClickListener(v -> shareListAsText());
        btnDelete.setOnClickListener(v -> {
            deleteMode = !deleteMode;
            adapter.setDeleteMode(deleteMode);

            if (deleteMode) {
                btnDelete.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.error));
                btnDelete.setTextColor(ContextCompat.getColor(requireContext(), R.color.background));

            } else {
                btnDelete.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.background));
                btnDelete.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            }
        });


        adapter = new FinalPackingListAdapter(categorizeItems(items));

        adapter.setOnDeleteClickListener(itemToDelete -> {
            adapter.removeItem(itemToDelete);
            recentlyDeletedItem = itemToDelete;

            Snackbar.make(view, "Item deleted", Snackbar.LENGTH_LONG)
                    .setAnchorView(R.id.actionButtonsLayout)
                    .setAction("UNDO", v1 -> {
                        adapter.restoreItem(itemToDelete);
                        recentlyDeletedItem = null;
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != DISMISS_EVENT_ACTION && recentlyDeletedItem != null) {
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                String tripId = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                        .getString("current_trip_id", null);

                                PackingListRepository.getInstance().deleteFinalPackingItem(
                                        userId, tripId, personName, recentlyDeletedItem, () -> {
                                            viewModel.loadItems(userId, tripId);
                                            recentlyDeletedItem = null;
                                        });
                            }
                        }
                    })
                    .show();

        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(FinalListViewModel.class);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String tripId = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("current_trip_id", null);

        viewModel.loadItems(userId, tripId);

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            List<PackingItem> personItems = new ArrayList<>();
            for (PackingItem item : items) {
                if (item.getPersonName().equals(personName)) {
                    personItems.add(item);
                }
            }

            currentItems = personItems;
            adapter.updateData(FinalPackingListAdapter.categorizeItems(personItems));
        });
    }

    private void showAddItemDialog(String personName) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_packing_item, null);

        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        EditText customCategoryInput = dialogView.findViewById(R.id.customCategoryInput);
        EditText itemNameInput = dialogView.findViewById(R.id.itemNameInput);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);

        List<String> categoryList = new ArrayList<>();
        categoryList.add("Select Category");
        categoryList.addAll(Arrays.asList("Toiletries", "Clothing", "Medication", "Documents", "Electronics", "Special Accessories", "Special Preferences", "Other"));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setSelection(0);


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = categoryList.get(pos);
                customCategoryInput.setVisibility(selected.equals("Other") ? View.VISIBLE : View.GONE);

            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        quantityInput.setText("1");
        btnPlus.setOnClickListener(v -> {
            int val = Integer.parseInt(quantityInput.getText().toString());
            quantityInput.setText(String.valueOf(val + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int val = Integer.parseInt(quantityInput.getText().toString());
            if (val > 1) quantityInput.setText(String.valueOf(val - 1));
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String category = categorySpinner.getSelectedItem().toString();
            if (category.equals("Other")) category = customCategoryInput.getText().toString().trim();

            String itemName = itemNameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString().trim();
            int quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);

            if (!itemName.isEmpty() && !category.isEmpty() && !category.equals("Select Category")) {
                PackingItem newItem = new PackingItem(category, itemName, quantity);
                newItem.setPersonName(personName);

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String tripId = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .getString("current_trip_id", null);

                viewModel.addItem(userId, tripId, personName, newItem);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "Complete all fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    private void shareListAsText() {
        if (currentItems == null || currentItems.isEmpty()) {
            Toast.makeText(getContext(), "List is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append("Packing list for ").append(personName).append(":\n");

        for (PackingItem item : currentItems) {
            content.append("• ").append(item.getItem())
                    .append(" – ").append(item.getQuantity()).append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, content.toString());
        sendIntent.setType("text/plain");

        startActivity(Intent.createChooser(sendIntent, "Share packing list via"));
    }
}
