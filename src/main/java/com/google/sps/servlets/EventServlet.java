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
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalTime;
import java.text.SimpleDateFormat;
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
    String tags = request.getParameter("tags");
    String duration = request.getParameter("duration");
    String location = request.getParameter("location");
 
    List<Event> events = EventStorage.getSearchedEvents(search, tags, duration, location);

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

    addEvent(request, response, userService);
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

  private void addEvent(HttpServletRequest request, HttpServletResponse response, UserService userService)
      throws IOException, ServletException {
    String title = Objects.toString(request.getParameter("title"), "");
    String description = Objects.toString(request.getParameter("description"), "");
    String duration_parameter = request.getParameter("duration");
    Long duration = 0L;
    if (duration_parameter != null && !duration_parameter.isEmpty()) {
      try {
        duration = Long.parseLong(duration_parameter);
      } catch (Exception e) {
        System.err.println(e + duration_parameter);
      }
    }

    String location = Objects.toString(request.getParameter("location"), "");

    Map<String, String> fields = new HashMap<String, String>();
    if (request.getParameterValues("fields") != null) {
      for (String field : request.getParameterValues("fields")) {
        fields.put(field, request.getParameter(field));
      }
    }

    List<String> tags = new ArrayList<String>();
    if (request.getParameterValues("tags") != null) {
      tags = Arrays.asList(request.getParameterValues("tags"));
    }

    String current_user_id = userService.getCurrentUser().getUserId();
    List<String> participants_ids = new ArrayList<String>();
    if (request.getParameterValues("people") != null) {
      participants_ids = Arrays.asList(request.getParameterValues("people"))
                               .stream().map(person -> UserStorage.getIDbyUsername(person)).collect(Collectors.toList());
    }

    String startDateAsString = request.getParameter("start-date");
    String startTimeAsString = request.getParameter("start-time");
    String idFromGCalendarEvent = null;
    if (startDateAsString != null && startTimeAsString != null && !startDateAsString.isEmpty() 
         && !startTimeAsString.isEmpty()){// event has time set
      com.google.api.services.calendar.model.Event newEvent = createGCalendarEvent(startTimeAsString, startDateAsString,
          duration, location, description, title, fields);
      if (newEvent == null){
        response.setContentType("text/html");
        response.getWriter().print("<script>alert(\"Please login first.\")</script>");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/add_event.html");
        dispatcher.include(request, response);
        return ;
      }
      idFromGCalendarEvent = newEvent.getId();
    }
    String idForDataStoreEvent = null;
    if (idFromGCalendarEvent == null)
      idForDataStoreEvent = UUID.randomUUID().toString();
    else idForDataStoreEvent = idFromGCalendarEvent;
    Event event = new Event(idForDataStoreEvent, title, description, 
        tags,
        formatDateRange(startDateAsString),
        formatTimeRange(startTimeAsString),
        duration,
        location, 
        parseLinks(request.getParameter("links")), 
        fields,
        current_user_id, participants_ids, new ArrayList<String>(), new ArrayList<String>());

    EventStorage.addEvent(event);
    response.sendRedirect("/index.html");
  }

  private com.google.api.services.calendar.model.Event createGCalendarEvent(String timeInString, String dateInString, long durationInMinutes, 
    String location, String description, String title, Map<String, String> externalFields) throws IOException {
    com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
    event.setDescription(description).setSummary(title);
    DateTime startDateTime = new DateTime(dateInString + "T" + timeInString + ":00Z"); // Set in UTC
    DateTime endDateTime = new DateTime(startDateTime.getValue() + durationInMinutes * 60 * 1000);
    event.setStart(new EventDateTime().setDateTime(startDateTime));
    event.setEnd(new EventDateTime().setDateTime(endDateTime));

    ExtendedProperties extendedProps = new ExtendedProperties();
    extendedProps.setShared(externalFields);
    event.setExtendedProperties(extendedProps);

    String calendarId = "primary";
    try {
      Calendar service = Utils.loadCalendarClient();
      System.out.println(service);
      if (service == null){
        return null;
      }
      service.events().insert(calendarId, event).execute();
      return event;
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }

  private List<String> parseLinks(String links) {
    return Arrays.asList(links.split(","));
  }

  private String formatDateRange(String date) {  // yyyy-mm-dd -> yyyy.mm.dd-yyyy.mm.dd
    if (date != null && !date.isEmpty()) {
      date = date.replace("-", ".");
      return date + "-" + date;
    }
    return "0000.01.01-9999.12.12";
  }

  private String formatTimeRange(String time) {  // hh:mm -> hh:mm-hh:mm
    if (time != null && !time.isEmpty()) {
      return time + "-" + time;
    }
    return "00:00-23:59";
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
