package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Event;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
    if (credential == null)
      return null;
    return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
        credential).setApplicationName("SEE").build();
  }

  static com.google.api.services.calendar.model.Event createGCalendarEvent(com.google.sps.data.Event event)
      throws IOException {
    com.google.api.services.calendar.model.Event gcalendarEvent = new com.google.api.services.calendar.model.Event();
    gcalendarEvent.setDescription(event.getDescription()).setSummary(event.getTitle());
    DateTime startDateTime = new DateTime(event.getDateTimeAsString());
    DateTime endDateTime = new DateTime(startDateTime.getValue() + event.getDuration() * 60 * 1000);
    gcalendarEvent.setStart(new EventDateTime().setDateTime(startDateTime));
    gcalendarEvent.setEnd(new EventDateTime().setDateTime(endDateTime));

    ExtendedProperties extendedProps = new ExtendedProperties();
    extendedProps.setShared(event.getFields());
    gcalendarEvent.setExtendedProperties(extendedProps);
    try {
      Calendar service = loadCalendarClient();
      if (service == null) {
        return null;
      }
      gcalendarEvent = service.events().insert("primary", gcalendarEvent).setSendUpdates("all").execute();
      return gcalendarEvent;
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
      return null;
    }
  }

  static void joinGCalendarEvent(String ownerId, String eventId, String userToBeAddedEmail)
      throws GeneralSecurityException, IOException {
    Credential credential = newFlow().loadCredential(ownerId);
    Calendar ownerCalendar = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        JacksonFactory.getDefaultInstance(), credential).setApplicationName("SEE").build();
    com.google.api.services.calendar.model.Event originalEvent = ownerCalendar.events().get("primary", eventId)
        .execute();
    List<EventAttendee> currentAttendees = originalEvent.getAttendees();
    EventAttendee newAttendee = new EventAttendee().setEmail(userToBeAddedEmail)
        .setResponseStatus("accepted");
    if (currentAttendees == null)
      currentAttendees = new ArrayList<>();
    currentAttendees.add(newAttendee);
    originalEvent.setAttendees(currentAttendees);
    ownerCalendar.events().patch("primary", eventId, originalEvent).setSendUpdates("all").execute();
    return ;
  }

}
