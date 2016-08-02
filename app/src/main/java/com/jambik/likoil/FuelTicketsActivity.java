package com.jambik.likoil;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONObject;

public class FuelTicketsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MyLogs";

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_tickets);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        try {

            JSONObject fuelTicket = new JSONObject(intent.getStringExtra("fuelTicket"));

            ListView fuelTicketsList = (ListView) findViewById(R.id.listView);

//            fuelTicketsList.add

        } catch (Throwable t) {

            Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + intent.getStringExtra("fuelTicket") + "\"", t);

        }
    }

    public void buttonScanOnClick(View view) {
        Intent i = new Intent(getApplication(), ScannerActivity.class);
        startActivity(i);
    }
}
