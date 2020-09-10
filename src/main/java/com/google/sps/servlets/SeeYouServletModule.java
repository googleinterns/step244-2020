package com.google.sps.servlets;

import com.google.inject.servlet.ServletModule;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Provides;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Singleton;

public class SeeYouServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    bind(AuthServlet.class).in(Singleton.class);
    bind(CalendarTokenServlet.class).in(Singleton.class);
    bind(OAuth2Callback.class).in(Singleton.class);
    bind(CredentialVerifierServlet.class).in(Singleton.class);
    bind(EventServlet.class).in(Singleton.class);
    bind(UserServlet.class).in(Singleton.class);
    bind(EventFreeTimeServlet.class).in(Singleton.class);
    serve("/auth").with(AuthServlet.class);
    serve("/token").with(CalendarTokenServlet.class);
    serve("/oauth2callback").with(OAuth2Callback.class);
    serve("/credentials").with(CredentialVerifierServlet.class);
    serve("/events/*", "/events").with(EventServlet.class);
    serve("/users/*", "/users").with(UserServlet.class);
    serve("/freetimes").with(EventFreeTimeServlet.class);
  }

  @Provides
  UserService provideUserService() {
    return UserServiceFactory.getUserService();
  }

  @Provides
  AuthorizationCodeFlow provideFlow() throws IOException, GeneralSecurityException {
    return Utils.newFlow();
  }
}
