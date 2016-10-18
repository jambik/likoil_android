package com.jambik.likoil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.JSONArrayBody;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuelTicketsActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "appsettings";
    public static final String APP_PREFERENCES_API_TOKEN = "api_token";
    private SharedPreferences mSettings;
    private String mApiToken;

    private static final String API_URL = "http://mve05.ru/api/v1/";
    private static final String LOG_TAG = "MyLogs";

    public static ArrayList<HashMap<String, String>> mFuelTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_tickets);
        mFuelTickets = new ArrayList<>();
    }

    public static void addTicket(String ticket) {
        try {
            JSONObject fuelTicket = new JSONObject(ticket);
            String code = fuelTicket.getString("code");
            for (Map<String, String> item : mFuelTickets) {
                if (item.get("Number").equalsIgnoreCase(code)) {
                    return;
                }
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("Number", code);
            map.put("Description", fuelTicket.getString("fuel") + " (" + fuelTicket.getString("liters") + "л.) - " + fuelTicket.getString("contractor"));
            mFuelTickets.add(map);
        } catch (Exception ignored) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Операции для выбранного пункта меню
        switch (item.getItemId())
        {
            case R.id.mRemoveAll:
                mFuelTickets.clear();
                refreshList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshList() {
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
        SimpleAdapter fuelTicketsAdapter = new SimpleAdapter(this, mFuelTickets, android.R.layout.simple_list_item_2, new String[]{"Number", "Description"}, new int[]{android.R.id.text1, android.R.id.text2});
        fuelTicketsList.setAdapter(fuelTicketsAdapter);
        findViewById(R.id.buttonUse).setVisibility(mFuelTickets.size() == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(LOG_TAG, mFuelTickets.toString());

        refreshList();

    }

    public void buttonScanOnClick(View view) {
        Intent i = new Intent(getApplication(), ScannerActivity.class);
        startActivity(i);
    }


    private void processResults(JSONArray found, JSONArray notFound, String message) {
        if (found.length() > 0 && notFound.length() == 0) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Успешных запрос")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .create();
            alertDialog.show();
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .create();
            alertDialog.show();
        }
        mFuelTickets.clear();
        refreshList();
    }

    public void buttonUseOnClick(View view) {

        if (mFuelTickets.isEmpty()) {

            Toast.makeText(getApplicationContext(), "Сначала сканируйте купон", Toast.LENGTH_SHORT).show();

        } else {

            mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            Ion.getDefault(getApplicationContext()).configure().setLogging(LOG_TAG, Log.DEBUG);

            if (mSettings.contains(APP_PREFERENCES_API_TOKEN)) {

                // Получаем прежде сохраненный api_token
                mApiToken = mSettings.getString(APP_PREFERENCES_API_TOKEN, "");

                // Получаем номера купонов в виде строки через запятую
               /* HashMap<String, String> ticket = (HashMap<String, String>) mFuelTickets.get(0);
                Log.i(LOG_TAG, ticket.get("Number"));
                String fuelTicketsParameter = ticket.get("Number");*/


                String ticketNumbers = "";
                for (Map<String, String> model : mFuelTickets) {
                    ticketNumbers += model.get("Number") + ",";
                }

                ticketNumbers = ticketNumbers.substring(0, ticketNumbers.length() - 1);


                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Пожалуйста, подождите");
                progressDialog.setMessage("Идет запрос");
                progressDialog.setCancelable(false);
                progressDialog.show();

                Ion.with(getApplicationContext())
                        .load(API_URL + "use_fuel_tickets")
                        .addQuery("api_token", mApiToken)
                        .addQuery("fuel_tickets", ticketNumbers)
                        .setHeader("Accept", "application/json")
                        .asString()
                        .withResponse()
                        .setCallback(new FutureCallback<Response<String>>() {
                            @Override
                            public void onCompleted(Exception e, Response<String> result) {

                                progressDialog.dismiss();

                                int code = result.getHeaders().code();

                                if (code == 200) {

                                    String json = result.getResult();

                                    try {

                                        JSONObject obj = new JSONObject(json);
                                        Log.d(LOG_TAG, "message: "   + obj.getString("message"));
                                        Log.d(LOG_TAG, "found: "     + obj.getJSONArray("found"));
                                        Log.d(LOG_TAG, "not_found: " + obj.getJSONArray("not_found"));

                                        processResults(obj.getJSONArray("found"), obj.getJSONArray("not_found"), obj.getString("message"));

                                    } catch (Throwable t) {

                                        Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + json + "\"", t);

                                    }

                                } else if (code == 401) { // Не авторизован

                                    Intent i = new Intent(getApplication(), AuthActivity.class);
                                    startActivity(i);
                                    finish();

                                } else { // Другая ошибка

                                    Toast.makeText(getApplicationContext(), "HTTP код ответа - " + code, Toast.LENGTH_SHORT).show();

                                }

                            }
                        });

            } else {

                Intent i = new Intent(getApplication(), AuthActivity.class);
                startActivity(i);
                finish();

            }

        }

    }
}
