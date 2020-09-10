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
  Weather WeatherObject;
  GeoCoding GeoCodingObject;

  @Inject
  WeatherServlet(Weather WeatherObject, GeoCoding GeoCodingObject) {
    this.WeatherObject = WeatherObject;
    this.GeoCodingObject = GeoCodingObject;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String location = request.getParameter("location");
    Integer hours = parseIntegerFromString(request.getParameter("hours"));
    Integer days = parseIntegerFromString(request.getParameter("days"));
    if (location == null || location.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().println("Location cannot be null or empty");
      return;
    }
    LatLng latlng = GeoCodingObject.fromAddressToLatLng(location);
    if (days != null) {
      if (days < 0 || days >= 8) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().println("Days should be in range 0 <= days < 8");
        return;
      }
      response.getWriter().println(new Gson().toJson(WeatherObject.atLatLngTroughDays(latlng, days)));
      return;
    } else if (hours != null) {
      if (hours < 0 || hours >= 48) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().println("Hours should be in range 0 <= hours < 48");
        return;
      }
      response.getWriter().println(new Gson().toJson(WeatherObject.atLatLngTroughHours(latlng, hours)));
      return;
    }
    response.getWriter().println(new Gson().toJson(WeatherObject.atLatLngNow(latlng)));
  }

  private Integer parseIntegerFromString(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
