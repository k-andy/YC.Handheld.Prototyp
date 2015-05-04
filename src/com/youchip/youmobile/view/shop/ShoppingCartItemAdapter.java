package com.youchip.youmobile.view.shop;

import java.util.Collections;
import java.util.List;

import com.youchip.youmobile.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.youchip.youmobile.model.shop.ShoppingCartItem;
import com.youchip.youmobile.utils.DataConverter;

public class ShoppingCartItemAdapter extends BaseAdapter implements ListAdapter{
    
    private LayoutInflater inflater;
    private final List<ShoppingCartItem> itemList;
    private final int rowViewRID;
    private final int itemPriceRID;
    private final int itemInfoRID;
    private final int itemQuantityRID;
//    private final String currencySymbol;
    
    private int colorBuy;
    private int colorLoad;
    private int colorUnLoad;
    private int colorCancel;

    private float fontSize;
    
    public ShoppingCartItemAdapter(final Context context, int rowViewRID, int itemInfoRID, int itemQuantityRID, int itemPriceRID, final List<ShoppingCartItem> itemList, String currencySymbol){
        super();
        this.inflater   = LayoutInflater.from(context);
        this.itemList   = itemList;
        this.rowViewRID = rowViewRID;
        this.itemInfoRID = itemInfoRID;
        this.itemPriceRID = itemPriceRID;
        this.itemQuantityRID = itemQuantityRID;
//        this.currencySymbol = currencySymbol;
        
        this.colorBuy = context.getResources().getColor(R.color.shop_item_standard);
        this.colorLoad = context.getResources().getColor(R.color.shop_item_load_credit);
        this.colorUnLoad = context.getResources().getColor(R.color.shop_item_unload_credit);
        this.colorCancel = context.getResources().getColor(R.color.shop_item_cancelation);
        
        fontSize = context.getResources().getDimension(R.dimen.dialog_fragment_font_size_tiny);
        
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
        
        if (element == null){ //if the view is not cashed
            //inflate the common view from xml file
            element = this.inflater.inflate(getChildView(), parent, false);
            holder = new ShopItemHolder();
            holder.itemInfo     = (TextView) element.findViewById(itemInfoRID);
            holder.itemQuantity = (TextView) element.findViewById(itemQuantityRID);
            holder.itemPrice    = (TextView) element.findViewById(itemPriceRID);
            
            
            element.setTag(holder);
        } else {
            holder = (ShopItemHolder) element.getTag();
        }
        
        ShoppingCartItem shopItem = (ShoppingCartItem) getItem(position);
        
        // fill element with data
        int color;
        switch (shopItem.getTxType()){
            case BUY_ARTICLE:
                color = colorBuy;
                break;
            case LOAD_CREDIT: 
                color = colorLoad;
                break;
            case UNLOAD_CREDIT:
                color = colorUnLoad;
                break;
            case CANCELATION:
                color = colorCancel;
                break;
            default:
                color = colorBuy; 
                break;            
        }
        
        holder.itemInfo.setText(shopItem.getTitle()  + " (" + DataConverter.longToCurrency(shopItem.getPrice()) + ")");
        holder.itemInfo.setBackgroundColor(color);
        holder.itemInfo.setTextSize(fontSize);
        
        holder.itemQuantity.setText("x " + shopItem.getQuantity());
        holder.itemQuantity.setBackgroundColor(color);
        holder.itemQuantity.setTextSize(fontSize);
        
        holder.itemPrice.setText("" + DataConverter.longToCurrency(shopItem.getPrice() * shopItem.getQuantity()));
        holder.itemPrice.setBackgroundColor(color);
        holder.itemPrice.setTextSize(fontSize);
        
        return element;
    }
    
    protected Integer getChildView() {
        return rowViewRID;
    }
    
    
    static class ShopItemHolder{
        TextView itemInfo;
        TextView itemQuantity;
        TextView itemPrice;
    }


    public int getColorBuy() {
        return colorBuy;
    }

    public int getColorLoad() {
        return colorLoad;
    }

    public int getColorUnLoad() {
        return colorUnLoad;
    }

    public int getColorCancel() {
        return colorCancel;
    }

    public void setColorBuy(int colorBuy) {
        this.colorBuy = colorBuy;
    }

    public void setColorLoad(int colorLoad) {
        this.colorLoad = colorLoad;
    }

    public void setColorUnload(int colorUnLoad) {
        this.colorUnLoad = colorUnLoad;
    }

    public void setColorCancel(int colorCancel) {
        this.colorCancel = colorCancel;
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }
    


}
