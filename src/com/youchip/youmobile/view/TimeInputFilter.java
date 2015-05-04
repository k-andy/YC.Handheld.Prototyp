package com.youchip.youmobile.view;

import android.text.InputFilter;
import android.text.Spanned;

public class TimeInputFilter implements InputFilter{

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.length() == 0) {
                return null;// deleting, keep original editing
            }
            String result = "";
            result += dest.toString().substring(0, dstart);
            result += source.toString().substring(start, end);
            result += dest.toString().substring(dend, dest.length());

            if (result.length() > 5) {
                return "";// do not allow this edit
            }
            boolean allowEdit = true;
            char c;
            if (result.length() > 0) {
                c = result.charAt(0);
                allowEdit &= (c >= '0' && c <= '2');
            }
            if (result.length() > 1) {
                c = result.charAt(1);
                if(result.charAt(0) == '0' || result.charAt(0) == '1')
                    allowEdit &= (c >= '0' && c <= '9');
                else
                    allowEdit &= (c >= '0' && c <= '3');
            }
            if (result.length() > 2) {
                c = result.charAt(2);
                allowEdit &= (c == ':');
            }
            if (result.length() > 3) {
                c = result.charAt(3);
                allowEdit &= (c >= '0' && c <= '5');
            }
            if (result.length() > 4) {
                c = result.charAt(4);
                allowEdit &= (c >= '0' && c <= '9');
            }
            return allowEdit ? null : "";
        }


}
