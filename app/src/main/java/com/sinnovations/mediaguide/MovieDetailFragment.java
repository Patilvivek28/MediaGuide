package com.sinnovations.mediaguide;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.sinnovations.mediaguide.data.Constants;
import com.sinnovations.mediaguide.data.Movie;
import com.sinnovations.mediaguide.utils.MyApplication;
import com.sinnovations.mediaguide.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {

    public static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Movie mItem;

    private ImageView ivMovieBackDrop;
    private TextView tvPosterCount;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = MovieListActivity.MOVIE_ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.movie_title);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

            RatingBar rb = rootView.findViewById(R.id.rb_movie_popularity);
            ivMovieBackDrop = rootView.findViewById(R.id.iv_movie_backdrop);
            ivMovieBackDrop.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    gdt.onTouchEvent(event);
                    return true;
                }
            });
            tvPosterCount = rootView.findViewById(R.id.tv_poster_count);
            ((TextView) rootView.findViewById(R.id.tv_movie_title)).setText(mItem.movie_title);
            ((TextView) rootView.findViewById(R.id.tv_movie_overview)).setText(mItem.movie_overview);
            rb.setRating((float) mItem.movie_vote_avg / 2);
            rb.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Toast.makeText(getContext(), "The Rating is: " + mItem.movie_vote_avg, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            if(new NetworkUtils(getContext()).getNetworkStatus()) { //If internet, get the posters
                ivMovieBackDrop.setVisibility(View.VISIBLE);
                tvPosterCount.setVisibility(View.VISIBLE);
                fetchMoviePosters(mItem.movie_id);
            }else {     //else hide the posters view and poster count
                Snackbar snackbar = Snackbar.make(ivMovieBackDrop, "No internet... Can't fetch posters!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Reload the posters if the network is back...
                            }
                        });
                // Changing message text color
                snackbar.setActionTextColor(Color.RED);

                // Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);

                snackbar.show();
                ivMovieBackDrop.setVisibility(View.GONE);
                tvPosterCount.setVisibility(View.GONE);
            }

        }

        return rootView;
    }

    private final GestureDetector gdt = new GestureDetector(this.getContext(), new GestureListener());

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int SWIPE_MIN_DISTANCE = 30;
        private final int SWIPE_THRESHOLD_VELOCITY = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                // Right to left, your code here
                if(POSTER_NO < POSTER_COUNT-1) {
                    POSTER_NO = POSTER_NO + 1;
                    String file_path = mPostersList.get(POSTER_NO);
                    String mMoviePosterPath = Constants.MOVIE_POSTER_URL + file_path; //"http://image.tmdb.org/t/p/w500/MOVIE_BACKDROP_NAME.jpg"
                    Glide.with(getContext()).load(mMoviePosterPath)
                            .thumbnail(0.5f)
                            .transition(withCrossFade())
                            .into(ivMovieBackDrop);
                    tvPosterCount.setText(getResources().getString(R.string.filler_poster_count, (POSTER_NO+1), POSTER_COUNT));
                }
                return true;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) >  SWIPE_THRESHOLD_VELOCITY) {
                // Left to right, your code here
                if(POSTER_NO < POSTER_COUNT && POSTER_NO > 0) {
                    POSTER_NO = POSTER_NO - 1;
                    String file_path = mPostersList.get(POSTER_NO);
                    String mMoviePosterPath = Constants.MOVIE_POSTER_URL + file_path; //"http://image.tmdb.org/t/p/w500/MOVIE_BACKDROP_NAME.jpg"
                    Glide.with(getContext()).load(mMoviePosterPath)
                            .thumbnail(0.5f)
                            .transition(withCrossFade())
                            .into(ivMovieBackDrop);
                    tvPosterCount.setText(getResources().getString(R.string.filler_poster_count, (POSTER_NO+1), POSTER_COUNT));
                }
                return true;
            }
            return true;
        }
    }

    String jsonResponse = "";
    private static final ArrayList<String> mPostersList = new ArrayList<>();
    private static int POSTER_NO = 0;
    private static int POSTER_COUNT;
    /**
     * fetches json by making http calls
     */
    private void fetchMoviePosters(int movieID) {
        String URL = Constants.MOVIE_IMAGES_URL_1 + movieID + Constants.MOVIE_IMAGES_URL_2;

        //showDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            Log.e(LOG_TAG, "Error: NULL response");
                            Toast.makeText(getContext(), "Couldn't fetch the posters! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // clearing previous list
                        mPostersList.clear();
                        jsonResponse = "";

                        try {
                            JSONArray backdrops = response.optJSONArray("backdrops");
                            for (int i = 0; i < backdrops.length(); i++) {
                                JSONObject backdropsObject = (JSONObject) backdrops.get(i);
                                String mFilePath = backdropsObject.getString("file_path");
                                mPostersList.add(mFilePath);

                                jsonResponse += "mFilePath: " + mFilePath + "\n";
                                Log.d(LOG_TAG, "\nPosters Processed: " + (i + 1));
                                Log.d(LOG_TAG, "\nJson Response: \n" + jsonResponse);

                                jsonResponse = "";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Update the POSTER_COUNT
                        POSTER_COUNT = mPostersList.size();
                        //Resetting the Poster No after the new Movie display
                        POSTER_NO = 0;  //Display the first poster
                        String file_path = mPostersList.get(POSTER_NO);
                        String mMoviePosterPath = Constants.MOVIE_POSTER_URL + file_path; //"http://image.tmdb.org/t/p/w500/MOVIE_BACKDROP_NAME.jpg"
                        Glide.with(getContext()).load(mMoviePosterPath)
                                .thumbnail(0.5f)
                                .transition(withCrossFade())
                                .into(ivMovieBackDrop);
                        tvPosterCount.setText(getResources().getString(R.string.filler_poster_count, (POSTER_NO+1), POSTER_COUNT));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(LOG_TAG, "Error: " + error.getMessage());
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                //hideDialog();
            }
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }
}
