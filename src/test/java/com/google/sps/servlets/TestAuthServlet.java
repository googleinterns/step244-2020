package com.google.sps.servlets;

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
    com.google.sps.data.User fakeUser = new com.google.sps.data.User("user1", null, null, null, null, null);
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

    verify(mockUserStorage, atLeast(1)).addOrUpdateUser(any());
  }
}
