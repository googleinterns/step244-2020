package com.google.sps.servlets;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event.ExtendedProperties;


@WebServlet("/events/gcalendar")
public class GCalendarEventsServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    String title = (String) request.getParameter("title");
    String description = (String) request.getParameter("description");
    String startTime = (String) request.getParameter("startTime");  //Format of time should be: YYYY-MM-DDTHH:MM:SSZ in UTC
    String endTime = (String) request.getParameter("endTime");      //Example: 2020-08-18T16:00:00Z
    Event event = new Event().setDescription(description).setSummary(title);
    
    DateTime startDateTime = new DateTime(startTime);
    DateTime endDateTime = new DateTime(endTime);
    EventDateTime start = new EventDateTime().setDateTime(startDateTime);
    event.setStart(start);
    EventDateTime end = new EventDateTime().setDateTime(endDateTime);
    event.setEnd(end);
    
    ExtendedProperties extendedProps = new ExtendedProperties();
    Map<String, String> shared = new HashMap<>();
    //shared.put("field1", "val1"); To be modifed in the future to get them from param
    extendedProps.setShared(shared);
    event.setExtendedProperties(extendedProps);
    String calendarId = "c_fmqkfbflaaoqltet2ei7shv184@group.calendar.google.com"; //To be modified with the user's primary
    try {
      Calendar service = Utils.loadCalendarClient();
      service.events().insert(calendarId, event).execute();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

}
