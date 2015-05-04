package com.youchip.youmobile.view.shop;

import com.youchip.youmobile.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PaymentMethodDialog extends DialogFragment{
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_value_picker, container);

        getDialog().setTitle(R.string.title_shop_select_payment_method);

        return view;
    }

}
