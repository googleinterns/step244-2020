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

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.util.List;
import javax.inject.Named;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api
public class EventServlet extends HttpServlet {
  @ApiMethod(path = "events", httpMethod = ApiMethod.HttpMethod.GET)
  public void searchEvents(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Search
    // List<Event> events = null;
    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);

    // Gson gson = new Gson();
    // String json = gson.toJson(event);
    // response.getWriter().println(json);
  }

  @ApiMethod(path = "events/{event_id}", httpMethod = ApiMethod.HttpMethod.GET)
  public void getEvent(HttpServletRequest request, HttpServletResponse response, @Named("event_id") Long event_id) throws IOException {
    // Event event = EventStorage.getEvent(Long.parseLong(request.getParameter("event_id")));
    // Check an access
    Event event = null;
    response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);

    // Gson gson = new Gson();
    // String json = gson.toJson(event);
    // response.getWriter().println(json);
  }

  @ApiMethod(path = "events/{event_id}/join", httpMethod = ApiMethod.HttpMethod.POST)
  public void joinEvent(HttpServletRequest request, HttpServletResponse response, @Named("event_id") Long event_id) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // UserStorage.joinEvent(Long.parseLong(request.getParameter("event_id")));
    
    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  @ApiMethod(path = "events", httpMethod = ApiMethod.HttpMethod.POST)
  public void addEvent(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // EventStorage.addEvent();
    
    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  @ApiMethod(path = "events/{event_id}", httpMethod = ApiMethod.HttpMethod.PUT)
  public void editEvent(HttpServletRequest request, HttpServletResponse response, @Named("event_id") Long event_id) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // EventStorage.editEvent();
    
    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
