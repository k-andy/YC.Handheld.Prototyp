package com.youchip.youmobile.model.network.response;


import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ksoap2.serialization.SoapObject;

import com.youchip.youmobile.controller.network.serviceInterface.SOAPResponse;
import com.youchip.youmobile.model.shop.VoucherInfo;
import com.youchip.youmobile.utils.DataConverter;

/**
 * This class represents the answer of the shop config soap service
 * @author muelleco
 *
 */

public class VoucherNamesSOAPResponse implements SOAPResponse, Serializable{

    private static final long serialVersionUID = 2176771730544878313L;
    private final Map<Long, VoucherInfo> list = new LinkedHashMap<>();
    private final String dateFormat    = DataConverter.getServiceDateFormatString();

    public VoucherNamesSOAPResponse(){

    }

    public VoucherNamesSOAPResponse(SoapObject response){

        setSOAPResponse(response);
    }

    @Override
    public void setSOAPResponse(SoapObject response) {
        final int m = response.getPropertyCount();
        for (int i=0; i < m ; i++){
            SoapObject voucher = (SoapObject) response.getProperty(i);

            long key        = Long.parseLong(voucher.getPrimitivePropertyAsString("Key"));
            String name    = voucher.getPrimitivePropertyAsString("Value");

            String timeStart  = voucher.getPrimitivePropertySafelyAsString(VoucherConfigFields.GueltigVon.toString());
            String timeStop   = voucher.getPrimitivePropertySafelyAsString(VoucherConfigFields.GueltigBis.toString());

            Date startValid   = DataConverter.serviceFormatToJavaDate(timeStart, dateFormat);
            Date stopValid    = DataConverter.serviceFormatToJavaDate(timeStop, dateFormat);

            this.list.put(key, new VoucherInfo(key, name, startValid, stopValid));
        }
    }

    public Map<Long, VoucherInfo> getResultMap(){
        return list;
    }

    private enum VoucherConfigFields{
        Key,
        Value,
        GueltigVon,
        GueltigBis,
    }

}