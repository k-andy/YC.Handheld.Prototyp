package com.youchip.youmobile.model.network.response;


import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;
import com.youchip.youmobile.controller.txlog.TxType;
import com.youchip.youmobile.model.shop.ShopItemConfig;

import org.ksoap2.serialization.SoapObject;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
/**
 * This class represents the answer of the shop config soap service
 * @author muelleco
 *
 */

public class ShopConfigSOAPResponse implements SOAPResponse, Serializable{

    private static final long serialVersionUID = 2176771730544878313L;
    private Map<Long,ShopItemConfig> articleList = new LinkedHashMap<>();

    public ShopConfigSOAPResponse(){

    }

    public ShopConfigSOAPResponse(SoapObject response){
        setSOAPResponse(response);
    }

    @Override
    public void setSOAPResponse(SoapObject response) {
        try {
            final int m = response.getPropertyCount();
            for (int i = 0; i < m; i++) {

                SoapObject soapGateConfig = (SoapObject) response.getProperty(i);
                ShopItemConfig shopItemConfig = new ShopItemConfig();

                shopItemConfig.setVat(Double.parseDouble(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.VATType.toString())));
                shopItemConfig.setPlu(Long.parseLong(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.PLU.toString())));
                shopItemConfig.setTitle(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.Titel.toString()));
                shopItemConfig.setPrice(Math.abs(Long.parseLong(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.Price.toString()))));
                shopItemConfig.setTxType(TxType.TxTypeFromInteger(Integer.parseInt(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.TXType.toString()))));
                shopItemConfig.setFirstCurrencyAllowed(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.FirstCurrencyAllowed.toString()).equals("true"));
                shopItemConfig.setSecondCurrencyAllowed(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.SecondCurrencyAllowed.toString()).equals("true"));
                shopItemConfig.setVoucherAllowed(soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.VoucherAllowed.toString()).equals("true"));

                long productGroupId = getProductGroup(soapGateConfig);
                shopItemConfig.setProductGroup(productGroupId);

                this.articleList.put(shopItemConfig.getPlu(), shopItemConfig);

//                if (productGroupId > 0) {
//                    if (useProductGroupsForVoucher) {
//                        this.groupList.put(productGroupId, soapGateConfig.getPrimitivePropertyAsString(ShopConfigFields.ProductGroupTitel.toString()));
//                    } else {
//                        this.groupList.put(shopItemConfig.getPlu(), shopItemConfig.getTitle());
//                    }
//                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Invalid shop config received!");
        }
    }

    private long getProductGroup(SoapObject soapGateConfig){
        long productGroup = 0;
        Object pg = soapGateConfig.getPrimitiveProperty(ShopConfigFields.ProductGroup.toString());
        if (pg != null){
            try {
                productGroup = Long.parseLong(pg.toString());
            } catch (NumberFormatException nfe){
                //keep the group id as 0
            }
        }

        return productGroup;
    }
     /**
      * Returns the result in a map
      * @return a map of shop items (mapped to its plu id)
      */
    public Map<Long,ShopItemConfig> getArticleConfig(){
        return articleList;
    }


    /**
     * Key-Names of the items in the service.
     * If the service changes, you have to change
     * this enum too. (case sensitive)
     * @author muelleco
     *
     */
    private enum ShopConfigFields{
        PLU,
        Titel,
        Price,
        TXType,
        ProductGroup,
        ProductGroupTitel,

        VATType,

        FirstCurrencyAllowed,
        SecondCurrencyAllowed,
        VoucherAllowed,
    }

}
