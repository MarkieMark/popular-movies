package com.halloit.mark.popularmovies;

/**
 * Author Mark
 */

public class Movie {
    private String title;
    private String posterPath;
    private String backdropPath;
    private String originalTitle;
    private Double voteAverage;
    private String releaseDate;
    private String originalLanguage;
    private String overview;
    private String imageFullPath;
    private static Movie[] movieList;

    static Movie[] getMovieList() {
        return movieList;
    }

    static void setMovieList(Movie[] moviesList) {
        movieList = moviesList;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getPosterPath() {
        return posterPath;
    }

    void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    String getBackdropPath() {
        return backdropPath;
    }

    void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    String getOriginalTitle() {
        return originalTitle;
    }

    void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    Double getVoteAverage() {
        return voteAverage;
    }

    void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    String getReleaseDate() {
        return releaseDate;
    }

    void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    String getOriginalLanguage() {
        return originalLanguage;
    }

    void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    String getOverview() {
        return overview;
    }

    void setOverview(String overview) {
        this.overview = overview;
    }

    public String getImageFullPath() {
        return imageFullPath;
    }

    public void setImageFullPath(String imageFullPath) {
        this.imageFullPath = imageFullPath;
    }

    @Override
    public String toString() {
        return this.title + "\n" +
                this.releaseDate + "\n" +
                this.posterPath + "\n" +
                String.valueOf(this.voteAverage) + "\n" +
                this.overview;
    }
}
