package com.sinnovations.mediaguide;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sinnovations.mediaguide.adapters.MovieListAdapter;
import com.sinnovations.mediaguide.data.Constants;
import com.sinnovations.mediaguide.data.Movie;
import com.sinnovations.mediaguide.utils.DateUtils;
import com.sinnovations.mediaguide.utils.MyApplication;
import com.sinnovations.mediaguide.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    public static final String LOG_TAG = MovieListActivity.class.getSimpleName();
    /**
     * An array of sample (movie) items.
     */
    public static final List<Movie> MOVIE_ITEM_LIST = new ArrayList<Movie>();
    /**
     * A map of sample (movie) items, by ID.
     */
    public static final Map<String, Movie> MOVIE_ITEM_MAP = new HashMap<String, Movie>();

    private static String jsonResponse;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    // Recycler instance
    View recyclerView;
    TextView emptyView;

    // Progress dialog
    private ProgressDialog pDialog;

    private MovieListAdapter mAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(true);

        emptyView = findViewById(R.id.tv_no_items);
        recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        mAdapter = new MovieListAdapter(this, MOVIE_ITEM_LIST, mTwoPane);

        if(new NetworkUtils(this).getNetworkStatus()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            if (MOVIE_ITEM_LIST.size() == 0) {     //To prevent reloading when the device rotates
                fetchMoviesList();
            } else {
                setupRecyclerView((RecyclerView) recyclerView);
            }
        }else {
            Snackbar snackbar = Snackbar.make(emptyView, "No internet connection!", Snackbar.LENGTH_LONG)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
            // Changing message text color
            snackbar.setActionTextColor(Color.RED);

            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);

            snackbar.show();
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }



    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    /**
     * fetches json by making http calls
     */
    private void fetchMoviesList() {

        Uri buildUri = Uri.parse(Constants.MOVIE_LIST_URL)
                .buildUpon()
                .appendQueryParameter(Constants.KEY_NAME_API_KEY, Constants.KEY_VALUE_API_KEY)
                .build();

        // url to fetch movies data json
        final String URL = buildUri.toString();     //"https://api.themoviedb.org/3/movie/upcoming?api_key=b7cd3340a794e5a2f35e3abb820b497f";
        showDialog();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            Log.e(LOG_TAG, "Error: NULL response");
                            Toast.makeText(getApplicationContext(), "Couldn't fetch the movies! Pleas try again.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // clearing previous list
                        MOVIE_ITEM_LIST.clear();
                        jsonResponse = "";

                        try {
                            JSONArray results = response.optJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject resultsObject = (JSONObject) results.get(i);
                                Movie item = new Movie(String.valueOf(i + 1));

                                int mMovieID = resultsObject.getInt("id");
                                item.movie_id = mMovieID;
                                String mMovieTitle = resultsObject.getString("title");
                                item.movie_title = mMovieTitle;
                                double mPopularity = resultsObject.getDouble("popularity");
                                item.movie_popularity = mPopularity;
                                double mVoteAverage = resultsObject.getDouble("vote_average");
                                item.movie_vote_avg = mVoteAverage;
                                boolean mRating = resultsObject.getBoolean("adult");
                                item.movie_rating = mRating;
                                String mReleaseDate = resultsObject.getString("release_date");
                                mReleaseDate = DateUtils.getCustomDate(mReleaseDate);
                                item.movie_release_date = mReleaseDate;
                                String mPosterPath = resultsObject.getString("poster_path");
                                item.movie_poster_path = mPosterPath;
                                String mBackdropPath = resultsObject.getString("backdrop_path");
                                item.movie_backdrop_path = mBackdropPath;
                                String mOverview = resultsObject.getString("overview");
                                item.movie_overview = mOverview;

                                jsonResponse = "mMovieID: " + mMovieID +
                                        "\nmMovieTitle: " + mMovieTitle +
                                        "\nmRating: " + mRating +
                                        "\nmVoteAverage" + mVoteAverage +
                                        "\nmPosterPath: " + mPosterPath +
                                        "\nmBackdropPath: " + mBackdropPath +
                                        "\nmReleaseDate: " + mReleaseDate +
                                        "\nmOverview" + mOverview +
                                        "\nmPopularity: " + mPopularity;

                                if (i == 0) {
                                    Log.d(LOG_TAG, "\nMovies Processed: " + (i + 1));
                                    Log.d(LOG_TAG, "\nJson Response: \n" + jsonResponse);
                                }

                                MOVIE_ITEM_LIST.add(item);
                                MOVIE_ITEM_MAP.put(item.id, item);

                                jsonResponse = "";
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();

                        setupRecyclerView((RecyclerView) recyclerView);

                        // refreshing recycler view
                        mAdapter.notifyDataSetChanged();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error in getting json
                Log.e(LOG_TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
