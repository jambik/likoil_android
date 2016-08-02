package com.jambik.likoil;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;

public class AuthActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "appsettings";
    public static final String APP_PREFERENCES_API_TOKEN = "api_token";
    private SharedPreferences mSettings;
    private String mApiToken;

    private static final String API_URL = "http://jambik.ru/api/v1/";
    private static final String LOG_TAG = "MyLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

//        Ion.getDefault(getApplicationContext()).configure().setLogging("MyLogs", Log.DEBUG);
    }

    public void buttonLoginOnClick(View view) {

        EditText loginEditText = (EditText) findViewById(R.id.login);
        EditText passwordEditText = (EditText) findViewById(R.id.password);

        Ion.with(getApplicationContext())
            .load(API_URL + "auth")
            .addQuery("email", String.valueOf(loginEditText.getText()))
            .addQuery("password", String.valueOf(passwordEditText.getText()))
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
                            Log.d(LOG_TAG, "API TOKEN: " + obj.getString("api_token"));

                            mApiToken = obj.getString("api_token");

                            Toast.makeText(getApplicationContext(), "Авторизация прошла успешно", Toast.LENGTH_SHORT).show();

                            mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString(APP_PREFERENCES_API_TOKEN, mApiToken);
                            editor.apply();

                            Intent i = new Intent(getApplication(), FuelTicketsActivity.class);
                            startActivity(i);
                            finish();

                        } catch (Throwable t) {

                            Log.e(LOG_TAG, "Could not parse malformed JSON: \"" + json + "\"");

                        }

                    } else if (code == 401) {

                        Toast.makeText(getApplicationContext(), "Неверный логин/пароль", Toast.LENGTH_SHORT).show();

                    } else if (code == 422) {

                        Toast.makeText(getApplicationContext(), "Введите логин и пароль", Toast.LENGTH_SHORT).show();

                    }

                }
            });
    }
}
