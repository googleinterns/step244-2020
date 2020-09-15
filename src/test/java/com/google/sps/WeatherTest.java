package com.google.sps;

import com.google.sps.data.Weather;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class WeatherTest {
  @Test
  public void testWeather_fromOpenWeatherApiJsonObject_withPrimitiveTemp_returnsWeather() {
    JsonObject openWeatherApiJsonObject = new JsonObject();
    
    openWeatherApiJsonObject.addProperty("temp", "10.7");
    openWeatherApiJsonObject.addProperty("feels_like", "11");
    openWeatherApiJsonObject.addProperty("pressure", "10");
    openWeatherApiJsonObject.addProperty("humidity", "100");
    openWeatherApiJsonObject.addProperty("clouds", "0");

    JsonObject weatherJsonObject = new JsonObject();
    weatherJsonObject.addProperty("main", "clear");
    weatherJsonObject.addProperty("icon", "10n");
    JsonArray weatherJsonArrayObject = new JsonArray();
    weatherJsonArrayObject.add(weatherJsonObject);
    openWeatherApiJsonObject.add("weather", weatherJsonArrayObject);
    
    Weather actual_weather = Weather.fromOpenWeatherApiJsonObject(openWeatherApiJsonObject);
    Weather expected_weather = new Weather(10.7, 11.0, 10, 100, 0, "clear", "10n");
    Assert.assertEquals(actual_weather, expected_weather);
  }

  @Test
  public void testWeather_fromOpenWeatherApiJsonObject_withNotPrimitiveTemp_returnsWeather() {
    JsonObject openWeatherApiJsonObject = new JsonObject();
    
    JsonObject tempJsonObject = new JsonObject();
    tempJsonObject.addProperty("day", "10.7");
    JsonObject feelsLikeJsonObject = new JsonObject();
    feelsLikeJsonObject.addProperty("day", "11");

    openWeatherApiJsonObject.add("temp", tempJsonObject);
    openWeatherApiJsonObject.add("feels_like", feelsLikeJsonObject);
    openWeatherApiJsonObject.addProperty("pressure", "10");
    openWeatherApiJsonObject.addProperty("humidity", "100");
    openWeatherApiJsonObject.addProperty("clouds", "0");

    JsonObject weatherJsonObject = new JsonObject();
    weatherJsonObject.addProperty("main", "clear");
    weatherJsonObject.addProperty("icon", "10n");
    JsonArray weatherJsonArrayObject = new JsonArray();
    weatherJsonArrayObject.add(weatherJsonObject);
    openWeatherApiJsonObject.add("weather", weatherJsonArrayObject);
    
    Weather actual_weather = Weather.fromOpenWeatherApiJsonObject(openWeatherApiJsonObject);
    Weather expected_weather = new Weather(10.7, 11.0, 10, 100, 0, "clear", "10n");
    Assert.assertEquals(actual_weather, expected_weather);
  }
}
