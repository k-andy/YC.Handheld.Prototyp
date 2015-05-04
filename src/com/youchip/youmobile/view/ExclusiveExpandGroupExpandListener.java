package com.youchip.youmobile.view;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class ExclusiveExpandGroupExpandListener implements OnGroupExpandListener{
    
    private int previousItem = -1;
    private final ExpandableListView expandableListView;

    public ExclusiveExpandGroupExpandListener(ExpandableListView expandableListView){
        this.expandableListView = expandableListView;
    }
    
    @Override
    public void onGroupExpand(int groupPosition) {
        if(groupPosition != previousItem )
            expandableListView.collapseGroup(previousItem );
        previousItem = groupPosition;
    }
}
