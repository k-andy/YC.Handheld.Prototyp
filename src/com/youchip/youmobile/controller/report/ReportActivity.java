package com.youchip.youmobile.controller.report;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.youchip.youmobile.R;
import com.youchip.youmobile.utils.ReportLogUtils;

import java.io.IOException;
import java.text.DecimalFormat;


public class ReportActivity extends Activity {

    private static final String REPORT_TOTAL_SALES_VALUE = "reportTotalSalesValue";
    private static final String REPORT_TOTAL_SALES_WTAX_VALUE = "reportTotalSalesWTaxValue";
    private static final String REPORT_TOTAL_SALES_NET_VALUE = "reportTotalSalesNetValue";
    private static final String REPORT_GROSS_VALUE = "reportGrossValue";
    private static final String REPORT_TAX_TOTAL_EXCL_PERCENT = "reportTaxTotalExclPercent";
    private static final String REPORT_TAX_TOTAL_EXCL_VALUE = "reportTaxTotalExclValue";
    private static final String REPORT_TAX_VAT_PERCENT = "reportTaxVatPercent";
    private static final String REPORT_TAX_VAT_VALUE = "reportTaxVatValue";
    private static final String REPORT_TAX_TOTAL_INCL_PERCENT = "reportTaxTotalInclPercent";
    private static final String REPORT_TAX_TOTAL_INCL_VALUE = "reportTaxTotalInclValue";
    private static final String REPORT_TAXES_WTTAX = "reportTaxesWtTax";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        updateReportView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void clearFile(View view) {
        ReportLogUtils.clearFile(this);

        updateReportView();
    }

    private void updateReportView() {
        TextView reportTotalSalesValue = ((TextView) findViewById(R.id.reportTotalSalesValue));
        TextView reportTotalSalesWTaxValue = ((TextView) findViewById(R.id.reportTotalSalesWTaxValue));
        TextView reportTotalSalesNetValue = ((TextView) findViewById(R.id.reportTotalSalesNetValue));
        TextView reportGrossValue = ((TextView) findViewById(R.id.reportGrossValue));
        TextView reportTaxTotalExclPercent = ((TextView) findViewById(R.id.reportTaxTotalExclPercent));
        TextView reportTaxTotalExclValue = ((TextView) findViewById(R.id.reportTaxTotalExclValue));
        TextView reportTaxVatPercent = ((TextView) findViewById(R.id.reportTaxVatPercent));
        TextView reportTaxVatValue = ((TextView) findViewById(R.id.reportTaxVatValue));
        TextView reportTaxTotalInclPercent = ((TextView) findViewById(R.id.reportTaxTotalInclPercent));
        TextView reportTaxTotalInclValue = ((TextView) findViewById(R.id.reportTaxTotalInclValue));
        TextView reportTaxesWtTax = ((TextView) findViewById(R.id.reportTaxesWtTax));

        RelativeLayout relativeLayout = ((RelativeLayout) findViewById(R.id.taxReportWrapper));
        relativeLayout.setVisibility(View.INVISIBLE);

        String[] strings = null;
        try {
            strings = ReportLogUtils.readFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DecimalFormat myFormatter = new DecimalFormat("0.00");

        double totalSales, totalSalesWTax, totalSalesNet, totalSalesGross, taxExcl, vat, taxIncl, wtTax;

        if (strings == null) {
            totalSales = 0;
            totalSalesWTax = 0;
            totalSalesNet = 0;
            totalSalesGross = 0;
            taxExcl = 0;
            vat = 0;
            taxIncl = 0;
            wtTax = 0;
        } else {
            totalSales = Long.parseLong(strings[0].replaceAll("[^0-9]", "")) / 100.0;
            totalSalesWTax = Long.parseLong(strings[1].replaceAll("[^0-9]", "")) / 100.0;
            totalSalesNet = Long.parseLong(strings[2].replaceAll("[^0-9]", "")) / 100.0;
            totalSalesGross = Long.parseLong(strings[3].replaceAll("[^0-9]", "")) / 100.0;
            taxExcl = Long.parseLong(strings[4].replaceAll("[^0-9]", "")) / 100.0;
            vat = Long.parseLong(strings[5].replaceAll("[^0-9]", "")) / 100.0;
            taxIncl = Long.parseLong(strings[6].replaceAll("[^0-9]", "")) / 100.0;
            wtTax = Long.parseLong(strings[7].replaceAll("[^0-9]", "")) / 100.0;
        }


        reportTotalSalesValue.setText(myFormatter.format(totalSales) + "");
        reportTotalSalesWTaxValue.setText(myFormatter.format(totalSalesWTax) + "");
        reportTotalSalesNetValue.setText(myFormatter.format(totalSalesNet) + "");
        reportGrossValue.setText(myFormatter.format(totalSalesGross) + "");
        reportTaxTotalExclPercent.setText("00");
        reportTaxTotalExclValue.setText(myFormatter.format(taxExcl) + "");
        reportTaxVatPercent.setText("00");
        reportTaxVatValue.setText(myFormatter.format(vat) + "");
        reportTaxTotalInclPercent.setText("00");
        reportTaxTotalInclValue.setText(myFormatter.format(taxIncl) + "");
        reportTaxesWtTax.setText(myFormatter.format(wtTax) + "");
    }
}
