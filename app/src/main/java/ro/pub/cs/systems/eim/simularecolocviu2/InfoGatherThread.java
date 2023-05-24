package ro.pub.cs.systems.eim.simularecolocviu2;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InfoGatherThread extends Thread {
    public String city;
    public String result;

    public InfoGatherThread(String city) {
        this.city = city;
    }

    public void makeRequest() {
        HttpURLConnection httpURLConnection = null;
        StringBuilder result = new StringBuilder();
        String error = null;
        try {
            String webPageAddress = "https://api.openweathermap.org/data/2.5/weather?appid=e03c3b32cfb5a6f7069f2ef29237d87e";
            webPageAddress += "&q=" + this.city;

            HttpURLConnection connection = (HttpURLConnection) new URL(webPageAddress).openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());

            Log.d("test", "ServerThread: url " + webPageAddress);

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String response = "";
            String line;
            while ((line = reader.readLine()) != null) {
                response += line;
            }

            // Parse the page source code into a JSONObject and extract the needed information
            JSONObject content = new JSONObject(response);

            Double windSpeed = content.getJSONObject("wind").getDouble("speed");
            Double temperature = content.getJSONObject("main").getDouble("temp");
            Double humidity = content.getJSONObject("main").getDouble("humidity");
            Double pressure = content.getJSONObject("main").getDouble("pressure");
            // Log.d("test", content.getJSONArray("weather").getString(0));
            String condition = content.getJSONArray("weather").getJSONObject(0).getString("main");

            // Create the result String
            result.append(windSpeed).append(" ").append(temperature).append(" ").append(humidity).append(" ").append(pressure).append(" ").append(condition);
        } catch(IOException e)  {
            Log.e("tag", "Error: " + e.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        this.result = result.toString();
    }

    public void run() {
        makeRequest();
    }
}
