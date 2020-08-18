package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.util.Arrays;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for adding an event. */
@WebServlet("/event")
public class AddEventServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // UserService userService = UserServiceFactory.getUserService();
    // if (!userService.isUserLoggedIn()) {
    //   response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    //   return;
    // }
    // String uid = userService.getCurrentUser().getUserId();

    // Make an Entity of event.
    Entity eventEntity = new Entity("Event");

    Entity eventInfoEntity = new Entity("EventInfo", eventEntity.getKey());
    eventInfoEntity.setProperty("title", request.getParameter("title"));
    eventInfoEntity.setProperty("type", request.getParameter("type"));
    eventInfoEntity.setProperty("description", request.getParameter("description"));
    eventInfoEntity.setProperty("start-date", request.getParameter("start-date"));
    eventInfoEntity.setProperty("start-time", request.getParameter("start-time"));
    eventInfoEntity.setProperty("end-date", request.getParameter("end-date"));
    eventInfoEntity.setProperty("end-time", request.getParameter("end-time"));
    eventInfoEntity.setProperty("location", request.getParameter("location"));
    eventInfoEntity.setProperty("links", request.getParameter("links")); 
    if (request.getParameterValues("custom-fields") != null) {
      eventInfoEntity.setProperty("custom-fields", Arrays.asList(request.getParameterValues("custom-fields")));
      for (String custom_field : request.getParameterValues("custom-fields")) {
        String custom_value = request.getParameter(custom_field);
        eventInfoEntity.setProperty(custom_field, custom_value);
      }
    }

    Entity eventParticipantsEntity = new Entity("EventParticipants", eventEntity.getKey());
    eventParticipantsEntity.setProperty("participating-people", "" /*current person*/);
    if (request.getParameterValues("people") != null) {
      eventParticipantsEntity.setProperty("invited-people", Arrays.asList(request.getParameterValues("people")));
    }
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);
    datastore.put(eventInfoEntity);
    datastore.put(eventParticipantsEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
