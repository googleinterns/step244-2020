package com.google.sps.servlets;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.FreeBusyCalendar;
import com.google.api.services.calendar.model.FreeBusyRequest;
import com.google.api.services.calendar.model.FreeBusyRequestItem;
import com.google.api.services.calendar.model.FreeBusyResponse;
import com.google.api.services.calendar.model.TimePeriod;
import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.users.UserService;
import com.google.gson.Gson;
import com.google.sps.data.DateTimeRange;
import com.google.sps.data.EventStorage;
import com.google.sps.data.Time;

@WebServlet("/freetimes")
public class EventFreeTimeServlet extends HttpServlet {
  AuthorizationCodeFlow flow;
  EventStorage eventStorageObject;
  UserService userService;

  @Inject
  EventFreeTimeServlet(AuthorizationCodeFlow flow, EventStorage eventStorageObject, UserService userService) {
    this.flow = flow;
    this.eventStorageObject = eventStorageObject;
    this.userService = userService;
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Set<Time> busyTimesForAttendees = new TreeSet<>();
    response.setContentType("text/html");

    JsonBatchCallback<FreeBusyResponse> callback = new JsonBatchCallback<FreeBusyResponse>() {

      public void onSuccess(FreeBusyResponse apiResponse, HttpHeaders responseHeaders) throws IOException {
        FreeBusyCalendar busyCalendar = apiResponse.getCalendars().get("primary");
        List<TimePeriod> listBusyTimes = busyCalendar.getBusy();
        listBusyTimes.forEach(busyTime -> {
          busyTimesForAttendees.add(new Time(busyTime.getStart().getValue(), busyTime.getEnd().getValue()));
        });
      }

      public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
        System.err.println("Error Message: " + e.getMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    };

    String eventId = request.getParameter("eventId");
    if (eventId == null || eventStorageObject.getEvent(eventId) == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DateTimeRange timeRange = eventStorageObject.getEvent(eventId).getDateTimeRange();
    Long duration = eventStorageObject.getEvent(eventId).getDuration();
    if (timeRange.isDateTimeSet()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    DateTime startDateTime = new DateTime(timeRange.convertStartTimeToString());
    DateTime startDateTimeWithShift = new DateTime(startDateTime.getValue() + timeRange.getShift());
    DateTime endDateTime = new DateTime(timeRange.convertEndTimeToString());
    DateTime endDateTimeWithShift = new DateTime(endDateTime.getValue() + timeRange.getShift());
    BatchRequest batchRequest = new BatchRequest(flow.getTransport(), flow.getRequestInitializer())
        .setBatchUrl(new GenericUrl("https://www.googleapis.com/batch/calendar/v3"));
    String ownerId = eventStorageObject.getEvent(eventId).getOwnerID();

    if (userService.getCurrentUser() == null || !userService.getCurrentUser().getUserId().equals(ownerId)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    List<String> joinedParticipantsIds = eventStorageObject.getEvent(eventId).getJoinedIDs();
    if (!joinedParticipantsIds.contains(ownerId)) {
      joinedParticipantsIds.add(ownerId);
    }

    List<Credential> usersCredentials;
    try {
      usersCredentials = getCredentialsFromUserList(joinedParticipantsIds);
    } catch (InterruptedException e2) {
      e2.printStackTrace();
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    usersCredentials.forEach(userCredential -> {
      try {
        createAndAddBatchRequest(batchRequest, callback, userCredential, startDateTimeWithShift, endDateTimeWithShift);
        batchRequest.execute();
        returnFreeTimesFromInterval(startDateTimeWithShift.getValue(), endDateTimeWithShift.getValue(), duration, busyTimesForAttendees, response);
        return;
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      } catch (GeneralSecurityException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      return;
    });

  }

  private void createAndAddBatchRequest(BatchRequest batchRequest, JsonBatchCallback<FreeBusyResponse> callback,
      Credential userCredential, DateTime start, DateTime end) throws IOException, GeneralSecurityException {
    Calendar userCalendar = loadCalendarClient(userCredential);
    FreeBusyRequest fBusyRequest = new FreeBusyRequest().setTimeMin(start).setTimeMax(end)
        .setItems(Collections.singletonList(new FreeBusyRequestItem().setId("primary")));
    HttpRequest request = userCalendar.freebusy().query(fBusyRequest).buildHttpRequest();
    batchRequest.queue(request, FreeBusyResponse.class, GoogleJsonErrorContainer.class, callback);
  }

  private void returnFreeTimesFromInterval(Long start, Long end, Long duration, Set<Time> busyTimes, HttpServletResponse response)
      throws IOException {
    List<Time> freeTimes = new ArrayList<>();
    Long shift = Long.valueOf(1000 * 60 * 15);
    Long currentStart = start;
    Long currentEnd = start + duration * 60 * 1000;
    while (currentEnd <= end) {
      boolean overlap = false;
      for (Time busyTime : busyTimes) {
        if (busyTime.getEnd() <= currentStart)
          continue;
        if (busyTime.getStart() >= currentEnd)
          break; // Ordered by start, so we can break early
        overlap = true;
        break;
      }
      if (!overlap)
        freeTimes.add(new Time(currentStart, currentEnd));
      currentStart = currentStart + shift;
      currentEnd += shift;
    }
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(freeTimes));

    return ;
  }

  private List<Credential> getCredentialsFromUserList(List<String> userIds) throws InterruptedException {
    List<Credential> usersCredential = new ArrayList<>();
    ExecutorService executorService = Executors.newCachedThreadPool(ThreadManager.currentRequestThreadFactory());

    userIds.forEach(userId -> {
      executorService.execute(ThreadManager.createThreadForCurrentRequest(() -> {
        try {
          usersCredential.add(flow.loadCredential(userId));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }));
    });
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS); // Await max 10 seconds before returning
    return usersCredential;
  }

  private Calendar loadCalendarClient(Credential credential) throws IOException, GeneralSecurityException {
    if (credential == null)
      return null;
    return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
        credential).setApplicationName("SEE").build();
  }
}
