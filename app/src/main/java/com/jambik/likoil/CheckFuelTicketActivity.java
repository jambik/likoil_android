package com.jambik.likoil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;

public class CheckFuelTicketActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "appsettings";
    public static final String APP_PREFERENCES_API_TOKEN = "api_token";
    private SharedPreferences mSettings;
    private String mApiToken;

    private static final String API_URL = "http://mve05.ru/api/v1/";
    private static final String LOG_TAG = "MyLogs";

    TextView txtFuelTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_fuel_ticket);

        Intent intent = getIntent();

        final String fuelTicket = intent.getStringExtra("fuelTicket");

        txtFuelTicket = (TextView) findViewById(R.id.txtFuelTicket);

        txtFuelTicket.setText(fuelTicket);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

//        Ion.getDefault(getApplicationContext()).configure().setLogging(LOG_TAG, Log.DEBUG);

        if (mSettings.contains(APP_PREFERENCES_API_TOKEN)) {

            // Получаем прежде сохраненный api_token
            mApiToken = mSettings.getString(APP_PREFERENCES_API_TOKEN, "");

            Ion.with(getApplicationContext())
                    .load(API_URL + "fuel_ticket")
                    .addQuery("api_token", mApiToken)
                    .addQuery("fuel_ticket", fuelTicket)
                    .setHeader("Accept", "application/json")
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {

                            int code = result.getHeaders().code();

                            if (code == 200) {

                                String json = result.getResult();

                                try {

                                    JSONObject obj = new JSONObject(json);
                                    Log.d(LOG_TAG, "message: " + obj.getString("message"));
                                    Log.d(LOG_TAG, "fuel_ticket: " + obj.getJSONObject("fuel_ticket"));

                                   /* Intent i = new Intent(getApplication(), FuelTicketsActivity.class);
                                    i.putExtra("fuelTicket", obj.getJSONObject("fuel_ticket").toString());
                                    startActivity(i);*/

                                    FuelTicketsActivity.addTicket(obj.getJSONObject("fuel_ticket").toString());

                                    finish();

                                } catch (Throwable t) {

                                    Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + json + "\"", t);

                                }

                            } else if (code == 401) { // Не авторизован
                                Intent i = new Intent(getApplication(), AuthActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();

                            } else if (code == 404) { // Купон не найден

                                Toast.makeText(getApplicationContext(), "Купон с номером - " + fuelTicket + " не найден", Toast.LENGTH_SHORT).show();

                                finish();

                            } else { // Другая ошибка

                                Toast.makeText(getApplicationContext(), "HTTP код ответа - " + code, Toast.LENGTH_SHORT).show();

                                finish();

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
