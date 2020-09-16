package com.google.sps;

import com.google.sps.data.GeoCoding;
import com.google.maps.GeoApiContext;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/** */
@RunWith(JUnit4.class)
public final class GeoCodingTest {
  @Mock
  GeoApiContext mockGeoApiContext;
  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void testGeoCoding_fromAddressOrPlaceIdToLatLng_withNull() {
    Assert.assertNull(new GeoCoding(mockGeoApiContext).fromAddressOrPlaceIdToLatLng(null));
  }
}
