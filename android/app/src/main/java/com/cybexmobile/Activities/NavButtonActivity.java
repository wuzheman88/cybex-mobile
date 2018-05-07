package com.cybexmobile.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cybexmobile.Fragments.ChooseThemeFragment;
import com.cybexmobile.Fragments.Data.WatchListData;
import com.cybexmobile.Fragments.FaqFragment;
import com.cybexmobile.Fragments.SettingFragment;
import com.cybexmobile.Fragments.WatchLIstFragment;
import com.cybexmobile.HelperClass.ActionBarTitleHelper;
import com.cybexmobile.HelperClass.BottomNavigationViewHelper;
import com.cybexmobile.HelperClass.StoreLanguageHelper;
import com.cybexmobile.R;
import com.cybexmobile.Market.MarketStat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

public class NavButtonActivity extends AppCompatActivity implements WatchLIstFragment.OnListFragmentInteractionListener, SettingFragment.OnFragmentInteractionListener, FaqFragment.OnFragmentInteractionListener {

    private BottomNavigationView mBottomNavigationView;
    private static final String KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID = "KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID";

    private WatchLIstFragment mWatchListFragment;
    private FaqFragment mFaqFragment;
    private SettingFragment mSettingFragment;
    private ChooseThemeFragment mChooseThemeFragment;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_watchlist:
                    setActionBarTitle(getResources().getString(R.string.title_watchlist));
                    showFragment(mWatchListFragment);
                    return true;
//                case R.id.navigation_explorer:
//                    setActionBarTitle(getResources().getString(R.string.title_explorer));
//                    return true;
                case R.id.navigation_faq:
                    setActionBarTitle(getResources().getString(R.string.title_faq));
                    showFragment(mFaqFragment);
                    return true;
                case R.id.navigation_setting:
                    setActionBarTitle(getResources().getString(R.string.title_setting));
                    showFragment(mSettingFragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateResources(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_nav_button);
        initFragments(savedInstanceState);
        if (mWatchListFragment != null) {
            if (MarketStat.getInstance().getWatchListDataList().size() == 0)
                MarketStat.getInstance().startRun(mWatchListFragment);
        }
        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        BottomNavigationViewHelper.removeShiftMode(mBottomNavigationView);
        mBottomNavigationView.setItemIconTintList(null);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mBottomNavigationView.getLayoutParams();
//        layoutParams.setBehavior(new BottomNavigationBehavior());
        ActionBarTitleHelper.centeredActionBarTitle(this);
        if (savedInstanceState != null) {
            int id = savedInstanceState.getInt(KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID, R.id.navigation_watchlist);
            switch (id) {
                case R.id.navigation_watchlist:
                    setActionBarTitle(getResources().getString(R.string.title_watchlist));
                    showFragment(mWatchListFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_faq:
                    setActionBarTitle(getResources().getString(R.string.title_faq));
                    showFragment(mFaqFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
                    break;
                case R.id.navigation_setting:
                    setActionBarTitle(getResources().getString(R.string.title_setting));
                    if (mChooseThemeFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .hide(mChooseThemeFragment)
                                .remove(mChooseThemeFragment)
                                .commit();
                        mBottomNavigationView.setVisibility(View.GONE);
                    }
                    showFragment(mSettingFragment);
                    mBottomNavigationView.setVisibility(View.VISIBLE);
//                    }
                    break;
            }
        } else {
            showFragment(mWatchListFragment);
        }
        EventBus.getDefault().register(this);
    }


    private Context updateResources(Context context) {
        String language = StoreLanguageHelper.getLanguageLocal(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        context = context.createConfigurationContext(config);
        return context;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String string) {
        switch (string) {
            case "EVENT_REFRESH_LANGUAGE":
                updateResources(getBaseContext());
                recreate();
                break;
            case "get_message":
               // recreate();
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_BOTTOM_NAVIGATION_VIEW_SELECTED_ID, mBottomNavigationView.getSelectedItemId());
        FragmentManager fm = getSupportFragmentManager();
        if (mWatchListFragment.isAdded()) {
            fm.putFragment(outState, WatchLIstFragment.class.getSimpleName(), mWatchListFragment);
        }
        if (mFaqFragment.isAdded()) {
            fm.putFragment(outState, FaqFragment.class.getSimpleName(), mFaqFragment);
        }
        if (mSettingFragment.isAdded()) {
            fm.putFragment(outState, SettingFragment.class.getSimpleName(), mSettingFragment);
        }
        if (mChooseThemeFragment != null && mChooseThemeFragment.isVisible()) {
            fm.putFragment(outState, ChooseThemeFragment.class.getSimpleName(), mChooseThemeFragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initFragments(Bundle savedInstanceState) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mWatchListFragment = new WatchLIstFragment();
            mFaqFragment = new FaqFragment();
            mSettingFragment = new SettingFragment();
        } else {
            mWatchListFragment = (WatchLIstFragment) fragmentManager.getFragment(savedInstanceState, WatchLIstFragment.class.getSimpleName());
            mFaqFragment = (FaqFragment) fragmentManager.getFragment(savedInstanceState, FaqFragment.class.getSimpleName());
            mSettingFragment = (SettingFragment) fragmentManager.getFragment(savedInstanceState, SettingFragment.class.getSimpleName());
            mChooseThemeFragment = (ChooseThemeFragment) fragmentManager.getFragment(savedInstanceState, ChooseThemeFragment.class.getSimpleName());
        }

        if (!mWatchListFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mWatchListFragment, WatchLIstFragment.class.getSimpleName())
                    .commit();
        }
        if (!mFaqFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mFaqFragment, FaqFragment.class.getSimpleName())
                    .commit();
        }
        if (!mSettingFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .add(R.id.frame_container, mSettingFragment, SettingFragment.class.getSimpleName())
                    .commit();
        }
    }

    private void setActionBarTitle(String actionBarTitle) {
        if (getSupportActionBar() != null) {
            TextView actionBarTextView = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.actionbar_title);
            actionBarTextView.setText(actionBarTitle);
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        if (fragment instanceof WatchLIstFragment) {
            fm.beginTransaction()
                    .show(mWatchListFragment)
                    .hide(mFaqFragment)
                    .hide(mSettingFragment)
                    .commit();

        } else if (fragment instanceof FaqFragment) {
            fm.beginTransaction()
                    .show(mFaqFragment)
                    .hide(mSettingFragment)
                    .hide(mWatchListFragment)
                    .commit();
        } else if (fragment instanceof SettingFragment) {
            fm.beginTransaction()
                    .show(mSettingFragment)
                    .hide(mFaqFragment)
                    .hide(mWatchListFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.frame_container);
        if (fragment instanceof ChooseThemeFragment) {
            super.onBackPressed();
            mBottomNavigationView.setVisibility(View.VISIBLE);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to Exit?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user pressed "yes", then he is allowed to exit from application
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if user select "No", just cancel this dialog and continue with app
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

        }


    }

    private void loadFragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().getCustomView().findViewById(R.id.action_bar_arrow_back_button).setVisibility(View.GONE);
//        }
    }

    @Override
    public void onListFragmentInteraction(WatchListData item, List<WatchListData> dataList, int position) {
        Intent intent = new Intent(NavButtonActivity.this, MarketsActivity.class);
        intent.putExtra("base", item.getBase());
        intent.putExtra("quote", item.getQuote());
        intent.putExtra("watchListData", item);
        intent.putExtra("id", position);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Fragment fragment) {
        mBottomNavigationView.setVisibility(View.GONE);
        mChooseThemeFragment = (ChooseThemeFragment) fragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frame_container, mChooseThemeFragment, "chooseFragment")
                .hide(mSettingFragment)
                .addToBackStack(null)
                .commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
