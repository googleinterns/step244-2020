package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class Utils {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);

  static String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    url.set("origin", req.getParameter("origin"));
    return url.build();
  }

  static GoogleAuthorizationCodeFlow newFlow() throws IOException, GeneralSecurityException {
    InputStream in = Utils.class.getResourceAsStream("/credentials.json");
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + "/credentials.json");
    }
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(AppEngineDataStoreFactory.getDefaultInstance()).setAccessType("offline")
            .setApprovalPrompt("force").build();
    return flow;
  }

  static Calendar loadCalendarClient() throws IOException, GeneralSecurityException {
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = newFlow().loadCredential(userId);
    credential.refreshToken();
    return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
        credential).setApplicationName("SEE").build();
  }
}
