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

import com.google.gson.Gson;
import com.google.sps.data.Event;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a random quote. */
@WebServlet("/event")
public final class EventServlet extends HttpServlet {

private List<Event> events;
 
  @Override
  public void init() {
    events = new ArrayList<>();
    events.add(new Event("Meeting", "Tomorrow 6pm", "Remote", "Bring a pen", "other", "business"));
    events.add(new Event("Party", "Friday 8pm", "London", "Dress code: yellow shirt", "culture", "friends"));
    events.add(new Event("Concert", "02/02/21 10pm", "Wembley", "RSVP here", "culture", "music"));
    events.add(new Event("Graduation party", "After the ceremony", "Royal Albert Hall", "At Tim's house", "sport", "friends"));
  }
 
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    /*
    Filter pastEventFilter = new FilterPredicate("date", FilterOperator.LESS_THAN, currentDate);

    Query query = new Query("Event").setFilter(pastEventFilter).addSort("time", SortDirection.ASCENDING);
 
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
 
    String search = request.getParameter("search");
 
    List<Event> events = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      String name = (String) entity.getProperty("title");
 
      if (search.isEmpty() || isResultForSearch(search, event)) {
        String time = (String) entity.getProperty("time");
        String location = (String) entity.getProperty("location");
        String description = (String) entity.getProperty("description");
        Event event = new Event(name, time, location, description);
        events.add(event);
      }
    }
    */
    String search = request.getParameter("search");
    String category = request.getParameter("category");
    String tags = request.getParameter("tags");

    List<Event> searchedEvents = new ArrayList<>(events);

    if (search != null && !search.isEmpty()) {
      for (Event event : events) {
        if (!isResultForSearch(search, event)) {
          searchedEvents.remove(event);
        }
      }
    }
 
    if (category != null && !category.isEmpty() && !category.equals("all")) {
      for (Event event : events) {
        if (!category.equals(event.getCategory())) {
          searchedEvents.remove(event);
        }
      }
    }

    if (tags != null && !tags.isEmpty()) {
      for (Event event : events) {
        if (!tags.contains(event.getTags())) {
          searchedEvents.remove(event);
        }
      }
    }

    Gson gson = new Gson();
    
    response.setContentType("application/json");
    response.getWriter().println(gson.toJson(searchedEvents));
  }
 
  private boolean isResultForSearch(String search, Event event) {
    return isTextMatch(search, event.getName()) || isTextMatch(search, event.getDescription());
  }

  private boolean isTextMatch(String search, String text) {
    return text.toLowerCase().contains(search.toLowerCase());
  }

}
