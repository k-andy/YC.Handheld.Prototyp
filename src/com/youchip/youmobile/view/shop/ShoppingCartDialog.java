package com.youchip.youmobile.view.shop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.shop.SerializableOnClickListener;
import com.youchip.youmobile.model.shop.ShoppingCart;
import com.youchip.youmobile.model.shop.ShoppingCartItem;
import com.youchip.youmobile.utils.DataConverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ShoppingCartDialog extends DialogFragment{
    
    private static final String BUNDLE_PROPERTY_SHOPPING_CART = "cart";
    private static final String BUNDLE_PROPERTY_CURRENCY_SYMBOL = "symbol";
    private static final String BUNDLE_PROPERTY_CANCELABLE = "cancelable";
    private static final String BUNDLE_PROPERTY_POSITIVE_LISTENER = "submitlistener";
    private static final String BUNDLE_PROPERTY_COLOR_DEFAULT = "color_default";
    private static final String BUNDLE_PROPERTY_COLOR_LOAD = "color_load";
    private static final String BUNDLE_PROPERTY_COLOR_UNLOAD = "color_unload";
    private static final String BUNDLE_PROPERTY_COLOR_CANCEL = "color_cancel";
    private static final String BUNDLE_PROPERTY_FONT_SIZE    = "font_size";

    private static DialogFragment dialog = null;

    public static void dismissDialog(){
        if (dialog != null)
            dialog.dismiss();
    }


    private OnClickListener submitListener = new OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int id) {
            dialog.dismiss();
        }
     };
     
     private OnClickListener cancelClickListener = new OnClickListener(){
         @Override
         public void onClick(DialogInterface dialog, int id) {
             dialog.dismiss();
         }
      };


    private ShoppingCartDialog(){}
    
    
    @SuppressWarnings("unchecked")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity();
        
        Bundle mArgs = getArguments();
        ShoppingCart shoppingCart = new ShoppingCart((HashMap<ShoppingCartItem, ShoppingCartItem>) mArgs.getSerializable(BUNDLE_PROPERTY_SHOPPING_CART));
        String currencySymbol = mArgs.getString(BUNDLE_PROPERTY_CURRENCY_SYMBOL);
        boolean cancelable = mArgs.getBoolean(BUNDLE_PROPERTY_CANCELABLE, false);
        OnClickListener userSubmitListener = (OnClickListener) mArgs.getSerializable(BUNDLE_PROPERTY_POSITIVE_LISTENER); 
        if (userSubmitListener != null){
          submitListener = userSubmitListener;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater li = (LayoutInflater)     
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate and set the layout for the dialog
        View view = li.inflate(R.layout.fragment_shopping_cart, null);
        ListView listView = (ListView) view.findViewById(R.id.list_view_shopping_cart);             
        TextView totalPrice    = (TextView) view.findViewById(R.id.list_item_shopping_cart_total_price);
        
        totalPrice.setText(DataConverter.longToCurrency(shoppingCart.calcTotalPrice()).toString());

        ShoppingCartItemAdapter shoppingCartItemAdapter = new ShoppingCartItemAdapter(context,
                R.layout.row_shopping_cart_item,
                R.id.list_item_shopping_cart_item_info,
                R.id.list_item_shopping_cart_quantity,
                R.id.list_item_shopping_cart_total_price,
                new LinkedList<>(shoppingCart.values()),
                currencySymbol);
        
        shoppingCartItemAdapter.setColorBuy(mArgs.getInt(BUNDLE_PROPERTY_COLOR_DEFAULT));
        shoppingCartItemAdapter.setColorLoad(mArgs.getInt(BUNDLE_PROPERTY_COLOR_LOAD));
        shoppingCartItemAdapter.setColorUnload(mArgs.getInt(BUNDLE_PROPERTY_COLOR_UNLOAD));
        shoppingCartItemAdapter.setColorCancel(mArgs.getInt(BUNDLE_PROPERTY_COLOR_CANCEL));
        shoppingCartItemAdapter.setFontSize(mArgs.getFloat(BUNDLE_PROPERTY_FONT_SIZE));
        
        listView.setAdapter(shoppingCartItemAdapter);
        
 
        builder
        // Set view:
           .setView(view)
        // Add action buttons
           .setPositiveButton(android.R.string.ok, submitListener);
        
        if (cancelable) {
            builder.setNegativeButton(android.R.string.cancel, cancelClickListener);
        }

        builder.setCancelable(true);

        builder.setTitle(R.string.title_shopping_cart);

        return builder.create();
    }

    /**
     * Opens a visual dialog which shows the shopping cart content
     */
    public static DialogFragment openShoppingCart(ShoppingCart shoppingCart, Activity context){
        ShoppingCartDialog dialog = null;

        if (shoppingCart != null) {

            Bundle args = new Bundle();
            args.putSerializable(ShoppingCartDialog.BUNDLE_PROPERTY_SHOPPING_CART, shoppingCart);
            args.putString(ShoppingCartDialog.BUNDLE_PROPERTY_CURRENCY_SYMBOL, ConfigAccess.get1stCurrencySymbol(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_DEFAULT, ConfigAccess.getArticleColorForBuy(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_LOAD,   ConfigAccess.getArticleColorForLoad(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_UNLOAD, ConfigAccess.getArticleColorForUnload(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_CANCEL, ConfigAccess.getArticleColorForCancelation(context));
            args.putFloat(ShoppingCartDialog.BUNDLE_PROPERTY_FONT_SIZE,  ConfigAccess.getShoppingCartFontSize(context));

            FragmentManager fm = context.getFragmentManager();
            dialog = new ShoppingCartDialog();
            dialog.setArguments(args);
            dialog.show(fm, "dialog_shopping_cart");

        }

        ShoppingCartDialog.dialog = dialog;
        return dialog;
    }

    public static DialogFragment openShoppingCartBeforePayment(ShoppingCart shoppingCart, Activity context, SerializableOnClickListener onSubmitShopping){
        ShoppingCartDialog dialog = null;

        if (shoppingCart != null) {

            Bundle args = new Bundle();
            args.putSerializable(ShoppingCartDialog.BUNDLE_PROPERTY_SHOPPING_CART, shoppingCart);
            args.putString(ShoppingCartDialog.BUNDLE_PROPERTY_CURRENCY_SYMBOL, ConfigAccess.get1stCurrencySymbol(context));
            args.putBoolean(ShoppingCartDialog.BUNDLE_PROPERTY_CANCELABLE, true);
            args.putSerializable(ShoppingCartDialog.BUNDLE_PROPERTY_POSITIVE_LISTENER, (Serializable) onSubmitShopping);
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_DEFAULT, ConfigAccess.getArticleColorForBuy(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_LOAD,   ConfigAccess.getArticleColorForLoad(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_UNLOAD, ConfigAccess.getArticleColorForUnload(context));
            args.putInt(ShoppingCartDialog.BUNDLE_PROPERTY_COLOR_CANCEL, ConfigAccess.getArticleColorForCancelation(context));
            args.putFloat(ShoppingCartDialog.BUNDLE_PROPERTY_FONT_SIZE,  ConfigAccess.getShoppingCartFontSize(context));

            FragmentManager fm = context.getFragmentManager();
            dialog = new ShoppingCartDialog();
            dialog.setArguments(args);
            dialog.show(fm, "dialog_shopping_cart");
        }

        ShoppingCartDialog.dialog = dialog;
        return dialog;
    }

}
