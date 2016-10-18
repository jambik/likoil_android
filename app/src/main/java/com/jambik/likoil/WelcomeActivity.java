package com.jambik.likoil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;

public class WelcomeActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "appsettings";
    public static final String APP_PREFERENCES_API_TOKEN = "api_token";
    private SharedPreferences mSettings;
    private String mApiToken;

    private static final String API_URL = "http://mve05.ru/api/v1/";
    private static final String LOG_TAG = "MyLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

//        Ion.getDefault(getApplicationContext()).configure().setLogging("MyLogs", Log.DEBUG);

        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.contains(APP_PREFERENCES_API_TOKEN)) {

            // Получаем прежде сохраненный api_token
            mApiToken = mSettings.getString(APP_PREFERENCES_API_TOKEN, "");

            Ion.with(getApplicationContext())
                .load(API_URL + "check_token")
                .addQuery("api_token", mApiToken)
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

                            } catch (Throwable t) {

                                Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + json + "\"", t);

                            }

                            Intent i = new Intent(getApplication(), FuelTicketsActivity.class);
                            startActivity(i);
                            finish();

                        } else if (code == 401) {

                            Intent i = new Intent(getApplication(), AuthActivity.class);
                            startActivity(i);
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
