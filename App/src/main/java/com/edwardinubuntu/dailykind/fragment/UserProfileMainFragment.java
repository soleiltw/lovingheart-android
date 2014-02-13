package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.edwardinubuntu.dailykind.R;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by edward_chiang on 2014/2/12.
 */
public class UserProfileMainFragment extends PlaceholderFragment {

    private FragmentStatePagerAdapter fragmentStatePagerAdapter;

    private ViewPager viewPager;

    private TabPageIndicator tabPageIndicator;


    public static final int VIEW_PAGER_BASIC = 0;
    public static final int VIEW_PAGER_STORIES = 1;
    public static final int VIEW_PAGER_COLLECTIONS = 2;

    public static final int VIEW_PAGER_COUNT = 3;

    public static UserProfileMainFragment newInstance(int sectionNumber) {
        UserProfileMainFragment fragment = new UserProfileMainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileMainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentStatePagerAdapter = new UserProfilePagerAdapter(getFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_profile_tab, container, false);

        viewPager = (ViewPager)rootView.findViewById(R.id.pager);
        viewPager.setAdapter(fragmentStatePagerAdapter);

        tabPageIndicator = (TabPageIndicator)rootView.findViewById(R.id.user_content_indicator);
        tabPageIndicator.setViewPager(viewPager);

        return rootView;
    }

    public class UserProfilePagerAdapter extends FragmentStatePagerAdapter {

        public UserProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new PlaceholderFragment();

            switch (position) {
                case VIEW_PAGER_BASIC:
                    fragment = new MeBasicFragment();
                    break;
                case VIEW_PAGER_STORIES:
                    fragment = new UserProfileStoriesFragment();
                    break;
                case VIEW_PAGER_COLLECTIONS:
                    break;
            }

            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return VIEW_PAGER_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String pageTitle = new String();
            switch (position) {
                case VIEW_PAGER_BASIC:
                    pageTitle = "Profile";
                    break;
                case VIEW_PAGER_STORIES:
                    pageTitle = "Stories";
                    break;
                case VIEW_PAGER_COLLECTIONS:
                    pageTitle = "Collections";
                    break;
            }
            return pageTitle;
        }
    }


}
