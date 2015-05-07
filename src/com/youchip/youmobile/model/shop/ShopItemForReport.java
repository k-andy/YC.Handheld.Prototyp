package com.youchip.youmobile.model.shop;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 5/5/15.
 */
public class ShopItemForReport {

    private static Map<Long, Long> pluAmount = new HashMap<>();
    private static Map<Long, Float> pluVAT = new HashMap<>();
    private static Map<Long, Long> pluPrice = new HashMap<>();
    private static Map<Long, String> pluTitle = new HashMap<>();
    private static Map<Long, Long> pluAmountCanceled = new HashMap<>();

    private static Map<Long, Long> tmpPluAmount = new HashMap<>();
    private static Map<Long, Float> tmpPluVAT = new HashMap<>();
    private static Map<Long, Long> tmpPluPrice = new HashMap<>();
    private static Map<Long, String> tmpPluTitle = new HashMap<>();
    private static Map<Long, Long> tmpPluAmountCanceled = new HashMap<>();

    public static Map<Long, Long> getPluPrice() {
        return pluPrice;
    }

    public static Map<Long, String> getPluTitle() {
        return pluTitle;
    }

    public static Map<Long, Long> getPluAmountCanceled() {
        return pluAmountCanceled;
    }

    public static Map<Long, Long> getPluAmount() {
        return pluAmount;
    }

    public static Map<Long, Float> getPluVAT() {
        return pluVAT;
    }

    private static void addAmount(long plu, long amount) {
        if (pluAmount.get(plu) != null) {
            pluAmount.put(plu, pluAmount.get(plu) + amount);
        } else {
            pluAmount.put(plu, amount);
        }
    }

    private static void addCancelation(long plu, long amount) {
        if (pluAmountCanceled.get(plu) != null) {
            pluAmountCanceled.put(plu, pluAmountCanceled.get(plu) + amount);
        } else {
            pluAmountCanceled.put(plu, amount);
        }
    }

    private static void addVAT(long plu, float VAT) {
        pluVAT.put(plu, VAT);
    }

    private static void addPrice(long plu, long price) {
        pluPrice.put(plu, price);
    }

    private static void addTitle(long plu, String title) {
        pluTitle.put(plu, title);
    }

    public static void addTmpAmount(long plu, long amount) {
        if (tmpPluAmount.get(plu) != null) {
            tmpPluAmount.put(plu, tmpPluAmount.get(plu) + amount);
        } else {
            tmpPluAmount.put(plu, amount);
        }
    }

    public static void addTmpCancelation(long plu, long amount) {
        if (tmpPluAmountCanceled.get(plu) != null) {
            tmpPluAmountCanceled.put(plu, tmpPluAmountCanceled.get(plu) + amount);
        } else {
            tmpPluAmountCanceled.put(plu, amount);
        }
    }

    public static void addTmpVAT(long plu, float VAT) {
        tmpPluVAT.put(plu, VAT);
    }

    public static void addTmpPrice(long plu, long price) {
        tmpPluPrice.put(plu, price);
    }

    public static void addTmpTitle(long plu, String title) {
        tmpPluTitle.put(plu, title);
    }

    public static void saveTmpData() {
        for (Map.Entry<Long, Long> entry : tmpPluAmount.entrySet()) {
            addAmount(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Long> entry : tmpPluAmountCanceled.entrySet()) {
            addCancelation(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Long> entry : tmpPluPrice.entrySet()) {
            addPrice(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, Float> entry : tmpPluVAT.entrySet()) {
            addVAT(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<Long, String> entry : tmpPluTitle.entrySet()) {
            addTitle(entry.getKey(), entry.getValue());
        }

        clearTmpData();
    }

    public static long getAmount(long plu) {
        if (pluAmount.containsKey(plu)) {
            return pluAmount.get(plu);
        } else {
            return 0;
        }
    }

    public static long getCancelationAmount(long plu) {
        return pluAmountCanceled.get(plu);
    }

    public static float getVAT(long plu) {
        return pluVAT.get(plu);
    }

    public static long getPrice(long plu) {
        return pluPrice.get(plu);
    }

    public static String getTitle(long plu) {
        return pluTitle.get(plu);
    }

    public static void clearData() {
        pluAmount = new HashMap<>();
        pluVAT = new HashMap<>();
        pluPrice = new HashMap<>();
        pluTitle = new HashMap<>();
        pluAmountCanceled = new HashMap<>();

        clearTmpData();
    }

    public static void clearTmpData() {
        tmpPluAmount = new HashMap<>();
        tmpPluVAT = new HashMap<>();
        tmpPluPrice = new HashMap<>();
        tmpPluTitle = new HashMap<>();
        tmpPluAmountCanceled = new HashMap<>();
    }
}
