package com.youchip.youmobile.model.shop;

import java.io.Serializable;

import com.youchip.youmobile.controller.txlog.TxType;
import static com.youchip.youmobile.controller.txlog.TxType.*;



   public class ShopItemConfig implements Serializable, Comparable<ShopItemConfig>{

        private static final long serialVersionUID = 8775390212565042365L;
        
        private long    plu;
        private String  title;
        private long    price;
        private TxType  txType;
        private long    productGroup;
        private boolean isFirstCurrencyAllowed;
        private boolean isSecondCurrencyAllowed;
        private boolean isVoucherAllowed;


        /**
         * Basic Constructor for the shop config data.
         */
        public ShopItemConfig(){
            // nothing to do here
        }
        
        
        /**
         * Copy constructor for the shop config data.
         * @param shopConfig configruation object, you want to make a deep copy
         */
        public ShopItemConfig(ShopItemConfig shopConfig){
            this.plu                       = shopConfig.plu;                                     
            this.title                     = shopConfig.title;                 
            this.price                     = shopConfig.price;                 
            this.txType                    = shopConfig.txType;                    
            this.isFirstCurrencyAllowed    = shopConfig.isFirstCurrencyAllowed;
            this.isSecondCurrencyAllowed   = shopConfig.isSecondCurrencyAllowed;      
            this.isVoucherAllowed          = shopConfig.isVoucherAllowed;
            this.productGroup              = shopConfig.getProductGroup();
        }


        public long getPlu() {
            return plu;
        }

        public String getTitle() {
            return title;
        }

        public long getPrice() {
            if (txType.equals(UNLOAD_CREDIT) || txType.equals(CANCELATION)) {
                return price*(-1);
            } else {
              return price;  
            }
        }

        public TxType getTxType() {
            return txType;
        }

        public boolean isFirstCurrencyAllowed() {
            return isFirstCurrencyAllowed;
        }

        public boolean isSecondCurrencyAllowed() {
            return isSecondCurrencyAllowed;
        }

        public boolean isVoucherAllowed(){
            return isVoucherAllowed;
        }


        public void setPlu(long plu) {
            this.plu = plu;
        }


        public void setTitle(String title) {
            this.title = title;
        }


        public void setPrice(long price) {
            this.price = price;
        }


        public void setTxType(TxType txType) {
            this.txType = txType;
        }


        public void setFirstCurrencyAllowed(boolean isFirstCurrencyAllowed) {
            this.isFirstCurrencyAllowed = isFirstCurrencyAllowed;
        }


        public void setSecondCurrencyAllowed(boolean isSecondCurrencyAllowed) {
            this.isSecondCurrencyAllowed = isSecondCurrencyAllowed;
        }


        public void setVoucherAllowed(boolean isVoucherAllowed) {
            this.isVoucherAllowed = isVoucherAllowed;
        }
        
        
        public long getProductGroup() {
            return productGroup;
        }


        public void setProductGroup(long productGroup) {
            this.productGroup = productGroup;
        }


        @Override
        public int hashCode(){
            int hash = 17;
            final int prime = 37;    
            
            hash = prime*hash + (int)(this.plu ^ (this.plu >>> 32));
            hash = prime*hash + (int)(this.price ^ (this.price >>> 32));
            hash = prime*hash + (this.txType != null ? this.txType.hashCode() : 0);
            
            return hash;
        }
        
        @Override
        public boolean equals(Object object){
            if (!ShopItemConfig.class.isAssignableFrom(object.getClass())){
                return false;
            } else {
            
            ShopItemConfig item = (ShopItemConfig) object;
                return item.plu == this.plu && item.txType == this.txType && item.price == this.price;
            }
        }


        @Override
        public int compareTo(ShopItemConfig another) {
           
            if (another.getPlu() == getPlu())
                return 0;
            
            if(getTxType() == another.getTxType() )
                return getTitle().compareTo(another.getTitle()); 

            
            if(getTxType() == TxType.UNLOAD_CREDIT && another.getTxType() == TxType.LOAD_CREDIT)
                return +1;
            
            if(getTxType() == TxType.CANCELATION && another.getTxType() == TxType.UNLOAD_CREDIT)
                return +1;
            if(getTxType() == TxType.CANCELATION && another.getTxType() == TxType.LOAD_CREDIT)
                return +1;
            

            if(getTxType() == TxType.BUY_ARTICLE && another.getTxType() == TxType.LOAD_CREDIT)
                return +1;
            if(getTxType() == TxType.BUY_ARTICLE && another.getTxType() == TxType.UNLOAD_CREDIT)
                return +1;
            if(getTxType() == TxType.BUY_ARTICLE && another.getTxType() == TxType.CANCELATION)
                return +1;
            
            return -1;
        }
        
    }