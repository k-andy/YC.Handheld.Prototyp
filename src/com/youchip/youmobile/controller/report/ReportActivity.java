package com.youchip.youmobile.controller.report;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.youchip.youmobile.R;
import com.youchip.youmobile.ReportDetails;
import com.youchip.youmobile.model.shop.ShopItemForReport;
import com.youchip.youmobile.utils.ReportLogUtils;

import java.text.DecimalFormat;


public class ReportActivity extends Activity {

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

    public void resetReport(View view) {
        ReportLogUtils.resetReport(this);
        updateReportView();
    }

    private void updateReportView() {
        TextView reportTotalSalesValue = ((TextView) findViewById(R.id.reportTotalSalesValue));
        TextView reportTotalSalesWTaxValue = ((TextView) findViewById(R.id.reportTotalSalesWTaxValue));
        TextView reportTotalSalesNetValue = ((TextView) findViewById(R.id.reportTotalSalesNetValue));
        TextView reportGrossValue = ((TextView) findViewById(R.id.reportGrossValue));
        TextView reportReturnsValue = ((TextView) findViewById(R.id.reportReturnsValue));

        LinearLayout taxesList = (LinearLayout) findViewById(R.id.taxesList);
        taxesList.setVisibility(View.GONE);
        Button reportDetailsBtn = (Button) findViewById(R.id.reportDetailsBtn);
        reportDetailsBtn.setVisibility(View.GONE);
        Button reportClearBtn = (Button) findViewById(R.id.reportClearBtn);
        reportClearBtn.setVisibility(View.GONE);

        if (ShopItemForReport.getPluAmount().isEmpty() && ShopItemForReport.getPluAmountCanceled().isEmpty()) {
            ReportLogUtils.loadReport(this);
        }

        TaxListAdapter adapter = new TaxListAdapter(this, ReportLogUtils.calculateValuesForVats());
        ListView lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);

        double totalSales, totalSalesWTax, totalSalesNet, totalSalesGross, totalReturns;
        long total = ReportLogUtils.calculateTotal();

        totalSales = total / 100d;
        totalSalesWTax = total / 100d;
        totalSalesNet = ReportLogUtils.calculateNet() / 100d;
        totalSalesGross = ReportLogUtils.calculateGross() / 100d;
        totalReturns = ReportLogUtils.calculateReturns() / 100d;

        if (!ShopItemForReport.getPluAmount().isEmpty()) {
            taxesList.setVisibility(View.VISIBLE);
            reportDetailsBtn.setVisibility(View.VISIBLE);
            reportClearBtn.setVisibility(View.VISIBLE);
        }
        if (!ShopItemForReport.getPluAmountCanceled().isEmpty()) {
            reportDetailsBtn.setVisibility(View.VISIBLE);
            reportClearBtn.setVisibility(View.VISIBLE);
        }

        DecimalFormat myFormatter = new DecimalFormat("0.00");

        reportTotalSalesValue.setText(myFormatter.format(totalSales) + "");
        reportTotalSalesWTaxValue.setText(myFormatter.format(totalSalesWTax) + "");
        reportTotalSalesNetValue.setText(myFormatter.format(totalSalesNet) + "");
        reportGrossValue.setText(myFormatter.format(totalSalesGross) + "");
        reportReturnsValue.setText(myFormatter.format(totalReturns) + "");
    }

    public void showReportDetails(View view) {
        Intent intent = new Intent(this, ReportDetails.class);
        startActivity(intent);
    }

}
