package com.youchip.youmobile.view;

import android.app.Fragment;

/**
 * Created by muelleco on 04.07.2014.
 */
public class LabeledFragment extends Fragment {

    protected int labelResourceID = 0;

    public String getLabel(){
        if (labelResourceID > 0) {
            return this.getActivity().getResources().getString(labelResourceID);
        } else {
            return null;
        }
    }
    public int getLabelResourceID(){
        return labelResourceID;
    }


    public void setLabel(int labelResourceID){
        this.labelResourceID = labelResourceID;
    }
}
