package com.exuberant.egws.interfaces;

import io.fotoapparat.result.PhotoResult;

public interface CameraCaptureInterface {

    void sendNameboardPhoto(PhotoResult photoResult);
    void sendBuildingPhoto(PhotoResult photoResult);

}
