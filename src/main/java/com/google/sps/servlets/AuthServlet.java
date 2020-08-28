package com.google.sps.servlets;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.AuthData;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String origin = request.getParameter("origin");
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("application/json;");
    Gson gson = new Gson();
    if (userService.isUserLoggedIn()) {
      String logoutUrl = userService.createLogoutURL("/index.html");
      response.getWriter().println(gson.toJson(new AuthData(true, logoutUrl)));
    } else {
      String redirectUrl = "/token?origin=";
      if (origin == null) {
        redirectUrl += "index";
      } else {
        redirectUrl += origin;
      }
      String loginUrl = userService.createLoginURL(redirectUrl);
      response.getWriter().println(gson.toJson(new AuthData(false, loginUrl)));
    }
  }
}
