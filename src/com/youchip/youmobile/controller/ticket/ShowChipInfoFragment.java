package com.youchip.youmobile.controller.ticket;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.youchip.youmobile.R;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.view.ChipDataPreparer;
import com.youchip.youmobile.view.ChipFieldExpandableListAdapter;
import com.youchip.youmobile.view.ExclusiveExpandGroupExpandListener;
import com.youchip.youmobile.view.GroupedKeyValueList;
import com.youchip.youmobile.view.LabeledFragment;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ShowChipInfoFragment extends LabeledFragment {

    private static final String ARG_CHIP = "mChip";

    private VisitorChip mChip;

    private TextView uidText;

    private ChipFieldExpandableListAdapter adapter;
    private ExpandableListView groupView;
    private ChipDataPreparer chipDataViewPreparer;

    public static ShowVisitorInfoFragment newInstance(VisitorChip chip) {
        ShowVisitorInfoFragment fragment = new ShowVisitorInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CHIP, chip);
        fragment.setArguments(args);
        return fragment;
    }

    private ShowChipInfoFragment() {
        setLabel(R.string.title_activity_ticketcx_check_chipdata);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mChip = (VisitorChip) getArguments().getSerializable(ARG_CHIP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_show_chip_info, container, false);


        this.groupView = (ExpandableListView) rootView.findViewById(R.id.list_view_expandable_expanded);
        this.groupView.setOnGroupExpandListener(new ExclusiveExpandGroupExpandListener(groupView));
        //this.groupView.setOnChildClickListener(onChipFieldClick);

        this.adapter = new ChipFieldExpandableListAdapter(rootView.getContext(), R.layout.expandable_list_title_expanded,
                R.layout.expandable_list_title_collapsed, R.layout.row_title_content,
                R.id.list_expandable_title, R.id.list_item_data_title, R.id.list_item_data_content,
                new GroupedKeyValueList());

        this.groupView.setAdapter(adapter);

        this.chipDataViewPreparer = new ChipDataPreparer(rootView.getContext(), adapter);

        this.chipDataViewPreparer.prepareChipDataViewForCashDesk(mChip);
        groupView.expandGroup(0);

        return rootView;
    }


}
