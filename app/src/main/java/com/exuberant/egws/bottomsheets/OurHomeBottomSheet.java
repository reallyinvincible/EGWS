package com.exuberant.egws.bottomsheets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.exuberant.egws.R;
import com.exuberant.egws.activities.LoginActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;
import static maes.tech.intentanim.CustomIntent.customType;

public class OurHomeBottomSheet extends BottomSheetDialogFragment {

    private MaterialButton logoutButton;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_our_home, container, false);
        logoutButton = view.findViewById(R.id.btn_logout);
        sharedPreferences = getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        return view;
    }

    void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        customType(getActivity(), "fadein-to-fadeout");
        getActivity().finishAfterTransition();
    }

}
