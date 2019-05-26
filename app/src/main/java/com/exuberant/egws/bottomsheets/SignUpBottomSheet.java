package com.exuberant.egws.bottomsheets;

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
import com.google.android.material.textfield.TextInputLayout;

public class SignUpBottomSheet extends BottomSheetDialogFragment {

    private TextInputLayout nameTextInput, emailTextInput, passwordTextInput, confirmPasswordTextInput;
    private MaterialButton signUpButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sign_up, container, false);
        initialize(view);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextInput.getEditText().getText().toString();
                String email = emailTextInput.getEditText().getText().toString();
                String password = passwordTextInput.getEditText().getText().toString();
                String confirmPassword = confirmPasswordTextInput.getEditText().getText().toString();
                if (name == null || name.length() == 0) {
                    nameTextInput.setError("Name is required to proceed further");
                } else if (email == null || email.length() == 0) {
                    emailTextInput.setError("Please enter your email");
                } else if (password == null || password.length() == 0) {
                    passwordTextInput.setError("Please create a password");
                } else if (confirmPassword == null || confirmPassword.length() == 0) {
                    confirmPasswordTextInput.setError("Please confirm your password");
                } else if (!password.equals(confirmPassword)) {
                    confirmPasswordTextInput.setError("Passwords do not match");
                } else {
                    LoginActivity.getSignUpInterface().signUp(email, password, name);
                }
            }
        });
        return view;
    }

    void initialize(View view) {
        nameTextInput = view.findViewById(R.id.tl_sign_up_name);
        emailTextInput = view.findViewById(R.id.tl_sign_up_email);
        passwordTextInput = view.findViewById(R.id.tl_sign_up_password);
        confirmPasswordTextInput = view.findViewById(R.id.tl_sign_up_confirm_password);
        signUpButton = view.findViewById(R.id.btn_email_sign_up);
    }

}
