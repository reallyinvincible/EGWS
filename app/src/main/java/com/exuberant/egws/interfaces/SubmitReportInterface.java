package com.exuberant.egws.interfaces;

import android.graphics.Bitmap;

import com.exuberant.egws.models.Report;

public interface SubmitReportInterface {

    void submitReport(Report report, Bitmap nameboardPhoto, Bitmap buildingPhoto);

}
