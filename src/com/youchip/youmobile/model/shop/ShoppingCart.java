package com.youchip.youmobile.model.shop;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import com.youchip.youmobile.controller.txlog.TxType;

public class ShoppingCart extends HashMap<ShoppingCartItem, ShoppingCartItem> implements Serializable{

    private static final long serialVersionUID = -4870188965853488992L;
    
    public ShoppingCart(){
        
    }
    
    public ShoppingCart(Map<ShoppingCartItem, ShoppingCartItem> shoppingCart){
        super(shoppingCart);
    }    
    
    public boolean add(ShoppingCartItem item){
        if(containsKey(item)){
            return false;
        } else {
            put(item,item);
            return true;
        }    
    }

    public void setItemQuantity(ShoppingCartItem item){
        ShoppingCartItem existing = get(item);
        if (item.getQuantity() > 0 && existing != null){
            existing.setQuantity(item.getQuantity());
        } else if (item.getQuantity() > 0) {
            put(item,item);
        } else if (item.getQuantity() == 0 && existing != null){
            remove(existing);
        }
    }
    
    public void addItemQuantity(ShoppingCartItem item){
        ShoppingCartItem existing = get(item);
        if (item.getQuantity() > 0 && existing != null){
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else if (item.getQuantity() > 0) {
            put(item,item);
        } else if (item.getQuantity() == 0 && existing != null){
            remove(existing);
        }       
    }
    
    
    public long calcTotalPrice(){
        long totalPrice = 0;
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            ShoppingCartItem  item = get(key);
            long quantity = item.getQuantity();
            long price    = item.getPrice();
            totalPrice += quantity*price;
        }
        return totalPrice;
    }
    

    public long calcTotalItems(){
        long totalItems = 0;
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            totalItems += get(key).getQuantity();
        }
        return totalItems;
    }
    
    
    public boolean needsCashPayment(){
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            TxType txType = get(key).getTxType();
            if ( txType == TxType.LOAD_CREDIT || txType == TxType.UNLOAD_CREDIT){
                return true;
            }
        }
        return false;
    }
    
    public boolean hasItemWithTxType(TxType ... txTypes){
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            for(TxType txType:txTypes){
                if(txType == get(key).getTxType())
                    return true;
            }
        }
        
        return false;
    }

    public boolean hasVoucherPayableItem(){
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            ShoppingCartItem value = get(key);
            if(TxType.BUY_ARTICLE == value.getTxType() && value.isVoucherAllowed()) {
                return true;
            }
        }
        return false;
    }

    public Set<ShoppingCartItem> getVoucherPayableItems(){
        Set<ShoppingCartItem> voucherPayableItems = new LinkedHashSet<>();
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            ShoppingCartItem value = get(key);
            if(TxType.BUY_ARTICLE == value.getTxType() && value.isVoucherAllowed()) {
                voucherPayableItems.add(value);
            }
        }
        return voucherPayableItems;
    }
    
    /**
     * returns true if the Shopping cart contains items which tx type is not 
     * one of the given txTypes
     * @param txTypes The txTypes which may not be in the shopping cart
     * @return true if there are articles with other tx types
     */
    public boolean hasItemOtherThanTxType(TxType ... txTypes){
        Set<ShoppingCartItem> keys = keySet();
        for(ShoppingCartItem key:keys){
            TxType itemTx = get(key).getTxType();

            boolean containsTx = false;
            for (TxType txType:txTypes){
                if(txType == itemTx)
                    containsTx = true;
            }
            if(!containsTx) return true;
        }
        
        return false;
    }


   
}
