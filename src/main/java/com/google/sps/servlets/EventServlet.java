// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event.ExtendedProperties;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/events/*")
public class EventServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String pathName = request.getPathInfo();
    UserService userService = UserServiceFactory.getUserService();
    if (pathName.length() <= 1) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return ;
    }

    if (pathName.contains("gcalendar")) {
      if (!userService.isUserLoggedIn()) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      }
      String eventsInJson = getGCalendarEventsByInterval(request.getParameter("start"), request.getParameter("end"));
      if (eventsInJson == null)
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      else response.getWriter().println(eventsInJson);
      return ;
    }
    // TODO: parse "events/{event_id}" here.
    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private String getGCalendarEventsByInterval(String startTime, String endTime) throws IOException {
    long start = Long.parseLong(startTime);
    long end = Long.parseLong(endTime);
    DateTime startDateTime = new DateTime(start);
    DateTime endDateTime = new DateTime(end);
    try {
      Calendar service = Utils.loadCalendarClient();
      com.google.api.client.http.HttpHeaders headers = new com.google.api.client.http.HttpHeaders()
          .setAcceptEncoding("gzip").setUserAgent("gzip");
      List<com.google.api.services.calendar.model.Event> events = service.events().list("primary")
          .setFields("items(summary,start,end,description,extendedProperties,location)").setSingleEvents(true)
          .setTimeMin(startDateTime).setTimeMax(endDateTime).setRequestHeaders(headers).execute().getItems();
      return new Gson().toJson(events);
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }
}
