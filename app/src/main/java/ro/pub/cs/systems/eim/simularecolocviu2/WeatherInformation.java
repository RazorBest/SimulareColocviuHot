package ro.pub.cs.systems.eim.simularecolocviu2;

import androidx.annotation.NonNull;

public class WeatherInformation {

    public Double temperature;
    public Double windSpeed;
    public String condition;
    public Double pressure;
    public Double humidity;

    public WeatherInformation(Double temperature, Double windSpeed, String condition, Double pressure, Double humidity) {
        this.temperature = temperature;
        this.windSpeed = windSpeed;
        this.condition = condition;
        this.pressure = pressure;
        this.humidity = humidity;
    }

    @NonNull
    @Override
    public String toString() {
        return "Temperature: " + temperature + "\n" +
                "Wind speed: " + windSpeed + "\n" +
                "Condition: " + condition + "\n" +
                "Pressure: " + pressure + "\n" +
                "Humidity: " + humidity + "\n";
    }
}
