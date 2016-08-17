package com.jambik.likoil;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FuelTicketsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MyLogs";

    public ArrayList<HashMap<String, String>> fuelTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_tickets);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();

        ArrayList<HashMap<String, String>> fuelTickets = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;

        ListView fuelTicketsList = (ListView) findViewById(R.id.listView);

        fuelTicketsList.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        HashMap<String, String> ticket = (HashMap<String, String>) parent.getItemAtPosition(position);
                        Toast.makeText(getApplicationContext(), ticket.get("Number"), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        if (intent.hasExtra("fuelTicket")) {

            try {

                JSONObject fuelTicket = new JSONObject(intent.getStringExtra("fuelTicket"));

                map = new HashMap<String, String>();
                map.put("Number", fuelTicket.getString("code"));
                map.put("Description", fuelTicket.getString("fuel") + " (" + fuelTicket.getString("liters") + "л.) - " + fuelTicket.getString("contractor"));
                fuelTickets.add(map);

                SimpleAdapter fuelTicketsAdapter = new SimpleAdapter(this, fuelTickets, android.R.layout.simple_list_item_2, new String[]{"Number", "Description"}, new int[]{android.R.id.text1, android.R.id.text2});
                fuelTicketsList.setAdapter(fuelTicketsAdapter);

            } catch (Throwable t) {

                Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + intent.getStringExtra("fuelTicket") + "\"", t);

            }

        }
    }

    public void buttonScanOnClick(View view) {
        Intent i = new Intent(getApplication(), ScannerActivity.class);
        startActivity(i);
    }

    public void buttonUseOnClick(View view) {

        Toast.makeText(getApplicationContext(), "Пока не работает", Toast.LENGTH_SHORT).show();

    }
}
