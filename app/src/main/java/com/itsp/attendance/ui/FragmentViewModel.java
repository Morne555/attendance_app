package com.itsp.attendance.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.itsp.attendance.ui.Data;
import com.itsp.attendance.ui.dashboard.DashboardFragment;

public class FragmentViewModel extends ViewModel
{

    public static MutableLiveData<Data> data;

    public LiveData<Data> getData()
    {
        if(data == null)
        {
            data = new MutableLiveData<>();
        }

        DashboardFragment.oldLevel = 0;
        return data;
    }

    public static void updateData(Data newData)
    {
        data.postValue(newData);
    }
}