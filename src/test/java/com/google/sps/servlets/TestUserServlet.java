package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.sps.data.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestUserServlet {
  @Mock
  UserService mockUserService;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Mock
  UserStorage mockUserStorage;
  @Mock
  EventStorage mockEventStorage;
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testUserServlet_doGet_WithUserHavingEvents_ReturnEventLists() throws IOException, ServletException {
    com.google.sps.data.User fakeUser = com.google.sps.data.User.newBuilder().setId("123").setEmail("email")
        .setUsername("username").setInvitedEventsId(Arrays.asList(new String[] { "firstId" })).setJoinedEventsId(null)
        .setDeclinedEventsId(null).build();

    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "123"));
    when(mockUserStorage.getUser("123")).thenReturn(fakeUser);
    when(mockEventStorage.getEvent("firstId"))
        .thenReturn(Event.newBuilder().setID("firstId").setOwnerID("123").setTitle("title").build());
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(printWriter);

    new UserServlet(mockUserStorage, mockEventStorage, mockUserService).doGet(mockRequest, mockResponse);

    JsonElement json = JsonParser.parseString(stringWriter.toString());
    JsonObject jsonObject = json.getAsJsonObject();
    Gson gson = new Gson();
    List<LinkedTreeMap> invitedEvents = gson.fromJson(jsonObject.get("invitedEvents"), List.class);
    List<LinkedTreeMap> joinedEvents = gson.fromJson(jsonObject.get("joinedEvents"), List.class);
    List<LinkedTreeMap> declinedEvents = gson.fromJson(jsonObject.get("declinedEvents"), List.class);
    assertTrue(invitedEvents.size() == 1);
    assertTrue(joinedEvents.size() == 0);
    assertTrue(declinedEvents.size() == 0);
    Event parsedEvent = gson.fromJson(JsonParser.parseString(invitedEvents.get(0).toString()), Event.class);
    assertTrue(parsedEvent.getTitle().equals("title"));
    assertTrue(parsedEvent.getOwnerID().equals("123"));
    assertTrue(parsedEvent.getID().equals("firstId"));
  }

  @Test
  public void testUserServlet_doGet_WithInvitedAndJoinedEvents_RemovesJoinedFromInvited()
      throws IOException, ServletException {
    com.google.sps.data.User fakeUser = com.google.sps.data.User.newBuilder().setId("123").setEmail("email")
        .setUsername("username").setInvitedEventsId(Arrays.asList(new String[] { "firstId" }))
        .setJoinedEventsId(Arrays.asList(new String[] { "secondId" })).setDeclinedEventsId(null).build();

    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "123"));
    when(mockUserStorage.getUser("123")).thenReturn(fakeUser);
    when(mockEventStorage.getEvent("firstId"))
        .thenReturn(Event.newBuilder().setID("firstId").setOwnerID("123").setTitle("title").build());
    when(mockEventStorage.getEvent("secondId"))
        .thenReturn(Event.newBuilder().setID("secondId").setOwnerID("124").setTitle("joinedTitle").build());
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(printWriter);

    new UserServlet(mockUserStorage, mockEventStorage, mockUserService).doGet(mockRequest, mockResponse);

    JsonElement json = JsonParser.parseString(stringWriter.toString());
    JsonObject jsonObject = json.getAsJsonObject();
    Gson gson = new Gson();
    List<LinkedTreeMap> invitedEvents = gson.fromJson(jsonObject.get("invitedEvents"), List.class);
    List<LinkedTreeMap> joinedEvents = gson.fromJson(jsonObject.get("joinedEvents"), List.class);
    List<LinkedTreeMap> declinedEvents = gson.fromJson(jsonObject.get("declinedEvents"), List.class);
    assertTrue(invitedEvents.size() == 1);
    assertTrue(joinedEvents.size() == 1);
    assertTrue(declinedEvents.size() == 0);
    Event parsedJoinedEvent = gson.fromJson(JsonParser.parseString(joinedEvents.get(0).toString()), Event.class);
    assertTrue(parsedJoinedEvent.getID().equals("secondId"));
  }

  @Test
  public void testUserServlet_doPost_WithUserLoggedIn_SetsUsername() throws IOException, ServletException {
    com.google.sps.data.User fakeUser = com.google.sps.data.User.newBuilder().setId("123").setEmail("email")
        .setUsername("username").setInvitedEventsId(Arrays.asList(new String[] { "firstId", "secondId" }))
        .setJoinedEventsId(Arrays.asList(new String[] { "secondId" })).setDeclinedEventsId(null).build();
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockRequest.getParameter("nickname")).thenReturn("andru47");
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "123"));
    when(mockUserStorage.getUser("123")).thenReturn(fakeUser);
    when(mockRequest.getPathInfo()).thenReturn("/username");

    new UserServlet(mockUserStorage, mockEventStorage, mockUserService).doPost(mockRequest, mockResponse);

    ArgumentCaptor<com.google.sps.data.User> argument = ArgumentCaptor.forClass(com.google.sps.data.User.class);
    assertTrue(fakeUser.getUsername().equals("andru47"));
    verify(mockUserStorage, atLeast(1)).addOrUpdateUser(argument.capture());
    fakeUser.setUsername("andru47");
    assertEquals(fakeUser, argument.getValue());
  }
}
