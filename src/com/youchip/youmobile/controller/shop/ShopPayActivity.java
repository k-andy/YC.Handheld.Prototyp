package com.youchip.youmobile.controller.shop;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.youchip.youmobile.controller.chipIO.ChipReaderActivity;
import com.youchip.youmobile.controller.chipIO.ChipReaderService;
import com.youchip.youmobile.controller.gate.AccessChecker;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxGateLogger.AccessState;
import com.youchip.youmobile.controller.txlog.TxShopLogger;
import com.youchip.youmobile.controller.txlog.TxType;
import com.youchip.youmobile.model.chip.interfaces.BasicChip;
import com.youchip.youmobile.model.chip.interfaces.ChipField;
import com.youchip.youmobile.model.chip.interfaces.VisitorChip;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.AppType;
import com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChip;
import com.youchip.youmobile.model.gate.BlockedChip;
import com.youchip.youmobile.model.shop.ShopItemForReport;
import com.youchip.youmobile.model.shop.ShoppingCart;
import com.youchip.youmobile.model.shop.ShoppingCartItem;
import com.youchip.youmobile.model.shop.VoucherInfo;
import com.youchip.youmobile.utils.DataConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_CREDIT1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_CREDIT2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CASHOUT;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CREDIT_1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CREDIT_2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_USED_VOUCHER;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_EVENT_ID;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOPPING_CART;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_ERROR_ID;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_PAYMENT_METHOD;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_USE_VOUCHER;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.APPTYPE;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.EVENT_ID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KChipSpecs.FactoryFields.UID;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.CREDIT_1;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.CREDIT_2;
import static com.youchip.youmobile.model.chip.mc1kImpl.MC1KVisitorChipField.VOUCHER;


public class ShopPayActivity extends ChipReaderActivity{
    
    private TxShopLogger txLogger;
    
    private static final Set<Integer> STATUS_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
           { UID, EVENT_ID, APPTYPE, CREDIT_1, CREDIT_2, VOUCHER }));
    
    private static final Set<Integer> CRC_BLOCKS = DataConverter.getRelevantBlocks(Arrays.asList(new ChipField[] 
            {EVENT_ID, APPTYPE, CREDIT_1, CREDIT_2,  VOUCHER}));
    
    private ShoppingCart shoppingCart;
    private long maxCredit1;
    private long maxCredit2;
    private long maxCashout;
    private PaymentMethod paymentMethod;
    private long eventID;
    private ShoppingErrorCause errorCause = ShoppingErrorCause.NO_ERROR;
    private boolean useVoucher = true;
    private long voucherUsed = 0;

    private List<BlockedChip> blackList;
    private Map<Long, VoucherInfo> voucherInfos;
    private AccessChecker checker = new AccessChecker();

    private boolean useProductGroupAsVoucher;
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        Intent intent = getIntent();
        this.shoppingCart    = new ShoppingCart( (Map<ShoppingCartItem, ShoppingCartItem>) intent.getSerializableExtra(INTENT_EXTRA_SHOPPING_CART));
        this.maxCredit1      = intent.getLongExtra(INTENT_EXTRA_CHIP_MAX_CREDIT_1, 0);
        this.maxCredit2      = intent.getLongExtra(INTENT_EXTRA_CHIP_MAX_CREDIT_2, 0);
        this.maxCashout      = intent.getLongExtra(INTENT_EXTRA_CHIP_MAX_CASHOUT, 0);
        this.paymentMethod   = (PaymentMethod) intent.getSerializableExtra(INTENT_EXTRA_SHOP_PAYMENT_METHOD);
        this.useVoucher      = intent.getBooleanExtra(INTENT_EXTRA_SHOP_USE_VOUCHER, true);
        this.eventID         = intent.getLongExtra(INTENT_EXTRA_EVENT_ID,0);
        String userID          = intent.getStringExtra(INTENT_EXTRA_USER_ID);
        
        this.blackList  = ConfigAccess.getBlackList(this);
        this.voucherInfos = ConfigAccess.getVoucherGroups(this);
        this.txLogger = new TxShopLogger(this, userID);
        this.useProductGroupAsVoucher = ConfigAccess.useProductGroupAsVoucherID(this);

        if (!ConfigAccess.getIgnoreVoucherValidityTime(this)) {
            invalidateVoucherPayablity(this.shoppingCart, this.voucherInfos, this.useProductGroupAsVoucher);
        }

        voucherUsed = 0;
    }

    @Override
    protected void onValidChipReadResult(Context context, BasicChip basicChip) {
        Intent intent = new Intent();
        boolean result = false;
        AppType chipAppType = basicChip.getAppType();
        
        if (!basicChip.isValid(CRC_BLOCKS)){
            Log.w(LOG_TAG, "CRC Error! Chip is corrupted.");
            txLogger.curruptedCRC(basicChip.getUID());
            intent.putExtra(INTENT_EXTRA_SHOP_ERROR_ID, ShoppingErrorCause.CAUSE_INVALID_CRC);   
        } else if (chipAppType != AppType.VISITOR_APP) {
            Log.w(LOG_TAG, "Invalid App Type");
            txLogger.invalidAppType(basicChip.getUID());
            intent.putExtra(INTENT_EXTRA_SHOP_ERROR_ID, ShoppingErrorCause.CAUSE_INVALID_APP);
        } else if (basicChip.getEventID() != eventID) {
            Log.w(LOG_TAG, "Invalid Event");
            txLogger.invalidAppType(basicChip.getUID());
            intent.putExtra(INTENT_EXTRA_SHOP_ERROR_ID, ShoppingErrorCause.CAUSE_INVALID_EVENT);
        } else if (checker.checkBannedList(basicChip.getUID(), blackList).getAccessState() == AccessState.BANNED) {
            txLogger.chipIsBanned(basicChip.getUID());
            intent.putExtra(INTENT_EXTRA_SHOP_ERROR_ID, ShoppingErrorCause.CAUSE_CHIP_BANNED);
        } else {
            VisitorChip chip = new MC1KVisitorChip(basicChip);
            
            long oldCredit1 = chip.getCredit1();
            long oldCredit2 = chip.getCredit2();
            
            result = performTrade(chip);
            intent.putExtra(INTENT_EXTRA_SHOP_ERROR_ID, errorCause);
            intent.putExtra(INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT1, oldCredit1);
            intent.putExtra(INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT2, oldCredit2);
            intent.putExtra(INTENT_EXTRA_CHIP_FIELD_CREDIT1, chip.getCredit1());
            intent.putExtra(INTENT_EXTRA_CHIP_FIELD_CREDIT2, chip.getCredit2());
            intent.putExtra(INTENT_EXTRA_CHIP_USED_VOUCHER, voucherUsed);
        }

        if (result){
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED, intent);            
        }
        this.finish();
    }

    @Override
    protected Set<Integer> getStatusBlocks() {
        return STATUS_BLOCKS;
    }

    private boolean performTrade(VisitorChip chip){
        Log.d(LOG_TAG, "Perform trade on Chip " + chip.getUID());

        txLogger.clearLog();
        long chipCredit1 = chip.getCredit1();
        long chipCredit2 = chip.getCredit2();
        Map<Long,Long> voucherOnChip = chip.getVoucher();

        
        boolean sufficient = true;
        long    totalVoucherUsed = 0;
        
        Set<ShoppingCartItem> itemKeys = shoppingCart.keySet();
        for (ShoppingCartItem itemKey : itemKeys) {
            ShoppingCartItem item = shoppingCart.get(itemKey);
            long plu          = item.getPlu();
            long quantity     = item.getQuantity();
            long price        = item.getPrice();
            long totalPerItem = price * quantity;
            TxType txType     = item.getTxType();
            long neededVoucher= useProductGroupAsVoucher ? item.getProductGroup() : item.getPlu();
            
//            PaymentMethod paymentMethod = txType == TxType.LOAD_CREDIT || txType == TxType.UNLOAD_CREDIT ? this.paymentMethod : PAYMENT_CHIP;
            PaymentMethod paymentMethod = this.paymentMethod;
            
            long txCredit1 = 0;
            long txCredit2 = 0;
            long modVoucher = 0;
            
            Log.d(LOG_TAG, "Check Shop Item "+ item.getTitle() +", ID: "+ plu + ", Value: " + price + ", Quantity: " + quantity);
            Log.d(LOG_TAG, "Credits on Chip: Credit1: " + chipCredit1 +", Credit2: " + chipCredit2);
            Log.d(LOG_TAG, "Max credits allowed: Max.Credit1: " + maxCredit1 + ", Max.Credit2: " + maxCredit2);
            
            // if item can be bought by voucher
            if (this.useVoucher && item.isVoucherAllowed() && txType == TxType.BUY_ARTICLE &&
                    voucherOnChip.containsKey(neededVoucher) && voucherOnChip.get(neededVoucher) > 0){
                Log.d(LOG_TAG, voucherOnChip.get(neededVoucher) + " Voucher available for this item");
                // take as much voucher as you can
                modVoucher = Math.min(voucherOnChip.get(neededVoucher), quantity);
                // remove voucher from list
                voucherOnChip.put(neededVoucher, voucherOnChip.get(neededVoucher)-modVoucher);
                // reduce needed money by number of voucher 
                totalPerItem -= price * modVoucher;
            } else {
                Log.d(LOG_TAG, "No voucher can be used to buy this item");
            }
            
            // cancellation of voucher-only-items is going to first currency
            if (item.isVoucherAllowed() && !item.isFirstCurrencyAllowed()
                    && !item.isSecondCurrencyAllowed()) {
                if (txType == TxType.CANCELATION) {
                    Log.d(LOG_TAG, "Credit 1 can be loaded canceling this item");
                    // put as mouch as you can
                    long itemModCredit = -totalPerItem;
                    Log.d(LOG_TAG, itemModCredit
                            + " Credits are loaded to Credit1 by canceling");
                    // add the money to credit1
                    chipCredit1 += itemModCredit;
                    txCredit1   += itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= -itemModCredit;
                }
            }
            
            // if credit 2 may be used
            if (item.isSecondCurrencyAllowed()){
                if (txType == TxType.BUY_ARTICLE){
                    Log.d(LOG_TAG, chipCredit2 + " Credit 2 available to buy/unload this item");
                    // take as much from credit2 as you can
                    long itemModCredit = Math.min(chipCredit2, totalPerItem);
                    Log.d(LOG_TAG, itemModCredit + " Credits are used from Credit2");
                    // remove it from available money
                    chipCredit2 -= itemModCredit;
                    txCredit2   += itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= itemModCredit;
                } else if (txType == TxType.UNLOAD_CREDIT){
                    // dont unload credit 2 
                } else if (txType == TxType.LOAD_CREDIT){
                    Log.d(LOG_TAG, "Credit 2 can be loaded by this item");
                    // put as mouch as you can
                    long maxToAdd      = Math.max(maxCredit2-chipCredit2,0);
                    Log.d(LOG_TAG, maxToAdd +" Credits may be loaded to Credit2 at most");
                    long itemModCredit = Math.min(maxToAdd, totalPerItem);
                    Log.d(LOG_TAG, itemModCredit + " Credits are loaded to Credit2");
                    // add the money to credit2
                    chipCredit2 += itemModCredit;
                    txCredit2   += itemModCredit;
                    // reduce total per item
                    totalPerItem -= itemModCredit;   
                } else if (txType == TxType.CANCELATION){
                    // cancelation only to first currency
                    Log.d(LOG_TAG, "Credit 1 can be loaded canceling this item");
                    // put as mouch as you can
                    long itemModCredit = -totalPerItem;
                    Log.d(LOG_TAG, itemModCredit + " Credits are loaded to Credit1 by canceling");
                    // add the money to credit2
                    chipCredit1 += itemModCredit;
                    txCredit1   += itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= -itemModCredit;  
                }
            }
            
            //if credit 1 may be used
            if (item.isFirstCurrencyAllowed()){
                if (txType == TxType.BUY_ARTICLE){
                    Log.d(LOG_TAG, chipCredit1 + " Credit 1 available to buy this item");
                    // take as much from credit1 as you can
                    long itemModCredit = Math.min(chipCredit1, totalPerItem);
                    Log.d(LOG_TAG, itemModCredit + " Credits are used from Credit1");
                    // remove it from available money
                    chipCredit1 -= itemModCredit;
                    txCredit1   +=  itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= itemModCredit;
                } else if (txType == TxType.UNLOAD_CREDIT && totalPerItem == 0){
                    Log.d(LOG_TAG, chipCredit1 + " Credit 1 available to unload all");
                    // total unload all credit 1
                    long itemModCredit = Math.min(chipCredit1, maxCashout);
                    Log.d(LOG_TAG, itemModCredit + " Credits are unloaded from Credit1");
                    // remove it from available money
                    chipCredit1 -= itemModCredit;
                    txCredit1   +=  itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= 0;
                } else if (txType == TxType.UNLOAD_CREDIT && totalPerItem < 0 ){
                    Log.d(LOG_TAG, chipCredit1 + " Credit 1 available to unload");
                    // total unload all credit 1
                    long itemModCredit = Math.min(Math.min(chipCredit1, -totalPerItem), maxCashout);
                    Log.d(LOG_TAG, itemModCredit + " Credits are unloaded from Credit1");
                    // remove it from available money
                    chipCredit1 -= itemModCredit;
                    txCredit1   +=  itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem += itemModCredit;
                } else if (txType == TxType.LOAD_CREDIT){
                    Log.d(LOG_TAG, "Credit 1 can be loaded by this item");
                    // put as mouch as you can
                    long maxToAdd      = Math.max(maxCredit1-chipCredit1, 0);
                    Log.d(LOG_TAG, maxToAdd +" Credits may be loaded to Credit1 at most");
                    long itemModCredit = Math.min(maxToAdd, totalPerItem);
                    Log.d(LOG_TAG, itemModCredit + " Credits are loaded to Credit1");
                    // add the money to credit2
                    chipCredit1 += itemModCredit;
                    txCredit1   += itemModCredit;
                    // reduce total per item
                    totalPerItem -= itemModCredit;   
                } else if (txType == TxType.CANCELATION){
                    Log.d(LOG_TAG, "Credit 1 can be loaded canceling this item");
                    // put as mouch as you can
                    long itemModCredit = -totalPerItem;
                    Log.d(LOG_TAG, itemModCredit + " Credits are loaded to Credit1 by canceling");
                    // add the money to credit2
                    chipCredit1 += itemModCredit;
                    txCredit1   +=  itemModCredit;
                    // reduce needed money by number of voucher
                    totalPerItem -= -itemModCredit;  
                }
            }
            
            // if the item was not affordable 
            if (totalPerItem != 0){
                Log.w(LOG_TAG, "No trade possibe for item " + plu + "! Value left: " + totalPerItem);
                setErrorMessage(txType);
                sufficient = false;
                break;
            } else if (txType == TxType.UNLOAD_CREDIT && txCredit1 == 0) {
                Log.w(LOG_TAG, "Nothing on chip to cash-out!");
                setErrorMessage(txType);
                sufficient = false;
                break;
            } else {
                Log.d(LOG_TAG, "Trade accepted for item " + plu);
                // log entry for voucher payment
                if (modVoucher > 0) {
                    txLogger.addToTempLog(txType, chip.getUID(), 0, 0, neededVoucher, paymentMethod, plu, modVoucher, item.getVat(), item.getTitle(), item.getPrice());
                    totalVoucherUsed += modVoucher;
                }
                // log entry for non-voucher payment
                if (quantity > modVoucher) {
                    txLogger.addToTempLog(txType, chip.getUID(), txCredit1, txCredit2, 0, paymentMethod, plu, quantity - modVoucher, item.getVat(), item.getTitle(), item.getPrice());
                }
            }
        }
        
        // if sufficient money/ voucher are on the chip. write it
        if (sufficient){
            // if writing was successfull, save the log to file
            
            chip.setCredit1(chipCredit1);
            chip.setCredit2(chipCredit2);
            chip.setVoucher(voucherOnChip);
            
            if (ChipReaderService.writeDataToChip(chip)){
                Log.d(LOG_TAG, "Trade was successful with chip " + chip.getUID());
                ShopItemForReport.saveTmpData();
                txLogger.saveLog(this);
                // clear log file
                txLogger.clearLog();

                // make voucherUsed global
                this.voucherUsed = totalVoucherUsed;

                // return with positive feadback
                return true;
            }
        }
        
        // TODO save error messages?
        ShopItemForReport.clearTmpData();
        txLogger.clearLog();
        return false;
    }
    

    
    private void setErrorMessage(TxType txType){
        switch (txType){
        case BUY_ARTICLE:
            this.errorCause = ShoppingErrorCause.CAUSE_BUY_ARTICLE;
            break;
        case LOAD_CREDIT:
            this.errorCause = ShoppingErrorCause.CAUSE_LOAD_CREDIT;
            break;
        case UNLOAD_CREDIT:
            this.errorCause = ShoppingErrorCause.CAUSE_UNLOAD_CREDIT;
            break;
        case CANCELATION:
            this.errorCause = ShoppingErrorCause.CAUSE_CANCELATION;
            break;
        default:
            this.errorCause = ShoppingErrorCause.CAUSE_UNKNOWN;
        }
    }


    /**
     * If the article generally could be payed by voucher.. the voucher may be invalide by
     * time. so this method is invalidating the voucher payment option for those articles.
     *
     * @param cart current shopping cart
     * @param voucherInfos voucher information
     * @param useGroupID if the the article group or the plu is used for voucher refferences
     */
    private void invalidateVoucherPayablity(ShoppingCart cart, Map<Long, VoucherInfo> voucherInfos, boolean useGroupID){

        Set<ShoppingCartItem> items = cart.getVoucherPayableItems();
        if (!items.isEmpty() && !voucherInfos.isEmpty()){
            for(ShoppingCartItem item:items){
                if (item.isVoucherAllowed()) {
                    long voucherID = useGroupID ? item.getProductGroup() : item.getPlu();
                    if (voucherInfos.containsKey(voucherID) && !voucherInfos.get(voucherID).isValid()) {
                        item.setVoucherAllowed(false);
                    }
                }
            }

        }

    }
}
