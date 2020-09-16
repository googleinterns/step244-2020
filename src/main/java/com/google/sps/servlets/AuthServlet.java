package com.google.sps.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.sps.data.AuthData;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
  UserService userService;
  UserStorage userStorageObject;

  @Inject
  AuthServlet(UserService userService, UserStorage userStorageObject) {
    this.userService = userService;
    this.userStorageObject = userStorageObject;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String origin = request.getParameter("origin");

    if (userService.isUserLoggedIn() && userStorageObject.getUser(userService.getCurrentUser().getUserId()) == null) {
      userStorageObject.addOrUpdateUser(User.newBuilder().setId(userService.getCurrentUser().getUserId())
          .setEmail(userService.getCurrentUser().getEmail()).setUsername(null).setInvitedEventsId(new ArrayList<>())
          .setJoinedEventsId(new ArrayList<>()).setDeclinedEventsId(new ArrayList<>()).build());
    }
    String userId = userService.isUserLoggedIn() ? userService.getCurrentUser().getUserId() : null;
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(new AuthData(userService.isUserLoggedIn(), buildAuthLink(origin), userId)));
  }

  private String buildAuthLink(String origin) {
    return userService.isUserLoggedIn() ? userService.createLogoutURL("/index.html")
        : userService.createLoginURL("/token?origin=" + Objects.toString(origin, "index"));
  }
}
