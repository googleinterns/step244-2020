package com.google.sps.servlets;

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

import com.google.appengine.api.users.UserService;

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
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testAuthServlet_doGet_WithLoggedInUser_ReturnsLogoutLink() throws IOException {
    when(mockUserService.isUserLoggedIn()).thenReturn(true);
    when(mockRequest.getParameter("origin")).thenReturn("someOrigin");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new AuthServlet(mockUserService).doGet(mockRequest, mockResponse);

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

    new AuthServlet(mockUserService).doGet(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).createLoginURL("/token?origin=someOrigin");
    verify(mockUserService, never()).createLogoutURL(anyString());
  }
}
