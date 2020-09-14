package com.google.sps.data;

import java.util.Objects;

public class Time implements Comparable<Time>{
  private Long start;
  private Long end;

  public Time(Long start, Long end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Time))
      return false;
    Time other = (Time) obj;
    return Objects.equals(this.start, other.start) && Objects.equals(this.end, end);
  }

  public Long getStart() {
    return this.start;
  }

  public Long getEnd() {
    return this.end;
  }

  @Override
  public int compareTo(Time other) {
    return Long.compare(this.start, other.start);
  }

  public boolean overlaps(Long start, Long end) {
    return !(this.start >= end || this.end <= start);
  }
}
