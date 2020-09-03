package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;

@WebServlet("/token")
public class CalendarTokenServlet extends AbstractAppEngineAuthorizationCodeServlet {
  protected AuthorizationCodeFlow flow;
  protected UserService userService;

  @Inject
  CalendarTokenServlet(AuthorizationCodeFlow flow, UserService userService) {
    this.flow = flow;
    this.userService = userService;
  }

  public CalendarTokenServlet() throws IOException {
    this.userService = UserServiceFactory.getUserService();
    try {
      this.flow = Utils.newFlow();
    } catch (GeneralSecurityException e) {
      this.flow = null;
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendRedirect("/" + request.getParameter("origin") + ".html");
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return flow;
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    return userService.getCurrentUser().getUserId();
  }
}
