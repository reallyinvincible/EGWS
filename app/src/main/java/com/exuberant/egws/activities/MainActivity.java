package com.exuberant.egws.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Fade;

import com.exuberant.egws.fragments.CameraFragment;
import com.exuberant.egws.fragments.MapFragment;
import com.exuberant.egws.fragments.PlaceholderFragment;
import com.exuberant.egws.R;
import com.exuberant.egws.interfaces.CameraCaptureInterface;
import com.exuberant.egws.interfaces.FragmentSwitchInterface;
import com.exuberant.egws.fragments.ReportFragment;
import com.exuberant.egws.interfaces.SubmitReportInterface;
import com.exuberant.egws.models.Report;
import com.exuberant.egws.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.exuberant.egws.fragments.CameraFragment.setCameraCaptureInterface;
import static com.exuberant.egws.fragments.CameraFragment.setRequestCode;

public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserReference, mReportsReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private SharedPreferences sharedPreferences;
    private User user;
    private Fragment mapFragment, placeHolderFragment;
    private static FragmentManager fragmentHandler;
    private static FragmentSwitchInterface fragmentSwitchInterface;
    private static SubmitReportInterface submitReportInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intitialize();

        String userString = sharedPreferences.getString("User", "");
        Gson gson = new Gson();
        user = gson.fromJson(userString, User.class);

        if (user.getAllowedToApp()){
            switchFragment(mapFragment);
        } else {
            switchFragment(placeHolderFragment);
        }

        mUserReference.child(user.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User newUser = dataSnapshot.getValue(User.class);
                saveDetails(newUser);
                if (newUser.getAllowedToApp()){
                    switchFragment(mapFragment);
                } else {
                    switchFragment(placeHolderFragment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void intitialize(){
        mDatabase = FirebaseDatabase.getInstance();
        mUserReference = mDatabase.getReference().child("users");
        mReportsReference = mDatabase.getReference().child("reports");
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference().child("report_images");
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        fragmentHandler = getSupportFragmentManager();
        mapFragment = new MapFragment();
        placeHolderFragment = new PlaceholderFragment();
        fragmentSwitchInterface = new FragmentSwitchInterface() {
            @Override
            public void switchToReport() {
                ReportFragment reportFragment = new ReportFragment();
                reportFragment.setEnterTransition(new Fade());
                reportFragment.setExitTransition(new Fade());
                addFragment(reportFragment);
            }

            @Override
            public void switchToCamera(int requestCode, CameraCaptureInterface cameraCaptureInterface) {
                CameraFragment cameraFragment = new CameraFragment();
                setRequestCode(requestCode);
                setCameraCaptureInterface(cameraCaptureInterface);
                cameraFragment.setEnterTransition(new Fade());
                cameraFragment.setExitTransition(new Fade());
                addFragment(cameraFragment);
            }
        };
        submitReportInterface = new SubmitReportInterface() {
            @Override
            public void submitReport(final Report report, final Bitmap nameboardPhoto, final Bitmap buildingPhoto) {
                mReportsReference.child(report.getReportId()).setValue(report).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            MainActivity.getFragmentHandler().popBackStack();
                            showSnackBar("Report Submitted", 1);
                            mUserReference.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User onlineUser = dataSnapshot.getValue(User.class);
                                    List<String> reports = onlineUser.getReports();
                                    if (reports == null){
                                        reports = new ArrayList<>();
                                        reports.add(report.getReportId());
                                        onlineUser.setReports(reports);
                                        mUserReference.child(onlineUser.getUserId()).setValue(onlineUser);
                                        saveDetails(onlineUser);
                                    } else {
                                        reports = onlineUser.getReports();
                                        if (!reports.contains(report.getReportId())) {
                                            reports.add(report.getReportId());
                                            onlineUser.setReports(reports);
                                            mUserReference.child(onlineUser.getUserId()).setValue(onlineUser);
                                            saveDetails(onlineUser);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            if (nameboardPhoto != null){
                                uploadImage(nameboardPhoto, report.getReportId(), 0);
                            }
                            if (buildingPhoto != null){
                                uploadImage(buildingPhoto, report.getReportId(), 1);
                            }
                        }
                    }
                });
            }
        };
    }

    void uploadImage(Bitmap bitmap, final String reportId, final int count) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        final StorageReference photoRef = mStorageReference.child(reportId + "_" + String.valueOf(count));
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return photoRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();
                    if (count == 0){
                        updateNameboardImageData(reportId, downUri.toString());
                    } else {
                        updateBuildingImageData(reportId, downUri.toString());
                    }
                }
            }
        });
    }

    void updateNameboardImageData(final String reportId, final String downloadUrl){
        mReportsReference.child(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);
                report.setNameBoardPhoto(downloadUrl);
                mReportsReference.child(reportId).setValue(report);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateBuildingImageData(final String reportId, final String downloadUrl){
        mReportsReference.child(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Report report = dataSnapshot.getValue(Report.class);
                report.setBuildingPhoto(downloadUrl);
                mReportsReference.child(reportId).setValue(report);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void switchFragment(Fragment fragment){
        fragmentHandler.beginTransaction().replace(R.id.fragment_container, fragment, "Replace").commitAllowingStateLoss();
    }

    void addFragment(Fragment fragment){
        fragmentHandler.beginTransaction().add(R.id.fragment_container, fragment, "Add").addToBackStack("Fragment").commit();
    }

    void saveDetails(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String userString = gson.toJson(user);
        editor.putString("User", userString);
        editor.apply();
    }

    void showSnackBar(String message, int color) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_container), message, Snackbar.LENGTH_SHORT);
        if (color == 0) {
            snackbar.getView().setBackgroundResource(R.color.colorErrorSnackbar);
        } else {
            snackbar.getView().setBackgroundResource(R.color.colorSuccessSnackbar);
        }
        snackbar.show();
    }

    public static FragmentSwitchInterface getFragmentSwitchInterface() {
        return fragmentSwitchInterface;
    }

    public static SubmitReportInterface getSubmitReportInterface() {
        return submitReportInterface;
    }

    public static FragmentManager getFragmentHandler() {
        return fragmentHandler;
    }



    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
