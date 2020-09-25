package com.google.sps.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserStorageTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private final UserStorage userStorageObject = new UserStorage();
  private final User user1 = User.newBuilder().setId("uid1").setEmail("email1").setInvitedEventsId(new ArrayList<>())
      .setJoinedEventsId(null).setDeclinedEventsId(null).setUsername("uname1").build();
  private final User user2 = User.newBuilder().setId("uid2").setEmail("email2").setInvitedEventsId(new ArrayList<>(Arrays.asList("event1")))
  .setJoinedEventsId(null).setDeclinedEventsId(null).setUsername("uname2").build();
  private final User user3 = User.newBuilder().setId("uid3").setEmail("email3").setInvitedEventsId(new ArrayList<>(Arrays.asList("event2")))
  .setJoinedEventsId(null).setDeclinedEventsId(null).setUsername("uname3").build();
  private Event event = Event.newBuilder().setOwnerID("ownerId").build();
  @Before
  public void setUp() {
    helper.setUp();
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    ds.put(getEntityFromObject(user1));
    ds.put(getEntityFromObject(user2));
    event.setId(new EventStorage().addOrUpdateEvent(event));
    return;
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  private Entity getEntityFromObject(User userObject) {
    Entity userEntity = new Entity("User", userObject.getID());
    userEntity.setProperty("email", userObject.getEmail());
    userEntity.setProperty("username", userObject.getUsername());
    userEntity.setProperty("invited-events", userObject.getInvitedEventsID());
    userEntity.setProperty("joined-events", userObject.getJoinedEventsID());
    userEntity.setProperty("declined-events", userObject.getDeclinedEventsID());
    return userEntity;
  }

  @Test
  public void userStorageTest_getUser_existentUser_returnsUser() {
    User returnedUser = userStorageObject.getUser("uid1");
    assertTrue(user1.equals(returnedUser));
  }

  @Test
  public void userStorageTest_getUser_nonExistentUser_returnsNull() {
    User returnedUser = userStorageObject.getUser("uid3");
    assertNull(returnedUser);
  }

  @Test
  public void userStorageTest_getIDByUsername_validUsername_returnsValidID() {
    String returnedId = userStorageObject.getIDbyUsername("uname1");
    assertEquals("uid1", returnedId);
  }

  @Test
  public void userStorageTest_getIDByUsername_invalidUsername_returnsNull() {
    String returnedId = userStorageObject.getIDbyUsername("uname3");
    assertNull(returnedId);
  }

  @Test
  public void userStorageTest_getUsernameByID_validID_validUsername() {
    String returnedUsername = userStorageObject.getUsernameByID("uid1");
    assertEquals("uname1", returnedUsername);
  }

  @Test
  public void userStorageTest_getUsernameByID_invalidID_returnsNull() {
    String returnedUsername = userStorageObject.getUsernameByID("uid3");
    assertNull(returnedUsername);
  }

  @Test
  public void userStorageTest_addOrUpdateUser_existentUser_updatesUser() {
    user2.setUsername("uname3");
    userStorageObject.addOrUpdateUser(user2);
    assertEquals(userStorageObject.getUser("uid2"), user2);
  }

  @Test
  public void userStorageTest_addOrUpdateUser_inexistentUser_addsUser() {
    userStorageObject.addOrUpdateUser(user3);
    assertEquals(userStorageObject.getUser("uid3"), user3);
  }

  @Test
  public void userStorageTest_joinEvent_existentUser_joinsEvent() {
    userStorageObject.joinEvent("uid1", event.getID(), true);
    assertEquals(userStorageObject.getUser("uid1").getJoinedEventsID(), Arrays.asList(event.getID()));
  }
}
