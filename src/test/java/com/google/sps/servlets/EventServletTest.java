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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;

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

  private void setParameters(Search search) throws IOException {
    when(mockRequest.getParameter("search")).thenReturn(search.getText());
    when(mockRequest.getParameter("category")).thenReturn(search.getCategory());
    when(mockRequest.getParameter("start")).thenReturn(search.getStart());
    when(mockRequest.getParameter("end")).thenReturn(search.getEnd());
    when(mockRequest.getParameter("duration")).thenReturn(search.getDuration());
    when(mockRequest.getParameter("location")).thenReturn(search.getLocation());
  }

  private void setWriterAndCreateEvent() throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockResponse.getWriter()).thenReturn(writer);

    new EventServlet(mockUserStorage, mockEventStorage).doGet(mockRequest, mockResponse);
  }

  @Test
  public void eventServletTest_doGet_WithNullPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn(null);

    setWriterAndCreateEvent();

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithEmptyPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("");

    setWriterAndCreateEvent();

    verify(mockEventStorage, atLeast(1)).getSearchedEvents(any(Search.class));
  }

  @Test
  public void eventServletTest_doGet_WithSlashPath_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/");

    setWriterAndCreateEvent();

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

    setWriterAndCreateEvent();
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }

  @Test
  public void eventServletTest_doGet_WithEmptyPathAndGivenParameters_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("");

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJGaK-SZcLkEcRA9wf5_GNbuY");
    
    setParameters(search);

    setWriterAndCreateEvent();
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }

  @Test
  public void eventServletTest_doGet_WithSlashPathAndGivenParameters_DoesSearch() throws IOException {
    when(mockRequest.getPathInfo()).thenReturn("/");

    Search search = new Search("", "all", "2020-01-01", "2020-12-31", "60", "ChIJGaK-SZcLkEcRA9wf5_GNbuY");
    
    setParameters(search);

    setWriterAndCreateEvent();
    
    verify(mockEventStorage, atLeast(1)).getSearchedEvents(eq(search));
  }
}
