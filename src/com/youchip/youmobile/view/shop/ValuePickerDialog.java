package com.youchip.youmobile.view.shop;

import com.youchip.youmobile.R;
import com.youchip.youmobile.model.shop.ShoppingCartItem;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class ValuePickerDialog extends DialogFragment {

    public static final String BUNDLE_PROPERTY_ITEM = "item";
    public static final String BUNDLE_PROPERTY_CURRENCY_SYMBOL = "symbol";
    public static final String BUNDLE_PROPERTY_MAX_VALUE = "max_value";
    private static String CURRENT_CLASS;
    
    private static final int MAX_MAIN_VALUE = 100000;
    private static final int MIN_MAIN_VALUE =     0;
    private static final int MAIN_FRAC_DIVIDER= 100;
    
    private static final int MAX_FRAC_VALUE =    99;
    private static final int MIN_FRAC_VALUE =     0;

    private int maxValue = 0;
    private int maxMainValue = 0;
    private int maxFracValue = 0;
    private ShoppingCartItem item = null;
    private NumberPicker mainUnit = null;
    private NumberPicker fractionalUnit = null;
    private Context context;

    public ValuePickerDialog(){}

    private NumberPicker.OnValueChangeListener onValueChangedListener = new NumberPicker.OnValueChangeListener(){
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            if (mainUnit.getValue() == maxMainValue && fractionalUnit.getValue() > maxFracValue) {
                fractionalUnit.setValue(maxFracValue);
            } else if (mainUnit.getValue() > maxMainValue){
                mainUnit.setValue(maxMainValue);
                if (fractionalUnit.getValue() > maxFracValue){
                    fractionalUnit.setValue(maxFracValue);
                }
            }
        }
    };


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        this.context = getActivity();
        CURRENT_CLASS = ValuePickerDialog.this.getClass().getName();
        
        Bundle mArgs = getArguments();
        this.item = (ShoppingCartItem) mArgs.getSerializable(BUNDLE_PROPERTY_ITEM);
        String currencySymbol = mArgs.getString(BUNDLE_PROPERTY_CURRENCY_SYMBOL);
        this.maxValue = ((int) mArgs.getLong(BUNDLE_PROPERTY_MAX_VALUE, MAX_MAIN_VALUE));
        this.maxMainValue = maxValue / MAIN_FRAC_DIVIDER;
        this.maxFracValue = maxValue - (maxMainValue * MAIN_FRAC_DIVIDER);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater li = (LayoutInflater)     
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate and set the layout for the dialog
        View view = li.inflate(R.layout.fragment_value_picker, null);
        
        
        builder
        // Set view:
           .setView(view)
        // Add action buttons
           .setPositiveButton(R.string.button_accept, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // clear focus to assure the number will be up to date when changed by softkeyboard 
                    mainUnit.clearFocus();
                    fractionalUnit.clearFocus();
                    // get the new price
                    int newPrice = mainUnit.getValue()*MAIN_FRAC_DIVIDER + fractionalUnit.getValue();
                    item.setPrice(newPrice);
                    Log.d(CURRENT_CLASS, "Changing price for shop item '"+item.getTitle()+"' (ID: " + item.getPlu()+ ") to " + newPrice);
                    //call the event listener
                    ((ShopItemValueChangeAlert)getActivity()).onSubmitValueChange(item);
                    dialog.dismiss();
                }
             })
           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(CURRENT_CLASS, "Canceling price changing");
                    dialog.dismiss();
                }
            });

        TextView currency = (TextView) view.findViewById(R.id.currency_symbol);
        currency.setText(currencySymbol);
        
        mainUnit = (NumberPicker) view.findViewById(R.id.mainCurrencyUnit);
        fractionalUnit = (NumberPicker) view.findViewById(R.id.fractionalCurrencyUnit);
        
        mainUnit.setWrapSelectorWheel(false);
        mainUnit.setMaxValue(maxMainValue);
        mainUnit.setMinValue(MIN_MAIN_VALUE);

        mainUnit.setFocusable(true);
        mainUnit.setFocusableInTouchMode(true);
        
        fractionalUnit.setWrapSelectorWheel(false);
        fractionalUnit.setMaxValue(MAX_FRAC_VALUE);
        fractionalUnit.setMinValue(MIN_FRAC_VALUE);

        fractionalUnit.setFocusable(true);
        fractionalUnit.setFocusableInTouchMode(true);
        
        mainUnit.setValue((int) (item.getPrice()/MAIN_FRAC_DIVIDER));
        fractionalUnit.setValue((int) (item.getPrice() - (item.getPrice()/MAIN_FRAC_DIVIDER)));

        mainUnit.setOnValueChangedListener(onValueChangedListener);
        fractionalUnit.setOnValueChangedListener(onValueChangedListener);

        return builder.create();
    }


}
