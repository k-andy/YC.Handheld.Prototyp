package com.youchip.youmobile.view.shop;

import java.util.Collections;
import java.util.List;

import com.youchip.youmobile.R;
import com.youchip.youmobile.controller.txlog.TxType;
import com.youchip.youmobile.model.shop.ShopItemConfig;
import com.youchip.youmobile.model.shop.ShoppingCart;
import com.youchip.youmobile.model.shop.ShoppingCartItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ShopItemAdapter extends BaseAdapter{
    
    private LayoutInflater inflater;
    private List<ShopItemConfig> itemList;
    private ShoppingCart shoppingCart;
    private int childView;
    private int elementResourceId;
    private Context context;
    private boolean isCancelationMode = false;
    
    public ShopItemAdapter(Context context, int childView, int elementResourceId, List<ShopItemConfig> itemList, ShoppingCart shoppingCart){
        super();
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.itemList = itemList;
        this.childView = childView;
        this.elementResourceId = elementResourceId;
        this.shoppingCart = shoppingCart;
        
        Collections.sort(this.itemList);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getPlu();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View element = convertView;
        ShopItemHolder holder = null;

        // creates a new holder item only if there is no old to recycle (saves cpu)
        if (element == null){ //if the view is not cashed
            //inflate the common view from xml file
            element = this.inflater.inflate(getChildView(), parent, false);
            holder = new ShopItemHolder();
            holder.shopItem = (ShopItemTextView) element.findViewById(elementResourceId);
            element.setTag(holder);
        } else {
            holder = (ShopItemHolder) element.getTag();
        }
        
        ShopItemConfig shopItem = (ShopItemConfig) getItem(position);
        setColorScheme(holder, shopItem);
        setQuantity(holder, shopItem);
        return element;
    }

    /**
     * Sets the colour scheme depending on the tx type of the shop item
     * @param holder the view holder which is a visual representation of the shop item
     * @param shopItem the shop item to display
     */
    private void setColorScheme(ShopItemHolder holder, ShopItemConfig shopItem){
        // fill element with data
        int shapeID;
        int fontColorID;

        switch (shopItem.getTxType()){
            case BUY_ARTICLE:
                shapeID = isCancelationMode ? R.drawable.grid_selector_cancelation: R.drawable.grid_selector_standard;
                fontColorID = R.color.shop_item_font_enabled;
                break;
            case CANCELATION:
                shapeID = isCancelationMode ? R.drawable.grid_selector_disabled: R.drawable.grid_selector_unload;
                fontColorID = isCancelationMode ? R.color.shop_item_font_disabled: R.color.shop_item_font_enabled;
                break;
            case LOAD_CREDIT:
                shapeID = isCancelationMode ? R.drawable.grid_selector_disabled: R.drawable.grid_selector_load;
                fontColorID = isCancelationMode ? R.color.shop_item_font_disabled: R.color.shop_item_font_enabled;
                break;
            case UNLOAD_CREDIT:
                shapeID = isCancelationMode ? R.drawable.grid_selector_disabled: R.drawable.grid_selector_unload;
                fontColorID = isCancelationMode ? R.color.shop_item_font_disabled: R.color.shop_item_font_enabled;
                break;
            default:
                shapeID = isCancelationMode ? R.drawable.grid_selector_disabled: R.drawable.grid_selector_unload;
                fontColorID = isCancelationMode ? R.color.shop_item_font_disabled: R.color.shop_item_font_enabled;
                break;
        }

        holder.shopItem.setText(shopItem.getTitle());
        holder.shopItem.setBackgroundDrawable(context.getResources().getDrawable(shapeID));
        holder.shopItem.setTextColor(context.getResources().getColor(fontColorID));
    }

    /**
     * Sets the quantity counter on screen and respects special
     * options like individual prices per PLU or the cancelation mode
     * @param holder the view holder which is a visual representation of the shop item
     * @param shopItem the shop item to display
     */
    private void setQuantity(ShopItemHolder holder, ShopItemConfig shopItem){
        //get quantity
        ShoppingCartItem cartItem = shoppingCart.get(shopItem);
        long quantity = 0;
        // set correct tx-type for search

        if (!isCancelationMode && shopItem.getTxType() == TxType.UNLOAD_CREDIT) {
            if (shoppingCart.hasItemWithTxType(TxType.UNLOAD_CREDIT)) {
                quantity = -1;
            }
        } else if (isCancelationMode && shopItem.getTxType() == TxType.BUY_ARTICLE){
            shopItem.setTxType(TxType.CANCELATION);
            cartItem = shoppingCart.get(shopItem);
            shopItem.setTxType(TxType.BUY_ARTICLE);
            quantity = cartItem != null ? cartItem.getQuantity() : 0;
        } else if (cartItem != null) {
            quantity = cartItem.getQuantity();
        }

//        if (cartItem != null){
            holder.shopItem.setQuantity(quantity);
//        } else if (holder.shopItem.getQuantity() != 0){
//            holder.shopItem.setQuantity(0);
//        }
    }
    
    protected Integer getChildView() {
        return childView;
    }
    
    
    static class ShopItemHolder{
        ShopItemTextView shopItem;
    }
    
    public boolean isCancelationMode(){
        return isCancelationMode;
    }
    
    public void setCancelationMode(boolean mode){          
        this.isCancelationMode = mode;
    }

}
