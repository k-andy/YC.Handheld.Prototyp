package com.youchip.youmobile.model.network;

public enum BasicSOAPConfigFields {
    DEVICE_IP, /** The IP of the handheld. used to determine the needed configurations the service will send back*/ 
    SHARED_SECRET, /** a password to encrypt the message and authorisize the device to talk to the service */
    
    IDORGANIZER, 
    IDEVENT,    /** ID of the current running event */
    
    LOGSYNCINTERVAL, /**Interval in which the logfiles will be send to the service*/
    LOGGINGSYNCSERVERIP,
    LOGGINGSYNCSERVERPORTNR,
    LOGFILEMAXSIZE,
    
    BLACKLISTUPDATEINTERVAL, /**Interval in which the the black list will be refreshed*/
    
    VOUCHERMODIFIER,
    FIRSTLEVELSEPARATOR,
    DATEFORMATSTRING, /** the format how to interpret the date/time data, received from the service*/
    
    CONFIGUPDATEINTERVAL, /**interval in which this configuration will be automatically updated*/
    SVCSTANDBY,
    KEY_A,
    CONFIGKEYA,
    CHIP_KEY_AES,
    DEVICE_ENABLED, /** enables/ disbales the device functionality*/
    
    CHIPRESULTDISPLAYDELAY, /** the time (in ms) how long to display a chip-interaction result*/
    CHIPIOENABLEDELAY,  /** delay between activating rfid adapter and starting to scan */
    CHIPIORETRYDELAY, /** delay beetween attempts to scan from a chip */
    
    GATEACCESSDELAY,  /** delay in min when a user can reenter a zone without regular checkout */
    
    FIRSTCURRENCYTEXT,
    SECONDCURRENCYTEXT,
    MAX1STCUR,
    MAX2NDCUR,
    MAX1STCASHOUT,
    
    ARTICLECOLORDEFAULT,
    ARTICLECOLORLOAD,
    ARTICLECOLORUNDLOAD,
    ARTICLECOLORCANCEL,
    ARTICLEFONTSIZE,

    USEPRODUCTGROUPASVOUCHER,
    WAKETIMEREADERSCREEN,

    ARTICLE_PAYABLE_CASH,
    PAY_CREDIT_ONLY_OPTION,
    IGNORE_VOUCHER_VALIDITY,

    TICKET_XC_SELECT_TAB,
    TICKET_XC_CHECK_TAB
}
