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

    // Make an Entity of event.
    Entity eventEntity = new Entity("Event");
    // String uid = userService.getCurrentUser().getUserId();

    eventEntity.setProperty("title", request.getParameter("title"));
    eventEntity.setProperty("type", request.getParameter("type"));
    eventEntity.setProperty("description", request.getParameter("description"));
    eventEntity.setProperty("start-date", request.getParameter("start-date"));
    eventEntity.setProperty("start-time", request.getParameter("start-time"));
    eventEntity.setProperty("end-date", request.getParameter("end-date"));
    eventEntity.setProperty("end-time", request.getParameter("end-time"));
    eventEntity.setProperty("location", request.getParameter("location"));
    eventEntity.setProperty("links", request.getParameter("links"));
    
    for (String custom_field : request.getParameterValues("custom-fields")) {
      String custom_value = request.getParameter(custom_field);
      eventEntity.setProperty(custom_field, custom_value);
    }

    eventEntity.setProperty("people", request.getParameterValues("people"));

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
