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

import com.google.sps.data.DateTimeRange;
import com.google.sps.data.Search;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public final class SearchTest {

  private static final List<String> EMPTY_TAGS = new ArrayList<>();
  private static final List<String> TAGS_A = Arrays.asList("Dogs", "Cats", "Golf");
  private static final List<String> TAGS_B = Arrays.asList("Cats", "Animal", "Violin");
  private static final List<String> TAGS_C = Arrays.asList("Dogs", "Golf");
  
  private static final Search NULL_SEARCH = new Search();
  private static final Search EMPTY_SEARCH = new Search("", "all", "", "", null, null, EMPTY_TAGS);
  private static final Search SEARCH_A = new Search("Meeting", "Business", "2020-06-06", "2020-07-07", null, null, TAGS_A);
  private static final Search SEARCH_B = new Search("eti", "Education", "2020-06-06", null, null, null, TAGS_B);
  private static final Search SEARCH_C = new Search("pEn", "Education", "", "2020-08-06", null, null, TAGS_C);

  private static final String TITLE_A = "Meeting";
  private static final String TITLE_B = "Party";
  private static final String TITLE_C = "Graduation party";
  private static final String TITLE_D = "Meetin";

  private static final String DESCRIPTION_A = "Bring a pen";
  private static final String DESCRIPTION_B = "RSVP via the provided link";
  private static final String DESCRIPTION_C = "Dress up for meeting the faculty";

  private static final DateTimeRange RANGE_A = new DateTimeRange("2020-06-06", "2020-07-01", "12:12", "13:13", new Long("0"));
  private static final DateTimeRange RANGE_B = new DateTimeRange("2020-06-29", "2020-07-09", "12:12", "13:13", new Long("0"));
  private static final DateTimeRange RANGE_C = new DateTimeRange("2020-06-05", "2020-06-30", "12:12", "13:13", new Long("0"));
  private static final DateTimeRange RANGE_D = new DateTimeRange("2020-06-05", "2020-08-08", "12:12", "13:13", new Long("0"));

  @Test
  public void nullTagsReturnsZero() {
    Assert.assertEquals(0, SEARCH_A.countMatchingTags(null));
    Assert.assertEquals(0, SEARCH_B.countMatchingTags(null));
    Assert.assertEquals(0, SEARCH_C.countMatchingTags(null));
  }

  @Test
  public void emptyTagsReturnsZero() {
    Assert.assertEquals(0, SEARCH_A.countMatchingTags(EMPTY_TAGS));
    Assert.assertEquals(0, SEARCH_B.countMatchingTags(EMPTY_TAGS));
    Assert.assertEquals(0, SEARCH_C.countMatchingTags(EMPTY_TAGS));
  }

  @Test
  public void nullSearchTagsReturnsNonZero() {
    Assert.assertEquals(-1, NULL_SEARCH.countMatchingTags(TAGS_A));
    Assert.assertEquals(-1, NULL_SEARCH.countMatchingTags(TAGS_B));
    Assert.assertEquals(-1, NULL_SEARCH.countMatchingTags(TAGS_C));
  }

  @Test
  public void emptySearchTagsReturnsNonZero() {
    Assert.assertEquals(-1, EMPTY_SEARCH.countMatchingTags(TAGS_A));
    Assert.assertEquals(-1, EMPTY_SEARCH.countMatchingTags(TAGS_B));
    Assert.assertEquals(-1, EMPTY_SEARCH.countMatchingTags(TAGS_C));
  }

  @Test
  public void tagsReturnCorrectNumberOfMatches() {
    Assert.assertEquals(3, SEARCH_A.countMatchingTags(TAGS_A));
    Assert.assertEquals(3, SEARCH_B.countMatchingTags(TAGS_B));
    Assert.assertEquals(2, SEARCH_C.countMatchingTags(TAGS_C));
    Assert.assertEquals(1, SEARCH_A.countMatchingTags(TAGS_B));
    Assert.assertEquals(0, SEARCH_B.countMatchingTags(TAGS_C));
    Assert.assertEquals(2, SEARCH_C.countMatchingTags(TAGS_A));
  }

  @Test
  public void textContainingMatches() {
    Assert.assertTrue(SEARCH_A.isTextMatching(TITLE_A));
    Assert.assertTrue(SEARCH_A.isTextMatching(DESCRIPTION_C));
    Assert.assertTrue(SEARCH_B.isTextMatching(TITLE_D));
  }

  @Test
  public void textContainingDoesntMatch() {
    Assert.assertFalse(SEARCH_A.isTextMatching(TITLE_D));
  }

  @Test
  public void nullTextMatches() {
    Assert.assertTrue(NULL_SEARCH.isSearchedTextMatching(TITLE_A, DESCRIPTION_A));
    Assert.assertTrue(NULL_SEARCH.isSearchedTextMatching(TITLE_B, DESCRIPTION_B));
    Assert.assertTrue(NULL_SEARCH.isSearchedTextMatching(TITLE_C, DESCRIPTION_C));
  }

  @Test
  public void emptyTextMatches() {
    Assert.assertTrue(EMPTY_SEARCH.isSearchedTextMatching(TITLE_A, DESCRIPTION_A));
    Assert.assertTrue(EMPTY_SEARCH.isSearchedTextMatching(TITLE_B, DESCRIPTION_B));
    Assert.assertTrue(EMPTY_SEARCH.isSearchedTextMatching(TITLE_C, DESCRIPTION_C));
  }

  @Test
  public void titleOrDescriptionMatches() {
    Assert.assertTrue(SEARCH_A.isSearchedTextMatching(TITLE_A, DESCRIPTION_A));
    Assert.assertTrue(SEARCH_B.isSearchedTextMatching(TITLE_A, DESCRIPTION_A));
    Assert.assertTrue(SEARCH_C.isSearchedTextMatching(TITLE_A, DESCRIPTION_A));
    Assert.assertTrue(SEARCH_A.isSearchedTextMatching(TITLE_C, DESCRIPTION_C));
    Assert.assertTrue(SEARCH_B.isSearchedTextMatching(TITLE_C, DESCRIPTION_C));
  }

  @Test
  public void titleOrDescriptionDoesntMatch() {
    Assert.assertFalse(SEARCH_A.isSearchedTextMatching(TITLE_B, DESCRIPTION_B));
    Assert.assertFalse(SEARCH_B.isSearchedTextMatching(TITLE_B, DESCRIPTION_B));
    Assert.assertFalse(SEARCH_C.isSearchedTextMatching(TITLE_B, DESCRIPTION_B));
  }

  @Test
  public void nullRangeMatches() {
    Assert.assertTrue(NULL_SEARCH.eventInRange(null));
    Assert.assertTrue(EMPTY_SEARCH.eventInRange(null));
    Assert.assertTrue(SEARCH_A.eventInRange(null));
    Assert.assertTrue(SEARCH_B.eventInRange(null));
  }

  @Test
  public void nullStartAndEndMatches() {
    Assert.assertTrue(NULL_SEARCH.eventInRange(RANGE_A));
  }

  @Test 
  public void emptyStartAndEndMatches() {
    Assert.assertTrue(EMPTY_SEARCH.eventInRange(RANGE_A));
  }

  @Test
  public void startAndEndInRangeMatches() {
    Assert.assertTrue(SEARCH_A.eventInRange(RANGE_A));
  }

  @Test
  public void startAndEndInRangeNullOrEmptyMatches() {
    Assert.assertTrue(SEARCH_B.eventInRange(RANGE_A));
    Assert.assertTrue(SEARCH_C.eventInRange(RANGE_A));
  }

  @Test
  public void startOrEndNotInRangeDoesntMatch() {
    Assert.assertFalse(SEARCH_A.eventInRange(RANGE_C));
    Assert.assertFalse(SEARCH_A.eventInRange(RANGE_D));
    Assert.assertFalse(SEARCH_B.eventInRange(RANGE_C));
    Assert.assertFalse(SEARCH_C.eventInRange(RANGE_D));
  }


  @Test
  public void nullSearchCategoryMatches() {
    Assert.assertTrue(NULL_SEARCH.eventInCategory("Business"));
    Assert.assertTrue(NULL_SEARCH.eventInCategory("Education"));
  }

  @Test
  public void nullCategoryMatches() {
    Assert.assertTrue(SEARCH_A.eventInCategory(null));
    Assert.assertTrue(SEARCH_B.eventInCategory(null));
  }

  @Test
  public void allCategoryMatches() {
    Assert.assertTrue(EMPTY_SEARCH.eventInCategory("Business"));
    Assert.assertTrue(EMPTY_SEARCH.eventInCategory(null));
    Assert.assertTrue(EMPTY_SEARCH.eventInCategory("random1234"));
  }

  @Test
  public void sameCategoryMatches() {
    Assert.assertTrue(SEARCH_A.eventInCategory("Business"));
    Assert.assertTrue(SEARCH_B.eventInCategory("Education"));
  }

  @Test
  public void differentCategoryDoesntMatch() {
    Assert.assertFalse(SEARCH_A.eventInCategory("business"));
    Assert.assertFalse(SEARCH_A.eventInCategory("Businesses"));
    Assert.assertFalse(SEARCH_A.eventInCategory("Businesss"));
  }
}
