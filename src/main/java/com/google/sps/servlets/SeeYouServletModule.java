package com.google.sps.servlets;

import com.google.inject.servlet.ServletModule;
import com.google.sps.data.ApiKeys;
import com.google.sps.data.GeoCoding;
import com.google.sps.data.Weather;
import com.google.sps.data.EventStorage;
import com.google.sps.data.UserStorage;
import com.google.maps.GeoApiContext;
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
    bind(WeatherServlet.class).in(Singleton.class);
    serve("/auth").with(AuthServlet.class);
    serve("/token").with(CalendarTokenServlet.class);
    serve("/oauth2callback").with(OAuth2Callback.class);
    serve("/credentials").with(CredentialVerifierServlet.class);
    serve("/events/*", "/events").with(EventServlet.class);
    serve("/users/*", "/users").with(UserServlet.class);
    serve("/weather").with(WeatherServlet.class);
  }

  @Provides
  GeoCoding provideGeoCoding() {
    return new GeoCoding(new GeoApiContext.Builder().apiKey(ApiKeys.MAPS_API_KEY).build());
  }

  @Provides
  Weather provideWeather() {
    return new Weather();
  }

  @Provides
  UserService provideUserService() {
    return UserServiceFactory.getUserService();
  }

  @Provides
  AuthorizationCodeFlow provideFlow() throws IOException, GeneralSecurityException {
    return Utils.newFlow();
  }

  @Provides
  UserStorage provideUserStorage() {
    return new UserStorage();
  }

  @Provides
  EventStorage provideEventStorage() {
    return new EventStorage();
  }

  @Provides
  Utils provideUtils() {
    return new Utils();
  }
}
