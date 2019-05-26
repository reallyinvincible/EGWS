package com.exuberant.egws.fragments;

import android.os.Bundle;
import android.renderscript.Long4;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.exuberant.egws.R;

public class PlaceholderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        LottieAnimationView lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setSpeed(0.75f);
        return view;
    }
}
