package com.youchip.youmobile.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.youchip.youmobile.model.shop.ShopItemForReport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportLogUtils {
    private static final String PLU_KEY = "plusChecked";
    private static final String PLU_CANCELED_KEY = "canceledPlusChecked";
    private static final String AMOUNT_SUFFIX = "A";
    private static final String AMOUNT_CANCELATION_SUFFIX = "AC";
    private static final String PRICE_SUFFIX = "P";
    private static final String VAT_SUFFIX = "V";
    private static final String TITLE_SUFFIX = "T";

    public static void resetReport(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPref.edit().clear().commit();
        ShopItemForReport.clearData();
    }

    public static void saveReport(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPref.edit();

        for (long plu : ShopItemForReport.getPluAmount().keySet()) {
            editor.putLong(plu + AMOUNT_SUFFIX, ShopItemForReport.getPluAmount().get(plu));
        }

        for (long plu : ShopItemForReport.getPluAmountCanceled().keySet()) {
            editor.putLong(plu + AMOUNT_CANCELATION_SUFFIX, ShopItemForReport.getPluAmountCanceled().get(plu));
        }

        for (long plu : ShopItemForReport.getPluPrice().keySet()) {
            editor.putLong(plu + PRICE_SUFFIX, ShopItemForReport.getPluPrice().get(plu));
        }

        for (long plu : ShopItemForReport.getPluVAT().keySet()) {
            editor.putFloat(plu + VAT_SUFFIX, ShopItemForReport.getPluVAT().get(plu));
        }

        for (long plu : ShopItemForReport.getPluTitle().keySet()) {
            editor.putString(plu + TITLE_SUFFIX, ShopItemForReport.getPluTitle().get(plu));
        }

        Set<String> plus = new HashSet<>();
        Set<String> canceledPlus = new HashSet<>();

        for (long plu : ShopItemForReport.getPluAmount().keySet()) {
            plus.add(plu + "");
        }

        for (long plu : ShopItemForReport.getPluAmountCanceled().keySet()) {
            canceledPlus.add(plu + "");
        }

        editor.putStringSet(PLU_KEY, plus);
        editor.putStringSet(PLU_CANCELED_KEY, canceledPlus);

        editor.commit();
    }

    public static void loadReport(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        Set<String> plus;
        Set<String> canceledPlus;
        plus = sharedPref.getStringSet(PLU_KEY, new HashSet<String>());
        canceledPlus = sharedPref.getStringSet(PLU_CANCELED_KEY, new HashSet<String>());

        if (!plus.isEmpty()) {
            for (String plu : plus) {
                ShopItemForReport.addTmpAmount(Long.parseLong(plu), sharedPref.getLong(plu + AMOUNT_SUFFIX, 0));
            }
            loadData(sharedPref, plus);
        }

        if (!canceledPlus.isEmpty()) {
            for (String plu : canceledPlus) {
                ShopItemForReport.addTmpCancelation(Long.parseLong(plu), sharedPref.getLong(plu + AMOUNT_CANCELATION_SUFFIX, 0));
            }
            loadData(sharedPref, canceledPlus);
        }
        ShopItemForReport.saveTmpData();
    }

    private static void loadData(SharedPreferences sharedPref, Set<String> plus) {
        for (String plu : plus) {
            ShopItemForReport.addTmpPrice(Long.parseLong(plu), sharedPref.getLong(plu + PRICE_SUFFIX, 0));
        }
        for (String plu : plus) {
            ShopItemForReport.addTmpVAT(Long.parseLong(plu), sharedPref.getFloat(plu + VAT_SUFFIX, 0));
        }
        for (String plu : plus) {
            ShopItemForReport.addTmpTitle(Long.parseLong(plu), sharedPref.getString(plu + TITLE_SUFFIX, ""));
        }
    }

    public static long calculateGross() {
        Map<Long, Long> pluAmount = ShopItemForReport.getPluAmountCanceled();
        long result = calculateTotal();

        for (long plu : pluAmount.keySet()) {
            result -= ShopItemForReport.getCancelationAmount(plu) * ShopItemForReport.getPrice(plu);
        }

        return result;
    }

    public static long calculateReturns() {
        Map<Long, Long> pluAmount = ShopItemForReport.getPluAmountCanceled();
        long result = 0;

        for (long plu : pluAmount.keySet()) {
            result += ShopItemForReport.getCancelationAmount(plu) * ShopItemForReport.getPrice(plu);
        }
        return result;
    }

    public static Map<Float, Set<Long>> getVats() {
        Map<Float, Set<Long>> vats = new HashMap<>();
        Set<Long> plus;
        for (long plu : ShopItemForReport.getPluVAT().keySet()) {
            float vat = ShopItemForReport.getVAT(plu);
            plus = new HashSet<>();
            plus.add(plu);
            for (Map.Entry<Long, Float> entry : ShopItemForReport.getPluVAT().entrySet()) {
                if (entry.getValue() == vat) {
                    plus.add(entry.getKey());
                }
            }
            vats.put(vat, plus);
        }
        return vats;
    }

    public static List<List<Number>> calculateValuesForVats() {
        List<Number> values;

        List<List<Number>> vatsValues = new LinkedList<>();

        Map<Float, Set<Long>> vats = getVats();
        for (float vat : vats.keySet()) {
            long salesInclTax = 0;
            for (long plu : vats.get(vat)) {
                salesInclTax += calculateTotalForPlu(plu);
            }
            long salesNet = 0;
            for (long plu : vats.get(vat)) {
                salesNet += calculateNetForPlu(plu);
            }
            long taxes = 0;
            for (long plu : vats.get(vat)) {
                taxes += calculateTaxesForPlu(plu);
            }
            values = new LinkedList<>();
            values.add(vat);
            values.add(salesInclTax);
            values.add(salesNet);
            values.add(taxes);

            vatsValues.add(values);
        }

        return vatsValues;
    }

    public static List<List<String>> getSoldItems() {
        Set<Long> plus = ShopItemForReport.getPluAmount().keySet();
        if (!plus.isEmpty()) {
            return getItems(plus, false);
        } else {
            return null;
        }
    }

    public static List<List<String>> getCanceledItems() {
        Set<Long> plus = ShopItemForReport.getPluAmountCanceled().keySet();
        if (!plus.isEmpty()) {
            return getItems(plus, true);
        } else {
            return null;
        }
    }

    private static List<List<String>> getItems(Set<Long> plus, boolean isCancelation) {
        List<List<String>> items = new LinkedList<>();
        List<String> names = new LinkedList<>();
        List<String> count = new LinkedList<>();

        for (Long plu : plus) {
            names.add(ShopItemForReport.getTitle(plu));
            if (isCancelation) {
                count.add(ShopItemForReport.getCancelationAmount(plu) + "");
            } else {
                count.add(ShopItemForReport.getAmount(plu) + "");
            }
        }

        items.add(names);
        items.add(count);

        return items;
    }

    public static long calculateTotal() {
        Map<Long, Long> pluAmount = ShopItemForReport.getPluAmount();
        long total = 0;

        for (long plu : pluAmount.keySet()) {
            total += calculateTotalForPlu(plu);
        }
        return total;
    }

    public static double calculateNetForPlu(long plu) {
        double net = calculateTotalForPlu(plu);

        net -= calculateTaxesForPlu(plu);
        return net;
    }

    public static double calculateNet() {
        Map<Long, Long> pluAmount = ShopItemForReport.getPluAmount();
        double result = 0;

        for (long plu : pluAmount.keySet()) {
            result += calculateNetForPlu(plu);
        }
        return result;
    }

    public static double calculateTaxesForPlu(long plu) {
        return (calculateTotalForPlu(plu) / 100d) * ShopItemForReport.getVAT(plu);
    }

    public static long calculateTotalForPlu(long plu) {
        return ShopItemForReport.getAmount(plu) * ShopItemForReport.getPrice(plu);
    }
}
