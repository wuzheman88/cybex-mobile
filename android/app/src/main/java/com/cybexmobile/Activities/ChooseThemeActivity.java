package com.cybexmobile.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cybexmobile.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChooseThemeActivity extends AppCompatActivity {

    private ListView mListView;
    private List<String> mThemeList;
    private int selectedPosition = 0;
    private String[] theme = new String[]{"Dark", "Light"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_theme);
        initView();
        initData();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.select_theme_list);
    }

    private void initData() {
        mThemeList = new ArrayList<String>();
        mThemeList.addAll(Arrays.asList(theme));
        final ThemeAdapter themeAdapter = new ThemeAdapter(this, mThemeList, selectedPosition);
        mListView.setAdapter(themeAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                themeAdapter.notifyDataSetChanged();
                if(mThemeList.get(position).equals("Light")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                recreate();
            }
        });
    }

    public class ThemeAdapter extends BaseAdapter {
        Context mContext;
        List<String> mList;
        int mPosiion;
        LayoutInflater inflater;

        public ThemeAdapter(Context context, List<String> list, int position) {
            mContext = context;
            mList = list;
            mPosiion = position;
            inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }



        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.theme_item_adapter, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) convertView.findViewById(R.id.theme_name);
                viewHolder.select = (RadioButton) convertView.findViewById(R.id.theme_radio_button);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            viewHolder.name.setText(mList.get(position));
            if(selectedPosition == position) {
                viewHolder.select.setBackgroundResource(R.drawable.ic_check);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_orange));
            } else {
                viewHolder.select.setBackgroundResource(0);
                viewHolder.name.setTextColor(getResources().getColor(R.color.primary_color_white));
            }

            return convertView;
        }

        public class ViewHolder {
            TextView name;
            RadioButton select;
        }
    }

}
