package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/credentials")
public class CredentialVerifierServlet extends HttpServlet {
  private AuthorizationCodeFlow flow;
  private UserService userService;

  @Inject
  CredentialVerifierServlet(AuthorizationCodeFlow flow, UserService userService) {
    this.flow = flow;
    this.userService = userService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("text/html");
    User user = userService.getCurrentUser();
    if (user == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }
    Credential credential = flow.loadCredential(user.getUserId());
    if (credential == null || credential.getAccessToken() == null) {
      response.getWriter().print("false");
      return;
    }
    response.getWriter().print("true");
    return;
  }

}
