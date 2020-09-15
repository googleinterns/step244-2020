package com.google.sps.data;

import java.lang.RuntimeException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.stream.Collectors;

public class ApiKeys {
  public static final String MAPS_API_KEY = readKeyFromFile("/maps_api_key.txt");
  public static final String WEATHER_API_KEY = readKeyFromFile("/openweather_api_key.txt");

  private static String readKeyFromFile(String fileName) {
    InputStream in = ApiKeys.class.getResourceAsStream(fileName);
    if (in == null) {
      throw new RuntimeException("Resource not found: " + fileName);
    }
    try {
      InputStreamReader inReader = new InputStreamReader(in);
      BufferedReader reader = new BufferedReader(inReader);
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    } catch (Exception e) {
      System.err.println("Cannot read API key from " + fileName + ": " + e.getMessage());
      throw new RuntimeException("Cannot read API key from " + fileName);
    }
  }
}
