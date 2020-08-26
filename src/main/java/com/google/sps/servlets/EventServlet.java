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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Status;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.text.SimpleDateFormat;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/events")
public class EventServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
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

    String title = Objects.toString(request.getParameter("title"), "");
    String description = Objects.toString(request.getParameter("description"), "");
    String location = Objects.toString(request.getParameter("location"), "");

    Map<String, String> fields = new HashMap<String, String>();
    if (request.getParameterValues("fields") != null) {
      for (String field : request.getParameterValues("fields")) {
        String value = request.getParameter(field);
        fields.put(field, value);
      }
    }
    
    List<String> tags = new ArrayList<String>();
    if (request.getParameterValues("tags") != null) {
      tags = Arrays.asList(request.getParameterValues("tags"));
    }

    Map<String, String> participants_status_by_id = new HashMap<String, String>();
    String current_user_id = userService.getCurrentUser().getUserId();
    participants_status_by_id.put(current_user_id, Status.OWNER);
    if (request.getParameterValues("people") != null) {
      List<String> participants_ids = Arrays.asList(request.getParameterValues("people"))
                                         .stream().map(person -> UserStorage.getIDbyUsername(person)).collect(Collectors.toList());
      for (String id : participants_ids) {
        participants_status_by_id.put(id, Status.INVITED);
      }
    }

    Event event = new Event(UUID.randomUUID().toString(), title, description, 
        tags, 
        formatDate(request.getParameter("start-date")), formatTime(request.getParameter("start-time")),
        formatDate(request.getParameter("end-date")), formatTime(request.getParameter("end-time")),
        location, 
        parseLinks(request.getParameter("links")), 
        fields, 
        participants_status_by_id);

    EventStorage.addEvent(event);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private List<String> parseLinks(String links) {
    return Arrays.asList(links.split(","));
  }

  // yyyy-MM-dd -> Date
  private Date formatDate(String date) {
    if (date != null) {
      try {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
      }
      catch (Exception e) {
        System.err.println(e.toString() + ", " + date);
      }
    }
    return null;
  }

  // HH:mm -> Date
  private Date formatTime(String time) {
    if (time != null) {
      try {
        return new SimpleDateFormat("HH:mm").parse(time);
      }
      catch (Exception e) {
        System.err.println(e.toString() + ", " + time);
      }
    }
    return null;
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
}
