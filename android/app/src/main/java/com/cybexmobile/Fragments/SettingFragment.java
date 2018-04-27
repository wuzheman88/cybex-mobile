package com.cybexmobile.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cybexmobile.Activities.ChooseLanguageActivity;
import com.cybexmobile.HelperClass.StoreLanguageHelper;
import com.cybexmobile.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private CardView mLanguageSettingView, mThemeSettingView;

    private BottomNavigationView mBottomNavigationView;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  view = inflater.inflate(R.layout.fragment_setting, container, false);
        initView(view);
        onClickListener();
        displayLanguage();
        displayTheme();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction();
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Fragment fragment);
    }

    private void initView(View view) {
        mLanguageSettingView = (CardView) view.findViewById(R.id.setting_language);
        mThemeSettingView = (CardView) view.findViewById(R.id.setting_theme);
    }

    private void onClickListener() {
        mLanguageSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChooseLanguageActivity.class);
                startActivity(intent);
            }
        });

        mThemeSettingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment chooseThemeFragment = new ChooseThemeFragment();
                mListener.onFragmentInteraction(chooseThemeFragment);

            }
        });
    }

    private void displayLanguage() {
        String defaultLanguage = StoreLanguageHelper.getLanguageLocal(getContext());
        TextView textView = (TextView) mLanguageSettingView.findViewById(R.id.setting_language_type);
        if(defaultLanguage != null) {
            if(defaultLanguage.equals("en")) {
                textView.setText(getActivity().getResources().getString(R.string.setting_language_english));
            } else if (defaultLanguage.equals("zh")) {
                textView.setText(getActivity().getResources().getString(R.string.setting_language_chinese));
            }

        }
    }

    private void displayTheme() {
        boolean isNight = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("night_mode", false);
        TextView textView = (TextView) mThemeSettingView.findViewById(R.id.setting_theme_content);
        if (isNight) {
            textView.setText(getContext().getResources().getString(R.string.setting_theme_light));
        } else {
            textView.setText(getContext().getResources().getString(R.string.setting_theme_dark));
        }

    }
}
