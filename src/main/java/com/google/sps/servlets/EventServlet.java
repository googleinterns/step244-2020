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
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.DateTimeRange;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import com.google.api.client.http.HttpHeaders;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
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

    if (pathName != null && pathName.equals("/gcalendar")) {
      getEvents(request, response, userService);
      return;
    }

    String search = request.getParameter("search");
    String category = request.getParameter("category");
    String duration = request.getParameter("duration");
    String location = request.getParameter("location");
 
    List<Event> events = EventStorage.getSearchedEvents(search, category, duration, location);

    Gson gson = new Gson();
    
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(events));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (addEvent(request, response, userService)) {
      response.sendRedirect("/index.html");
    } else {
      // TODO: Write message to user
    }
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

  private boolean addEvent(HttpServletRequest request, HttpServletResponse response, UserService userService)
      throws IOException, ServletException {
    String title = Objects.toString(request.getParameter("title").trim(), "");
    String description = Objects.toString(request.getParameter("description").trim(), "");
    String category = Objects.toString(request.getParameter("category").trim(), "");

    List<String> tags = new ArrayList<String>();
    if (request.getParameterValues("tags") != null) {
      tags = Arrays.asList(Arrays.stream(request.getParameterValues("tags")).map(String::trim).toArray(String[]::new));
    }
    
    String startDate = request.getParameter("start-date");
    String startTime = request.getParameter("start-time");
    
    String durationParameter = request.getParameter("duration").trim();
    Long duration = 0L;
    if (durationParameter != null && !durationParameter.isEmpty()) {
      try {
        duration = Long.parseLong(durationParameter);
      } catch (Exception e) {
        System.err.println(e + durationParameter);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return false;
      }
    }

    String location = Objects.toString(request.getParameter("location").trim(), "");

    Map<String, String> fields = new HashMap<String, String>();
    if (request.getParameterValues("fields") != null) {
      for (String field : request.getParameterValues("fields")) {
        if (request.getParameter(field) != null) {
          fields.put(field.trim(), request.getParameter(field).trim());
        }
      }
    }

    String currentUserId = null;
    try {
      currentUserId = userService.getCurrentUser().getUserId();
    } catch (Exception e) {
      System.err.println("Can't get current user id: " + e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return false;
    }
    
    List<String> participantsIds = new ArrayList<String>();
    if (request.getParameterValues("people") != null) {
      participantsIds = Arrays.asList(request.getParameterValues("people"))
                               .stream().map(person -> UserStorage.getIDbyUsername(person)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    
    String gcalendarId = null;

    Event event = new Event(UUID.randomUUID().toString(), gcalendarId, title, description, 
        category, tags,
        formatDateTimeRange(startDate, startTime),
        duration,
        location, 
        parseLinks(request.getParameter("links")), 
        fields,
        currentUserId, participantsIds, new ArrayList<String>(), new ArrayList<String>()
    );

    
    if (event.isDateTimeSet()) {
      com.google.api.services.calendar.model.Event newEvent = Utils.createGCalendarEvent(event);
      if (newEvent == null) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return false;
      }
      gcalendarId = newEvent.getId();
    }

    if (gcalendarId != null) {
      event.setGCalendarID(gcalendarId);
    }

    try {
      EventStorage.addEvent(event);
    } catch (Exception e) {
      System.err.println("Can't add new event to storage: " + e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return false;
    }
    
    return true;
  }

  private List<String> parseLinks(String links) {
    return Arrays.asList(links.split(","));
  }

  private DateTimeRange formatDateTimeRange(String date, String time) {
    return new DateTimeRange(date, time);
  }

  private void getEvents(HttpServletRequest request, HttpServletResponse response, UserService userService)
      throws IOException {
    String startEpochInSeconds = request.getParameter("startEpochInSeconds");
    String endEpochInSeconds = request.getParameter("endEpochInSeconds");

    if (startEpochInSeconds == null || endEpochInSeconds == null || !startEpochInSeconds.matches("^[0-9]+$")
        || !endEpochInSeconds.matches("^[0-9]+$")) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Long startEpochInSecondsParsed = parseLongFromString(startEpochInSeconds);
    Long endEpochInSecondsParsed = parseLongFromString(endEpochInSeconds);

    if (startEpochInSecondsParsed < 0 || endEpochInSecondsParsed < 0
        || startEpochInSecondsParsed > endEpochInSecondsParsed) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String eventsInJson = getGCalendarEventsByInterval(startEpochInSecondsParsed, endEpochInSecondsParsed);
    if (eventsInJson == null)
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    else
      response.getWriter().println(eventsInJson);
    return;
  }

  private long parseLongFromString(String str) {
    try {
      long nr = Long.parseLong(str);
      return nr;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private String getGCalendarEventsByInterval(Long startEpochInSeconds, Long endEpochInSeconds) throws IOException {
    DateTime startDateTime = new DateTime(startEpochInSeconds);
    DateTime endDateTime = new DateTime(endEpochInSeconds);
    try {
      Calendar service = Utils.loadCalendarClient();
      HttpHeaders headers = new com.google.api.client.http.HttpHeaders()
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
