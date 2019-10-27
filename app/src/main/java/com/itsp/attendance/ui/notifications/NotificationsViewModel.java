package com.itsp.attendance.ui.notifications;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itsp.attendance.database.DatabaseClient;
import com.itsp.attendance.database.Notification;

import java.util.List;

public class NotificationsViewModel extends ViewModel
{
    private MutableLiveData<List<Notification>> data;
    private final static String TAG = NotificationsViewModel.class.getName();
    public Activity activity;
    public Context context;

    LiveData<List<Notification>> getData()
    {
        if (data == null)
        {
            data = new MutableLiveData<>();
        }

        loadData();
        return data;
    }

    void loadData()
    {
        class GetNotifications extends AsyncTask<Void, Void, List<Notification>>
        {

            @Override
            protected List<Notification> doInBackground(Void... voids)
            {
                Log.d(TAG, "GetNotification onPostExecute: Reading notifications from DB.");
                List<Notification> notifications = DatabaseClient
                        .getInstance(activity.getApplicationContext())
                        .getAppDatabase()
                        .notificationDao()
                        .getAll();
                return notifications;
            }

            @Override
            protected void onPostExecute(List<Notification> notifications)
            {
                super.onPostExecute(notifications);

                Log.d(TAG, "GetNotification onPostExecute: Completed reading notifications.");
                data.postValue(notifications);
            }
        }

        GetNotifications gn = new GetNotifications();
        gn.execute();
    }
}