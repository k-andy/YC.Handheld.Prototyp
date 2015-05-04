package com.youchip.youmobile.view.shop;

import com.youchip.youmobile.R;
import com.youchip.youmobile.model.shop.ShoppingCartItem;
import com.youchip.youmobile.utils.DataConverter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.NumberPicker.OnValueChangeListener;

public class QuantityPickerDialog extends DialogFragment {
    
    public static final String BUNDLE_PROPERTY_ITEM = "item";
    public static final String BUNDLE_PROPERTY_CURRENCY_SYMBOL = "symbol";
    
    private static final int MAX_QUANTITY = 100;
    private static final int MIN_QUANTITY =   0;

    private ShoppingCartItem item = null;
    private String currencySymbol = "€";
    private TextView itemTotal = null;
    
    private NumberPicker quantity = null;
    
    private OnValueChangeListener onValueChangedListener = new OnValueChangeListener(){

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            itemTotal.setText(DataConverter.longToCurrency(item.getPrice()*newVal) + " " + currencySymbol);
        }
        
    };
    
    public QuantityPickerDialog(){}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity();
        
        Bundle mArgs = getArguments();
        item = (ShoppingCartItem) mArgs.getSerializable(BUNDLE_PROPERTY_ITEM);
        currencySymbol = mArgs.getString(BUNDLE_PROPERTY_CURRENCY_SYMBOL);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater li = (LayoutInflater)     
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate and set the layout for the dialog
        View view = li.inflate(R.layout.fragment_quantity_picker, null);
        
        
        builder
        // Set view:
           .setView(view)
        // Add action buttons
           .setPositiveButton(R.string.button_accept, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // clear focus to assure the number will be up to date when changed by softkeyboard 
                    quantity.clearFocus();
                    item.setQuantity(quantity.getValue());
                    ((ShopItemQuantatiyChangeAlert)getActivity()).onSubmitQuantityChange(item);
                }
             })
           .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }); 
        
        TextView itemInfo = (TextView) view.findViewById(R.id.shop_item_description);
        itemInfo.setText(item.getTitle() + " (" + DataConverter.longToCurrency(item.getPrice())+ " "+ currencySymbol +")");
        
        itemTotal = (TextView) view.findViewById(R.id.shop_item_total_price);
        itemTotal.setText(DataConverter.longToCurrency(item.getPrice()*item.getQuantity()) + " " + currencySymbol);
        
        quantity = (NumberPicker) view.findViewById(R.id.action_quantity_selection);
        quantity.setMaxValue(MAX_QUANTITY);
        quantity.setMinValue(MIN_QUANTITY);
        quantity.setWrapSelectorWheel(false);
        quantity.setFocusable(true);
        quantity.setFocusableInTouchMode(true);
        quantity.setValue((int) item.getQuantity());
        
        quantity.setOnValueChangedListener(onValueChangedListener);

        return builder.create();
    }
    
    
}
