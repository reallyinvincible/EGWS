package com.exuberant.egws.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.exuberant.egws.R;
import com.exuberant.egws.interfaces.SignUpInterface;
import com.exuberant.egws.bottomsheets.SignUpBottomSheet;
import com.exuberant.egws.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

import static maes.tech.intentanim.CustomIntent.customType;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    public static final int PERMISSIONS_MULTIPLE_REQUEST = 9004;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserReference;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences sharedPreferences;
    private static SignUpInterface signUpInterface;

    //UI Elements
    private TextInputLayout emailTextInput, passwordTextInput;
    private MaterialButton signinButton, signupButton, googleSigninButton;
    private SignUpBottomSheet signUpBottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
        googleSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTextInput.getEditText().getText().toString();
                String password = passwordTextInput.getEditText().getText().toString();
                if (email == null || email.length() == 0) {
                    emailTextInput.setError("Email is necessary to proceed further");
                } else if (password == null || password.length() == 0) {
                    passwordTextInput.setError("Please enter the password");
                } else {
                    hideSoftKeyBoard();
                    signInWithEmail(email, password);
                }
            }
        });
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpBottomSheet = new SignUpBottomSheet();
                signUpBottomSheet.show(getSupportFragmentManager(), "SignUp");
            }
        });
        disableAllButtons();
        checkPermission();
    }

    void initialize() {
        emailTextInput = findViewById(R.id.tl_login_email);
        passwordTextInput = findViewById(R.id.tl_login_password);
        signinButton = findViewById(R.id.btn_email_sign_in);
        signupButton = findViewById(R.id.btn_sign_up);
        googleSigninButton = findViewById(R.id.btn_google_signin);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUserReference = mDatabase.getReference().child("users");
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signUpInterface = new SignUpInterface() {
            @Override
            public void signUp(String email, String password, String name) {
                hideSoftKeyBoard();
                signUpBottomSheet.dismiss();
                signUpWithEmail(email, password, name);
            }
        };
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            showSnackBar("Authentication Successful", 1);
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleUser(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            showSnackBar("Authentication Failed", 0);
                        }

                    }
                });
    }

    void signUpWithEmail(String email, String password, final String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            showSnackBar("Account sucessfully created", 1);
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                handleUser(mAuth.getCurrentUser());
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                showSnackBar("Weak Password. Please sign up with a strong password.", 0);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                showSnackBar("Invalid Email. Please sign up with a valid email", 0);
                            } catch (FirebaseAuthUserCollisionException e) {
                                showSnackBar("User already exists. Please sign in.", 0);
                            } catch (Exception e) {
                                showSnackBar("Authentication Failed", 0);
                            }
                        }

                    }
                });
    }

    void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleUser(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                showSnackBar("User doesn't exist. Please sign up", 0);
                            } catch (Exception e) {
                                showSnackBar("Authentication Failed", 0);
                            }
                        }
                    }
                });
    }

    void handleUser(final FirebaseUser firebaseUser) {
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(firebaseUser.getUid())){
                    User user = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                    saveDetails(user);
                } else {
                    createUser(firebaseUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void createUser(FirebaseUser firebaseUser){
        User user = new User(firebaseUser.getDisplayName(), firebaseUser.getUid(), firebaseUser.getEmail(), false, new ArrayList<String>());
        mUserReference.child(user.getUserId()).setValue(user);
        saveDetails(user);
    }

    void saveDetails(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String userString = gson.toJson(user);
        editor.putString("User", userString);
        editor.apply();
        launchMain();
    }

    void launchMain(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        customType(LoginActivity.this, "fadein-to-fadeout");
        finishAfterTransition();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.CAMERA)) {

                Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content),
                        "We really need these permissions for app to work",
                        Snackbar.LENGTH_INDEFINITE).setAction("Grant",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{Manifest.permission
                                                .ACCESS_FINE_LOCATION, Manifest.permission.CAMERA},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        });
                snackbar.getView().setBackgroundResource(R.color.colorErrorSnackbar);
                snackbar.show();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission
                                .ACCESS_FINE_LOCATION, Manifest.permission.CAMERA},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            enableAllButtons();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraPermission && locationPermission) {
                        enableAllButtons();
                    } else {
                        Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content),
                                "We really need these permissions for app to work",
                                Snackbar.LENGTH_INDEFINITE).setAction("Grant",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .ACCESS_FINE_LOCATION, Manifest.permission.CAMERA},
                                                PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                });
                        snackbar.getView().setBackgroundResource(R.color.colorErrorSnackbar);
                        snackbar.show();
                    }
                }
                break;
        }
    }

    void disableAllButtons() {
        signinButton.setEnabled(false);
        signupButton.setEnabled(false);
        googleSigninButton.setEnabled(false);
    }

    void enableAllButtons() {
        signinButton.setEnabled(true);
        signupButton.setEnabled(true);
        googleSigninButton.setEnabled(true);
    }

    void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    void showSnackBar(String message, int color) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.login_container), message, Snackbar.LENGTH_SHORT);
        if (color == 0) {
            snackbar.getView().setBackgroundResource(R.color.colorErrorSnackbar);
        } else {
            snackbar.getView().setBackgroundResource(R.color.colorSuccessSnackbar);
        }
        snackbar.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String userString = sharedPreferences.getString("User", null);
        if (userString != null) {
            launchMain();
        }
    }

    public static SignUpInterface getSignUpInterface() {
        return signUpInterface;
    }
}
