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

package com.google.sps;

import com.google.sps.data.Event;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class EventTest {
  private static final String USER_ID1 = "test_uid_1";
  private static final String USER_ID2 = "test_uid_2";
  private static final String USER_ID3 = "test_uid_3";
  private static final String USER_ID4 = "test_uid_4";
  private static final String USER_ID5 = "test_uid_5";
  private static final String USER_ID6 = "test_uid_6";
  private static final String EVENT_ID1 = "test_id_1";
  private static final String EVENT_ID2 = "test_id_2";
  private static final String G_ID1 = "g_id_1";
  private static final String TITLE1 = "title_1";
  private static final String DESCRIPTION1 = "description_1";
  private static final String CATEGORY1 = "category_1";
  private static final List<String> TAGS1 = Arrays.asList("description_1");
  private static final Long MINUTES_10 = 10L;
  private static final Long MINUTES_20 = 20L;
  private static final String LOCATION1 = "location_1";
  private static final List<String> LINKS1 = Arrays.asList("link1.com", "link11.ru");
  private static final Map<String, String> FIELDS10 = Stream.of(new String[][] {
    {"f1", "v1"}, 
    {"f2", "v2"}, 
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
  private static final Map<String, String> FIELDS11 = Stream.of(new String[][] {
    {"f2", "v2"}, 
    {"f1", "v1"}, 
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));


//   private static Event event;

//   @Before
//   public void setUp() {
//     event = Event.newBuilder()
//         .setOwnerID(USER_ID1)
//         .setID(EVENT_ID1)
//         .setInvitedIDs({USER_ID2})
//         .build();
//   }

  @Test
  public void equalityOfEmptyEvents() {
    Event event1 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Event event2 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertTrue(event1.equals(event2));
  }

  @Test
  public void equalityOfEvents() {
    Event event1 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_10)
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS10)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();
    
    Event event2 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_10)
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertTrue(event1.equals(event2));
  }

  @Test
  public void nonEqualityOfEvents() {
    Event event1 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_10)
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS10)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();
    
    Event event2 = Event.newBuilder()
      .setID(EVENT_ID2)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_20)
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertFalse(event1.equals(event2));
  }

  @Test
  public void nonEqualityOfEventsWithDifferentParameters() {
    Event event1 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .build();
    
    Event event2 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_20) // different duration
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertFalse(event1.equals(event2));
  }

  @Test
  public void nonEqualityOfEventsDuration() {
    Event event1 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_10) // different duration
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS10)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();
    
    Event event2 = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID1)
      .setTitle(TITLE1)
      .setDescription(DESCRIPTION1)
      .setCategory(CATEGORY1)
      .setTags(TAGS1)
      .setDuration(MINUTES_20) // different duration
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertFalse(event1.equals(event2));
  }

  @Test
  public void joinInvitedEvent() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    actual_event.joinEvent(USER_ID2);

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setJoinedIDs(new ArrayList<String>(Arrays.asList(USER_ID2)))
      .build();

    Assert.assertEquals(expected_event.equals(actual_event), true);
  }

  @Test
  public void joinNotInvitedEvent() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    actual_event.joinEvent(USER_ID2);

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertEquals(expected_event.equals(actual_event), true);
  }

  @Test
  public void joinOwnEvent() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    actual_event.joinEvent(USER_ID1);

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertEquals(expected_event.equals(actual_event), true);
  }

  @Test
  public void hasAccessToEvent() {
    Event event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .setJoinedIDs(Arrays.asList(USER_ID3))
      .setDeclinedIDs(Arrays.asList(USER_ID4))
      .build();

    Assert.assertTrue(event.userHasAccessToEvent(USER_ID1));
    Assert.assertTrue(event.userHasAccessToEvent(USER_ID2));
    Assert.assertTrue(event.userHasAccessToEvent(USER_ID3));
    Assert.assertTrue(event.userHasAccessToEvent(USER_ID4));
    Assert.assertFalse(event.userHasAccessToEvent(USER_ID5));
  }
}
