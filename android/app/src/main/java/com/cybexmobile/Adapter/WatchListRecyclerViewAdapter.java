package com.cybexmobile.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cybexmobile.Fragments.Data.WatchListData;
import com.cybexmobile.Fragments.WatchLIstFragment.OnListFragmentInteractionListener;
import com.cybexmobile.R;
import com.cybexmobile.Utils.MyUtils;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class WatchListRecyclerViewAdapter extends RecyclerView.Adapter<WatchListRecyclerViewAdapter.ViewHolder> {

    private final List<WatchListData> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;

    public WatchListRecyclerViewAdapter(List<WatchListData> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                 .inflate(R.layout.watch_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        NumberFormat formatter = new DecimalFormat("##,##0.00000");
        NumberFormat formatter2 = new DecimalFormat("0.00");
        holder.mItem = mValues.get(position);
        holder.mBaseCurrency.setText(mValues.get(position).getBase());
        holder.mQuoteCurrency.setText(String.format("/%s", mValues.get(position).getQuote()));
        holder.mVolume.setText(holder.mItem.getVol() == 0.f ? "-" : String.format("V:%s", MyUtils.format(mValues.get(position).getVol())));
        holder.mCurrentPrice.setText(holder.mItem.getCurrentPrice() == 0.f ? "-" : String.valueOf(formatter.format(mValues.get(position).getCurrentPrice())));
        holder.mHighPrice.setText(holder.mItem.getHigh() == 0.f ? "-" : String.format("H:%s", String.valueOf(formatter.format(mValues.get(position).getHigh()))));
        holder.mLowPrice.setText(holder.mItem.getLow() == 0.f ? "-" : String.format("/L:%s", String.valueOf(formatter.format(mValues.get(position).getLow()))));
//        holder.mCoinSymbol.setImageDrawable(getCoinIcon(holder.mItem.getQuote()));
        loadImage(holder.mItem.getQuoteId(), holder.mCoinSymbol);

        double change = 0.f;
        if(mValues.get(position).getChange()!= null) {
            try {
                change = Double.parseDouble(mValues.get(position).getChange());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if( change > 0.f) {
            holder.mChangeRate.setText(String.format("+%s%%",String.valueOf(formatter2.format(change * 100))));
            holder.mChangeRate.setTextColor(mContext.getResources().getColor(R.color.increasing_color));
            holder.mChangeRateSymbol.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_green));
        } else if (change < 0.f) {
            holder.mChangeRate.setText(String.format("%s%%",String.valueOf(formatter2.format(change * 100))));
            holder.mChangeRate.setTextColor(mContext.getResources().getColor(R.color.decreasing_color));
            holder.mChangeRateSymbol.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_red));
        } else {
            holder.mChangeRate.setText("-");
            holder.mChangeRate.setTextColor(mContext.getResources().getColor(R.color.no_change_color));
            holder.mChangeRateSymbol.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_arrow_right_grey));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem, mValues, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public WatchListData mItem;
        TextView mBaseCurrency;
        TextView mQuoteCurrency;
        TextView mCurrentPrice;
        TextView mHighPrice;
        TextView mLowPrice;
        TextView mVolume;
        TextView mChangeRate;
        ImageView mCoinSymbol;
        ImageView mChangeRateSymbol;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBaseCurrency = (TextView) view.findViewById(R.id.base_currency_watchlist);
            mQuoteCurrency = (TextView) view.findViewById(R.id.quote_currency_watchlist);
            mCurrentPrice = (TextView) view.findViewById(R.id.current_price_watchlist);
            mHighPrice = (TextView) view.findViewById(R.id.highValue);
            mLowPrice = (TextView) view.findViewById(R.id.lowValue);
            mVolume = (TextView) view.findViewById(R.id.volume);
            mChangeRate = (TextView) view.findViewById(R.id.change_rate_watchlist);
            mCoinSymbol = (ImageView) view.findViewById(R.id.base_currency_icon);
            mChangeRateSymbol = (ImageView) view.findViewById(R.id.change_rate_symbol);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + "'";
        }
    }

    private Drawable getCoinIcon(String coinName) {
        if(coinName.equals("CYB")) {
            return mContext.getResources().getDrawable(R.drawable.ic_cyb_grey);
        } else if (coinName.equals("JADE.BAT")) {
            return mContext.getResources().getDrawable(R.drawable.ic_bat_grey);
        } else if (coinName.equals("JADE.ENG")) {
            return mContext.getResources().getDrawable(R.drawable.ic_eng_grey);
        } else if (coinName.equals("JADE.OMG")) {
            return mContext.getResources().getDrawable(R.drawable.ic_omg_grey);
        } else if (coinName.equals("JADE.VEN")) {
            return mContext.getResources().getDrawable(R.drawable.ic_ven_grey);
        } else if (coinName.equals("JADE.EOS")) {
            return mContext.getResources().getDrawable(R.drawable.ic_eos_grey);
        } else if (coinName.equals("JADE.ETH")) {
            return mContext.getResources().getDrawable(R.drawable.ic_eth_grey);
        } else if (coinName.equals("JADE.BTC")) {
            return mContext.getResources().getDrawable(R.drawable.ic_btc_grey);
        } else if (coinName.equals("JADE.KNC")) {
            return mContext.getResources().getDrawable(R.drawable.ic_knc_grey);
        } else if (coinName.equals("JADE.NAS")) {
            return mContext.getResources().getDrawable(R.drawable.ic_nas_grey);
        } else if (coinName.equals("JADE.PAY")) {
            return mContext.getResources().getDrawable(R.drawable.ic_pay_grey);
        } else if (coinName.equals("JADE.SNT")) {
            return mContext.getResources().getDrawable(R.drawable.ic_snt_grey);
        } else if (coinName.equals("JADE.GET")) {
            return mContext.getResources().getDrawable(R.drawable.ic_get_grey);
        }
        return null;
    }

    private void loadImage(String quoteId, ImageView mCoinSymbol) {
        String quoteIdWithUnderLine = quoteId.replaceAll("\\.", "_");
        Picasso.get().load("https://cybex.io/icons/" + quoteIdWithUnderLine +"_grey.png").into(mCoinSymbol);
    }
}
