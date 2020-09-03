package com.google.sps.servlets;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestTokenServlet {
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Mock
  AuthorizationCodeFlow mockFlow;
  @Mock
  UserService mockUserService;
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testReturnTokenIfUserHasOne() throws IOException, ServletException {
    Credential fakeCredential = new Credential(BearerToken.authorizationHeaderAccessMethod())
        .setAccessToken("validToken");
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "1234"));
    when(mockFlow.loadCredential("1234")).thenReturn(fakeCredential);
    when(mockRequest.getParameter("origin")).thenReturn("testOrigin");
    when(mockRequest.getMethod()).thenReturn("GET");
    new CalendarTokenServlet(mockFlow, mockUserService).service(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).getCurrentUser();
    verify(mockFlow, atLeast(1)).loadCredential("1234");
    verify(mockResponse, atLeast(1)).sendRedirect("/testOrigin.html");
  }

  @Test
  public void testRedirectToGrantPermissionIfNoToken() throws IOException, ServletException {
    AuthorizationCodeRequestUrl fakeAuthUrl = new AuthorizationCodeRequestUrl(
        "https://accounts.google.com/o/oauth2/auth", "someId");
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "1234"));
    when(mockFlow.loadCredential("1234")).thenReturn(null);
    when(mockRequest.getParameter("origin")).thenReturn("testOrigin");
    when(mockFlow.newAuthorizationUrl()).thenReturn(fakeAuthUrl);
    when(mockRequest.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/token"));
    new CalendarTokenServlet(mockFlow, mockUserService).service(mockRequest, mockResponse);

    verify(mockFlow, atLeast(1)).newAuthorizationUrl();
    verify(mockResponse, atLeast(1)).sendRedirect(Mockito.contains("oauth2callback"));
    verify(mockResponse, atLeast(1)).sendRedirect(Mockito.contains("testOrigin"));
  }
}
