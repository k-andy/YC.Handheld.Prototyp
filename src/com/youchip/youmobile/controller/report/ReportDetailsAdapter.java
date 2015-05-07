package com.youchip.youmobile.controller.report;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.youchip.youmobile.R;

import java.util.List;

/**
 * Created by andy on 5/6/15.
 */
public class ReportDetailsAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<List<String>> items;

    public ReportDetailsAdapter(Context context, List<List<String>> items) {
        super(context, R.layout.report_details_row);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View single_row = inflater.inflate(R.layout.report_details_row, null, true);

        String name = items.get(0).get(position);
        String value = items.get(1).get(position);

        TextView reportDetailsItemName = (TextView) single_row.findViewById(R.id.reportDetailsItemName);
        TextView reportDetailsItemValue = (TextView) single_row.findViewById(R.id.reportDetailsItemValue);

        reportDetailsItemName.setText(name);
        reportDetailsItemValue.setText("x" + value);

        return single_row;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return items.get(0).size();
    }

}
