package com.sinnovations.mediaguide.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sinnovations.mediaguide.MovieDetailActivity;
import com.sinnovations.mediaguide.MovieDetailFragment;
import com.sinnovations.mediaguide.MovieListActivity;
import com.sinnovations.mediaguide.R;
import com.sinnovations.mediaguide.data.Constants;
import com.sinnovations.mediaguide.data.Movie;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Patil on 04-Dec-17.
 *
 */

public class MovieListAdapter
        extends RecyclerView.Adapter<MovieListAdapter.ViewHolder>
        implements Filterable{

    private final MovieListActivity mParentActivity;
    private final List<Movie> mValues;
    private List<Movie> mValuesFiltered;
    private final boolean mTwoPane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Movie item = (Movie) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(MovieDetailFragment.ARG_ITEM_ID, item.id);
                MovieDetailFragment fragment = new MovieDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, item.id);

                context.startActivity(intent);
            }
        }
    };

    public MovieListAdapter(MovieListActivity parent,
                             List<Movie> movies,
                             boolean twoPane) {
        mParentActivity = parent;
        mValues = movies;
        mValuesFiltered = movies;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        //holder.ivMoviePoster.setImageResource(R.mipmap.ic_launcher);
        final Movie movie = mValuesFiltered.get(position);
        String mMoviePosterPath = Constants.MOVIE_POSTER_URL + movie.movie_poster_path; //"http://image.tmdb.org/t/p/w500/MOVIE_POSTER_NAME.jpg"

        Glide.with(mParentActivity).load(mMoviePosterPath)
                .thumbnail(0.5f)
                .transition(withCrossFade())
                .into(holder.ivMoviePoster);

        holder.tvMovieName.setText(movie.movie_title);
        holder.tvMovieReleaseDate.setText(movie.movie_release_date);
        boolean mMovieRating = movie.movie_rating;
        if(mMovieRating) {
            holder.tvMovieRating.setText("(A)");
        }else {
            holder.tvMovieRating.setText("(U/A)");
        }
        holder.itemView.setTag(mValuesFiltered.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValuesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mValuesFiltered = mValues;
                } else {
                    List<Movie> filteredList = new ArrayList<>();
                    for (Movie row : mValues) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.movie_title.toLowerCase().contains(charString.toLowerCase()) || row.movie_release_date.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mValuesFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mValuesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mValuesFiltered = (ArrayList<Movie>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivMoviePoster;
        final TextView tvMovieName, tvMovieReleaseDate, tvMovieRating;

        ViewHolder(View view) {
            super(view);
            ivMoviePoster = (ImageView) view.findViewById(R.id.iv_movie_poster);
            tvMovieName = (TextView) view.findViewById(R.id.tv_movie_name);
            tvMovieReleaseDate = (TextView) view.findViewById(R.id.tv_movie_release_date);
            tvMovieRating = (TextView) view.findViewById(R.id.tv_movie_rating);
        }
    }
}

