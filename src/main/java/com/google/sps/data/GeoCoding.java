package com.google.sps.data;

import com.google.maps.errors.ApiException;
import java.lang.InterruptedException;
import java.io.IOException;

import com.google.maps.model.LatLng;
import com.google.maps.model.GeocodingResult;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;

public class GeoCoding {
  public LatLng fromPlaceIdToLatLng(String placeId) {
    if (placeId == null) {
      return null;
    }
    GeoApiContext context = new GeoApiContext.Builder()
        .apiKey(ApiKeys.MAPS_API_KEY)
        .build();
    
    GeocodingResult[] results = null;
    try {
      results = GeocodingApi.geocode(context, placeId).await();
    } catch (InterruptedException e) {
      System.err.println("Smth wrong with threads: " + e.getMessage());
    } catch (ApiException e) {
      System.err.println("Smth wrong with api call by placeId" + placeId + ": " + e.getMessage());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    LatLng latlng = null;
    if (results != null && results.length != 0) {
      latlng = new LatLng(results[0].geometry.location.lat, results[0].geometry.location.lng);
    }
    return latlng;
  }

  public LatLng fromAddressToLatLng(String address) {
    if (address == null) {
      return null;
    }
    GeoApiContext context = new GeoApiContext.Builder()
        .apiKey(ApiKeys.MAPS_API_KEY)
        .build();
    
    GeocodingResult[] results = null;
    try {
      results = GeocodingApi.geocode(context, address).await();
    } catch (InterruptedException e) {
      System.err.println("Smth wrong with threads: " + e.getMessage());
    } catch (ApiException e) {
      System.err.println("Smth wrong with api call by address" + address + ": " + e.getMessage());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

    LatLng latlng = null;
    if (results != null && results.length != 0) {
      latlng = new LatLng(results[0].geometry.location.lat, results[0].geometry.location.lng);
    }
    return latlng;
  }
}
