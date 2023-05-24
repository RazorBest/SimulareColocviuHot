package ro.pub.cs.systems.eim.simularecolocviu2;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    private String city;
    private String informationType;

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    private void readPayload(BufferedReader bufferedReader) {
        city = null;
        informationType = null;
        try {
            city = bufferedReader.readLine();
            informationType = bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (city == null || city.isEmpty() || informationType == null || informationType.isEmpty()) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
        }
    }

    private WeatherInformation getResultFromRequest() {
        WeatherInformation weatherInformation = null;
        HttpURLConnection httpURLConnection = null;
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

            weatherInformation = new WeatherInformation(temperature, windSpeed, condition, pressure, humidity);
        } catch(IOException e)  {
            Log.e("tag", "Error: " + e.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return weatherInformation;
    }

    private String getResult() {
        // It checks whether the serverThread has already received the weather forecast information for the given city.
        HashMap<String, WeatherInformation> data = serverThread.getData();
        WeatherInformation weatherInformation;
        if (data.containsKey(city)) {
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
            weatherInformation = data.get(city);
        } else {
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
            weatherInformation = getResultFromRequest();
            serverThread.setData(city, weatherInformation);
        }

        if (weatherInformation == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
            return null;
        }

        Log.d(Constants.TAG, "informationType: " + informationType);

        String result = null;
        switch (informationType) {
            case "all":
                result = weatherInformation.toString();
                break;
            case "temperature":
                result = "Temperature: " + weatherInformation.temperature;
                break;
            case "wind_speed":
                result = "Wind speed: " + weatherInformation.windSpeed;
                break;
            case "condition":
                result = "Condition: " + weatherInformation.condition;
                break;
            case "humidity":
                result = "Humidity: " + weatherInformation.humidity;
                break;
            case "pressure":
                result = "Pressure: " + weatherInformation.pressure;
                break;
            default:
                result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
        }

        Log.d(Constants.TAG, "[COMMUNICATION THREAD] result: " + result);

        return result;
    }

    // run() method: The run method is the entry point for the thread when it starts executing.
    // It's responsible for reading data from the client, interacting with the server,
    // and sending a response back to the client.
    @Override
    public void run() {
        // It first checks whether the socket is null, and if so, it logs an error and returns.
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Create BufferedReader and PrintWriter instances for reading from and writing to the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");

            readPayload(bufferedReader);

            // Get the result from the webservice or cache
            String result = getResult();

            // Send the result back to the client
            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}