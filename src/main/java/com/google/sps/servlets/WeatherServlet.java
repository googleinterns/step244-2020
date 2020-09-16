package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import com.google.gson.Gson;
import com.google.maps.model.LatLng;
import com.google.sps.data.Weather;
import com.google.sps.data.GeoCoding;

@WebServlet("/weather")
public class WeatherServlet extends HttpServlet {
  Weather weather;
  GeoCoding geoCoding;

  @Inject
  WeatherServlet(Weather weather, GeoCoding geoCoding) {
    this.weather = weather;
    this.geoCoding = geoCoding;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String location = request.getParameter("location");
    Integer hours = parseIntegerFromString(request.getParameter("hours"));
    Integer days = parseIntegerFromString(request.getParameter("days"));
    response.setContentType("application/json");
    if (location == null || location.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println("Location cannot be null or empty");
      return;
    }
    LatLng latlng = geoCoding.fromAddressOrPlaceIdToLatLng(location);
    if (latlng == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println("Cannot find location: " + location);
      return;
    }

    if (hours != null && hours >= 0 && hours < 48) {
      response.getWriter().println(new Gson().toJson(weather.atLatLngThroughHours(latlng, hours)));
      return;
    } else if (days != null && days >= 0 && days < 8) {
      response.getWriter().println(new Gson().toJson(weather.atLatLngThroughDays(latlng, days)));
      return;
    }
    response.getWriter().println(new Gson().toJson(weather.atLatLngNow(latlng)));
    return;
  }

  private Integer parseIntegerFromString(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
