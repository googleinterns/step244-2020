package com.google.sps.servlets;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.maps.model.LatLng;
import com.google.sps.data.Weather;
import com.google.sps.data.GeoCoding;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestWeatherServlet {
  private static final double EPSILON = 0.0001;
  private static final String RUSSIA_MOSCOW_ADDRESS = "Russia, Moscow, Red Square";
  private static final LatLng RUSSIA_MOSCOW_LATLNG = new LatLng(55.75393030, 37.62079500);

  @Mock
  Weather mockWeather;
  @Mock
  GeoCoding mockGeoCoding;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testWeatherServlet_doGet_withEmptyLocation_returnsLocationErrorAnswer() throws IOException {
    when(mockRequest.getParameter("location")).thenReturn("");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new WeatherServlet(mockWeather, mockGeoCoding).doGet(mockRequest, mockResponse);

    Assert.assertTrue(stringWriter.toString().equals("Location cannot be null or empty\n"));
  }

  @Test
  public void testWeatherServlet_doGet_withLocation_returnsAnswer() throws IOException {
    when(mockRequest.getParameter("location")).thenReturn(RUSSIA_MOSCOW_ADDRESS);
    when(mockGeoCoding.fromAddressOrPlaceIdToLatLng(RUSSIA_MOSCOW_ADDRESS)).thenReturn(RUSSIA_MOSCOW_LATLNG);
    when(mockWeather.atLatLngNow(RUSSIA_MOSCOW_LATLNG)).thenReturn(new Weather(10.7, 11.0, 10, 100, 0, "Clear", "10n"));
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new WeatherServlet(mockWeather, mockGeoCoding).doGet(mockRequest, mockResponse);

    JsonElement json = JsonParser.parseString(stringWriter.toString());
    JsonObject jsonObject = json.getAsJsonObject();
    double temperature = jsonObject.get("temperature").getAsDouble();
    double temperatureFeelsLike = jsonObject.get("temperatureFeelsLike").getAsDouble();
    int pressure = jsonObject.get("pressure").getAsInt();
    int humidity = jsonObject.get("humidity").getAsInt();
    int clouds = jsonObject.get("clouds").getAsInt();
    String type = jsonObject.get("type").getAsString();
    String iconId = jsonObject.get("iconId").getAsString();
    Assert.assertEquals(10.7, temperature, EPSILON);
    Assert.assertEquals(11.0, temperatureFeelsLike, EPSILON);
    Assert.assertEquals(10, pressure);
    Assert.assertEquals(100, humidity);
    Assert.assertEquals(0, clouds);
    Assert.assertEquals("Clear", type);
    Assert.assertEquals("10n", iconId);
  }

  @Test
  public void testWeatherServlet_doGet_withLocationThroughDays_returnsAnswer() throws IOException {
    when(mockRequest.getParameter("location")).thenReturn(RUSSIA_MOSCOW_ADDRESS);
    when(mockGeoCoding.fromAddressOrPlaceIdToLatLng(RUSSIA_MOSCOW_ADDRESS)).thenReturn(RUSSIA_MOSCOW_LATLNG);
    when(mockRequest.getParameter("days")).thenReturn("3");
    when(mockWeather.atLatLngThroughDays(RUSSIA_MOSCOW_LATLNG, 3)).thenReturn(new Weather(10.7, 11.0, 10, 100, 0, "Clear", "10n"));
    
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new WeatherServlet(mockWeather, mockGeoCoding).doGet(mockRequest, mockResponse);

    JsonElement json = JsonParser.parseString(stringWriter.toString());
    JsonObject jsonObject = json.getAsJsonObject();
    double temperature = jsonObject.get("temperature").getAsDouble();
    double temperatureFeelsLike = jsonObject.get("temperatureFeelsLike").getAsDouble();
    int pressure = jsonObject.get("pressure").getAsInt();
    int humidity = jsonObject.get("humidity").getAsInt();
    int clouds = jsonObject.get("clouds").getAsInt();
    String type = jsonObject.get("type").getAsString();
    String iconId = jsonObject.get("iconId").getAsString();
    Assert.assertEquals(10.7, temperature, EPSILON);
    Assert.assertEquals(11.0, temperatureFeelsLike, EPSILON);
    Assert.assertEquals(10, pressure);
    Assert.assertEquals(100, humidity);
    Assert.assertEquals(0, clouds);
    Assert.assertEquals("Clear", type);
    Assert.assertEquals("10n", iconId);
  }
}
