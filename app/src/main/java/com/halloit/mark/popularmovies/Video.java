package com.halloit.mark.popularmovies;

/**
 * Mark Benjamin 5/12/17.
 */

class Video {
    private String urlId;
    private String name;
    private String type;
    private String movieId;
    static Video[] theVideos;

    void setUrlId (String urlId) {
        this.urlId = urlId;
    }

    String getUrlId() {
        return urlId;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getType() {
        return type;
    }

    void setType(String type) {
        this.type = type;
    }

    String getMovieId() {
        return movieId;
    }

    void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    static void setVideos(Video [] videos) {
        theVideos = videos;
    }

    static Video[] getVideos() {
        return theVideos;
    }

    @Override
    public String toString() {
        return "URL id: " + urlId + ", name: " + name + ", type: " + type + ", movie ID: " + movieId;
    }
}
