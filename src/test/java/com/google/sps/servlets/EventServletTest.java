// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.users.UserService;

import com.google.gson.Gson;

import com.google.sps.data.DateTimeRange;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.UserStorage;
import com.google.sps.data.Search;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class EventServletTest {
  @Mock
  EventStorage mockEventStorage;
  @Mock
  UserStorage mockUserStorage;
  @Mock
  HttpServletRequest mockRequest;
  @Mock
  HttpServletResponse mockResponse;
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  private static final List<String> LIST = new ArrayList<>();
  private static final Map<String, String> MAP = new HashMap<>();

  private static final DateTimeRange RANGE = new DateTimeRange("2020-06-06", "2020-07-01", "12:12", "13:13");

  private static final Event EVENT = Event.newBuilder()
        .setID("2")
        .setGCalendarID("1")
        .setOwnerID("1")
        .setTitle("Party")
        .setDescription("RSVP through the link")
        .setCategory("Entertainment")
        .setTags(LIST)
        .setLocation("London, UK")
        .setLocationId("ChIJdd4hrwug2EcRmSrV3Vo6llI")
        .setDateTimeRange(RANGE)
        .setDuration(new Long("30"))
        .setLinks(LIST)
        .setFields(MAP)
        .setInvitedIDs(LIST)
        .setJoinedIDs(LIST)
        .setDeclinedIDs(LIST).build();

  private static final List<Event> EVENT_LIST = Arrays.asList(EVENT);

  private void setParameters(Search search) throws IOException {
    when(mockRequest.getParameter("search")).thenReturn(search.getText());
    when(mockRequest.getParameter("category")).thenReturn(search.getCategory());
    when(mockRequest.getParameter("start")).thenReturn(search.getStart());
    when(mockRequest.getParameter("end")).thenReturn(search.getEnd());
    when(mockRequest.getParameter("duration")).thenReturn(search.getDuration());
    when(mockRequest.getParameter("location")).thenReturn(search.getLocation());
  }

  @Test
  public void eventServletTest_doGet_WithNullPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn(null);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithEmptyPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithSlashPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithDifferentNonEmptyPath_DoesntSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/gcalendar");

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);

    verify(mockEventStorage, never()).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithNullPathAndGivenParameters_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn(null);

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJGaK-SZcLkEcRA9wf5_GNbuY");
    
    setParameters(search);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }

  @Test
  public void eventServletTest_doGet_WithEmptyPathAndGivenParameters_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("");

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJGaK-SZcLkEcRA9wf5_GNbuY");
    
    setParameters(search);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }

  @Test
  public void eventServletTest_doGet_WithSlashPathAndGivenParameters_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/");

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJGaK-SZcLkEcRA9wf5_GNbuY");
    
    setParameters(search);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }

  @Test
  public void eventServletTest_doGet_returnedEventsOutputCorrectly() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/");

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJdd4hrwug2EcRmSrV3Vo6llI");
    
    setParameters(search);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    when(mockEventStorage.getSearchedEvents(eq(search))).thenReturn(EVENT_LIST);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
    writer.flush();
    assertTrue(stringWriter.toString().contains(new Gson().toJson(EVENT_LIST)));
  }
}
