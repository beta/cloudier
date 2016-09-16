package net.kyouko.cloudier.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link FragmentPagerAdapter} that displays each page as a tab.
 *
 * @author beta
 */
public class TabsFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<String> titles;
    private List<Fragment> fragments;


    public TabsFragmentPagerAdapter(FragmentManager fragmentManager) {
        this(fragmentManager, null, null);
    }


    public TabsFragmentPagerAdapter(FragmentManager fragmentManager,
                                    List<String> titles, List<Fragment> fragments) {
        super(fragmentManager);

        this.titles = (titles == null) ? new ArrayList<String>() : titles;
        this.fragments = (fragments == null) ? new ArrayList<Fragment>() : fragments;
    }


    public void add(String title, Fragment fragment) {
        titles.add(title);
        fragments.add(fragment);
    }


    public void add(Tab tab) {
        add(tab.title, tab.fragment);
    }


    public void add(Tab... tabs) {
        for (Tab tab : tabs) {
            add(tab);
        }
    }


    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }


    @Override
    public int getCount() {
        return fragments.size();
    }


    public static class Tab {

        public String title;
        public Fragment fragment;


        public Tab(String title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }

    }

}
