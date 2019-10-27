package com.itsp.attendance.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.itsp.attendance.R;
import com.itsp.attendance.ui.Data;
import com.itsp.attendance.ui.FragmentViewModel;
import com.squareup.picasso.Picasso;

public class DashboardFragment extends Fragment
{
    public static int oldLevel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        FragmentViewModel model =
                ViewModelProviders.of(getActivity()).get(FragmentViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        final TextView studentNameText = root.findViewById(R.id.dash_text_name);
        final TextView studentNumberText = root.findViewById(R.id.dash_text_studentNumber);
        final TextView attendedTotalText = root.findViewById(R.id.dash_text_attendedTotal);
        final TextView classTotalText = root.findViewById(R.id.dash_text_classTotal);
        final TextView missedTotalText = root.findViewById(R.id.dash_text_missedTotal);
        final TextView textRatingPercentage = root.findViewById(R.id.dash_text_ratingIndicator);
        final ImageView imageRating = root.findViewById(R.id.dash_image_rating);
        final ImageView imageFace = root.findViewById(R.id.dash_image_face);

        model.getData().observe(this, new Observer<Data>()
        {
            @Override
            public void onChanged(@Nullable Data newData)
            {
                studentNameText.setText(newData.studentName);
                studentNumberText.setText(newData.studentNumber);
                attendedTotalText.setText(Integer.toString(newData.attendedTotal));
                classTotalText.setText(Integer.toString(newData.classTotal));

                int missedTotal = newData.classTotal - newData.attendedTotal;
                missedTotalText.setText(Integer.toString(missedTotal));

                float ratingPercentage = (float) (newData.attendedTotal * 100) / (float) newData.classTotal;
                textRatingPercentage.setText((Math.round(ratingPercentage * 100.0f) / 100.0f) + "%");

                int level = (int) Math.ceil(ratingPercentage) * 100;

                RatingFillAnimation ratingAnimation = new RatingFillAnimation(imageRating, oldLevel, level);
                ratingAnimation.setDuration(1000);
                imageRating.startAnimation(ratingAnimation);

                Picasso.get().load("https://images.pexels.com/photos/614810/pexels-photo-614810.jpeg?crop=entropy&cs=srgb&dl=facial-hair-fine-looking-guy-614810.jpg&fit=crop&fm=jpg&h=546&w=640").into(imageFace);
            }

        });
        return root;
    }

    class RatingFillAnimation extends Animation
    {
        private ImageView rating;
        private float from;
        private float to;

        public RatingFillAnimation(ImageView rating, float from, float to)
        {
            super();
            this.rating = rating;
            this.from = from;
            this.to = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t)
        {
            super.applyTransformation(interpolatedTime, t);

            boolean fillDown;
            float fillAmount;
            if(to < from)
            {
                // Fill down
                fillAmount = from - to;
                fillDown = true;
            }
            else
            {
                // Fill up
                fillAmount = to - from;
                fillDown = false;
            }

            float value = (float) smoothStep(0, 1, ((fillAmount / 10000) * interpolatedTime)) * 10000;

            if(fillDown)
            {
                // Fill going down
                rating.setImageLevel((int) (from - value));
                oldLevel = (int) (from - value);
            }
            else
            {
                // Fill going up
                rating.setImageLevel((int) (from + value));
                oldLevel = (int) (from + value);
            }
        }

        public double clamp(double value, double min, double max)
        {
            return Math.max(min, Math.min(value, max));
        }

        public double smoothStep(double start, double end, double amount)
        {
            amount = clamp(amount, 0, 1);
            amount = clamp((amount - start) / (end - start), 0, 1);
            return amount * amount * (3 - 2 * amount);
        }
    }
}