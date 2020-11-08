package com.titans.grouptravelplanner;

import android.location.Location;
import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public class GeneralUtility {

    public UserModel convertFirebaseUserToUser(FirebaseUser firebaseUser, Location userLocation) {
        UserModel user = new UserModel();

        user.setEmail(firebaseUser.getEmail());

        if (userLocation != null) {
            GeoPoint geoPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
            user.setLocation(geoPoint);
        }
        user.setMobile(firebaseUser.getPhoneNumber());
        user.setName(firebaseUser.getDisplayName());
        user.setUid(firebaseUser.getUid());
        if (firebaseUser.getPhotoUrl() != null) {
            user.setPhotoUrl(firebaseUser.getPhotoUrl().toString());
        }

        return user;
    }

    /**
     * Latitude: 1 deg = 110.574 KM
     * Longitude: 1 deg = 111.320*cos(latitude) KM
     */
    public GeoPoint getLesserGeoPointFromLocation(Location location, int range) {
        LatLng latLng = AppConstants.DEFAULT_LAT_LNG;
        if (location != null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return getLesserGeoPointFromLatLng(latLng, range);
    }

    public GeoPoint getGreaterGeoPointFromLocation(Location location, int range) {
        LatLng latLng = AppConstants.DEFAULT_LAT_LNG;
        if (location != null) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        return getGreaterGeoPointFromLatLng(latLng, range);
    }

    public GeoPoint getLesserGeoPointFromLatLng(LatLng latLng, int range) {
        double lowerLatitude = latLng.latitude - (range * (1 / 110.574));
        double lowerLongitude = latLng.longitude - (range * (1 / 111.320));
        GeoPoint lesserGeoPoint = new GeoPoint(lowerLatitude, lowerLongitude);
        return lesserGeoPoint;
    }

    public GeoPoint getGreaterGeoPointFromLatLng(LatLng latLng, int range) {
        double greaterLatitude = latLng.latitude + (range * (1 / 110.574));
        double greaterLongitude = latLng.longitude + (range * (1 / 111.320));
        GeoPoint greaterGeoPoint = new GeoPoint(greaterLatitude, greaterLongitude);
        return greaterGeoPoint;
    }

    // being used for nearby
    public void showTravellersOnMap(GoogleMap googleMap, QuerySnapshot querySnapshot,
                                    CameraPosition cameraPosition) {
        if (googleMap != null) {
            //googleMap.clear();
            LatLng latLng = null;
            for (DocumentSnapshot documentSnapshot: querySnapshot.getDocuments()) {
                try {
                    if (documentSnapshot.getData() != null) {
                        GeoPoint geoPoint = (GeoPoint) documentSnapshot.getData().get("location");
                        if (geoPoint != null) {
                            UserModel userModel = documentSnapshot.toObject(UserModel.class);
                            setTravellerMarkerOnMap(googleMap, geoPoint, userModel);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (cameraPosition != null) {
                // user has already seen the map, set camera position as what was left
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraPosition.target,
                        cameraPosition.zoom));
            } else {
                // animate for last location, zoom level = 10 (city)
                if (latLng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                }
            }
        }
    }

    public void showSingleTravellerOnMap(FirestoreDbUtility firestoreDbUtility,
                                         final GoogleMap googleMap, String userUid) {

        firestoreDbUtility.getOne(AppConstants.Collections.USERS, userUid,
                new FirestoreDbOperationCallback() {

            @Override
            public void onSuccess(Object object) {
                DocumentSnapshot documentSnapshot = (DocumentSnapshot) object;
                if (documentSnapshot.getData() != null) {
                    GeoPoint geoPoint = (GeoPoint) documentSnapshot.getData().get("location");
                    if (geoPoint != null) {
                        UserModel userModel = documentSnapshot.toObject(UserModel.class);
                        setTravellerMarkerOnMap(googleMap, geoPoint, userModel);
                    }
                }
            }

            @Override
            public void onFailure(Object object) {
            }
        });
    }

    // unique firestore collections document id
    public String getUniqueDocumentId(String uid) {
        String id = uid + "_" + System.currentTimeMillis();
        return id;
    }

    private void setTravellerMarkerOnMap(GoogleMap googleMap, GeoPoint geoPoint, UserModel userModel) {
        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        String markerTitle = "";
        if (userModel.getName() != null && !userModel.getName().isEmpty()) {
            markerTitle = userModel.getName();
        } else if (userModel.getEmail() != null && !userModel.getEmail().isEmpty()) {
            markerTitle = userModel.getEmail();
        } else if (userModel.getMobile() != null && !userModel.getMobile().isEmpty()) {
            markerTitle = userModel.getMobile();
        } else {
            markerTitle = "Unknown";
        }
        Marker marker = googleMap.addMarker(
                new MarkerOptions()
                        .position(latLng)
                        .title(markerTitle)
                        .snippet(userModel.getEmail())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account))
        );
        marker.setTag(userModel);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6)); // zoom-level states
    }

    private void silentlyUpdateUserProfile(FirestoreDbUtility firestoreDbUtility,
                                           Map<String, Object> hashMap, String userUid) {
        firestoreDbUtility.update(AppConstants.Collections.USERS,
                userUid,
                hashMap, new FirestoreDbOperationCallback() {
                    @Override
                    public void onSuccess(Object object) {
                    }

                    @Override
                    public void onFailure(Object object) {
                    }
                });
    }

    public void setUserLastSeenStatus(FirestoreDbUtility firestoreDbUtility,
                                      String userUid) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", false);
        hashMap.put("lastSeen", new Date());

        silentlyUpdateUserProfile(firestoreDbUtility, hashMap, userUid);
    }

    // format can be 7:56 AM or 2 Nov 2018
    public String convertDateToChatDateFormat(Date chatDate) {
        String chatDateString = "";
        Calendar calendar = Calendar.getInstance();
        Date currentDate = new Date();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, -1); // 1 day ago
        Date yesterdayDate = calendar.getTime();

        if (chatDate.after(yesterdayDate)) {
            // format would be 7:56 AM
            chatDateString = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(chatDate);
        } else {
            // format would be 2 Nov 2018
            chatDateString = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                    .format(chatDate);
        }
        return chatDateString;
    }

    public void getTravellersAroundALocation(LatLng locationLatLng,
                                             int filterRange,
                                             FirestoreDbUtility firestoreDbUtility,
                                             Singleton singleton,
                                             boolean isSourcePlace,
                                             FirestoreDbOperationCallback callback) {
        GeoPoint locationLesserGeoPoint =
                getLesserGeoPointFromLatLng(locationLatLng, filterRange);
        GeoPoint locationGreaterGeoPoint =
                getGreaterGeoPointFromLatLng(locationLatLng, filterRange);
        String queryField;
        if (isSourcePlace) {
            queryField = "sourceLocation";
        } else {
            queryField = "destinationLocation";
        }
        List<FirestoreQuery> firestoreQueryList = new ArrayList<>();

        firestoreQueryList.add(new FirestoreQuery(
                FirestoreQueryConditionCode.WHERE_LESS_THAN,
                queryField,
                locationGreaterGeoPoint
        ));
        firestoreQueryList.add(new FirestoreQuery(
                FirestoreQueryConditionCode.WHERE_GREATER_THAN,
                queryField,
                locationLesserGeoPoint
        ));

        firestoreDbUtility.getMany(AppConstants.Collections.SEARCH_HISTORIES,
                firestoreQueryList, null, new FirestoreDbOperationCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        QuerySnapshot querySnapshot = (QuerySnapshot) object;
                        Set<String> userUidSet = new HashSet<>();
                        for (DocumentSnapshot documentSnapshot: querySnapshot) {
                            if (documentSnapshot.getData() != null) {
                                String userUid = documentSnapshot.getData().get("userUid").toString();

                                // not user himself
                                if (singleton.getFirebaseUser() != null) {
                                    if (!userUid.equals(singleton.getFirebaseUser().getUid())) {
                                        userUidSet.add(userUid);
                                    }
                                } else {
                                    userUidSet.add(userUid);
                                }
                            }
                        }
                        callback.onSuccess(userUidSet);
                    }

                    @Override
                    public void onFailure(Object object) {
                        callback.onFailure(object);
                    }
                });

    }
}
