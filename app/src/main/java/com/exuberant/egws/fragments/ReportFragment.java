package com.exuberant.egws.fragments;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.exuberant.egws.R;
import com.exuberant.egws.Utils;
import com.exuberant.egws.activities.MainActivity;
import com.exuberant.egws.interfaces.CameraCaptureInterface;
import com.exuberant.egws.models.Report;
import com.exuberant.egws.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import lib.kingja.switchbutton.SwitchMultiButton;
import spencerstudios.com.ezdialoglib.Animation;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static androidx.core.content.ContextCompat.getColor;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;

public class ReportFragment extends Fragment {

    private FusedLocationProviderClient mFusedLocationClient;
    private Geocoder geocoder;
    private List<Address> addresses;
    private Location lastLocation;
    private Bitmap nameboardPhoto, buildingPhoto;
    private SharedPreferences sharedPreferences;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReportsReference;
    private static CameraCaptureInterface cameraCaptureInterface;
    private Report matchedReport;
    private boolean isNew = true;

    //UI Elements
    private TextInputLayout buildingNameTextInput, buildingAddressTextInput, buildingCityTextInput,
            buildingPincodeTextInput, buildingEmailTextInput, firstPhoneTextInput, alternatePhoneTextInput;
    private MaterialButton nameboardPhotoButton, buildingPhotoButton, submitButton;
    private ImageView nameboardImageView, buildingImageView;
    private LottieAnimationView loadingAnimation;
    private SwitchMultiButton buildingTypeSwitchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        initialize(view);
        getLocationData();
        nameboardPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFragmentSwitchInterface().switchToCamera(Utils.NAMEBOARD_REQUEST, cameraCaptureInterface);
            }
        });
        buildingPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getFragmentSwitchInterface().switchToCamera(Utils.BUILDING_REQUEST, cameraCaptureInterface);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport();
            }
        });
        return view;
    }

    void initialize(View view) {
        buildingNameTextInput = view.findViewById(R.id.tl_building_name);
        buildingAddressTextInput = view.findViewById(R.id.tl_building_address);
        buildingCityTextInput = view.findViewById(R.id.tl_building_city);
        buildingPincodeTextInput = view.findViewById(R.id.tl_building_pincode);
        buildingEmailTextInput = view.findViewById(R.id.tl_building_email);
        firstPhoneTextInput = view.findViewById(R.id.tl_building_phone_1);
        alternatePhoneTextInput = view.findViewById(R.id.tl_building_phone_2);
        nameboardPhotoButton = view.findViewById(R.id.btn_nameboard);
        buildingPhotoButton = view.findViewById(R.id.btn_building_photo);
        submitButton = view.findViewById(R.id.btn_submit_button);
        nameboardImageView = view.findViewById(R.id.iv_nameboard_picture);
        buildingImageView = view.findViewById(R.id.iv_building_picture);
        loadingAnimation = view.findViewById(R.id.loading_animation);
        buildingTypeSwitchButton = view.findViewById(R.id.smb_building_type);
        sharedPreferences = getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mDatabase = FirebaseDatabase.getInstance();
        mReportsReference = mDatabase.getReference().child("reports");
        cameraCaptureInterface = new CameraCaptureInterface() {
            @Override
            public void sendNameboardPhoto(PhotoResult photoResult) {
                photoResult
                        .toBitmap(scaled(0.25f))
                        .whenDone(new WhenDoneListener<BitmapPhoto>() {
                            @Override
                            public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
                                if (bitmapPhoto == null) {
                                    return;
                                }
                                nameboardPhoto = bitmapPhoto.bitmap;
                                Glide.with(getActivity())
                                        .load(bitmapPhoto.bitmap)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(nameboardImageView);
                                nameboardImageView.setRotation(-bitmapPhoto.rotationDegrees);
                                nameboardImageView.setVisibility(View.VISIBLE);
                                MainActivity.getFragmentHandler().popBackStack();
                            }
                        });
            }

            @Override
            public void sendBuildingPhoto(PhotoResult photoResult) {
                photoResult
                        .toBitmap(scaled(0.25f))
                        .whenDone(new WhenDoneListener<BitmapPhoto>() {
                            @Override
                            public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
                                if (bitmapPhoto == null) {
                                    return;
                                }
                                buildingPhoto = bitmapPhoto.bitmap;
                                Glide.with(getActivity())
                                        .load(bitmapPhoto.bitmap)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(buildingImageView);
                                buildingImageView.setRotation(-bitmapPhoto.rotationDegrees);
                                buildingImageView.setVisibility(View.VISIBLE);
                                MainActivity.getFragmentHandler().popBackStack();
                            }
                        });
            }
        };
    }

    void getLocationData() {

        startAnimation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        if (location != null) {
                            lastLocation = location;
                            mReportsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    matchedReport = null;
                                    Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                                    for (DataSnapshot snapshot : dataSnapshots) {
                                        Report report = snapshot.getValue(Report.class);
                                        double distance = Utils.calculateDistance(report.getLatitude(), location.getLatitude(),
                                                report.getLongitude(), location.getLongitude());
                                        if (distance < 20) {
                                            matchedReport = report;
                                            break;
                                        }
                                    }
                                    if (matchedReport == null) {
                                        geocoder = new Geocoder(getContext(), Locale.getDefault());
                                        try {
                                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                            String address = addresses.get(0).getAddressLine(0);
                                            buildingAddressTextInput.getEditText().setText(address);
                                            String city = addresses.get(0).getLocality();
                                            buildingCityTextInput.getEditText().setText(city);
                                            String postalCode = addresses.get(0).getPostalCode();
                                            buildingPincodeTextInput.getEditText().setText(postalCode);
                                            submitButton.setEnabled(true);
                                            stopAnimation();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            stopAnimation();
                                        }
                                    } else {
                                        String message = "There is already a report for building " + matchedReport.getBuildingName() + ".\nDo you want to edit it's details instead?";
                                        final Report finalMatchedReport = matchedReport;

                                        new EZDialog.Builder(getContext())
                                                .setTitle("Duplicate Entry Found")
                                                .setMessage(message)
                                                .setPositiveBtnText("New")
                                                .setNeutralBtnText("Yes")
                                                .setNegativeBtnText("No")
                                                .setBackgroundColor(getColor(getContext(), R.color.colorPrimaryDark))
                                                .setButtonTextColor(getColor(getContext(), R.color.colorAccent))
                                                .setHeaderColor(getColor(getContext(), R.color.headerColor))
                                                .setTitleTextColor(R.color.textColor)
                                                .setMessageTextColor(getColor(getContext(), R.color.textColor))
                                                .setCancelableOnTouchOutside(false)
                                                .OnNeutralClicked(new EZDialogListener() {
                                                    @Override
                                                    public void OnClick() {
                                                        submitButton.setEnabled(true);
                                                        isNew = false;
                                                        buildingNameTextInput.getEditText().setText(finalMatchedReport.getBuildingName());
                                                        buildingAddressTextInput.getEditText().setText(finalMatchedReport.getBuildingAddress());
                                                        buildingCityTextInput.getEditText().setText(finalMatchedReport.getBuildingCity());
                                                        buildingPincodeTextInput.getEditText().setText(finalMatchedReport.getPinCode());
                                                        String[] types = getActivity().getResources().getStringArray(R.array.building_type);
                                                        int selectedIndex = Arrays.asList(types).indexOf(finalMatchedReport.getType());
                                                        buildingTypeSwitchButton.setSelectedTab(selectedIndex);
                                                        try {
                                                            buildingEmailTextInput.getEditText().setText(finalMatchedReport.getBuildingEmail());
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            firstPhoneTextInput.getEditText().setText(finalMatchedReport.getFirstPhoneNumber());
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }

                                                        try {
                                                            alternatePhoneTextInput.getEditText().setText(finalMatchedReport.getSecondPhoneNumber());
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            String nameboardUrl = finalMatchedReport.getNameBoardPhoto();
                                                            if (nameboardUrl.equals("") || nameboardUrl.length() < 2) {
                                                                nameboardImageView.setVisibility(View.GONE);
                                                            } else {
                                                                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getActivity());
                                                                circularProgressDrawable.setColorSchemeColors(getActivity().getColor(R.color.colorAccent));
                                                                circularProgressDrawable.setStrokeWidth(10f);
                                                                circularProgressDrawable.setArrowEnabled(true);
                                                                circularProgressDrawable.setCenterRadius(100f);
                                                                circularProgressDrawable.start();
                                                                Glide.with(getActivity())
                                                                        .load(nameboardUrl)
                                                                        .placeholder(circularProgressDrawable)
                                                                        .transition(DrawableTransitionOptions.withCrossFade())
                                                                        .into(nameboardImageView);
                                                                nameboardImageView.setVisibility(View.VISIBLE);
                                                            }
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            String buildingUrl = finalMatchedReport.getBuildingPhoto();
                                                            if (buildingUrl.equals("") || buildingUrl.length() < 2) {
                                                                buildingImageView.setVisibility(View.GONE);
                                                            } else {
                                                                CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(getActivity());
                                                                circularProgressDrawable.setColorSchemeColors(getActivity().getColor(R.color.colorAccent));
                                                                circularProgressDrawable.setStrokeWidth(10f);
                                                                circularProgressDrawable.setArrowEnabled(true);
                                                                circularProgressDrawable.setCenterRadius(100f);
                                                                circularProgressDrawable.start();
                                                                Glide.with(getActivity())
                                                                        .load(buildingUrl)
                                                                        .placeholder(circularProgressDrawable)
                                                                        .transition(DrawableTransitionOptions.withCrossFade())
                                                                        .into(buildingImageView);
                                                                buildingImageView.setVisibility(View.VISIBLE);
                                                            }
                                                        } catch (NullPointerException e) {
                                                            e.printStackTrace();
                                                        }
                                                        stopAnimation();
                                                    }
                                                })
                                                .OnNegativeClicked(new EZDialogListener() {
                                                    @Override
                                                    public void OnClick() {
                                                        MainActivity.getFragmentHandler().popBackStack();
                                                    }
                                                }).setAnimation(Animation.UP)
                                                .OnPositiveClicked(new EZDialogListener() {
                                                    @Override
                                                    public void OnClick() {
                                                        isNew = true;
                                                        geocoder = new Geocoder(getContext(), Locale.getDefault());
                                                        try {
                                                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                                            String address = addresses.get(0).getAddressLine(0);
                                                            buildingAddressTextInput.getEditText().setText(address);
                                                            String city = addresses.get(0).getLocality();
                                                            buildingCityTextInput.getEditText().setText(city);
                                                            String postalCode = addresses.get(0).getPostalCode();
                                                            buildingPincodeTextInput.getEditText().setText(postalCode);
                                                            submitButton.setEnabled(true);
                                                            stopAnimation();
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                            stopAnimation();
                                                        }
                                                    }
                                                })
                                                .build();

                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            stopAnimation();
                        }
                    }
                });
    }

    void sendReport() {
        String userString = sharedPreferences.getString("User", "");
        Gson gson = new Gson();
        User user = gson.fromJson(userString, User.class);
        List<String> time = Utils.getCurrentTime();
        String buildingName = buildingNameTextInput.getEditText().getText().toString();
        String address = buildingAddressTextInput.getEditText().getText().toString();
        String city = buildingCityTextInput.getEditText().getText().toString();
        String pincode = buildingPincodeTextInput.getEditText().getText().toString();
        String email = buildingEmailTextInput.getEditText().getText().toString();
        String phone = firstPhoneTextInput.getEditText().getText().toString();
        String alternatePhone = alternatePhoneTextInput.getEditText().getText().toString();
        String[] types = getActivity().getResources().getStringArray(R.array.building_type);
        String type = types[buildingTypeSwitchButton.getSelectedTab()];

        if (buildingName.length() == 0) {
            buildingNameTextInput.setError("This is a mandatory field");
        } else if (address.length() == 0) {
            buildingAddressTextInput.setError("This is a mandatory field");
        } else if (city.length() == 0) {
            buildingCityTextInput.setError("This is a mandatory field");
        } else if (pincode.length() == 0) {
            buildingPincodeTextInput.setError("This is a mandatory field");
        } else {
            String reportId;
            if (matchedReport == null || isNew) {
                reportId = mReportsReference.push().getKey();
                Report report = new Report(user.getUserId(), user.getEmail(), reportId, lastLocation.getLatitude(), lastLocation.getLongitude(),
                        time.get(0), time.get(1), buildingName, address, city, pincode, email, phone, alternatePhone, type, null, null);
                MainActivity.getSubmitReportInterface().submitReport(report, nameboardPhoto, buildingPhoto);
            } else {
                reportId = matchedReport.getReportId();
                Report report = new Report(user.getUserId(), user.getEmail(), reportId, lastLocation.getLatitude(), lastLocation.getLongitude(),
                        time.get(0), time.get(1), buildingName, address, city, pincode, email, phone, alternatePhone, type, null, null);
                try {
                    String nameboardUrl = matchedReport.getNameBoardPhoto();
                    if (nameboardUrl.equals("") || nameboardUrl.length() < 2) {
                        //Invalid URL
                    } else {
                        report.setNameBoardPhoto(matchedReport.getNameBoardPhoto());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    String buildingUrl = matchedReport.getBuildingPhoto();
                    if (buildingUrl.equals("") || buildingUrl.length() < 2) {
                        //Invalid URL
                    } else {
                        report.setBuildingPhoto(matchedReport.getBuildingPhoto());
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                MainActivity.getSubmitReportInterface().submitReport(report, nameboardPhoto, buildingPhoto);
            }
        }
    }

    public void startAnimation() {
        loadingAnimation.playAnimation();
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    public void stopAnimation() {
        loadingAnimation.pauseAnimation();
        loadingAnimation.setVisibility(View.GONE);
    }

}
