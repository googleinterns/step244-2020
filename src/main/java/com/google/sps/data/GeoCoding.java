package com.google.sps.data;

import com.google.maps.errors.ApiException;
import java.lang.IllegalStateException;
import java.lang.InterruptedException;
import java.io.IOException;

import com.google.inject.Inject;

import com.google.maps.model.LatLng;
import com.google.maps.model.GeocodingResult;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;

public class GeoCoding {
  GeoApiContext geoApiContext;

  @Inject
  public GeoCoding(GeoApiContext geoApiContext) {
    this.geoApiContext = geoApiContext;
  }

  public LatLng fromAddressOrPlaceIdToLatLng(String addressOrPlaceId) {
    if (addressOrPlaceId == null) {
      return null;
    }
    
    GeocodingResult[] results = null;
    try {
      results = GeocodingApi.geocode(geoApiContext, addressOrPlaceId).await();
    } catch (IllegalStateException e) {
      System.err.println("Wrong API key: " + e.getMessage());
    } catch (InterruptedException e) {
      System.err.println("Error with threads: " + e.getMessage());
    } catch (ApiException e) {
      System.err.println("Wrong API call by " + addressOrPlaceId + ": " + e.getMessage());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    if (results == null || results.length == 0) {
      return null;
    }
    return new LatLng(results[0].geometry.location.lat, results[0].geometry.location.lng);
  }
}
