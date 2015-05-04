package com.youchip.youmobile.model.shop;

import com.youchip.youmobile.model.shop.ShopItemConfig;

public class ShoppingCartItem extends ShopItemConfig{

    private static final long serialVersionUID = -3417128438632853255L;
    private long quantity = 1;
    
    public ShoppingCartItem(){
        
    }
    
    public ShoppingCartItem(ShopItemConfig item){
        super(item);
    }
    
    public ShoppingCartItem(ShoppingCartItem item){
        super(item);
        this.quantity = item.getQuantity();
    }
    
    public long getQuantity(){
        return this.quantity;
    }
    
    public void setQuantity(long quantity){
        this.quantity = quantity;
    }
}
