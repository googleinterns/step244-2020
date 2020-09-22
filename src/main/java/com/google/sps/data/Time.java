package com.google.sps.data;

import java.util.List;
import java.util.Objects;

public class Time implements Comparable<Time> {
  private Long start;
  private Long end;
  private String ownerID;
  private List<String> availableAttendees;

  public Time(Long start, Long end, List<String> availableAttendees) {
    this.start = start;
    this.end = end;
    this.availableAttendees = availableAttendees;
    this.ownerID = null;
  }

  public Time(Long start, Long end) {
    this.start = start;
    this.end = end;
    this.availableAttendees = null;
    this.ownerID = null;
  }

  public Time(Long start, Long end, String ownerId) {
    this.start = start;
    this.end = end;
    this.ownerID = ownerId;
    this.availableAttendees = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Time))
      return false;
    Time other = (Time) obj;
    return Objects.equals(this.start, other.start) && Objects.equals(this.end, other.end)
        && Objects.equals(this.availableAttendees, other.availableAttendees)
        && Objects.equals(this.ownerID, other.ownerID);
  }

  public Long getStart() {
    return this.start;
  }

  public Long getEnd() {
    return this.end;
  }

  public String getOwnerID() {
    return this.ownerID;
  }

  @Override
  public int compareTo(Time other) {
    return Long.compare(this.start, other.start);
  }

  public boolean overlaps(Long start, Long end) {
    return !(this.start >= end || this.end <= start);
  }
}
