package com.cybexmobile.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cybexmobile.Adapter.CoinPairRecyclerViewAdapter;
import com.cybexmobile.Adapter.OrderHistoryFragmentPageAdapter;
import com.cybexmobile.Constant.ConstantTest;
import com.cybexmobile.Data.DataParse;
import com.cybexmobile.Data.KLineBean;
import com.cybexmobile.Fragments.Data.WatchListData;
import com.cybexmobile.Fragments.MarketTradeHistoryFragment;
import com.cybexmobile.Fragments.OrderHistoryListFragment;
import com.cybexmobile.Fragments.dummy.DummyContent;
import com.cybexmobile.HelperClass.ActionBarTitleHelper;
import com.cybexmobile.HelperClass.StoreLanguageHelper;
import com.cybexmobile.Market.MarketStat;
import com.cybexmobile.Market.MarketTrade;
import com.cybexmobile.Mychart.CoupleChartGestureListener;
import com.cybexmobile.Mychart.MyBottomMarkerView;
import com.cybexmobile.Mychart.MyCombinedChart;
import com.cybexmobile.Mychart.MyHMarkerView;
import com.cybexmobile.Mychart.MyLeftMarkerView;
import com.cybexmobile.R;
import com.cybexmobile.Utils.MyUtils;
import com.cybexmobile.Utils.VolFormatter;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MarketsActivity extends AppCompatActivity implements MarketStat.OnMarketStatUpdateListener, CoinPairRecyclerViewAdapter.updateDataListener, OrderHistoryListFragment.OnListFragmentInteractionListener,
        MarketTradeHistoryFragment.OnListFragmentInteractionListener {

    private static final long MARKET_STAT_INTERVAL_MILLIS_5_MIN = TimeUnit.MINUTES.toSeconds(5);
    private static final long MARKET_STAT_INTERVAL_MILLIS_1_HOUR = TimeUnit.HOURS.toSeconds(1);
    private static final long MARKET_STAT_INTERVAL_MILLIS_1_DAY = TimeUnit.DAYS.toSeconds(1);


    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mRecyeclerViewLayoutManager;
    protected CoinPairRecyclerViewAdapter mCoinPairRecyclerViewAdapter;

    protected MyCombinedChart mChartKline;
    protected MyCombinedChart mChartVolume;
    protected MyCombinedChart mChartCharts;
    protected XAxis xAxisKline, xAxisVolume, xAxisCharts;
    protected YAxis axisLeftKline, axisLeftVolume, axisLeftCharts;
    protected YAxis axisRightKline, axisRightVolume, axisRightCharts;

    protected TextView mMAView, mMACDView, mBOLLView, mEMAView, mRSIView;
    protected TextView mTvKMa5, mTvKMa10, mTvKMa20;
    protected TextView mCurrentPriceView, mHighPriceView, mLowPriceView, mChangeRateView, mVolumeBaseView, mVolumeQuoteView, mDuration5mView, mDuration1hView, mDuration1dView;
    protected ProgressBar mProgressBar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private OrderHistoryFragmentPageAdapter mOrderHistoryFragmentPageAdapter;
    protected List<MarketStat.HistoryPrice> mHistoryPriceList;
    protected WatchListData mWatchListData;
    protected MarketStat.OnMarketStatUpdateListener mListener;
    protected OrderHistoryListFragment mOrderHistoryListFragment;
    private long mDuration = MARKET_STAT_INTERVAL_MILLIS_1_DAY;

    protected String base;
    protected String quote;

    private DataParse mData;
    private DataParse mCacheData;
    private MarketStat mMarketStat;
    private CandleData mCandleData;

    private ArrayList<KLineBean> kLineDatas;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mChartKline.setAutoScaleMinMaxEnabled(true);
            mChartVolume.setAutoScaleMinMaxEnabled(true);
            mChartCharts.setAutoScaleMinMaxEnabled(true);

            mChartKline.notifyDataSetChanged();
            mChartVolume.notifyDataSetChanged();
            mChartCharts.notifyDataSetChanged();

            mChartKline.invalidate();
            mChartVolume.invalidate();
            mChartCharts.invalidate();
        }
    };


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateResources(base));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markets);
        ActionBarTitleHelper.centeredActionBarTitle(this);
        setActionBarTitle(getResources().getString(R.string.markets_title));
        Intent intent = getIntent();
        mWatchListData = (WatchListData) intent.getSerializableExtra("watchListData");
        int id = getIntent().getIntExtra("id", 0);
        mMarketStat = MarketStat.getInstance();
        mMarketStat.subscribe(mWatchListData.getBase(), mWatchListData.getQuote(), MarketStat.STAT_MARKET_HISTORY, (long) 5, mDuration, this);
        initViews();
        addContentToView(mWatchListData);
        mListener = this;
        mDuration1dView.setSelected(true);
        mDuration1dView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(R.drawable.market_page_highlight_line));

        mRecyeclerViewLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mRecyeclerViewLayoutManager);
        mCoinPairRecyclerViewAdapter = new CoinPairRecyclerViewAdapter(this, mMarketStat.getWatchListData(), id, this);
        mRecyclerView.setAdapter(mCoinPairRecyclerViewAdapter);
        mRecyclerView.scrollToPosition(id);


        initChartKline();
        initChartVolume();
        initChartChart();
        setChartListener();
        mProgressBar.setVisibility(View.VISIBLE);
//        EventBus.getDefault().register(this);
//        initChartData(mHistoryPriceList);
//
//        setKlineByChart(mChartKline);
//        setVolumeByChart(mChartVolume);
//        setMACDByChart(mChartCharts);
//        mChartKline.moveViewToX(kLineDatas.size() - 1);
//        mChartVolume.moveViewToX(kLineDatas.size() - 1);
//        mChartCharts.moveViewToX(kLineDatas.size() - 1);
//        setOnClickListener();
//        handler.sendEmptyMessageDelayed(0, 300);

    }

    @Override
    public void onClickHorizontalItem(WatchListData watchListData) {
        addContentToView(watchListData);
        mWatchListData = watchListData;
        mMarketStat.subscribe(watchListData.getBase(), watchListData.getQuote(), MarketStat.STAT_MARKET_HISTORY, (long) 300, MARKET_STAT_INTERVAL_MILLIS_1_DAY, this);
        mProgressBar.setVisibility(View.VISIBLE);
        mChartKline.setVisibility(View.GONE);
        mChartVolume.setVisibility(View.GONE);
    }

    @Override
    public void onMarketStatUpdate(MarketStat.Stat stat) {
        if (stat.prices != null && stat.prices.length != 0) {
            mHistoryPriceList = Arrays.asList(stat.prices);
            initChartData(mHistoryPriceList, mDuration);
            setKlineByChart(mChartKline);
            setVolumeByChart(mChartVolume);
            setMACDByChart(mChartCharts);
            mChartKline.moveViewToX(kLineDatas.size() - 1);
            mChartVolume.moveViewToX(kLineDatas.size() - 1);
            mChartCharts.moveViewToX(kLineDatas.size() - 1);
            setOnClickListener();
            mChartKline.setVisibility(View.VISIBLE);
            mChartVolume.setVisibility(View.VISIBLE);
            handler.sendEmptyMessageDelayed(0, 300);
        }
        mProgressBar.setVisibility(View.GONE);
        mOrderHistoryFragmentPageAdapter = new OrderHistoryFragmentPageAdapter(getSupportFragmentManager());
        mOrderHistoryFragmentPageAdapter.addFragment(OrderHistoryListFragment.newInstance(1, stat.orderBook, mWatchListData), getResources().getString(R.string.market_page_order_book));
        mOrderHistoryFragmentPageAdapter.addFragment(MarketTradeHistoryFragment.newInstance(1, stat.marketTradeList, mWatchListData), getResources().getString(R.string.market_page_trade_history));
        mViewPager.setAdapter(mOrderHistoryFragmentPageAdapter);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private double getLowFromPriceList(List<MarketStat.HistoryPrice> historyPriceList) {
        double min = historyPriceList.get(0).low;
        for (MarketStat.HistoryPrice historyPrice : historyPriceList) {
            min = Math.min(historyPrice.low, min);
        }
        return min;
    }

    private void setActionBarTitle(String actionBarTitle) {
        if (getSupportActionBar() != null) {
            TextView actionBarTextView = (TextView) getSupportActionBar().getCustomView().findViewById(R.id.actionbar_title);
            ImageView backButton = (ImageView) getSupportActionBar().getCustomView().findViewById(R.id.action_bar_back_button);
            actionBarTextView.setText(actionBarTitle);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.coin_pair_horizontal_recycler_view);
        mChartKline = (MyCombinedChart) findViewById(R.id.kline_chart_k);
        mChartVolume = (MyCombinedChart) findViewById(R.id.kline_chart_volume);
        mChartCharts = (MyCombinedChart) findViewById(R.id.kline_chart_chart);
        mMAView = (TextView) findViewById(R.id.kline_ma);
        mMACDView = (TextView) findViewById(R.id.kline_macd);
        mBOLLView = (TextView) findViewById(R.id.kline_boll);
        mEMAView = (TextView) findViewById(R.id.kline_ema);
        mRSIView = (TextView) findViewById(R.id.kline_rsi);
        mCurrentPriceView = (TextView) findViewById(R.id.market_page_current_money);
        mHighPriceView = (TextView) findViewById(R.id.market_page_high_price);
        mLowPriceView = (TextView) findViewById(R.id.market_page_low_price);
        mVolumeBaseView = (TextView) findViewById(R.id.market_page_volume_base);
        mVolumeQuoteView = (TextView) findViewById(R.id.market_page_volume_quote);
        mChangeRateView = (TextView) findViewById(R.id.market_page_exchange_variation);
        mProgressBar = (ProgressBar) findViewById(R.id.market_page_progress_bar);
        mDuration5mView = (TextView) findViewById(R.id.market_page_5_min);
        mDuration1hView = (TextView) findViewById(R.id.market_page_1_hour);
        mDuration1dView = (TextView) findViewById(R.id.market_page_1_day);
        mViewPager = (ViewPager) findViewById(R.id.market_page_view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.market_page_tab_layout);

        mTvKMa5 = (TextView) findViewById(R.id.view_kline_tv_ma5);
        mTvKMa10 = (TextView) findViewById(R.id.view_kline_tv_ma10);
        mTvKMa20 = (TextView) findViewById(R.id.view_kline_tv_ma20);
    }

    private void addContentToView(WatchListData watchListData) {

        NumberFormat formatter = new DecimalFormat("##,##0.00000");
        NumberFormat formatter2 = new DecimalFormat("0.00");
        mCurrentPriceView.setText(watchListData.getCurrentPrice() == 0.f ? "-" : String.valueOf(formatter.format(watchListData.getCurrentPrice())));
        mHighPriceView.setText(watchListData.getHigh() == 0.f ? "-" : String.format("High:%s", String.valueOf(formatter.format(watchListData.getHigh()))));
        mLowPriceView.setText(watchListData.getLow() == 0.f ? "-" : String.format("Low:%s", String.valueOf(formatter.format(watchListData.getLow()))));
        mVolumeBaseView.setText(watchListData.getVol() == 0.f ? "-" : String.format("Vol:%s", MyUtils.format(watchListData.getVol())));

        double volQuote = 0.f;
        if (watchListData.getCurrentPrice() != 0.f) {
            volQuote = watchListData.getVol() / watchListData.getCurrentPrice();
        }
        mVolumeQuoteView.setText(volQuote == 0.f ? "-" : MyUtils.format(watchListData.getQuoteVol()));

        double change = 0.f;
        if (watchListData.getChange() != null) {
            try {
                change = Double.parseDouble(watchListData.getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (change > 0.f) {
            mChangeRateView.setText(String.format("+%s%%", String.valueOf(formatter2.format(change * 100))));
            mChangeRateView.setTextColor(getResources().getColor(R.color.increasing_color));
        } else if (change < 0.f) {
            mChangeRateView.setText(String.format("%s%%", String.valueOf(formatter2.format(change * 100))));
            mChangeRateView.setTextColor(getResources().getColor(R.color.decreasing_color));
        } else {
            mChangeRateView.setText("--");
            mChangeRateView.setTextColor(getResources().getColor(R.color.no_change_color));
        }
    }

    private void initChartKline() {
        mChartKline.setScaleEnabled(true);//启用图表缩放事件
        mChartKline.setDrawBorders(true);//是否绘制边线
        mChartKline.setBorderWidth(1);//边线宽度，单位dp
        mChartKline.setDragEnabled(true);//启用图表拖拽事件
        mChartKline.setScaleYEnabled(true);//启用Y轴上的缩放
        mChartKline.setBorderColor(Color.TRANSPARENT);//边线颜色
        mChartKline.setDescription("");//右下角对图表的描述信息
        mChartKline.setMinOffset(0f);
        mChartKline.setExtraOffsets(0f, 0f, 0f, 3f);

        Legend lineChartLegend = mChartKline.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例
        lineChartLegend.setForm(Legend.LegendForm.CIRCLE);

        //bar x y轴
        xAxisKline = mChartKline.getXAxis();
        xAxisKline.setDrawLabels(true); //是否显示X坐标轴上的刻度，默认是true
        xAxisKline.setDrawGridLines(true);//是否显示X坐标轴上的刻度竖线，默认是true
        xAxisKline.setDrawAxisLine(false); //是否绘制坐标轴的线，即含有坐标的那条线，默认是true
        xAxisKline.enableGridDashedLine(10f, 10f, 0f);//虚线表示X轴上的刻度竖线(float lineLength, float spaceLength, float phase)三个参数，1.线长，2.虚线间距，3.虚线开始坐标
        xAxisKline.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));//设置字的颜色
        xAxisKline.setPosition(XAxis.XAxisPosition.TOP_INSIDE);//设置值显示在什么位置
        xAxisKline.setAvoidFirstLastClipping(true);//设置首尾的值是否自动调整，避免被遮挡

        axisLeftKline = mChartKline.getAxisLeft();
        axisLeftKline.setDrawGridLines(false);
        axisLeftKline.setDrawAxisLine(false);
        axisLeftKline.setDrawZeroLine(false);
        axisLeftKline.setDrawLabels(true);
        axisLeftKline.enableGridDashedLine(10f, 10f, 0f);
        axisLeftKline.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
//        axisLeftKline.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftKline.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftKline.setLabelCount(4, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftKline.setSpaceTop(10);//距离顶部留白

        axisRightKline = mChartKline.getAxisRight();
        axisRightKline.setDrawLabels(false);
        axisRightKline.setDrawGridLines(false);
        axisRightKline.setDrawAxisLine(false);
        axisRightKline.setLabelCount(4, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布

        mChartKline.setDragDecelerationEnabled(true);
        mChartKline.setDragDecelerationFrictionCoef(0.2f);

        mChartKline.animateXY(2000, 2000);
    }

    private void initChartVolume() {
        mChartVolume.setDrawBorders(true);  //边框是否显示
        mChartVolume.setBorderWidth(1);//边框的宽度，float类型，dp单位
        mChartVolume.setBorderColor(Color.TRANSPARENT);//边框颜色
        mChartVolume.setDescription(""); //图表默认右下方的描述，参数是String对象
        mChartVolume.setDragEnabled(true);// 是否可以拖拽
        mChartVolume.setScaleYEnabled(false); //是否可以缩放 仅y轴
        mChartVolume.setMinOffset(3f);
        mChartVolume.setExtraOffsets(0f, 0f, 0f, 5f);

        Legend combinedchartLegend = mChartVolume.getLegend(); // 设置比例图标示，就是那个一组y的value的
        combinedchartLegend.setEnabled(false);//是否绘制比例图

        //bar x y轴
        xAxisVolume = mChartVolume.getXAxis();
        xAxisVolume.setEnabled(false);
//        xAxisVolume.setDrawLabels(false); //是否显示X坐标轴上的刻度，默认是true
//        xAxisVolume.setDrawGridLines(false);//是否显示X坐标轴上的刻度竖线，默认是true
//        xAxisVolume.setDrawAxisLine(false); //是否绘制坐标轴的线，即含有坐标的那条线，默认是true
//        xAxisVolume.enableGridDashedLine(10f, 10f, 0f);//虚线表示X轴上的刻度竖线(float lineLength, float spaceLength, float phase)三个参数，1.线长，2.虚线间距，3.虚线开始坐标
//        xAxisVolume.setTextColor(getResources().getColor(R.color.text_color_common));//设置字的颜色
//        xAxisVolume.setPosition(XAxis.XAxisPosition.BOTTOM);//设置值显示在什么位置
//        xAxisVolume.setAvoidFirstLastClipping(true);//设置首尾的值是否自动调整，避免被遮挡

        axisLeftVolume = mChartVolume.getAxisLeft();
        axisLeftVolume.setAxisMinValue(0);//设置Y轴坐标最小为多少
//        axisLeftVolume.setShowOnlyMinMax(true);//设置Y轴坐标最小为多少
        axisLeftVolume.setDrawGridLines(true);
        axisLeftVolume.setDrawAxisLine(false);
//        axisLeftVolume.setShowOnlyMinMax(true);
        axisLeftVolume.setDrawLabels(true);
        axisLeftVolume.enableGridDashedLine(10f, 10f, 0f);
        axisLeftVolume.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
//        axisLeftVolume.setGridColor(getResources().getColor(R.color.minute_grayLine));
        axisLeftVolume.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftVolume.setLabelCount(1, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftVolume.setSpaceTop(0);//距离顶部留白
//        axisLeftVolume.setSpaceBottom(0);//距离顶部留白

        axisRightVolume = mChartVolume.getAxisRight();
        axisRightVolume.setDrawLabels(false);
        axisRightVolume.setDrawGridLines(false);
        axisRightVolume.setDrawAxisLine(false);

        mChartVolume.setDragDecelerationEnabled(true);
        mChartVolume.setDragDecelerationFrictionCoef(0.2f);

        mChartVolume.animateXY(2000, 2000);
    }

    private void initChartChart() {

        mChartCharts.setScaleEnabled(true);//启用图表缩放事件
        mChartCharts.setDrawBorders(true);//是否绘制边线
        mChartCharts.setBorderWidth(1);//边线宽度，单位dp
        mChartCharts.setDragEnabled(true);//启用图表拖拽事件
        mChartCharts.setScaleYEnabled(false);//启用Y轴上的缩放
        mChartCharts.setBorderColor(Color.TRANSPARENT);//边线颜色
        mChartCharts.setDescription("");//右下角对图表的描述信息
        mChartCharts.setMinOffset(0f);
        mChartCharts.setExtraOffsets(0f, 0f, 0f, 3f);

        Legend lineChartLegend = mChartCharts.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例

        //bar x y轴
        xAxisCharts = mChartCharts.getXAxis();
        xAxisCharts.setEnabled(false);

        axisLeftCharts = mChartCharts.getAxisLeft();
        axisLeftCharts.setDrawGridLines(true);
        axisLeftCharts.setDrawAxisLine(false);
        axisLeftCharts.setDrawLabels(true);
        axisLeftCharts.enableGridDashedLine(10f, 10f, 0f);
        axisLeftCharts.setTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
        axisLeftCharts.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftCharts.setLabelCount(1, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布


        axisRightCharts = mChartCharts.getAxisRight();
        axisRightCharts.setDrawLabels(false);
        axisRightCharts.setDrawGridLines(false);
        axisRightCharts.setDrawAxisLine(false);

        mChartCharts.setDragDecelerationEnabled(true);
        mChartCharts.setDragDecelerationFrictionCoef(0.2f);

        mChartCharts.animateXY(2000, 2000);

    }

    private void setChartListener() {
        // 将K线控的滑动事件传递给交易量控件
        mChartKline.setOnChartGestureListener(new CoupleChartGestureListener(mChartKline, new Chart[]{mChartVolume, mChartCharts}));
        // 将交易量控件的滑动事件传递给K线控件
        mChartVolume.setOnChartGestureListener(new CoupleChartGestureListener(mChartVolume, new Chart[]{mChartKline, mChartCharts}));

        mChartCharts.setOnChartGestureListener(new CoupleChartGestureListener(mChartCharts, new Chart[]{mChartKline, mChartVolume}));

        mChartKline.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() - mChartKline.getHeight();
                Highlight h1 = mChartVolume.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartVolume.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() - mChartKline.getHeight() - mChartVolume.getHeight();
                Highlight h2 = mChartCharts.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartCharts.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartVolume.highlightValue(null);
                mChartCharts.highlightValue(null);
            }
        });

        mChartVolume.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() + mChartKline.getHeight();
                Highlight h1 = mChartKline.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartKline.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() - mChartVolume.getHeight();
                Highlight h2 = mChartCharts.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartCharts.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartKline.highlightValue(null);
                mChartCharts.highlightValue(null);
            }
        });

        mChartCharts.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                Highlight highlight = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY = h.getTouchY() + mChartVolume.getHeight();
                Highlight h1 = mChartVolume.getHighlightByTouchPoint(h.getXIndex(), touchY);
                highlight.setTouchY(touchY);
                if (null == h1) {
                    highlight.setTouchYValue(0);
                } else {
                    highlight.setTouchYValue(h1.getTouchYValue());
                }
                mChartVolume.highlightValues(new Highlight[]{highlight});

                Highlight highlight2 = new Highlight(h.getXIndex(), h.getValue(), h.getDataIndex(), h.getDataSetIndex());

                float touchY2 = h.getTouchY() + mChartVolume.getHeight() + mChartKline.getHeight();
                Highlight h2 = mChartKline.getHighlightByTouchPoint(h.getXIndex(), touchY2);
                highlight2.setTouchY(touchY2);
                if (null == h2) {
                    highlight2.setTouchYValue(0);
                } else {
                    highlight2.setTouchYValue(h2.getTouchYValue());
                }
                mChartKline.highlightValues(new Highlight[]{highlight2});

                updateText(e.getXIndex());
            }

            @Override
            public void onNothingSelected() {
                mChartKline.highlightValue(null);
                mChartVolume.highlightValue(null);
            }
        });

    }

    private void initChartData(List<MarketStat.HistoryPrice> historyPriceList, long duration) {
        getOffLineData(historyPriceList, duration);
        setKLineDatas();

        //setMarkerViewButtom(mData, mChartKline);
    }

    private void getOffLineData(List<MarketStat.HistoryPrice> historyPriceList, long duraton) {
        /*方便测试，加入假数据*/
        mData = new DataParse();
        JSONObject object = null;
        try {
            object = new JSONObject(ConstantTest.KLINEURL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("数据-----", object.toString());
        mData.parseKlineHistoryData(historyPriceList, duraton);
        //mData.parseKLine(object);

        mCacheData = new DataParse();
        mCacheData.parseKLine(object);
    }

    private void setKLineDatas() {
        kLineDatas = mData.getKLineDatas();
        mData.initLineDatas(kLineDatas);
    }

    private void setMarkerViewButtom(DataParse mData, MyCombinedChart combinedChart) {
        MyLeftMarkerView leftMarkerView = new MyLeftMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        MyHMarkerView hMarkerView = new MyHMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        MyBottomMarkerView bottomMarkerView = new MyBottomMarkerView(MarketsActivity.this, R.layout.my_marker_view);
        combinedChart.setMarker(leftMarkerView, bottomMarkerView, hMarkerView, mData);
    }

    private void setKlineByChart(MyCombinedChart combinedChart) {
        CandleDataSet set = new CandleDataSet(mData.getCandleEntries(), "");
        set.setDrawHorizontalHighlightIndicator(true);
        set.setHighlightEnabled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(1f);
        set.setValueTextSize(10f);
        set.setDecreasingColor(getResources().getColor(R.color.decreasing_color));//设置开盘价高于收盘价的颜色
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(getResources().getColor(R.color.increasing_color));//设置开盘价地狱收盘价的颜色
        set.setIncreasingPaintStyle(Paint.Style.FILL);
        set.setNeutralColor(getResources().getColor(R.color.increasing_color));//设置开盘价等于收盘价的颜色
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(.5f);
        set.setHighLightColor(getResources().getColor(R.color.no_change_color));
        set.setDrawValues(true);
        set.setValueTextColor(getResources().getColor(R.color.primary_color_white));
        CandleData candleData = new CandleData(mData.getXVals(), set);
        mCandleData = candleData;
        combinedChart.getAxisLeft().setAxisMinValue((float) getLowFromPriceList(mHistoryPriceList));

        mData.initKLineMA(kLineDatas);
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        /******此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
        sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataL()));
        sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataL()));
        sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataL()));


        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
//        combinedData.setData(lineData);
        combinedData.setData(candleData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);
    }

    private void setVolumeByChart(MyCombinedChart combinedChart) {

        String unit = MyUtils.getVolUnit(mData.getVolmax());
        String wan = "万手";
        String yi = "亿手";
        int u = 1;
        if (wan.equals(unit)) {
            u = 4;
        } else if (yi.equals(unit)) {
            u = 8;
        }
        combinedChart.getAxisLeft().setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
//        combinedChart.getAxisLeft().setAxisMaxValue(mData.getVolmax());
        Log.e("@@@", mData.getVolmax() + "da");

        BarDataSet set = new BarDataSet(mData.getBarEntries(), "成交量");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(false);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.no_change_color));
        set.setDrawValues(false);

        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.fade_background_green));
        list.add(getResources().getColor(R.color.fade_background_red));
        set.setColors(list);
        BarData barData = new BarData(mData.getXVals(), set);

        mData.initVlumeMA(kLineDatas);
//        ArrayList<ILineDataSet> sets = new ArrayList<>();
//
//        /******此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
//        sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataV()));
//        sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataV()));
//        sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataV()));
//        sets.add(setMaLine(30, mData.getXVals(), mData.getMa30DataV()));

//        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
//        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);

    }

    private void setMACDByChart(MyCombinedChart combinedChart) {
        mData.initMACD(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getMacdData(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.primary_color_white));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.increasing_color));
        list.add(getResources().getColor(R.color.decreasing_color));
        set.setColors(list);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setMACDMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getDeaData()));
        sets.add(setMACDMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getDifData()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
        setHandler(combinedChart);
    }

    private void setKDJByChart(MyCombinedChart combinedChart) {
        mData.initKDJ(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getBarDatasKDJ(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.kline_x_axis_text_color));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getkData()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getdData()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getjData()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

//        if (isRefresh)
//            setHandler(combinedChart);
    }

    private void setRSIByChart(MyCombinedChart combinedChart) {
        mData.initRSI(kLineDatas);

        BarDataSet set = new BarDataSet(mData.getBarDatasRSI(), "BarDataSet");
        set.setBarSpacePercent(20); //bar空隙
        set.setHighlightEnabled(true);
        set.setHighLightAlpha(255);
        set.setHighLightColor(getResources().getColor(R.color.primary_color_white));
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.TRANSPARENT);

        BarData barData = new BarData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData6()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData12()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getRsiData24()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(barData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);

        setHandler(combinedChart);
    }

    private void setBOLLByChart(MyCombinedChart combinedChart) {
        mData.initBOLL(kLineDatas);

//        BarDataSet set = new BarDataSet(mData.getBarDatasBOLL(), "Sinus Function");
//        set.setBarSpacePercent(20); //bar空隙
//        set.setHighlightEnabled(true);
//        set.setHighLightAlpha(255);
//        set.setHighLightColor(getResources().getColor(R.color.marker_line_bg));
//        set.setDrawValues(false);
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setColor(getResources().getColor(R.color.transparent));
//
//        BarData barData = new BarData(mData.getXVals(), set);

        int size = kLineDatas.size();   //点的个数
        CandleDataSet set = new CandleDataSet(mData.getCandleEntries(), "");
        set.setDrawHorizontalHighlightIndicator(false);
        set.setHighlightEnabled(true);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(1f);
        set.setValueTextSize(10f);
        set.setDecreasingColor(getResources().getColor(R.color.decreasing_color));
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(getResources().getColor(R.color.increasing_color));
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(getResources().getColor(R.color.decreasing_color));
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(1f);
        set.setHighLightColor(getResources().getColor(R.color.primary_color_white));
        set.setDrawValues(true);
        set.setValueTextColor(getResources().getColor(R.color.kline_x_axis_text_color));
        set.setShowCandleBar(false);
        CandleData candleData = new CandleData(mData.getXVals(), set);

        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataUP()));
        sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataMB()));
        sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataDN()));
        LineData lineData = new LineData(mData.getXVals(), sets);

        CombinedData combinedData = new CombinedData(mData.getXVals());
        combinedData.setData(candleData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
    }

    @NonNull
    private LineDataSet setMaLine(int ma, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + ma);
        if (ma == 5) {
            lineDataSetMa.setHighlightEnabled(true);
            lineDataSetMa.setDrawHorizontalHighlightIndicator(false);
            lineDataSetMa.setHighLightColor(getResources().getColor(R.color.increasing_color));
        } else {/*此处必须得写*/
            lineDataSetMa.setHighlightEnabled(false);
        }
        lineDataSetMa.setDrawValues(false);
        if (ma == 5) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else if (ma == 10) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        } else if (ma == 20) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        }
        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineDataSetMa.setHighlightEnabled(false);
        return lineDataSetMa;
    }

    @NonNull
    private LineDataSet setKDJMaLine(int type, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setHighlightEnabled(false);
        lineDataSetMa.setDrawValues(false);

        //DEA
        if (type == 0) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else if (type == 1) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        } else if (type == 2) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_20_color));
        }

        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        return lineDataSetMa;
    }


    private void setHandler(MyCombinedChart combinedChart) {
        final ViewPortHandler viewPortHandlerBar = combinedChart.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(mData.getXVals().size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        final float xscale = 3;
        touchmatrix.postScale(xscale, 1f);
    }

    @NonNull
    private LineDataSet setMACDMaLine(int type, ArrayList<String> xVals, ArrayList<Entry> lineEntries) {
        LineDataSet lineDataSetMa = new LineDataSet(lineEntries, "ma" + type);
        lineDataSetMa.setHighlightEnabled(false);
        lineDataSetMa.setDrawValues(false);

        //DEA
        if (type == 0) {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_5_color));
        } else {
            lineDataSetMa.setColor(getResources().getColor(R.color.ma_10_color));
        }

        lineDataSetMa.setLineWidth(1f);
        lineDataSetMa.setDrawCircles(false);
        lineDataSetMa.setAxisDependency(YAxis.AxisDependency.LEFT);

        return lineDataSetMa;
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    private void updateText(int index) {

        int newIndex = index;
        if (null != mData.getMa5DataL() && mData.getMa5DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa5DataL().size())
                mTvKMa5.setText(MyUtils.getDecimalFormatVol(mData.getMa5DataL().get(newIndex).getVal()));
        }
        if (null != mData.getMa10DataL() && mData.getMa10DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa10DataL().size())
                mTvKMa10.setText(MyUtils.getDecimalFormatVol(mData.getMa10DataL().get(newIndex).getVal()));
        }
        if (null != mData.getMa20DataL() && mData.getMa20DataL().size() > 0) {
            if (newIndex >= 0 && newIndex < mData.getMa20DataL().size())
                mTvKMa20.setText(MyUtils.getDecimalFormatVol(mData.getMa20DataL().get(newIndex).getVal()));
        }
    }

    private void setOnClickListener() {
        mMAView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMAView.setSelected(true);
                mBOLLView.setSelected(false);
                mMACDView.setSelected(false);
                mEMAView.setSelected(false);
                mData.initKLineMA(kLineDatas);
                ArrayList<ILineDataSet> sets = new ArrayList<>();
                /******此处修复如果显示的点的个数达不到MA均线的位置所有的点都从0开始计算最小值的问题******************************/
                sets.add(setMaLine(5, mData.getXVals(), mData.getMa5DataL()));
                sets.add(setMaLine(10, mData.getXVals(), mData.getMa10DataL()));
                sets.add(setMaLine(20, mData.getXVals(), mData.getMa20DataL()));


                LineData lineData = new LineData(mData.getXVals(), sets);
                CombinedData combinedData = new CombinedData(mData.getXVals());
                combinedData.setData(lineData);
                combinedData.setData(mCandleData);
                mChartKline.setData(combinedData);
                mChartKline.notifyDataSetChanged();
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);
//                setHandler(mChartKline);

            }
        });

        mMACDView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMACDView.setSelected(true);
                mMAView.setSelected(false);
                mBOLLView.setSelected(false);
                mEMAView.setSelected(false);
                setMACDByChart(mChartCharts);
                mChartCharts.setVisibility(View.VISIBLE);
                mChartCharts.invalidate();
            }
        });

        mBOLLView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setBOLLByChart(mChartCharts);
//                mChartCharts.invalidate();
                mMAView.setSelected(false);
                mBOLLView.setSelected(true);
                mEMAView.setSelected(false);
                mMACDView.setSelected(false);
                mData.initBOLL(kLineDatas);
                ArrayList<ILineDataSet> sets = new ArrayList<>();
                sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataUP()));
                sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataMB()));
                sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getBollDataDN()));
                LineData lineData = new LineData(mData.getXVals(), sets);
                CombinedData combinedData = new CombinedData(mData.getXVals());
                combinedData.setData(lineData);
                combinedData.setData(mCandleData);
                mChartKline.setData(combinedData);
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);

            }
        });

        mEMAView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMAView.setSelected(false);
                mBOLLView.setSelected(false);
                mEMAView.setSelected(true);
                mMACDView.setSelected(false);
                mData.initEXPMA(kLineDatas);

                ArrayList<ILineDataSet> sets = new ArrayList<>();
                sets.add(setKDJMaLine(0, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData5()));
                sets.add(setKDJMaLine(1, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData10()));
                sets.add(setKDJMaLine(2, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData20()));
                sets.add(setKDJMaLine(3, mData.getXVals(), (ArrayList<Entry>) mData.getExpmaData60()));
                LineData lineData = new LineData(mData.getXVals(), sets);

                CombinedData combinedData = new CombinedData(mData.getXVals());
                combinedData.setData(lineData);
                combinedData.setData(mCandleData);
                mChartKline.setData(combinedData);
                mChartKline.invalidate();
                mChartCharts.setVisibility(View.GONE);
            }
        });

        mRSIView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRSIByChart(mChartCharts);
                mChartCharts.invalidate();
            }
        });

        mDuration5mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDuration5mView.setSelected(true);
                mDuration1hView.setSelected(false);
                mDuration1dView.setSelected(false);
                mDuration5mView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(R.drawable.market_page_highlight_line));
                mDuration1dView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mDuration1hView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mMarketStat.subscribe(mWatchListData.getBase(), mWatchListData.getQuote(), MarketStat.STAT_MARKET_HISTORY, (long) 300, MARKET_STAT_INTERVAL_MILLIS_5_MIN, mListener);
                mDuration = MARKET_STAT_INTERVAL_MILLIS_5_MIN;
                mProgressBar.setVisibility(View.VISIBLE);
                mChartKline.setVisibility(View.GONE);
                mChartVolume.setVisibility(View.GONE);
            }
        });

        mDuration1hView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDuration5mView.setSelected(false);
                mDuration1hView.setSelected(true);
                mDuration1dView.setSelected(false);
                mDuration5mView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mDuration1dView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mDuration1hView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(R.drawable.market_page_highlight_line));
                mMarketStat.subscribe(mWatchListData.getBase(), mWatchListData.getQuote(), MarketStat.STAT_MARKET_HISTORY, (long) 300, MARKET_STAT_INTERVAL_MILLIS_1_HOUR, mListener);
                mDuration = MARKET_STAT_INTERVAL_MILLIS_1_HOUR;
                mProgressBar.setVisibility(View.VISIBLE);
                mChartKline.setVisibility(View.GONE);
                mChartVolume.setVisibility(View.GONE);
            }
        });

        mDuration1dView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDuration5mView.setSelected(false);
                mDuration1hView.setSelected(false);
                mDuration1dView.setSelected(true);
                mDuration5mView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mDuration1dView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getDrawable(R.drawable.market_page_highlight_line));
                mDuration1hView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                mMarketStat.subscribe(mWatchListData.getBase(), mWatchListData.getQuote(), MarketStat.STAT_MARKET_HISTORY, (long) 300, MARKET_STAT_INTERVAL_MILLIS_1_DAY, mListener);
                mDuration = MARKET_STAT_INTERVAL_MILLIS_1_DAY;
                mProgressBar.setVisibility(View.VISIBLE);
                mChartKline.setVisibility(View.GONE);
                mChartVolume.setVisibility(View.GONE);

            }
        });
    }

    @Override
    public void onListFragmentInteraction(MarketTrade item) {

    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }
}
