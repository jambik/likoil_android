package com.jambik.likoil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScannerActivity extends Activity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    private static final String LOG_TAG = "MyLogs";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(LOG_TAG, rawResult.getContents()); // Prints scan results
        Log.v(LOG_TAG, rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        if (rawResult.getBarcodeFormat() == BarcodeFormat.EAN13) {

            Intent i = new Intent(getApplication(), CheckFuelTicketActivity.class);
            i.putExtra("fuelTicket", rawResult.getContents());
            startActivity(i);
            finish();

//            Toast.makeText(getApplicationContext(), "Штрихкод отсканирован - " + rawResult.getText(), Toast.LENGTH_SHORT).show();

        } else {

            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(this);

        }
    }

}
