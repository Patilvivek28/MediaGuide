package com.sinnovations.mediaguide.data;

/**
 * Created by Patil on 04-Dec-17.
 *
 */

public class Movie {
    public String id;
    public int movie_id;
    public int movie_vote_count;
    public double movie_vote_avg;
    public String movie_title;
    public double movie_popularity;
    public String movie_poster_path;
    public String movie_backdrop_path;
    public String movie_language;
    public boolean movie_rating;
    public String movie_overview;
    public String movie_release_date;

    public Movie(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movie_id=" + movie_id +
                ", movie_vote_count=" + movie_vote_count +
                ", movie_vote_avg=" + movie_vote_avg +
                ", movie_title='" + movie_title + '\'' +
                ", movie_popularity=" + movie_popularity +
                ", movie_poster_path='" + movie_poster_path + '\'' +
                ", movie_backdrop_path='" + movie_backdrop_path + '\'' +
                ", movie_language='" + movie_language + '\'' +
                ", movie_rating=" + movie_rating +
                ", movie_overview='" + movie_overview + '\'' +
                ", movie_release_date='" + movie_release_date + '\'' +
                '}';
    }

    public String shareMovie() {

        return "Hey...!\nLet's go for this new Movie: [\'"+movie_title+"\', Rating: "+movie_vote_avg+"].\nI found this info from the MediaGuide App.";
    }
}
