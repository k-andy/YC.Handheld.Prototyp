package com.youchip.youmobile.controller.report;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.youchip.youmobile.R;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by andy on 5/5/15.
 */
public class TaxListAdapter extends ArrayAdapter<String> {
    private List<List<Number>> valuesForVats;
    private Context context;

    public TaxListAdapter(Activity context, List<List<Number>> valuesForVats) {
        super(context, R.layout.tax_list_row);

        this.valuesForVats = valuesForVats;
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View single_row = inflater.inflate(R.layout.tax_list_row, null, true);

        List<Number> values = valuesForVats.get(position);

        float vat = values.get(0).floatValue();
        long salesInclTax = values.get(1).longValue();
        long salesNet = values.get(2).longValue();
        long taxes = values.get(3).longValue();

        TextView reportTaxVatPercent = (TextView) single_row.findViewById(R.id.reportTaxVatPercent);
        TextView taxesSalesInclTaxValue = (TextView) single_row.findViewById(R.id.taxesSalesInclTaxValue);
        TextView taxesSalesNetValue = (TextView) single_row.findViewById(R.id.taxesSalesNetValue);
        TextView taxesTaxesValue = (TextView) single_row.findViewById(R.id.taxesTaxesValue);

        DecimalFormat myFormatter = new DecimalFormat("0.00");

        reportTaxVatPercent.setText(vat + "");
        taxesSalesInclTaxValue.setText(myFormatter.format(salesInclTax / 100d) + "");
        taxesSalesNetValue.setText(myFormatter.format(salesNet / 100d) + "");
        taxesTaxesValue.setText(myFormatter.format(taxes / 100d) + "");

        return single_row;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return valuesForVats.size();
    }
}
