package com.exuberant.egws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.exuberant.egws.R;
import com.exuberant.egws.Utils;
import com.exuberant.egws.activities.MainActivity;
import com.exuberant.egws.interfaces.CameraCaptureInterface;
import com.google.android.material.button.MaterialButton;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.selector.FlashSelectorsKt;
import io.fotoapparat.selector.FocusModeSelectorsKt;
import io.fotoapparat.selector.LensPositionSelectorsKt;
import io.fotoapparat.selector.ResolutionSelectorsKt;
import io.fotoapparat.selector.SelectorsKt;
import io.fotoapparat.view.CameraView;

public class CameraFragment extends Fragment {

    private Fotoapparat fotoapparat;
    private static CameraCaptureInterface cameraCaptureInterface;
    private CameraView cameraView;
    private MaterialButton captureButton;
    private static int REQUEST_CODE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initialize(view);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (REQUEST_CODE == Utils.NAMEBOARD_REQUEST){
                    PhotoResult photoResult = fotoapparat.takePicture();
                    cameraCaptureInterface.sendNameboardPhoto(photoResult);
                } else {
                    PhotoResult photoResult = fotoapparat.takePicture();
                    cameraCaptureInterface.sendBuildingPhoto(photoResult);
                }
            }
        });
        return view;
    }

    void initialize(View view){
        cameraView = view.findViewById(R.id.cv_camera);
        captureButton = view.findViewById(R.id.btn_capture);
        fotoapparat = Fotoapparat
                .with(getContext())
                .into(cameraView)
                .previewScaleType(ScaleType.CenterCrop)
                .photoResolution(ResolutionSelectorsKt.highestResolution())
                .lensPosition(LensPositionSelectorsKt.back())
                .focusMode(SelectorsKt.firstAvailable(
                        FocusModeSelectorsKt.continuousFocusPicture(),
                        FocusModeSelectorsKt.autoFocus(),
                        FocusModeSelectorsKt.fixed()
                ))
                .flash(SelectorsKt.firstAvailable(
                        FlashSelectorsKt.autoFlash(),
                        FlashSelectorsKt.torch()
                ))
                .build();
        fotoapparat.start();
    }

    public static void setRequestCode(int requestCode) {
        REQUEST_CODE = requestCode;
    }

    public static void setCameraCaptureInterface(CameraCaptureInterface cameraCaptureInterface) {
        CameraFragment.cameraCaptureInterface = cameraCaptureInterface;
    }
}
