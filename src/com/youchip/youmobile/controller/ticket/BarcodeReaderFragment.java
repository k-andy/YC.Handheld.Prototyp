package com.youchip.youmobile.controller.ticket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.barcodeIO.BarcodeScanner;
import com.youchip.youmobile.controller.barcodeIO.NXPBarcodeScanner;
import com.youchip.youmobile.view.LabeledFragment;


/**
 * This Activity is mainly for reading the barcode of a real world
 * paper ticket. and check online if the ticket is valid and yet not
 * exchanged for a rfid chip.
 * For fall back it may provide the option to search tickets by
 * last name and first name of the visitor.
 */
public class BarcodeReaderFragment extends LabeledFragment {

    private BarcodeScanner barcodeScanner;
    private Button readBarcodeButton;
    private EditText barcodeText;
    private TextView ticketStatusView;
    private static final int timeout = 0;


    private View.OnClickListener onClickBarcodeEdit = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if (!barcodeText.isInEditMode()){
//                barcodeText.edit
            }
        }
    };


    public BarcodeReaderFragment(){
        setLabel(R.string.title_activity_ticketcx_read_barcode);
    }

    private BarcodeScanner.ResultListener barcodeScannResult =
            new BarcodeScanner.ResultListener() {
        @Override
        public void onReadSuccessful(byte[] data) {
            barcodeText.setText(new String(data));
            //ticketStatusView.setText(R.string.error_ticketxc_ticket_status_checkfail);

            Activity parent = BarcodeReaderFragment.this.getActivity();
            Intent intent = new Intent(parent, CheckTicketActivity.class);
            parent.startActivity(intent);
        }

        @Override
        public void onReadFailed() {
            barcodeText.setText(R.string.failed_title);
        }
    };

    private View.OnClickListener onClickReadBarcode = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!barcodeScanner.isScanning()) {
                try {
                    barcodeScanner.scan(BarcodeReaderFragment.this.getActivity(), timeout);
                } catch (NXPBarcodeScanner.IllegalScannerStateException isse){
                    Log.w(BarcodeReaderFragment.class.getName(), "Ignoring command");
                }
            } else {
                Log.w(BarcodeReaderFragment.class.getName(), "Barcode Reader is still running! Ignoring command");
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ticket_request, container, false);

        this.barcodeScanner = NXPBarcodeScanner.getInstance();
        this.barcodeScanner.setResultListener(barcodeScannResult);

        this.readBarcodeButton  = ((Button) rootView.findViewById(R.id.action_search_ticket));
        this.barcodeText    = ((EditText) rootView.findViewById(R.id.ticket_barcode_value));
        this.ticketStatusView    = ((TextView) rootView.findViewById(R.id.ticket_barcode_status));

        this.readBarcodeButton.setOnClickListener(onClickReadBarcode);

        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();
        this.barcodeScanner.enableDevice();
        this.ticketStatusView.setText("");
        this.barcodeText.setText("");
    }

    @Override
    public void onPause() {
        super.onPause();
        this.barcodeScanner.disableDevice();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
