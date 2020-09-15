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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.DateTimeRange;
import com.google.sps.data.Event;
import com.google.sps.data.EventStorage;
import com.google.sps.data.Search;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventStorageTest {

  private static final Search NULL_SEARCH = new Search();
  private static final Search EMPTY_SEARCH = new Search("", "all", "", "", "", "all");
  private static final Search SEARCH_A = new Search("Meeting", "Business", "2020-06-06", "2020-07-07", "200", "ChIJyc_U0TTDQUcRYBEeDCnEAAQ");
  private static final Search SEARCH_B = new Search("party", "Entertainment", "2020-06-06", null, "30", null);
  private static final Search SEARCH_C = new Search("pEn", "Education", "", "2020-08-06", "20", "ChIJdd4hrwug2EcRmSrV3Vo6llI");

  private static final List<String> LIST = new ArrayList<>();
  private static final Map<String, String> MAP = new HashMap<>();
  private static final DateTimeRange RANGE_A = new DateTimeRange("2020-06-06", "2020-07-01", "12:12", "13:13");
  private static final DateTimeRange RANGE_B = new DateTimeRange("2020-06-05", "2020-06-30", "12:12", "13:13");

  private static final Event EVENT_A = Event.newBuilder()
        .setID("1")
        .setGCalendarID("1")
        .setOwnerID("1")
        .setTitle("Meeting")
        .setDescription("Bring a pen")
        .setCategory("Business")
        .setTags(LIST)
        .setLocation("Budapest, Hungary")
        .setLocationId("ChIJyc_U0TTDQUcRYBEeDCnEAAQ")
        .setDateTimeRange(RANGE_A)
        .setDuration(new Long("30"))
        .setLinks(LIST)
        .setFields(MAP)
        .setInvitedIDs(LIST)
        .setJoinedIDs(LIST)
        .setDeclinedIDs(LIST).build();

  private static final Event EVENT_B = Event.newBuilder()
        .setID("2")
        .setGCalendarID("1")
        .setOwnerID("1")
        .setTitle("Party")
        .setDescription("RSVP through the link")
        .setCategory("Entertainment")
        .setTags(LIST)
        .setLocation("London, UK")
        .setLocationId("ChIJdd4hrwug2EcRmSrV3Vo6llI")
        .setDateTimeRange(RANGE_A)
        .setDuration(new Long("30"))
        .setLinks(LIST)
        .setFields(MAP)
        .setInvitedIDs(LIST)
        .setJoinedIDs(LIST)
        .setDeclinedIDs(LIST).build();

  private static final Event EVENT_C = Event.newBuilder()
        .setID("3")
        .setGCalendarID("1")
        .setOwnerID("1")
        .setTitle("Graduation")
        .setDescription("After the celebration join us for the party")
        .setCategory("Entertainment")
        .setTags(LIST)
        .setLocation("Budapest, Hungary")
        .setLocationId("ChIJyc_U0TTDQUcRYBEeDCnEAAQ")
        .setDateTimeRange(RANGE_B)
        .setDuration(new Long("30"))
        .setLinks(LIST)
        .setFields(MAP)
        .setInvitedIDs(LIST)
        .setJoinedIDs(LIST)
        .setDeclinedIDs(LIST).build();

  private static final List<Event> ALL_EVENTS = Arrays.asList(EVENT_A, EVENT_B, EVENT_C);

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  EventStorage eventStorageObject = new EventStorage();

  @Before
  public void setUp() {
    helper.setUp();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Event")).countEntities(withLimit(10)));
    ds.put(getEntityFromEvent(EVENT_A));
    ds.put(getEntityFromEvent(EVENT_B));
    ds.put(getEntityFromEvent(EVENT_C));
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private Entity getEntityFromEvent(Event event) {
    Entity entity = new Entity("Event", event.getID());

    entity.setProperty("gcalendar-id", event.getGCalendarID());
    entity.setProperty("title", event.getTitle());
    entity.setProperty("description", event.getDescription());
    entity.setProperty("category", event.getCategory());
    entity.setProperty("tags", event.getTags());
    entity.setProperty("date", event.getDate());
    entity.setProperty("time", event.getTime());
    entity.setProperty("date-time-range", event.getDateTimeRangeAsJSON());
    entity.setProperty("duration", event.getDuration());
    entity.setProperty("location", event.getLocation());
    entity.setProperty("location-id", event.getLocationId());
    entity.setProperty("links", event.getLinks());
    entity.setProperty("fields", event.getFieldsAsJSON());
    entity.setProperty("owner", event.getOwnerID());
    entity.setProperty("invited-users", event.getInvitedIDs());
    entity.setProperty("joined-users", event.getJoinedIDs());
    entity.setProperty("declined-users", event.getDeclinedIDs());

    return entity;
  }

  public boolean eventEqualsWithoutID(Event event, Event other) {
    return Objects.equals(event.getGCalendarID(), other.getGCalendarID())
        && Objects.equals(event.getTitle(), other.getTitle())
        && Objects.equals(event.getDescription(), other.getDescription())
        && Objects.equals(event.getCategory(), other.getCategory())
        && Objects.equals(event.getTags(), other.getTags())
        && Objects.equals(event.getDateTimeRangeAsJSON(), other.getDateTimeRangeAsJSON())
        && Objects.equals(event.getDuration(), other.getDuration())
        && Objects.equals(event.getLocation(), other.getLocation())
        && Objects.equals(event.getLinks(), other.getLinks())
        && Objects.equals(event.getFields(), other.getFields())
        && Objects.equals(event.getOwnerID(), other.getOwnerID())
        && Objects.equals(event.getInvitedIDs(), other.getInvitedIDs())
        && Objects.equals(event.getJoinedIDs(), other.getJoinedIDs())
        && Objects.equals(event.getDeclinedIDs(), other.getDeclinedIDs());
  }

  @Test
  public void eventStorageTest_getSearchedEvents_nullSearch_MatchesAll() {
    List<Event> searchedEvents = eventStorageObject.getSearchedEvents(NULL_SEARCH);
    assertEquals(3, searchedEvents.size());
    for (int i = 0; i < searchedEvents.size(); i++) {
      assertTrue(eventEqualsWithoutID(searchedEvents.get(i), ALL_EVENTS.get(i)));
    }
  }

  @Test
  public void eventStorageTest_getSearchedEvents_emptySearch_MatchesAll() {
    List<Event> searchedEvents = eventStorageObject.getSearchedEvents(EMPTY_SEARCH);
    assertEquals(3, searchedEvents.size());
    for (int i = 0; i < searchedEvents.size(); i++) {
      assertTrue(eventEqualsWithoutID(searchedEvents.get(i), ALL_EVENTS.get(i)));
    }
  }

  @Test
  public void eventStorageTest_getSearchedEvents_Matches() {
    List<Event> searchedAEvents = eventStorageObject.getSearchedEvents(SEARCH_A);
    assertEquals(1, searchedAEvents.size());
    assertTrue(eventEqualsWithoutID(searchedAEvents.get(0), EVENT_A));

    List<Event> searchedBEvents = eventStorageObject.getSearchedEvents(SEARCH_B);
    assertEquals(1, searchedBEvents.size());
    assertTrue(eventEqualsWithoutID(searchedBEvents.get(0), EVENT_B));
  }

  @Test
  public void eventStorageTest_getSearchedEvents_DoesntMatch() {
    assertEquals(0, eventStorageObject.getSearchedEvents(SEARCH_C).size());
  }
}
