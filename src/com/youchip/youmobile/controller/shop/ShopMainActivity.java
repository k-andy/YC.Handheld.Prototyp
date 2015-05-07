package com.youchip.youmobile.controller.shop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.AbstractAppControlActivity;
import com.youchip.youmobile.controller.helpdesk.HelpDeskMainActivity;
import com.youchip.youmobile.controller.settings.ConfigAccess;
import com.youchip.youmobile.controller.txlog.TxLogger;
import com.youchip.youmobile.controller.txlog.TxShopLogger;
import com.youchip.youmobile.controller.txlog.TxType;
import com.youchip.youmobile.model.shop.ShopItemConfig;
import com.youchip.youmobile.model.shop.ShoppingCart;
import com.youchip.youmobile.model.shop.ShoppingCartItem;
import com.youchip.youmobile.utils.AlertBox;
import com.youchip.youmobile.utils.DataConverter;
import com.youchip.youmobile.view.shop.QuantityPickerDialog;
import com.youchip.youmobile.view.shop.ShopItemAdapter;
import com.youchip.youmobile.view.shop.ShopItemQuantatiyChangeAlert;
import com.youchip.youmobile.view.shop.ShopItemValueChangeAlert;
import com.youchip.youmobile.view.shop.ShoppingCartDialog;
import com.youchip.youmobile.view.shop.ValuePickerDialog;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_ADMIN;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_EMPLOYEE;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_BO_ROLE_SUPERVISOR;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_CREDIT1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_CREDIT2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_KEY_A;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CASHOUT;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CREDIT_1;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_MAX_CREDIT_2;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_CHIP_USED_VOUCHER;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_EVENT_ID;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_MODE_NAME;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOPPING_CART;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_ERROR_ID;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_PAYMENT_METHOD;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOP_USE_VOUCHER;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_SHOW_BALANCE_ONLY;
import static com.youchip.youmobile.controller.IntentExtrasKeys.INTENT_EXTRA_USER_ID;

public class ShopMainActivity extends AbstractAppControlActivity implements ShopItemQuantatiyChangeAlert, ShopItemValueChangeAlert{
    private final String LOG_TAG =  ShopMainActivity.class.getName();
    private static final int INTENT_FOR_PAY_SHOP_ITEMS = 1238964;
    private boolean isCancellationMode = false;

    private String keyA;
    private String userID;
    private long eventID;

    private Map<Long,ShopItemConfig> shopInventory;
    private ShoppingCart shoppingCart = new ShoppingCart();

    private TextView totalPrice;
    private TextView totalQuantity;
    private Button   submitPurchaseButton;
    private TextView shopHintSelectItem;
    private MenuItem shopModeButton;

    private long maxCredit1;
    private long maxCredit2;
    private long maxCashout;

    private String currencySymbol;
    private String currencySymbol2;
    private ShopItemAdapter shopItemAdapter;

    private boolean isBoAdmin = false;
    private boolean isBoSupervisor = false;

    /**
     * Dialog payment methods which are shown when clicking the "buy" button
     */
    private Dialog virtualPaymentMethodDialog;

    private Dialog allPaymentMethodDialog;
    private Dialog allPaymentMethodSimpleDialog;
//    private Dialog cashPaymentMethodDialog;
    private Dialog topUpPaymentMethodDialog;
    private Dialog cancelationPaymentMethodDialog;

    // have to close dialogs before finishing
    private DialogFragment dialogFragment = null;


    /**
     * When clicking on a shop item
     */
    private OnItemClickListener onClickShopItem = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ShoppingCartItem item = new ShoppingCartItem(shopInventory.get(id));

            if(item.isSecondCurrencyAllowed() && item.getTxType() == TxType.LOAD_CREDIT && !isBoAdmin){
                // only admins are allowed to load to second currency
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_permit_load_2nd_currency);
            } else if (!isCancellationMode && (item.getTxType() == TxType.LOAD_CREDIT)  && shoppingCart.hasItemOtherThanTxType(TxType.LOAD_CREDIT)){
                // dont mix loading with other articles
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_cant_mix_article_types);
            } else if (!isCancellationMode && (item.getTxType() == TxType.UNLOAD_CREDIT)  && shoppingCart.hasItemOtherThanTxType(TxType.UNLOAD_CREDIT)){
                // dont mix loading with other articles
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_cant_mix_article_types);
            } else if (!isCancellationMode && (item.getTxType() == TxType.CANCELATION || item.getTxType() == TxType.BUY_ARTICLE)  && shoppingCart.hasItemOtherThanTxType(TxType.BUY_ARTICLE, TxType.CANCELATION)){
                //  dont mix buying and pfand with others
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_cant_mix_article_types);
            } else // exit if trying to mix tx types in cancelation mode 
                if (isCancellationMode && item.getTxType() == TxType.BUY_ARTICLE && shoppingCart.hasItemOtherThanTxType(TxType.CANCELATION)){
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_cant_mix_article_types);
            } else             // if trying to unload multiple times
                if (!isCancellationMode && item.getTxType() == TxType.UNLOAD_CREDIT && !shoppingCart.isEmpty() ){
                    Toast toast = Toast.makeText(ShopMainActivity.this, R.string.hint_shopping_cant_multi_unload, Toast.LENGTH_SHORT);
                    toast.show();
            } else
            // only put this to cart, if cancellation mode (storno) is not active or it is an item-to-buy, which CAN be canceled
                if ((item.getPrice() != 0)  && (!isCancellationMode || item.getTxType() == TxType.BUY_ARTICLE)){
                    addItemToCart(id);
            } else if (!isCancellationMode && item.getTxType() == TxType.UNLOAD_CREDIT){
                addItemToCart(id);
            } else if ((item.getPrice() != 0)  && isCancellationMode && item.getTxType() == TxType.CANCELATION){
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_disabled_in_cancelation_mode);
            } else if((item.getPrice() == 0)  && item.getTxType() == TxType.BUY_ARTICLE) {
                    if (dialogFragment == null || !dialogFragment.isVisible()) {
                        Bundle args = new Bundle();
                        args.putSerializable(ValuePickerDialog.BUNDLE_PROPERTY_ITEM, item);
                        args.putString(ValuePickerDialog.BUNDLE_PROPERTY_CURRENCY_SYMBOL, currencySymbol);
                        FragmentManager fm = getFragmentManager();
                        ValuePickerDialog vpc = new ValuePickerDialog();
                        dialogFragment = vpc;
                        vpc.setArguments(args);
                        vpc.show(fm, "fragment_value_picker");
                    }
            } else {
                AlertBox.allertOnWarning(ShopMainActivity.this, R.string.failed_title, R.string.hint_shopping_disabled_in_cancelation_mode);
            }
        }
    };


    private void addItemToCart(long id){
        ShoppingCartItem item = new ShoppingCartItem(shopInventory.get(id));
        addItemToCart(item);
    }

    private void addItemToCart(ShoppingCartItem item){
        long oldQuantity = 0;

        if(isCancellationMode && item.getTxType() == TxType.BUY_ARTICLE){
            item.setTxType(TxType.CANCELATION);
            ShoppingCartItem old = shoppingCart.get(item);
            oldQuantity = old != null? old.getQuantity() : 0;
            item.setTxType(TxType.BUY_ARTICLE);
        } else {
            ShoppingCartItem old = shoppingCart.get(item);
            oldQuantity = old != null? old.getQuantity() : 0;
        }

        item.setQuantity(oldQuantity + item.getQuantity());
        setCartItemQuantity(item);
    }


    private void setCartItemQuantity(ShoppingCartItem item){
        if(isCancellationMode && item.getTxType() == TxType.BUY_ARTICLE){
            item.setTxType(TxType.CANCELATION);
        }
        // increase shopping cart amount of this item

        shoppingCart.setItemQuantity(item);
        totalPrice.setText(DataConverter.longToCurrency(shoppingCart.calcTotalPrice()).toString());

        shopItemAdapter.notifyDataSetChanged();
        totalQuantity.setText(String.valueOf(shoppingCart.calcTotalItems()));
    }



    private OnItemLongClickListener onLongclickShopItem = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ShoppingCartItem plainItem = new ShoppingCartItem(shopInventory.get(id));
            ShoppingCartItem item = shoppingCart.get(plainItem);
            item = item != null ? item : plainItem;


            if (isCancellationMode && item.getTxType() != TxType.BUY_ARTICLE) {
                return false;
            } else if (!isCancellationMode && item.getTxType() == TxType.UNLOAD_CREDIT && !shoppingCart.isEmpty()) {
                return false;
            } else if(!isCancellationMode && item.isSecondCurrencyAllowed() && item.getTxType() == TxType.LOAD_CREDIT && !isBoAdmin){
                // only admins are allowed to load to second currency
                return false;
            } else if (!isCancellationMode && (item.getTxType() == TxType.LOAD_CREDIT)  && shoppingCart.hasItemOtherThanTxType(TxType.LOAD_CREDIT)){
                // dont mix loading with other articles
                return false;
            } else if (!isCancellationMode && (item.getTxType() == TxType.UNLOAD_CREDIT)  && shoppingCart.hasItemOtherThanTxType(TxType.UNLOAD_CREDIT)){
                // dont mix loading with other articles
                return false;
            } else if (!isCancellationMode && (item.getTxType() == TxType.CANCELATION || item.getTxType() == TxType.BUY_ARTICLE)  && shoppingCart.hasItemOtherThanTxType(TxType.BUY_ARTICLE, TxType.CANCELATION)){
                //  dont mix buying and pfand with others
                return false;
            } else if (!isCancellationMode && item.getTxType() == TxType.UNLOAD_CREDIT ) {
                if (dialogFragment == null || !dialogFragment.isVisible()) {
                    Bundle args = new Bundle();
                    args.putSerializable(ValuePickerDialog.BUNDLE_PROPERTY_ITEM, item);
                    args.putString(ValuePickerDialog.BUNDLE_PROPERTY_CURRENCY_SYMBOL, currencySymbol);
                    args.putLong(ValuePickerDialog.BUNDLE_PROPERTY_MAX_VALUE, ShopMainActivity.this.maxCashout);
                    FragmentManager fm = getFragmentManager();
                    ValuePickerDialog qpc = new ValuePickerDialog();
                    dialogFragment = qpc;
                    qpc.setArguments(args);
                    qpc.show(fm, "fragment_value_picker");
                }
                return true;
            } else {
                if (dialogFragment == null || !dialogFragment.isVisible()) {
                    Bundle args = new Bundle();
                    args.putSerializable(QuantityPickerDialog.BUNDLE_PROPERTY_ITEM, item);
                    args.putString(QuantityPickerDialog.BUNDLE_PROPERTY_CURRENCY_SYMBOL, currencySymbol);
                    FragmentManager fm = getFragmentManager();
                    QuantityPickerDialog qpc = new QuantityPickerDialog();
                    dialogFragment = qpc;
                    qpc.setArguments(args);
                    qpc.show(fm, "fragment_number_picker");
                }
                return true;
            }
        }
    };



    private OnClickListener onCancelPurchase = new OnClickListener(){

        @Override
        public void onClick(View v) {
            clearShoppingCart();

            Toast toast = Toast.makeText(ShopMainActivity.this, R.string.hint_shopping_cart_cleared, Toast.LENGTH_SHORT);
            toast.show();
        }
    };


    private SerializableOnClickListener onSubmitWhenBuyAndPayWithVirtual = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            virtualPaymentMethodDialog.show();
        }
    };

    private SerializableOnClickListener onSubmitWhenBuyWithChip = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            submitTradeWithChip(shoppingCart, true, PaymentMethod.PAYMENT_CHIP);
        }
    };

//    private SerializableOnClickListener onSubmitWhenBuyAndPayWithReal = new SerializableOnClickListener(){
//
//        @Override
//        public void onClick(DialogInterface dialogInterface, int i) {
//            cashPaymentMethodDialog.show();
//        }
//    };

    private SerializableOnClickListener onSubmitWheTopUpOrCashOut = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            topUpPaymentMethodDialog.show();
        }
    };

    private SerializableOnClickListener onSubmitWhenBuyAndPayWithAllSimple = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            allPaymentMethodSimpleDialog.show();
        }
    };

    private SerializableOnClickListener onSubmitWhenBuyAndPayWithAll = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            allPaymentMethodDialog.show();
        }
    };

    private SerializableOnClickListener onSubmitWhenCancellationWithCashAndChip = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            cancelationPaymentMethodDialog.show();
        }
    };

    private SerializableOnClickListener onSubmitWhenCancellationWithChip = new SerializableOnClickListener(){

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            submitTradeWithChip(shoppingCart, false, PaymentMethod.PAYMENT_CHIP);
        }
    };

    private OnClickListener onClickPurchaseButton = new OnClickListener(){
        @Override
        public void onClick(View v) {
            if (shoppingCart.size() > 0){

                boolean needsCashPayment = shoppingCart.needsCashPayment();
                boolean hasVoucherPayableItem = shoppingCart.hasVoucherPayableItem();
                boolean isAllwaysCashPayable = ConfigAccess.getIsPayableWithCash(ShopMainActivity.this);
                boolean creditOnlyOption = ConfigAccess.isPayableWithCreditOnly(ShopMainActivity.this);

                if (!isCancellationMode && needsCashPayment) { // for cashout and load currencies
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWheTopUpOrCashOut);
                } else if (!isCancellationMode && isAllwaysCashPayable && shoppingCart.hasItemOtherThanTxType() && creditOnlyOption && hasVoucherPayableItem) {
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenBuyAndPayWithAll);
                } else if (!isCancellationMode && isAllwaysCashPayable && shoppingCart.hasItemOtherThanTxType() && (!creditOnlyOption || !hasVoucherPayableItem)) {
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenBuyAndPayWithAllSimple);
                } else if (!isCancellationMode && creditOnlyOption && hasVoucherPayableItem/* && !ConfigAccess.getIsPayableWithCash(ShopMainActivity.this) */){
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenBuyAndPayWithVirtual);
                }
                  else if (!isCancellationMode && creditOnlyOption && !isAllwaysCashPayable && !hasVoucherPayableItem)/* && !ConfigAccess.getIsPayableWithCash(ShopMainActivity.this) */{
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenBuyWithChip);
                } else if (!isCancellationMode && !creditOnlyOption && !isAllwaysCashPayable)/* && !ConfigAccess.getIsPayableWithCash(ShopMainActivity.this) */{
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenBuyWithChip);
                }
                else if (isCancellationMode && isAllwaysCashPayable) {
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenCancellationWithCashAndChip);
                } else /* if (isCancellationMode && !isAllwaysCashPayable )*/{
                    dialogFragment = ShoppingCartDialog.openShoppingCartBeforePayment(shoppingCart, ShopMainActivity.this, onSubmitWhenCancellationWithChip);
                }

            } else {
                Toast toast = Toast.makeText(ShopMainActivity.this, R.string.hint_shopping_cart_empty, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };


    public ShopMainActivity(){
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_main);

        this.shopHintSelectItem = (TextView) findViewById(R.id.shop_item_select_hint);

        Intent intent = getIntent();
        this.keyA           = intent.getStringExtra(INTENT_EXTRA_CHIP_KEY_A);
        this.userID         = intent.getStringExtra(INTENT_EXTRA_USER_ID);
        this.eventID        = ConfigAccess.getEventID(this);

        if( intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_ADMIN,false) ){
            this.isBoAdmin = true; this.isBoSupervisor = true;
        }else if (intent.getBooleanExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, false)) {
            this.isBoSupervisor = true;
        }

        this.currencySymbol      = ConfigAccess.get1stCurrencySymbol(this);
        this.currencySymbol2     = ConfigAccess.get2ndCurrencySymbol(this);
        this.maxCredit1          = ConfigAccess.getMaxCredit1(this);
        this.maxCredit2          = ConfigAccess.getMaxCredit2(this);
        this.maxCashout          = ConfigAccess.getMaxCashOut(this);

        this.totalPrice = (TextView) findViewById(R.id.shop_total_price);
        this.totalQuantity = (TextView) findViewById(R.id.shop_total_quantity);
        this.submitPurchaseButton = (Button) findViewById(R.id.shop_submit_purchase);
        ((ViewGroup) findViewById(R.id.action_shopping_cart_link)).setOnClickListener(onClickShoppingCart);
        ((Button) findViewById(R.id.shop_cancel_purchase)).setOnClickListener(onCancelPurchase);

        this.submitPurchaseButton.setOnClickListener(onClickPurchaseButton);


        this.shopInventory = ConfigAccess.getArticleConfig(this);

        this.shopItemAdapter = new ShopItemAdapter(this,
                R.layout.tile_shop_item,
                R.id.list_element_shop_item,
                new LinkedList<>(shopInventory.values()),
                shoppingCart
                );

        GridView shopItemGrid = (GridView) findViewById(R.id.shop_item_grid);
        shopItemGrid.setAdapter(shopItemAdapter);
        shopItemGrid.setOnItemClickListener(onClickShopItem);
        shopItemGrid.setOnItemLongClickListener(onLongclickShopItem);

        virtualPaymentMethodDialog = createItemPaymentMethodDialog(new String[]{
                        this.getResources().getString(R.string.action_shopping_select_voucher_method_yes),
                        this.getResources().getString(R.string.action_shopping_select_voucher_method_no)},
                    onClickAllPaymentMethods);

        allPaymentMethodDialog = createItemPaymentMethodDialog(new String[]{
                        this.getResources().getString(R.string.action_shopping_select_voucher_method_yes),
                        this.getResources().getString(R.string.action_shopping_select_voucher_method_no),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_cash),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_card)},
                    onClickAllPaymentMethods);

        allPaymentMethodSimpleDialog = createItemPaymentMethodDialog(new String[]{
                        this.getResources().getString(R.string.action_shopping_select_payment_method_chip),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_cash),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_card)},
                onClickAllPaymentMethodsSimple
        );

//        cashPaymentMethodDialog = createItemPaymentMethodDialog(new String[]{
//                        this.getResources().getString(R.string.action_shopping_select_payment_method_cash),
//                        this.getResources().getString(R.string.action_shopping_select_payment_method_card)},
//                    onClickOfficialPaymentMethods);

        cancelationPaymentMethodDialog = createItemPaymentMethodDialog(new String[]{
                        this.getResources().getString(R.string.action_shopping_select_payment_method_chip),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_cash),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_card)},
                onClickCancellationPaymentMethods);

        topUpPaymentMethodDialog = createItemPaymentMethodDialog(new String[]{
                        this.getResources().getString(R.string.action_shopping_select_payment_method_cash),
                        this.getResources().getString(R.string.action_shopping_select_payment_method_card)},
                onClickTopUpPaymentMethods);
    }

    @Override
    protected void onPause(){
        super.onPause();

        ShoppingCartDialog.dismissDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_main, menu);
        this.shopModeButton = menu.findItem(R.id.action_toggle_cancellation_mode);
        return true;
    }


    private Dialog createItemPaymentMethodDialog(String[] paymentMethods, DialogInterface.OnClickListener onClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_shop_select_payment_method);
        builder.setItems(paymentMethods,onClick);
        return builder.create();
    }

    private DialogInterface.OnClickListener onClickAllPaymentMethods = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch(which){
                case 0:
                case 1:
                    submitTradeWithChip(shoppingCart, which == 0, PaymentMethod.PAYMENT_CHIP);
                    break;
                case 2:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CASH, R.string.hint_payed_by_cash, R.string.hint_cancelled_to_cash);
                    break;
                case 3:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CARD, R.string.hint_payed_by_card, R.string.hint_cancelled_to_card);
                    break;
                default:
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.title_shop_select_payment_method, R.string.hint_shop_select_payment_method);
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener onClickAllPaymentMethodsSimple = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch(which){
                case 0:
                    submitTradeWithChip(shoppingCart, true, PaymentMethod.PAYMENT_CHIP);
                    break;
                case 1:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CASH, R.string.hint_payed_by_cash, R.string.hint_cancelled_to_cash);
                    break;
                case 2:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CARD, R.string.hint_payed_by_card, R.string.hint_cancelled_to_card);
                    break;
                default:
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.title_shop_select_payment_method, R.string.hint_shop_select_payment_method);
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener onClickOfficialPaymentMethods = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch(which){
                case 0:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CASH, R.string.hint_payed_by_cash, R.string.hint_cancelled_to_cash);
                    break;
                case 1:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CARD, R.string.hint_payed_by_card, R.string.hint_cancelled_to_card);
                    break;
                default:
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.title_shop_select_payment_method, R.string.hint_shop_select_payment_method);
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener onClickCancellationPaymentMethods = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch(which){
                case 0:
                    submitTradeWithChip(shoppingCart, false, PaymentMethod.PAYMENT_CHIP);
                    break;
                case 1:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CASH, R.string.hint_payed_by_cash, R.string.hint_cancelled_to_cash);
                    break;
                case 2:
                    submitPurchaseWithRealMoney(shoppingCart, PaymentMethod.PAYMENT_CARD, R.string.hint_payed_by_card, R.string.hint_cancelled_to_card);
                    break;
                default:
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.title_shop_select_payment_method, R.string.hint_shop_select_method_not_supported);
                    break;
            }
        }
    };

    private DialogInterface.OnClickListener onClickTopUpPaymentMethods = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch(which){
                case 0:
                    submitTradeWithChip(shoppingCart, false, PaymentMethod.PAYMENT_CASH);
                    break;
                case 1:
                    submitTradeWithChip(shoppingCart, false, PaymentMethod.PAYMENT_CARD);
                    break;
                default:
                    AlertBox.allertOnWarning(ShopMainActivity.this, R.string.title_shop_select_payment_method, R.string.hint_shop_select_method_not_supported);
                    break;
            }
        }
    };

    /**
     * procedes shopping without a chip and handles the txLog
     * @param shoppingCart
     */
    private void submitPurchaseWithRealMoney(final ShoppingCart shoppingCart, final PaymentMethod pamentMethod, final int successMessageID, final int cancelMessageID){
        // check preconditions
        if (!checkAppState()) {
            showDisableMessage();
            disableApp();

        } else if(shoppingCart.isEmpty()) {
            AlertBox.allertOnWarning(ShopMainActivity.this, R.string.hint_shopping_failed, R.string.hint_shopping_cart_empty);
        } else {

            TxShopLogger txLogger = new TxShopLogger(this, userID);

            Collection<ShoppingCartItem> itemKeys = shoppingCart.values();
            for (ShoppingCartItem itemKey : itemKeys) {
                ShoppingCartItem item = shoppingCart.get(itemKey);
                long totalPerItem = item.getQuantity() * item.getPrice();


                // log to tmplog
                txLogger.addToTempLog(item.getTxType(), TxLogger.NO_UID, totalPerItem, 0, 0, pamentMethod, item.getPlu(), item.getQuantity(), item.getVat(), item.getTitle(), item.getPrice());
            }

            // persist log
            Log.d(LOG_TAG, "Trade was successful with cash");
            txLogger.saveLog(this);
            // clear log file
            txLogger.clearLog();

            // print out result
            printRealMoneyPaymentResult(shoppingCart.calcTotalItems(), shoppingCart.calcTotalPrice(), successMessageID, cancelMessageID);

        }
    }

    private void printRealMoneyPaymentResult(long totalAmount, long totalValue, int successMessageID, int cancelMessageID){

        String message = "";
        if (isCancellationMode){
            message += getResources().getString(cancelMessageID) + "\n";
        } else {
            message += getResources().getString(successMessageID)+ "\n";
        }
        message += "\n" + getResources().getString(R.string.title_shop_total_quantity) + " " + totalAmount +"\n";
        message += getResources().getString(R.string.hint_shop_total_price_title) + " " + DataConverter.longToCurrency(totalValue) + " " + currencySymbol;

        AlertBox.allertOnInfo(ShopMainActivity.this, R.string.hint_shopping_success, message, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        clearShoppingCart();
        switchToNormalShopMode();
    }


    /**
     * Opens a new Activity to scan and write the chip and handles tx log
     * @param shoppingCart
     * @param useVoucher
     */
    private void submitTradeWithChip(ShoppingCart shoppingCart, boolean useVoucher, PaymentMethod paymentMethod) {

        // check preconditions
        if (!checkAppState()) {
            showDisableMessage();
            disableApp();

        } else {
            Intent intent = new Intent(ShopMainActivity.this, ShopPayActivity.class);

            intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, this.keyA);
            intent.putExtra(INTENT_EXTRA_SHOPPING_CART, (Serializable) shoppingCart);
            intent.putExtra(INTENT_EXTRA_CHIP_MAX_CREDIT_1, this.maxCredit1);
            intent.putExtra(INTENT_EXTRA_CHIP_MAX_CREDIT_2, this.maxCredit2);
            intent.putExtra(INTENT_EXTRA_CHIP_MAX_CASHOUT, this.maxCashout);
            intent.putExtra(INTENT_EXTRA_SHOP_PAYMENT_METHOD, paymentMethod);
            intent.putExtra(INTENT_EXTRA_SHOP_USE_VOUCHER, useVoucher);
            intent.putExtra(INTENT_EXTRA_USER_ID, this.userID);
            intent.putExtra(INTENT_EXTRA_EVENT_ID, this.eventID);

            if(this.isBoAdmin){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_ADMIN, true);
            } else if (isBoSupervisor){
                intent.putExtra(INTENT_EXTRA_BO_ROLE_SUPERVISOR, true);
            } else {
                intent.putExtra(INTENT_EXTRA_BO_ROLE_EMPLOYEE, true);
            }

            Log.d(LOG_TAG, "Starting ShopPayActivity..");
            startActivityForResult(intent, INTENT_FOR_PAY_SHOP_ITEMS);
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // react on result
        switch (requestCode) {
        case INTENT_FOR_PAY_SHOP_ITEMS:
            // retrieve result details
            if (intent != null) {
                ShoppingErrorCause cause = (ShoppingErrorCause) intent.getSerializableExtra(INTENT_EXTRA_SHOP_ERROR_ID);

                if (cause == ShoppingErrorCause.CAUSE_INVALID_CRC) {
                    onCRCError();
                    break;
                } else if (cause == ShoppingErrorCause.CAUSE_INVALID_APP) {
                    onWrongAPPType();
                    break;                    
                } else {
                    long oldCredit1 = intent.getLongExtra(INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT1,Long.MIN_VALUE);
                    long oldCredit2 = intent.getLongExtra(INTENT_EXTRA_CHIP_FIELD_OLD_CREDIT2,Long.MIN_VALUE);
                    long lastingCredit1 = intent.getLongExtra(INTENT_EXTRA_CHIP_FIELD_CREDIT1,Long.MIN_VALUE);
                    long lastingCredit2 = intent.getLongExtra(INTENT_EXTRA_CHIP_FIELD_CREDIT2,Long.MIN_VALUE);
                    long voucherUsed    = intent.getLongExtra(INTENT_EXTRA_CHIP_USED_VOUCHER,0);

                    if (resultCode == RESULT_OK) {
                        onSuccessfullyPurchasedByChip(lastingCredit1, lastingCredit2, oldCredit1, oldCredit2, voucherUsed);
                        break;
                    } else {
                        onFailToPurchaseByChip(cause, oldCredit1, oldCredit2);
                        break;
                    }
                } 
            } else {
                onFailToPurchaseByChip(ShoppingErrorCause.CAUSE_UNKNOWN, Long.MIN_VALUE, Long.MIN_VALUE);
                break;
            }
        }
    }

    
    /**
     * After confirming a purchase, this is the reaction on a positive chip-reader-interaction 
     * (shopping was successful)   
     * @param newCredit1 remaining credit1 on the chip
     * @param newCredit2 remaining credit2 on the chip
     */
    private void onSuccessfullyPurchasedByChip(long newCredit1, long newCredit2, long oldCredit1, long oldCredit2, long voucherUsed) {
        
        String message = "";
        
        if (shoppingCart.hasItemWithTxType(TxType.UNLOAD_CREDIT)){
            long cashOutValue = (oldCredit1-newCredit1) + (oldCredit2-newCredit2);
            
            message = ShopMainActivity.this.getResources().getString(R.string.hint_money_to_cash_out) + "\n\n" + 
                    DataConverter.longToCurrency(cashOutValue) + " " + currencySymbol + "\n";
            
        } else {

            if (shoppingCart.hasItemWithTxType(TxType.BUY_ARTICLE)) {

                message += ShopMainActivity.this.getResources().getString(R.string.hint_remaining_payed);

                if ( newCredit1 != oldCredit1){
                    message += "\n " + DataConverter.longToCurrency(oldCredit1-newCredit1) + " " + currencySymbol + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_payed_with) + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_1);
                }

                if ( newCredit2 != oldCredit2){
                    message += "\n " + DataConverter.longToCurrency(oldCredit2-newCredit2) + " " + currencySymbol2 + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_payed_with) + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_2);
                }

                if (voucherUsed > 0) {
                    message += "\n " + voucherUsed + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_payed_with) + " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_voucher);
                }

                message += "\n\n";
            }

            message += ShopMainActivity.this.getResources().getString(R.string.hint_remaining_credits) + "\n" +
                    " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_1) + ": " + DataConverter.longToCurrency(newCredit1) + " " + currencySymbol + "\n" +
                    " " + ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_2) + ": " + DataConverter.longToCurrency(newCredit2) + " " + currencySymbol2 + "\n";
        }
        
        clearShoppingCart();
        switchToNormalShopMode();

        AlertBox.allertOnInfo(ShopMainActivity.this, R.string.hint_shopping_success, message, onConfirmPositiveShoppingResult);
    }

    
    /**
     * After confirming a purchase, this is the reaction on a negative chip-reader-interaction 
     * (aborting the process, or insufficient credits)   
     * @param lastingCredit1 remaining credit1 on the chip
     * @param lastingCredit2 remaining credit2 on the chip
     */
    private void onFailToPurchaseByChip(ShoppingErrorCause cause, long lastingCredit1, long lastingCredit2) {
        int messageID = getErrorMessageID(cause);
        
        String message = ShopMainActivity.this.getResources().getString(messageID) + "\n\n"; 
        if (lastingCredit1 != Long.MIN_VALUE && lastingCredit2 != Long.MIN_VALUE){
            message += ShopMainActivity.this.getResources().getString(R.string.hint_remaining_credits) + "\n" +
                    ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_1) + ": " + DataConverter.longToCurrency(lastingCredit1) + " " + currencySymbol + "\n" + 
                    ShopMainActivity.this.getResources().getString(R.string.hint_rfid_credit_2) + ": " + DataConverter.longToCurrency(lastingCredit2) + " " + currencySymbol2;
        }
        AlertBox.allertOnWarning(ShopMainActivity.this, R.string.hint_shopping_failed, message);
    }


    private android.content.DialogInterface.OnClickListener onConfirmPositiveShoppingResult = new android.content.DialogInterface.OnClickListener(){

        @Override
        public void onClick(DialogInterface dialog, int which) {
            // check preconditions
            if (!checkAppState()) {
                showDisableMessage();
                disableApp();
            } else {
                switchToNormalShopMode();
            }
        }
    };

    private void clearShoppingCart(){
        shoppingCart.clear();
        totalPrice.setText("");
        totalQuantity.setText("");
        shopItemAdapter.notifyDataSetChanged();
    }

    /**
     * After confirming a purchase, this is the reaction on a negative chip-reader-interaction 
     * (aborting the process, or insufficient credits)
     */
    private void onCRCError() {
        AlertBox.allertOnWarning(ShopMainActivity.this, R.string.hint_shopping_failed, R.string.hint_chip_invalid_crc);
    }
    
    private void onWrongAPPType() {
        AlertBox.allertOnWarning(ShopMainActivity.this, R.string.hint_shopping_failed, R.string.hint_chip_invalid_app);
    }



    /**
     * This will open the shopping cart when clicking on the price and amount summery
     */
    private OnClickListener onClickShoppingCart = new OnClickListener(){

        @Override
        public void onClick(View v) {
            ShoppingCartDialog.openShoppingCart(shoppingCart, ShopMainActivity.this);
        }

    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_toggle_cancellation_mode:
            toggleShoppingMode(item);
            break;
            
        case R.id.action_show_shopping_cart:
            Log.d(LOG_TAG, "Opening Shopping Cart..");
            ShoppingCartDialog.openShoppingCart(shoppingCart,this);
            break;

        case R.id.action_show_balance_check:
                Log.d(LOG_TAG, "Opening BalanceCheck View");
                openBalanceCheckView();
                break;
            
        default:
            return false;// super.onOptionsItemSelected(item); --TODO error
        }
        return true;
    }

    private void openBalanceCheckView(){
        Intent intent = new Intent(this, HelpDeskMainActivity.class);
        String keyA = ConfigAccess.getKeyA(this);
        intent.putExtra(INTENT_EXTRA_CHIP_KEY_A, keyA);
        intent.putExtra(INTENT_EXTRA_USER_ID, this.userID);
        intent.putExtra(INTENT_EXTRA_BO_ROLE_EMPLOYEE, true);
        intent.putExtra(INTENT_EXTRA_SHOW_BALANCE_ONLY, true);
        intent.putExtra(INTENT_EXTRA_MODE_NAME,this.getResources().getString(R.string.title_activity_balance_check));
        startActivity(intent);
    }
    
    protected void toggleShoppingMode(final MenuItem item) {
        Log.d(LOG_TAG, "Toggle cancellation mode.");

        // denie on missing permission
        if (!(isBoAdmin || isBoSupervisor)) {
            AlertBox.allertOnWarning(this, R.string.title_shopping_toggle_mode, R.string.hint_shopping_change_mode_denied);
        } else if (!isCancellationMode) {
            // ask user if he REALY wants to change.. shopping cart will be cleared
            AlertBox.allertOnRequest(this, R.string.title_shopping_toggle_mode,
                    R.string.hint_shopping_change_to_cancelation_mode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                switchToCancellationShopMode();
                                clearShoppingCart();
                            }
                        }
                    });
        } else {
            AlertBox.allertOnRequest(this, R.string.title_shopping_toggle_mode, R.string.hint_shopping_change_to_normalshop_mode,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                switchToNormalShopMode();
                                clearShoppingCart();
                            }
                        }
                    });
        }
    }

    /**
     * Switches shop mode (from buy-mode) to cancellation mode
     */
    private void switchToCancellationShopMode() {
        isCancellationMode = true;
        this.shopItemAdapter.setCancelationMode(isCancellationMode);
        this.shopModeButton.setIcon(R.drawable.ic_action_remove_active);
        this.shopModeButton.setChecked(true);
        this.submitPurchaseButton.setText(R.string.action_shop_submit_return);
        this.shopHintSelectItem.setText(R.string.title_shop_item_select_cancel);
    }

    /**
     * Switches shop mode (from cancellation mode) to normal shop mode (buy)
     */
    private void switchToNormalShopMode(){
        isCancellationMode = false;
        this.shopItemAdapter.setCancelationMode(isCancellationMode);
        this.shopModeButton.setIcon(R.drawable.ic_action_remove);
        this.shopModeButton.setChecked(false);
        this.submitPurchaseButton.setText(R.string.action_shop_submit_purchase);
        this.shopHintSelectItem.setText(R.string.title_shop_item_select_purchase);
    }
    

    /**
     * Acting on the result, when a item quantity was changed by an external dialog.
     * Changing the quantity of an item, or adding a new item, if it is not allready
     *  existing in the shopping cart
     * @param item shopping cart item which was changed.
     */
    @Override
    public void onSubmitQuantityChange(ShoppingCartItem item) {
        setCartItemQuantity(item);
    }

    
    /**
     * Acting on the result, when a item value was changed by an external dialog.
     * This item will be handled as new. so it will be added to the shopping cart.
     */
    @Override
    public void onSubmitValueChange(ShoppingCartItem item) {
        addItemToCart(item);
    }
    
    private int getErrorMessageID(ShoppingErrorCause cause){
        switch (cause){
        case CAUSE_INVALID_CRC:
            return R.string.hint_chip_invalid_crc;
        case CAUSE_INVALID_APP:
            return R.string.hint_chip_invalid_app;
        case CAUSE_INVALID_EVENT:
            return R.string.hint_chip_invalid_eventID;
        case CAUSE_CHIP_BANNED:
            return R.string.hint_shopping_failed_banned;
        case CAUSE_BUY_ARTICLE:
            return R.string.hint_shopping_insufficient_credit;
        case CAUSE_LOAD_CREDIT:
            return R.string.hint_shopping_credit_exceeds_maximum;
        case CAUSE_UNLOAD_CREDIT:
            return R.string.hint_shopping_unload_value_exceeds_maximum;
        case CAUSE_CANCELATION:
            return R.string.hint_shopping_cancelation_not_possible;
        case CAUSE_UNKNOWN:
            return R.string.hint_shopping_failed;
        default:
            return R.string.hint_shopping_failed;
        }
    }
    
}
