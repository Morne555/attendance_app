package com.itsp.attendance.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.itsp.attendance.R;
import com.itsp.attendance.database.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment
{
    private NotificationsViewModel notificationsViewModel;

    NotificationAdapter notificationAdapter;
    RecyclerView notifciationRecycler;
    List<Notification> notifications;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        notificationsViewModel =
                ViewModelProviders.of(getActivity()).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationsViewModel.context = getContext();
        notificationsViewModel.activity = getActivity();

        notifications = new ArrayList<>(); // empty subjects list for initial view

        notificationAdapter = new NotificationAdapter(notifications);

        notifciationRecycler = root.findViewById(R.id.notification_recycler);
        notifciationRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        notifciationRecycler.setItemAnimator(new DefaultItemAnimator());
        notifciationRecycler.setAdapter(notificationAdapter);

        notificationsViewModel.getData().observe(this, new Observer<List<Notification>>()
        {
            @Override
            public void onChanged(@Nullable List<Notification> newData)
            {
                notificationAdapter.updateData(newData);
                notificationAdapter.notifyDataSetChanged();
            }
        });

        return root;
    }
}