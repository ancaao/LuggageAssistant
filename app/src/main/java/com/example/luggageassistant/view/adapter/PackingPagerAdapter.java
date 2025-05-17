package com.example.luggageassistant.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.luggageassistant.model.PersonPackingList;
import com.example.luggageassistant.view.PackingListFragment;

import java.util.List;

public class PackingPagerAdapter extends FragmentStateAdapter {
    private List<PersonPackingList> people;

    public PackingPagerAdapter(FragmentActivity activity, List<PersonPackingList> people) {
        super(activity);
        this.people = people;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return PackingListFragment.newInstance(people.get(position));
    }

    @Override
    public int getItemCount() {
        return people.size();
    }
}