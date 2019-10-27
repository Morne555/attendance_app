package com.itsp.attendance.ui;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.itsp.attendance.ui.home.Subject;
import com.squareup.picasso.Target;

import java.util.List;

public class Data
{
    public String studentNumber;
    public String studentName;
    public int attendedTotal;
    public int classTotal;
    public Target imageTarget;
    public List<Subject> subjectList;
}
