package com.youchip.youmobile.view;

/**
 * Created by muelleco on 04.07.2014.
 */

import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.youchip.youmobile.controller.ticket.ShowVisitorInfoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link android.support.v13.app.FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    List<LabeledFragment> fragmentList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm, LabeledFragment fragment, LabeledFragment... fragments) {
        super(fm);

        fragmentList.add(fragment);

        for(LabeledFragment f: fragments){
            fragmentList.add(f);
        }
    }



    public List<LabeledFragment>  getFragmentList(){
        return this.fragmentList;
    }

    @Override
    public LabeledFragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return getItem(position).getLabel();
    }

    public int getPageTitleResourceID(int position) {
        return getItem(position).getLabelResourceID();
    }
}
