package com.youchip.youmobile.controller.helpdesk;

import static com.youchip.youmobile.controller.IntentExtrasKeys.*;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxHelpDeskLogger;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.gate.AreaConfig;
import com.youchip.youmobile.model.gate.VisitorRole;
import com.youchip.youmobile.model.shop.VoucherInfo;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;
import com.youchip.youmobile.view.ChipDataPreparer;
import com.youchip.youmobile.view.ChipFieldExpandableListAdapter;
import com.youchip.youmobile.view.ExclusiveExpandGroupExpandListener;
import com.youchip.youmobile.view.GroupedKeyValueList;

public class HelpDeskDetailsExtendedActivity extends Activity {

    private static final int INTENT_FOR_EDIT_CHIP = 17;
    private static final int INTENT_FOR_UPDATE_CHIP = 23;
    @SuppressLint("SimpleDateFormat")
    private static final DateFormat formatter = new SimpleDateFormat("HH:mm");
    private TxHelpDeskLogger logger;

    private VisitorChip rfidChip;
    private long oldAreaID;
    private String keyA;

    private boolean isBoSupervisor = false;

    private MenuItem saveButton = null;


    private ChipFieldExpandableListAdapter adapter;
    private ExpandableListView groupView;
    private ChipDataPreparer chipDataViewPreparer;

    private boolean showBalanceOnly = false; // if true, shows only credits and voucher infos

    private ExpandableListView.OnChildClickListener onChipFieldClick = new ExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                int childPosition, long id) {

            if (!showBalanceOnly && groupPosition == 0 && childPosition == 2) {
                if(!isBoSupervisor){
                    AlertBox.allertOnWarning(HelpDeskDetailsExtendedActivity.this, R.string.failed_title, R.string.hint_helpdesk_checkout_correction_denied);
                } else {
                    createCheckOutAreaDialog();
                }
            }
            
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expandable_list_view);


        Intent intent = getIntent();
        this.rfidChip = (VisitorChip) intent.getSerializableExtra(INTENT_EXTRA_CHIP_OBJECT);
        this.oldAreaID = rfidChip.getInAreaID();
        this.keyA           = intent.getStringExtra(INTENT_EXTRA_CHIP_KEY_A);
        String userID         = intent.getStringExtra(INTENT_EXTRA_USER_ID);

        if(intent.hasExtra(INTENT_EXTRA_MODE_NAME)){
            String name = intent.getStringExtra(INTENT_EXTRA_MODE_NAME);
            if (name != null)
                this.setTitle(name);
        }

        this.showBalanceOnly= intent.getBooleanExtra(INTENT_EXTRA_SHOW_BALANCE_ONLY, false);

        if( intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_ADMIN,false) ){
            this.isBoSupervisor = true;
        }else if (intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, false)) {
            this.isBoSupervisor = true;
        }

        this.logger = new TxHelpDeskLogger(this, userID);


        this.groupView = (ExpandableListView) findViewById(R.id.list_view_expandable_expanded);
        this.groupView.setOnGroupExpandListener(new ExclusiveExpandGroupExpandListener(groupView));
        this.groupView.setOnChildClickListener(onChipFieldClick);

        this.adapter = new ChipFieldExpandableListAdapter(this, R.layout.expandable_list_title_expanded,
                R.layout.expandable_list_title_collapsed, R.layout.row_title_content,
                R.id.list_expandable_title, R.id.list_item_data_title, R.id.list_item_data_content,
                new GroupedKeyValueList());
        
        this.groupView.setAdapter(adapter);

        this.chipDataViewPreparer = new ChipDataPreparer(this,adapter);
        
        refreshHelpDeskData(rfidChip);
    }
    


    protected void refreshHelpDeskData(VisitorChip chip) {
        if(!showBalanceOnly) {
            this.chipDataViewPreparer.prepareChipDataViewForHelpDesk(chip);
            groupView.expandGroup(1);
        } else {
            this.chipDataViewPreparer.prepareChipDataViewForCashDesk(chip);
            groupView.expandGroup(0);
        }

    }

    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help_desk_details, menu);
        this.saveButton = menu.findItem(R.id.action_helpdesk_write);
        this.saveButton.setEnabled(false);
        if (showBalanceOnly)
            this.saveButton.setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_helpdesk_write:
            if(!isBoSupervisor){
                AlertBox.allertOnWarning(HelpDeskDetailsExtendedActivity.this, R.string.failed_title, R.string.hint_helpdesk_checkout_correction_denied);
            } else {
                Intent intent = new Intent(this, HelpDeskModifyActivity.class);
                intent.putExtra(INTENT_EXTRA_CHIP_OBJECT, (Serializable) rfidChip);
                intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
                startActivityForResult(intent, INTENT_FOR_UPDATE_CHIP);
            }
        default:
            return false;// super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case INTENT_FOR_EDIT_CHIP:
            if (data != null && resultCode == RESULT_OK) {
                rfidChip = (VisitorChip) data.getSerializableExtra(INTENT_EXTRA_CHIP_OBJECT);
                refreshHelpDeskData(rfidChip);
            }
            break;
        case INTENT_FOR_UPDATE_CHIP:
            if (resultCode == RESULT_OK){
                logger.i(rfidChip.getUID(), oldAreaID, "Chip corrected: Checked out from Zone " + oldAreaID);
                Toast toast = Toast.makeText(this, R.string.success_helpdesk_update_chip_msg, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                AlertBox.allertOnWarning(this, R.string.failed_title, R.string.failed_helpdesk_update_chip_msg);
            }
            saveButton.setEnabled(false);
        }
    }

    private OnClickListener onSubmitCheckOut = new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d("HelpDeskDetailsExtendedActivity", "Resetting Area Check In State.");
            rfidChip.setInAreaID(0);
            refreshHelpDeskData(rfidChip);
            HelpDeskDetailsExtendedActivity.this.saveButton.setEnabled(true);
        }

    };

    private void createCheckOutAreaDialog() {
        Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.title_update_chip_state);
        alertDialog.setMessage(R.string.hint_helpdesk_update_chip_checkout);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setPositiveButton(android.R.string.yes, onSubmitCheckOut);
        alertDialog.setNegativeButton(android.R.string.cancel, null);
        alertDialog.show();
    }

}
