package com.google.sps.servlets;

import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;

@WebServlet("/token")
public class CalendarTokenServlet extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    try {
      return Utils.newFlow();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
    return null;
  }
}
