package com.example.luggageassistant.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.luggageassistant.model.PackingItem;
import com.example.luggageassistant.view.FinalPersonPackingListFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FinalPackingPagerAdapter extends FragmentStateAdapter {

    private final List<String> personNames;
    private final Map<String, List<PackingItem>> personToItems;

    public FinalPackingPagerAdapter(@NonNull Fragment fragment,
                                    Map<String, List<PackingItem>> personToItems) {
        super(fragment);
        this.personNames = new ArrayList<>(personToItems.keySet());
        this.personToItems = personToItems;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String name = personNames.get(position);
        List<PackingItem> items = personToItems.get(name);
        return FinalPersonPackingListFragment.newInstance(name, items);
    }

    @Override
    public int getItemCount() {
        return personNames.size();
    }

    public String getPersonName(int position) {
        return personNames.get(position);
    }
}

