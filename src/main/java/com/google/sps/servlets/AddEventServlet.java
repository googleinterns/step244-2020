package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for adding an event. */
@WebServlet("/add-event")
public class AddEventServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Make an Entity of event.
    Entity eventEntity = new Entity("Event");
    String uid = userService.getCurrentUser().getUserId();

    eventEntity.setProperty("title", request.getParameter("title"));
    eventEntity.setProperty("type", request.getParameter("type"));
    eventEntity.setProperty("description", request.getParameter("description"));
    eventEntity.setProperty("date", request.getParameter("date"));
    eventEntity.setProperty("time", request.getParameter("time"));
    eventEntity.setProperty("location", request.getParameter("location"));
    eventEntity.setProperty("links", request.getParameter("links"));
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(eventEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}
