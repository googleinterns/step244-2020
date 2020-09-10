package com.google.sps.data;

import com.google.maps.errors.ApiException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.lang.InterruptedException;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.servlet.http.HttpServletResponse;

import com.google.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;

public class Weather {
  private Double temperature;
  private Double temperatureFeelsLike;
  private Integer pressure;
  private Integer humidity;
  private Integer clouds;
  private String type;
  private String iconId;

  public Weather() {}

  public Weather(Double temperature, Double temperatureFeelsLike, 
          Integer pressure, Integer humidity, Integer clouds, 
          String type, String iconId) {
    this.temperature = temperature;
    this.temperatureFeelsLike = temperatureFeelsLike;
    this.pressure = pressure;
    this.humidity = humidity;
    this.clouds = clouds;
    this.type = type;
    this.iconId = iconId;
  }

  private Weather fromOpenWeatherApiJsonObject(JsonObject openWeatherApiJsonObject) {
    if (openWeatherApiJsonObject == null)
      return null;
    try {
      JsonElement temperatureElement = openWeatherApiJsonObject.get("temp");
      Double temperature = null;
      Double feelsLike = null;
      if (temperatureElement.isJsonPrimitive()) {
        temperature = temperatureElement.getAsDouble();
        feelsLike = openWeatherApiJsonObject.get("feels_like").getAsDouble();
      } else {
        temperature = temperatureElement.getAsJsonObject().get("day").getAsDouble();
        feelsLike = openWeatherApiJsonObject.get("feels_like").getAsJsonObject().get("day").getAsDouble();
      }
      return new Weather(temperature,
                         feelsLike, 
                         openWeatherApiJsonObject.get("pressure").getAsInt(), 
                         openWeatherApiJsonObject.get("humidity").getAsInt(),
                         openWeatherApiJsonObject.get("clouds").getAsInt(),
                         openWeatherApiJsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString(), 
                         openWeatherApiJsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString());
    } catch (Exception e) {
      return null;
    }
  }

  private JsonObject getOpenWeatherApiJsonObject(LatLng latlng) {
    if (latlng == null)
      return null;

    //https://api.openweathermap.org/data/2.5/onecall?lat={lat}&lon={lon}&exclude={part}&units=metric&appid={YOUR API KEY}
    URL openWeatherApiUrl = null;
    try {
      openWeatherApiUrl = new URL(String.format(
        "https://api.openweathermap.org/data/2.5/onecall?lat=%f&lon=%f&units=metric&appid=" + ApiKeys.WEATHER_API_KEY, latlng.lat, latlng.lng
      ));
    } catch (MalformedURLException e) {
      System.err.println(String.format("Cannot form URl with %f, %f ", latlng.lat, latlng.lng) + e.getMessage());
      return null;
    } catch (IOException e) {
      System.err.println(String.format("Cannot form URl with %f, %f ", latlng.lat, latlng.lng) + e.getMessage());
      return null;
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) openWeatherApiUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();
      if (connection.getResponseCode() != HttpServletResponse.SC_OK)
        return null;
    } catch (ProtocolException e) {
      System.err.println(e.getMessage());
      return null;
    } catch (IOException e) {
      System.err.println(e.getMessage());
      return null;
    }

    try {
      return new JsonParser().parse(new InputStreamReader((InputStream) connection.getContent())).getAsJsonObject();
    } catch (IOException e) {
      System.err.println(String.format("Cannot parse json form URl with %f, %f ", latlng.lat, latlng.lng) + e.getMessage());
      return null;
    }
  }
  
  public Weather atLatLngNow(LatLng latlng) {
    JsonObject openWeatherApiJsonObject = getOpenWeatherApiJsonObject(latlng);
    if (openWeatherApiJsonObject == null)
      return null;
    return fromOpenWeatherApiJsonObject(openWeatherApiJsonObject.getAsJsonObject("current"));
  }

  public Weather atLatLngTroughHours(LatLng latlng, Integer hours) {
    JsonObject openWeatherApiJsonObject = getOpenWeatherApiJsonObject(latlng);
    if (openWeatherApiJsonObject == null)
      return null;
    return fromOpenWeatherApiJsonObject(openWeatherApiJsonObject.getAsJsonArray("hourly").get(hours).getAsJsonObject());
  }

  public Weather atLatLngTroughDays(LatLng latlng, Integer days) {
    JsonObject openWeatherApiJsonObject = getOpenWeatherApiJsonObject(latlng);
    if (openWeatherApiJsonObject == null)
      return null;
    return fromOpenWeatherApiJsonObject(openWeatherApiJsonObject.getAsJsonArray("daily").get(days).getAsJsonObject());
  }
}
