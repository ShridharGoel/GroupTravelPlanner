package com.titans.grouptravelplanner;

public class Post extends PostId {

    private String userId,name, timestamp, likes, favourites, description, color,username,userimage;
    private int image_count;

    public Post(String userId, String name, String timestamp, String likes, String favourites, String description, String color, String username, String userimage, int image_count, String image_url_0, String image_url_1, String image_url_2, String image_url_3, String image_url_4, String image_url_5, String image_url_6) {
        this.userId = userId;
        this.name = name;
        this.timestamp = timestamp;
        this.likes = likes;
        this.favourites = favourites;
        this.description = description;
        this.color = color;
        this.username = username;
        this.userimage = userimage;
        this.image_count = image_count;
    }

    public Post() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getFavourites() {
        return favourites;
    }

    public void setFavourites(String favourites) {
        this.favourites = favourites;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public int getImage_count() {
        return image_count;
    }

    public void setImage_count(int image_count) {
        this.image_count = image_count;
    }
}
