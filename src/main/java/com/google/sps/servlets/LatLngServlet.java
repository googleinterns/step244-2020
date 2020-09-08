package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import com.google.sps.data.GeoCoding;
import com.google.gson.Gson;

@WebServlet("/latlng")
public class LatLngServlet extends HttpServlet {
  GeoCoding GeoCodingObject;

  @Inject
  LatLngServlet(GeoCoding GeoCodingObject) {
    this.GeoCodingObject = GeoCodingObject;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String location = request.getParameter("location");
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(GeoCodingObject.fromAddressToLatLng(location)));
  }
}
