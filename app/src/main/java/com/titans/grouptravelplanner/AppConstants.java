package com.titans.grouptravelplanner;

import com.google.android.gms.maps.model.LatLng;

public class AppConstants {
    public static final LatLng DEFAULT_LAT_LNG = new LatLng(12.97, 77.6);
    public static final int DEFAULT_RANGE = 50; // 50 KM

    public static final String USER_DEFAULT_PREFERENCE = "I like mostly trekking and sightseeing"
            + " and expecting similar preference from fellow travellers.";

    public static class Gender {
        private Gender() {}

        public static final String MALE = "male";
        public static final String FEMALE = "female";
    }

    public static class Collections {
        private Collections() {}

        public static final String USERS = "users";
        public static final String SEARCH_HISTORIES = "searchHistories";
        public static final String CHATS = "chats";
        public static final String CHAT_MESSAGES = "chatMessages";
        public static final String PLACE_REVIEWS = "placeReviews";
    }
}
