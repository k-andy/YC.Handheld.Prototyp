package com.youchip.youmobile.view;

import android.content.Context;
import android.util.Log;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.shop.VoucherInfo;
import com.youchip.youmobile.utils.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

/**
 * Created by muelleco on 08.07.2014.
 */
public class ChipDataPreparer {

    private static final String LOG_TAG = ChipDataPreparer.class.getName();
    private static final DateFormat formatter = new SimpleDateFormat("HH:mm");

    private final Context context;
    private final ChipFieldExpandableListAdapter adapter;

    private Map<Long, String> visitorRolesText;
    private Map<Long, VoucherInfo> voucherInfo;

    public ChipDataPreparer(Context context, ChipFieldExpandableListAdapter adapter){

        this.context = context;
        this.adapter = adapter;

        this.visitorRolesText = ConfigAccess.getVisitorRoles(context);
        this.voucherInfo = ConfigAccess.getVoucherGroups(context);

    }

    public void prepareChipDataViewForHelpDesk(VisitorChip chip){
        adapter.clear();
        setBasicData(chip);
        setCredits(chip);
        setVisitorRoles(chip);
        setVoucherData(chip);

        if (chip.isEmployee() ||chip.isSupervisor() || chip.isAdmin() ){
            setBackOfficeRoles(chip);
        }
    }

    public void prepareChipDataViewForCashDesk(VisitorChip chip){
        adapter.clear();
        setCredits(chip);
    }


    public void prepareChipDataViewForTicketExchange(VisitorChip chip){
        adapter.clear();
        setCredits(chip);
        setVisitorRoles(chip);
        setVoucherData(chip);
        setBackOfficeRoles(chip);
    }

    private void setBasicData(VisitorChip chip){

        int blockedTextResource;
        if (chip.isBlocked()) {
            blockedTextResource = R.string.yes;
        } else {
            blockedTextResource = R.string.no;
        }


        adapter.addElement("Basic Data", context.getResources().getText(R.string.hint_rfid_uid)
                .toString(), chip.getUID());

        adapter.addElement("Basic Data", context.getResources().getText(R.string.hint_rfid_event_id)
                .toString(), Long.toString(chip.getEventID()));
        adapter.addElement("Basic Data", context.getResources().getText(R.string.hint_rfid_area_id)
                .toString(), Long.toString(chip.getInAreaID()));
        adapter.addElement("Basic Data", context.getResources().getText(R.string.hint_rfid_area_time)
                .toString(), formatter.format(chip.getInAreaTime()));

        adapter.addElement("Basic Data",
                context.getResources().getText(R.string.hint_rfid_status_blocked).toString(),
                context.getResources().getText(blockedTextResource).toString()
        );
    }


    private void setCredits(VisitorChip chip){
        adapter.addElement("Credits", context.getResources().getText(R.string.hint_rfid_credit_1)
                .toString(), DataConverter.longToCurrency(chip.getCredit1()).toString() + " "
                + ConfigAccess.get1stCurrencySymbol(context));
        adapter.addElement("Credits", context.getResources().getText(R.string.hint_rfid_credit_2)
                .toString(), DataConverter.longToCurrency(chip.getCredit2()).toString() + " "
                + ConfigAccess.get2ndCurrencySymbol(context));
    }


    private void setVisitorRoles(VisitorChip chip){
        Set<Long> visitorRoles = chip.getVisitorRoles();

        int i = 0;
        for (Long role : visitorRoles) {
            if (role > 0) {
                String text = visitorRolesText.get(role);
                Log.d(LOG_TAG, "Adding Role '" + text + "'");
                if (text == null) {
                    text = Long.toString(role);
                }
                adapter.addElement("Visitor Roles",
                        context.getResources().getText(R.string.hint_rfid_visitor_role).toString() + " "
                                + Integer.toString(++i), text
                );
            }
        }
    }


    private void setVoucherData(VisitorChip chip){

        Map<Long, Long> voucher = chip.getVoucher();

        Set<Long> voucherIDs = voucher.keySet();
        for (long voucherID : voucherIDs) {

            if (voucherID > 0 && voucher.get(voucherID) > 0) {
                VoucherInfo info = voucherInfo.get(voucherID);
                String validityInfo;
                String name;
                if (info != null) {
                    validityInfo = info.getValidityInfoAsString();
                    name = info.getTitle();
                } else {
                    validityInfo = "";
                    name = String.valueOf(voucherID);
                }

                adapter.addElement("Voucher",
                        context.getResources().getText(R.string.hint_rfid_voucher_id).toString() + validityInfo,
                        String.valueOf(voucher.get(voucherID)) + " x " + name);
            }
        }
    }

    private void setBackOfficeRoles(VisitorChip chip) {

        if (chip.isAdmin()){
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_admin).toString(),
                    context.getResources().getText(R.string.yes).toString()
                    );
        } else {
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_admin).toString(),
                    context.getResources().getText(R.string.no).toString()
            );
        }

        if (chip.isSupervisor()){
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_supervisor).toString(),
                    context.getResources().getText(R.string.yes).toString()
            );
        } else {
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_supervisor).toString(),
                    context.getResources().getText(R.string.no).toString()
            );
        }

        if (chip.isSupervisor()){
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_employee).toString(),
                    context.getResources().getText(R.string.yes).toString()
            );
        } else {
            adapter.addElement("Backoffice Role",
                    context.getResources().getText(R.string.hint_rfid_backofficerole_employee).toString(),
                    context.getResources().getText(R.string.no).toString()
            );
        }

    }

    // for (int i=0; i< backofficeRoles.length; i++){
    // adapter.addElement("Backoffice Roles",
    // getResources().getText(R.string.hint_rfid_backoffice_role).toString()
    // +" " + Integer.toString(i), Long.toString(backofficeRoles[i]));
    // }
}
