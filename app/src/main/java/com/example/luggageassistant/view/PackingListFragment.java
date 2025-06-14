package com.example.luggageassistant.view;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.example.luggageassistant.R;
import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.model.PersonPackingList;

import java.util.List;
import java.util.Map;

public class PackingListFragment extends Fragment {
    private static final String ARG_PERSON = "person_data";
    private PersonPackingList personData;

    public static PackingListFragment newInstance(PersonPackingList person) {
        PackingListFragment fragment = new PackingListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PERSON, person);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            personData = (PersonPackingList) getArguments().getSerializable(ARG_PERSON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_packing_list, container, false);
        LinearLayout listContainer = view.findViewById(R.id.listContainer);

        TextView title = new TextView(getContext());
        title.setText("Packing list for " + personData.getPersonName());
        title.setTextSize(20f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(getResources().getColor(R.color.linkColor));
        listContainer.addView(title);

        Map<String, List<PackingItem>> categorizedItems = personData.getCategorizedItems();

        for (String category : categorizedItems.keySet()) {
            TextView categoryTitle = new TextView(getContext());
            categoryTitle.setText("\n" + category);
            categoryTitle.setTextSize(16f);
            categoryTitle.setTypeface(null, Typeface.BOLD);
            categoryTitle.setTextColor(getResources().getColor(R.color.onBackground));
            listContainer.addView(categoryTitle);

            for (PackingItem item : categorizedItems.get(category)) {
                CheckBox checkBox = new CheckBox(getContext());
                checkBox.setText(item.getItem() + " (" + item.getQuantity() + ")");
                checkBox.setButtonTintList(ContextCompat.getColorStateList(requireContext(), R.color.linkColor));

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    item.setChecked(isChecked);
                });

                listContainer.addView(checkBox);
            }
        }

        return view;
    }
}

