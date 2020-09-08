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
import com.google.sps.data.AuthData;
import com.google.sps.data.User;
import com.google.sps.data.UserStorage;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String origin = request.getParameter("origin");
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn() && UserStorage.getUser(userService.getCurrentUser().getUserId()) == null) {
      UserStorage.addOrUpdateUser(new User(userService.getCurrentUser().getUserId(),
                                           userService.getCurrentUser().getEmail(),
                                           null,
                                           new ArrayList<>(),
                                           new ArrayList<>(),
                                           new ArrayList<>()));
    }
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(new AuthData(userService.isUserLoggedIn(), buildAuthLink(userService, origin))));
  }

  private String buildAuthLink(UserService userService, String origin) {
    return userService.isUserLoggedIn() ? userService.createLogoutURL("/index.html")
        : userService.createLoginURL("/token?origin=" + Objects.toString(origin, "index"));
  }
}
