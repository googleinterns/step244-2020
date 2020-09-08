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
  private static final String G_ID2 = "g_id_2";
  private static final String TITLE1 = "title_1";
  private static final String TITLE2 = "title_2";
  private static final String DESCRIPTION1 = "description_1";
  private static final String DESCRIPTION2 = "description_2";
  private static final String CATEGORY1 = "category_1";
  private static final String CATEGORY2 = "category_2";
  private static final List<String> TAGS1 = Arrays.asList("tag1", "tag11");
  private static final List<String> TAGS2 = Arrays.asList("tag2");
  private static final Long MINUTES_10 = 10L;
  private static final Long MINUTES_20 = 20L;
  private static final String LOCATION1 = "location_1";
  private static final String LOCATION2 = "location_2";
  private static final List<String> LINKS1 = Arrays.asList("link1.com", "link11.ru");
  private static final List<String> LINKS2 = Arrays.asList("link2.com", "link22.ru");
  private static final Map<String, String> FIELDS10 = Stream.of(new String[][] {
    {"f1", "v1"}, 
    {"f2", "v2"}, 
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
  private static final Map<String, String> FIELDS11 = Stream.of(new String[][] {
    {"f2", "v2"}, 
    {"f1", "v1"}, 
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

  @Test
  public void testEvent_Event() {
    Event event = Event.newBuilder()
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

    Assert.assertNull(event.getID());
    Assert.assertEquals(USER_ID1, event.getOwnerID());
    Assert.assertEquals(G_ID1, event.getGCalendarID());
    Assert.assertEquals(TITLE1, event.getTitle());
    Assert.assertEquals(DESCRIPTION1, event.getDescription());
    Assert.assertEquals(CATEGORY1, event.getCategory());
    Assert.assertEquals(TAGS1, event.getTags());
    Assert.assertFalse(event.isDateTimeSet());
    Assert.assertNull(event.getDate());
    Assert.assertNull(event.getTime());
    Assert.assertNull(event.getDateTimeAsString());
    Assert.assertEquals(MINUTES_10, event.getDuration());
    Assert.assertEquals(LOCATION1, event.getLocation());
    Assert.assertEquals(LINKS1, event.getLinks());
    Assert.assertEquals(FIELDS10, event.getFields());
    Assert.assertEquals(Arrays.asList(USER_ID2), event.getInvitedIDs());
    Assert.assertEquals(new ArrayList<>(), event.getJoinedIDs());
    Assert.assertEquals(new ArrayList<>(), event.getDeclinedIDs());
  }

  @Test
  public void testEvent_equals_withEmptyEvents_returnsTrue() {
    Event event1 = Event.newBuilder()
      .setOwnerID(USER_ID1)
      .build();

    Event event2 = Event.newBuilder()
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertTrue(event1.equals(event2));
    Assert.assertEquals(event1, event2);
  }

  @Test
  public void testEvent_equals_withParameters_returnsTrue() {
    Event event1 = Event.newBuilder()
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
    Assert.assertEquals(event1, event2);
  }

  @Test
  public void testEvent_equals_withParameters_returnsNotEqual() {
    Event event1 = Event.newBuilder()
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
      .setOwnerID(USER_ID1)
      .setGCalendarID(G_ID2)
      .setTitle(TITLE2)
      .setDescription(DESCRIPTION2)
      .setCategory(CATEGORY2)
      .setTags(TAGS2)
      .setDuration(MINUTES_20)
      .setLocation(LOCATION2)
      .setLinks(LINKS2)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertFalse(event1.equals(event2));
    Assert.assertNotEquals(event1, event2);
  }

  @Test
  public void testEvent_equals_withDifferentNumberParameters_returnsNotEqual() {
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
      .setDuration(MINUTES_20)
      .setLocation(LOCATION1)
      .setLinks(LINKS1)
      .setFields(FIELDS11)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertNotEquals(event1, event2);
  }

  @Test
  public void testEvent_joinEvent_withInvitedUser_returnsTrue() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setInvitedIDs(Arrays.asList(USER_ID2))
      .build();

    Assert.assertTrue(actual_event.joinEvent(USER_ID2));

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .setJoinedIDs(new ArrayList<String>(Arrays.asList(USER_ID2)))
      .build();

    Assert.assertEquals(expected_event, actual_event);
  }

  @Test
  public void testEvent_joinEvent_withNotInvitedUser_returnsFalse() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertFalse(actual_event.joinEvent(USER_ID2));

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertEquals(expected_event, actual_event);
  }

  @Test
  public void testEvent_joinEvent_withOwner_returnsFalse() {
    Event actual_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertFalse(actual_event.joinEvent(USER_ID1));

    Event expected_event = Event.newBuilder()
      .setID(EVENT_ID1)
      .setOwnerID(USER_ID1)
      .build();

    Assert.assertEquals(expected_event, actual_event);
  }

  @Test
  public void testEvent_hasAccessToEvent_withDifferentStatus() {
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
