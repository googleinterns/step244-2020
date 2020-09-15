package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.sps.data.UserStorage;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestAuthServlet {
  @Mock
  UserService mockUserService;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Mock
  UserStorage mockUserStorage;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testAuthServlet_doGet_WithLoggedInUser_ReturnsLogoutLink() throws IOException {
    com.google.sps.data.User fakeUser = com.google.sps.data.User.newBuilder().setId("user1").setEmail(null)
        .setUsername(null).setInvitedEventsId(null).setJoinedEventsId(null).setDeclinedEventsId(null).build();
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockRequest.getParameter("origin")).thenReturn("someOrigin");
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "domain", "user1"));
    when(mockUserStorage.getUser("user1")).thenReturn(fakeUser);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new AuthServlet(mockUserService, mockUserStorage).doGet(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).createLogoutURL(anyString());
    verify(mockUserService, never()).createLoginURL(anyString());
  }

  @Test
  public void testAuthServlet_doGet_WithLoggedOutUser_ReturnsLoginLink() throws IOException {
    when(mockUserService.isUserLoggedIn()).thenReturn(false);
    when(mockRequest.getParameter("origin")).thenReturn("someOrigin");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new AuthServlet(mockUserService, mockUserStorage).doGet(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).createLoginURL("/token?origin=someOrigin");
    verify(mockUserService, never()).createLogoutURL(anyString());
  }

  @Test
  public void testAuthServlet_doGet_WithUserLoggedInAndNotInDB_AddsUserToDB() throws IOException {
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockRequest.getParameter("origin")).thenReturn("someOrigin");
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "domain", "user1"));
    when(mockUserStorage.getUser("user1")).thenReturn(null);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new AuthServlet(mockUserService, mockUserStorage).doGet(mockRequest, mockResponse);

    ArgumentCaptor<com.google.sps.data.User> argument = ArgumentCaptor.forClass(com.google.sps.data.User.class);
    verify(mockUserStorage, atLeast(1)).addOrUpdateUser(argument.capture());
    assertEquals(com.google.sps.data.User.newBuilder().setId("user1").setEmail("email").setUsername(null)
        .setInvitedEventsId(null).setJoinedEventsId(null).setDeclinedEventsId(null).build(), argument.getValue());
  }
}
