package com.google.sps.servlets;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import com.google.api.services.calendar.model.Event.ExtendedProperties;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Time;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Utils {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Arrays
      .asList(new String[] { CalendarScopes.CALENDAR_EVENTS, CalendarScopes.CALENDAR_READONLY });

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
    DateTime startDateTimeWithShift = new DateTime(startDateTime.getValue() + event.getDateTimeRange().getShift());
    DateTime endDateTime = new DateTime(startDateTimeWithShift.getValue() + event.getDuration() * 60 * 1000);
    gcalendarEvent.setStart(new EventDateTime().setDateTime(startDateTimeWithShift));
    gcalendarEvent.setEnd(new EventDateTime().setDateTime(endDateTime));

    if (event.getLocation() != null) {
      gcalendarEvent.setLocation(event.getLocation());
    }
    
    List<String> joinedParticipants = event.getJoinedIDs();
    if (joinedParticipants != null) {
      List<String> participantsEmails = DatastoreServiceFactory.getDatastoreService()
          .get(joinedParticipants.stream().map(userId -> KeyFactory.createKey("User", userId))
              .collect(Collectors.toList()))
          .values().stream().map(entity -> (String) entity.getProperty("email")).collect(Collectors.toList());
      gcalendarEvent.setAttendees(
          participantsEmails.stream().map(email -> new EventAttendee().setEmail(email).setResponseStatus("accepted"))
              .collect(Collectors.toList()));
    }

    ExtendedProperties extendedProps = new ExtendedProperties();
    Map<String, String> allFields = new HashMap<>();
    allFields.putAll(event.getFields());
    allFields.put("seeYouId", event.getID());
    extendedProps.setShared((Map) allFields);
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
    EventAttendee newAttendee = new EventAttendee().setEmail(userToBeAddedEmail).setResponseStatus("accepted");
    if (currentAttendees == null)
      currentAttendees = new ArrayList<>();
    currentAttendees.add(newAttendee);
    originalEvent.setAttendees(currentAttendees);
    ownerCalendar.events().patch("primary", eventId, originalEvent).setSendUpdates("all").execute();
    return;
  }

  protected Vector<Time> getBusyTimesForAttendees(HttpServletResponse response, AuthorizationCodeFlow flow,
      List<Credential> usersCredentials, List<String> participantsIds, DateTime startDateTimeWithShift,
      DateTime endDateTimeWithShift) {
    Vector<Time> busyTimesForAttendees = new Vector<>();
    AtomicInteger atomicInt = new AtomicInteger(0);
    JsonBatchCallback<FreeBusyResponse> callback = new JsonBatchCallback<FreeBusyResponse>() {

      public void onSuccess(FreeBusyResponse apiResponse, HttpHeaders responseHeaders) throws IOException {
        FreeBusyCalendar freeBusyCalendar = apiResponse.getCalendars().get("primary");
        String userIdForThisRequest = participantsIds.get(atomicInt.getAndIncrement());
        List<TimePeriod> userBusyTimes = freeBusyCalendar.getBusy();
        userBusyTimes.forEach(busyTime -> {
          busyTimesForAttendees
              .add(new Time(busyTime.getStart().getValue(), busyTime.getEnd().getValue(), userIdForThisRequest));
        });
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        System.err.println("Error Message: " + e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    };
    BatchRequest batchRequest = new BatchRequest(flow.getTransport(), flow.getRequestInitializer())
        .setBatchUrl(new GenericUrl("https://www.googleapis.com/batch/calendar/v3"));

    usersCredentials.forEach(userCredential -> {
      try {
        createAndAddBatchRequest(batchRequest, callback, userCredential, startDateTimeWithShift, endDateTimeWithShift);
      } catch (IOException e1) {
        e1.printStackTrace();
      } catch (GeneralSecurityException e1) {
        e1.printStackTrace();
      }
    });
    try {
      batchRequest.execute();
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return busyTimesForAttendees;
  }

  private void createAndAddBatchRequest(BatchRequest batchRequest, JsonBatchCallback<FreeBusyResponse> callback,
      Credential userCredential, DateTime start, DateTime end) throws IOException, GeneralSecurityException {
    Calendar userCalendar = loadCalendarClient(userCredential);
    FreeBusyRequest fBusyRequest = new FreeBusyRequest().setTimeMin(start).setTimeMax(end)
        .setItems(Collections.singletonList(new FreeBusyRequestItem().setId("primary")));
    HttpRequest request = userCalendar.freebusy().query(fBusyRequest).buildHttpRequest();
    batchRequest.queue(request, FreeBusyResponse.class, GoogleJsonErrorContainer.class, callback);
  }

  private Calendar loadCalendarClient(Credential credential) throws IOException, GeneralSecurityException {
    if (credential == null)
      return null;
    return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
        credential).setApplicationName("SEE").build();
  }

}
