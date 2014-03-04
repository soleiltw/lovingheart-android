package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.LoginActivity;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.parse.ParseUser;
import com.viewpagerindicator.TabPageIndicator;

/**
 * Created by edward_chiang on 2014/2/12.
 */
public class UserProfileMainFragment extends UserProfileFragment {

    private FragmentStatePagerAdapter fragmentStatePagerAdapter;

    private ViewPager viewPager;

    private TabPageIndicator tabPageIndicator;

    public static final int VIEW_PAGER_BASIC = 0;
    public static final int VIEW_PAGER_STORIES = 1;
    public static final int VIEW_PAGER_COLLECTIONS = 2;
    public static final int VIEW_PAGER_REPORT = 3;

    public static final int VIEW_PAGER_COUNT = 4;

    private View userProfileLayout;
    private View askLoginLayout;

    private BootstrapButton askLoginButton;


    public static UserProfileMainFragment newInstance(int sectionNumber) {
        UserProfileMainFragment fragment = new UserProfileMainFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public UserProfileMainFragment() {
        setupUserId();
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

        userProfileLayout = rootView.findViewById(R.id.user_profile_info_layout);
        askLoginLayout = rootView.findViewById(R.id.user_ask_login_layout);

        askLoginButton = (BootstrapButton)rootView.findViewById(R.id.me_ask_login_button);
        askLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(loginIntent, CheckUserLoginUtil.ASK_USER_LOGIN);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CheckUserLoginUtil.ASK_USER_LOGIN) {
            checkLoginForLayout();
        }
        Fragment profileFragment = (Fragment)fragmentStatePagerAdapter.instantiateItem(viewPager, 0);
        if (profileFragment != null) {
            profileFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkLoginForLayout();
    }

    protected void checkLoginForLayout() {
        if (!CheckUserLoginUtil.hasLogin()) {
            userProfileLayout.setVisibility(View.GONE);
            askLoginLayout.setVisibility(View.VISIBLE);
        } else {
            userProfileLayout.setVisibility(View.VISIBLE);
            askLoginLayout.setVisibility(View.GONE);
        }
    }

    public class UserProfilePagerAdapter extends FragmentStatePagerAdapter {

        public UserProfilePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {


            UserProfileFragment fragment = null;

            switch (position) {
                case VIEW_PAGER_BASIC:
                    if (getUserId() == null) {
                        fragment = UserProfileMeFragment.newInstance(position + 1);
                    } else {
                        fragment = new UserProfileBasicFragment();
                    }
                    break;
                case VIEW_PAGER_STORIES:
                    fragment = new UserProfileStoriesFragment();
                    break;
                case VIEW_PAGER_COLLECTIONS:
                    fragment = new UserProfileGraphicsFragment();
                    break;
                case VIEW_PAGER_REPORT:
                    if (ParseUser.getCurrentUser().getObjectId().equals(getUserId())) {
                        fragment = UserProfileReportsMeFragment.newInstance(position + 1);
                    } else {
                        fragment = new UserProfileReportsOtherFragment();
                    }
                    break;
            }
            if (getUserId()!= null) {
                fragment.setUserId(getUserId());
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
                    pageTitle = getResources().getString(R.string.profile_tab_home);
                    break;
                case VIEW_PAGER_STORIES:
                    pageTitle = getResources().getString(R.string.profile_tab_stories);
                    break;
                case VIEW_PAGER_COLLECTIONS:
                    pageTitle = getResources().getString(R.string.profile_tab_collections);
                    break;
                case VIEW_PAGER_REPORT:
                    pageTitle = getResources().getString(R.string.profile_tab_report);
                    break;
            }
            return pageTitle;
        }
    }


}
