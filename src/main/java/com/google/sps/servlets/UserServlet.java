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
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.gson.Gson;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/users/*")
public class UserServlet extends HttpServlet {
  UserStorage userStorageObject;
  EventStorage eventStorageObject;
  UserService userService;

  @Inject
  UserServlet(UserStorage userStorageObject, EventStorage eventStorageObject, UserService userService) {
    this.userStorageObject = userStorageObject;
    this.eventStorageObject = eventStorageObject;
    this.userService = userService;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    User user = userStorageObject.getUser(userService.getCurrentUser().getUserId());
    if (user == null) {
      response.setContentType("text/html");
      response.getWriter().print("<script>alert(\"Please login first.\")</script>");
      RequestDispatcher dispatcher = request.getRequestDispatcher("/user.html");
      dispatcher.include(request, response);
      return;
    }

    List<Event> joinedEvents = getEventsFromIds(user.getJoinedEventsID());
    List<Event> invitedEvents = getEventsFromIds(user.getInvitedEventsID());
    List<Event> declinedEvents = getEventsFromIds(user.getDeclinedEventsID());
    invitedEvents.removeAll(joinedEvents);
    invitedEvents.removeAll(declinedEvents);
    Gson gson = new Gson();
    JsonElement userJson = gson.toJsonTree(user);
    JsonElement joinedEventsJson = gson.toJsonTree(joinedEvents);
    JsonElement invitedEventsJson = gson.toJsonTree(invitedEvents);
    JsonElement declinedEventsJson = gson.toJsonTree(declinedEvents);
    userJson.getAsJsonObject().add("joinedEvents", joinedEventsJson);
    userJson.getAsJsonObject().add("invitedEvents", invitedEventsJson);
    userJson.getAsJsonObject().add("declinedEvents", declinedEventsJson);

    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(userJson));
    return;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    if (request.getPathInfo().equals("/username")) {
      String nickname = request.getParameter("nickname");
      if (nickname == null || nickname.isEmpty()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }
      User user = userStorageObject.getUser(userService.getCurrentUser().getUserId());
      if (user == null) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      }
      user.setUsername(nickname);
      userStorageObject.addOrUpdateUser(user);
      response.sendRedirect("/user.html");
      return;
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  private List<Event> getEventsFromIds(List<String> idList) {
    List<Event> eventList = new ArrayList<>();
    for (String id : idList) {
      //TODO Add datastore batching
      Event queriedEvent = eventStorageObject.getEvent(id);
      if (queriedEvent == null)
        continue;
      eventList.add(queriedEvent);
    }
    return eventList;
  }
}
