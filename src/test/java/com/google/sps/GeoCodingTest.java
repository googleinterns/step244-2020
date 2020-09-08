package com.google.sps;

import com.google.sps.data.GeoCoding;
import com.google.maps.model.LatLng;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class GeoCodingTest {
  private static final double EPSILON = 0.0001;
  private static final String RUSSIA_MOSCOW_ADDRESS = "Russia, Moscow, Red Square";
  private static final LatLng RUSSIA_MOSCOW_LATLNG = new LatLng(55.75393030, 37.62079500);
  private static final String USA_NEW_YORK_ADDRESS = "Central Park USA New York";
  private static final LatLng USA_NEW_YORK_LATLNG = new LatLng(40.78121990, -73.96651380);
  private static final String STRANGE_ADDRESS = "fdskjgl cjtwet ckKERF SEJFN";

  @Test
  public void testGeoCoding_fromAddressToLatLng_withLocations() {
    Assert.assertEquals(RUSSIA_MOSCOW_LATLNG.lat, new GeoCoding().fromAddressToLatLng(RUSSIA_MOSCOW_ADDRESS).lat, EPSILON);
    Assert.assertEquals(RUSSIA_MOSCOW_LATLNG.lng, new GeoCoding().fromAddressToLatLng(RUSSIA_MOSCOW_ADDRESS).lng, EPSILON);
    Assert.assertEquals(USA_NEW_YORK_LATLNG.lat, new GeoCoding().fromAddressToLatLng(USA_NEW_YORK_ADDRESS).lat, EPSILON);
    Assert.assertEquals(USA_NEW_YORK_LATLNG.lng, new GeoCoding().fromAddressToLatLng(USA_NEW_YORK_ADDRESS).lng, EPSILON);
  }

  @Test
  public void testGeoCoding_fromAddressToLatLng_withNull() {
    Assert.assertNull(new GeoCoding().fromAddressToLatLng(null));
  }

  @Test
  public void testGeoCoding_fromAddressToLatLng_withStrangeLocations() {
    Assert.assertNull(new GeoCoding().fromAddressToLatLng(STRANGE_ADDRESS));
  }
}
