package com.cybexmobile.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.cybexmobile.Adapter.WatchListRecyclerViewAdapter;
import com.cybexmobile.Fragments.Data.WatchListData;
import com.cybexmobile.R;
import com.cybexmobile.Market.MarketStat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class WatchLIstFragment extends Fragment implements MarketStat.OnMarketStatUpdateListener, MarketStat.getResultListener {

    private static final String TAG = "WatchListFragment";
    private static final long MARKET_STAT_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(30);
    private static final long TICKER_STAT_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(10);

    private MarketStat marketStat;

    private String baseAsset;
    private String quoteAsset;
    private List<WatchListData> watchListDataList = new ArrayList<>();
    protected RecyclerView mRecyclerView;
    private Context mContext;
    private View view;
    private ProgressBar mProgressBar;


    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    private WatchListRecyclerViewAdapter mWatchListRecyclerViewAdapter;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WatchLIstFragment() {
        marketStat = MarketStat.getInstance();
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WatchLIstFragment newInstance(int columnCount) {
        WatchLIstFragment fragment = new WatchLIstFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.watchlist_list, container, false);
        mContext = view.getContext();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mProgressBar = (ProgressBar) view.findViewById(R.id.watch_list_progress_bar);
        if (marketStat.getWatchListData().size() == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        mWatchListRecyclerViewAdapter = new WatchListRecyclerViewAdapter(marketStat.getWatchListData(), mListener, getContext());
        if (mColumnCount <= 1) {
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 1);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setNestedScrollingEnabled(false);
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, mColumnCount));
        }
        mRecyclerView.setAdapter(mWatchListRecyclerViewAdapter);
        return view;
    }

    @Override
    public void getResultListener(List<WatchListData> DataList) {
        if (watchListDataList != null) {
            watchListDataList.clear();
            watchListDataList.addAll(DataList);
        }
        mWatchListRecyclerViewAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(WatchListData item, List<WatchListData> dataList, int position);
    }
}
