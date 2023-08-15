package com.example.juahali;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText cityEditText;
    private Button searchButton;
    private TextView weatherTextView;

    private static final String API_KEY = "c6bb3bfe8915fc29675203449a7b5304"; // Replace with your OpenWeatherMap API key
    private static final String API_ENDPOINT = "http://api.openweathermap.org/data/2.5/forecast";

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = findViewById(R.id.editTextText);
        searchButton = findViewById(R.id.button);
        weatherTextView = findViewById(R.id.editTextTextMultiLine);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityEditText.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    fetchWeatherForCity(cityName);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchWeatherForCity(String cityName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CompletableFuture.supplyAsync(() -> {
                String url = API_ENDPOINT + "?q=" + cityName + "&appid=" + API_KEY;
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                CompletableFuture<String> future = new CompletableFuture<>();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        future.completeExceptionally(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try (Response responseCopy = response) {
                            if (!response.isSuccessful()) {
                                future.completeExceptionally(new IOException("Unexpected code " + response));
                            }
                            future.complete(response.body().string());
                        }
                    }
                });

                return future;
            }).thenAcceptAsync(responseBody -> {
                try {
                    JSONObject jsonResponse = new JSONObject(String.valueOf(responseBody)); // Parse JSON response
                    JSONArray weatherArray = jsonResponse.getJSONArray("list");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    JSONObject mainObject = weatherObject.getJSONObject("main");

                    double temperature = mainObject.getDouble("temp");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            weatherTextView.setText("Temperature: " + temperature);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                return null;
            });

        }
    }
}
