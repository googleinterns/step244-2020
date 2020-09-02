package com.google.sps.servlets;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;

import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TestCredVerifierServlet {
  Credential mockCredential;
  StringWriter stringWriter;
  PrintWriter writer;
  @Mock
  AuthorizationCodeFlow mockFlow;
  @Mock
  UserService mockUserService;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;

  @Before
  public void setUp() throws IOException {
    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    mockCredential = new Credential(BearerToken.authorizationHeaderAccessMethod());
    when(mockUserService.getCurrentUser()).thenReturn(new User("email", "authDomain", "1234"));
    when(mockFlow.loadCredential("1234")).thenReturn(mockCredential);
    when(mockResponse.getWriter()).thenReturn(writer);
  }

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void credentialIsTrueIfUserHasToken() throws IOException, ServletException {
    mockCredential.setAccessToken("valid");
    new CredentialVerifierServlet(mockFlow, mockUserService).doGet(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).getCurrentUser();
    verify(mockFlow, atLeast(1)).loadCredential("1234");
    writer.flush();
    assertTrue(stringWriter.toString().contains("true"));
  }

  @Test
  public void credentialIsFalseIfNoToken() throws ServletException, IOException {
    mockCredential.setAccessToken(null);
    new CredentialVerifierServlet(mockFlow, mockUserService).doGet(mockRequest, mockResponse);

    verify(mockUserService, atLeast(1)).getCurrentUser();
    verify(mockFlow, atLeast(1)).loadCredential("1234");
    writer.flush();
    assertTrue(stringWriter.toString().contains("false"));
  }
}
