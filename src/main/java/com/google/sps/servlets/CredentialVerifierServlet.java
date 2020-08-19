package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/credentials")
public class CredentialVerifierServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    AuthorizationCodeFlow flow;
    try {
      flow = Utils.newFlow();

      User user = UserServiceFactory.getUserService().getCurrentUser();
      if (user == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      response.setContentType("text/html");
      Credential credential = flow.loadCredential(user.getUserId());
      if (credential == null) {
        response.getWriter().print("false");
      } else if (credential.getAccessToken() == null) {
        if (credential.refreshToken() == false) {
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } else {
          response.getWriter().print("true");
        }
      } else {
        response.getWriter().print("true");
      }
    } catch (GeneralSecurityException e) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace();
    }
  }

}
