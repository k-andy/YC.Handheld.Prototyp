package com.youchip.youmobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.youchip.youmobile.controller.report.ReportDetailsAdapter;
import com.youchip.youmobile.utils.ReportLogUtils;

import java.util.List;


public class ReportDetails extends Activity {
    private LinearLayout soldItemsView;
    private LinearLayout canceledItemsView;
    private View detailsSeparator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        soldItemsView = (LinearLayout) findViewById(R.id.soldItemsView);
        soldItemsView.setVisibility(View.GONE);
        canceledItemsView = (LinearLayout) findViewById(R.id.canceledItemsView);
        canceledItemsView.setVisibility(View.GONE);

        detailsSeparator = findViewById(R.id.detailsSeparator);
        detailsSeparator.setVisibility(View.GONE);

        List<List<String>> soldItems = ReportLogUtils.getSoldItems();
        if (soldItems != null) {
            ReportDetailsAdapter soldItemsAdapter = new ReportDetailsAdapter(this, soldItems);
            ListView soldItemsList = (ListView) findViewById(R.id.soldItemsList);
            soldItemsList.setAdapter(soldItemsAdapter);
            soldItemsView.setVisibility(View.VISIBLE);
            detailsSeparator.setVisibility(View.VISIBLE);
        }

        List<List<String>> canceledItems = ReportLogUtils.getCanceledItems();
        if (canceledItems != null) {
            ReportDetailsAdapter canceledItemsAdapter = new ReportDetailsAdapter(this, canceledItems);
            ListView canceledItemsList = (ListView) findViewById(R.id.canceledItemsList);
            canceledItemsList.setAdapter(canceledItemsAdapter);
            canceledItemsView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report_details, menu);
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
}
