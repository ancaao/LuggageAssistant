package com.example.luggageassistant.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.view.adapter.FinalPackingListAdapter;
import com.example.luggageassistant.view.adapter.FinalPackingPagerAdapter;
import com.example.luggageassistant.viewmodel.FinalListViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinalPackingListFragment extends Fragment {

    private FinalListViewModel viewModel;
    private RecyclerView recyclerView;
    private FinalPackingListAdapter adapter;
    private String tripId;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    public static FinalPackingListFragment newInstance(String tripId) {
        FinalPackingListFragment fragment = new FinalPackingListFragment();
        Bundle args = new Bundle();
        args.putString("tripId", tripId);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final_packing_list, container, false);
        viewPager = view.findViewById(R.id.finalViewPager);
        tabLayout = view.findViewById(R.id.finalTabLayout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FinalListViewModel.class);

        if (getArguments() != null && getArguments().containsKey("tripId")) {
            tripId = getArguments().getString("tripId");

            SharedPreferences.Editor editor = requireContext()
                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit();
            editor.putString("current_trip_id", tripId);
            editor.apply();

        } else {
            tripId = requireContext()
                    .getSharedPreferences("app_prefs", getContext().MODE_PRIVATE)
                    .getString("current_trip_id", null);
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (tripId != null && !tripId.isEmpty()) {
            viewModel.loadItems(userId, tripId);
        } else {
            Toast.makeText(getContext(), "Missing trip ID", Toast.LENGTH_LONG).show();
        }

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            displayItems(items);
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        });
    }

    private void displayItems(List<PackingItem> items) {
        Map<String, List<PackingItem>> personToItems = new LinkedHashMap<>();

        for (PackingItem item : items) {
            String personName = item.getPersonName();
            if (!personToItems.containsKey(personName)) {
                personToItems.put(personName, new ArrayList<>());
            }
            personToItems.get(personName).add(item);
        }

        // → Aici folosești ViewPager cu câte o pagină pentru fiecare persoană
        FinalPackingPagerAdapter pagerAdapter = new FinalPackingPagerAdapter(this, personToItems);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(pagerAdapter.getPersonName(position))
        ).attach();
    }
    @Override
    public void onResume() {
        super.onResume();

        if (tripId == null || tripId.isEmpty()) {
            tripId = requireContext()
                    .getSharedPreferences("app_prefs", getContext().MODE_PRIVATE)
                    .getString("current_trip_id", null);
        }

        if (tripId != null && !tripId.isEmpty()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            viewModel.loadItems(userId, tripId); // 🔁 Reîncarcă lista din Firebase
        } else {
            Toast.makeText(getContext(), "Missing trip ID", Toast.LENGTH_LONG).show();
        }
    }
}
