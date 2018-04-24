package com.example.maitr.pitchtracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by maitr on 24-Mar-18.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
private int n;
private FileList fileList;
private Tuner tuner;
public String tuning;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        n = NumOfTabs;
        fileList = new FileList();
        tuner = new Tuner();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0 : return fileList;
            case 1 : return tuner;
        }
        return null;
    }

    @Override
    public int getCount() {
        return n;
    }
}
