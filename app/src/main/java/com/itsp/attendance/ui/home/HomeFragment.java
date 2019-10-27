package com.itsp.attendance.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itsp.attendance.R;
import com.itsp.attendance.ui.Data;
import com.itsp.attendance.ui.FragmentViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment
{
    List<Subject> subjects;
    HomeAdapter homeAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        FragmentViewModel fragmentViewModel =
                ViewModelProviders.of(getActivity()).get(FragmentViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        subjects = new ArrayList<>(); // empty subjects list for initial view

        homeAdapter = new HomeAdapter(subjects);

        RecyclerView homeRecycler = root.findViewById(R.id.home_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());
        homeRecycler.setAdapter(homeAdapter);

        fragmentViewModel.getData().observe(this, new Observer<Data>()
        {
            @Override
            public void onChanged(@Nullable Data newData)
            {
                homeAdapter.updateData(newData.subjectList);
                homeAdapter.notifyDataSetChanged();
            }
        });
        return root;
    }
}