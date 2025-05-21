package com.example.luggageassistant.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.view.adapter.FinalPackingListAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalPersonPackingListFragment extends Fragment {

    private static final String ARG_ITEMS = "arg_items";
    private static final String ARG_NAME = "arg_name";

    private List<PackingItem> items;
    private String personName;

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
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Map<String, List<PackingItem>> categorized = new LinkedHashMap<>();
        for (PackingItem item : items) {
            String category = item.getCategory();
            if (!categorized.containsKey(category)) {
                categorized.put(category, new ArrayList<>());
            }
            categorized.get(category).add(item);
        }

        FinalPackingListAdapter adapter = new FinalPackingListAdapter(categorized);
        recyclerView.setAdapter(adapter);

        return recyclerView;
    }
}
