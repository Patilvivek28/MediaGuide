package com.sinnovations.mediaguide.data;

import com.sinnovations.mediaguide.BuildConfig;

/**
 * Created by Patil on 04-Dec-17.
 */

public class Constants {

    public static final String MOVIE_LIST_URL = "https://api.themoviedb.org/3/movie/upcoming";
    public static final String KEY_NAME_API_KEY = "api_key";
    public static final String KEY_VALUE_API_KEY = BuildConfig.TMDB_API_KEY;

    public static final String MOVIE_POSTER_URL = "http://image.tmdb.org/t/p/w500";
    public static final String MOVIE_IMAGES_URL_1 = "https://api.themoviedb.org/3/movie/";
    public static final String MOVIE_IMAGES_URL_2 = "/images?api_key=" + KEY_VALUE_API_KEY;
}
