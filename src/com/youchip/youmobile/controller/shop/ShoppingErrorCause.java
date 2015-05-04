package com.youchip.youmobile.controller.shop;

import java.io.Serializable;

public enum ShoppingErrorCause implements Serializable{
    NO_ERROR,
    CAUSE_INVALID_CRC,
    CAUSE_INVALID_APP,
    CAUSE_INVALID_EVENT,
    CAUSE_CHIP_BANNED,
    CAUSE_BUY_ARTICLE,
    CAUSE_LOAD_CREDIT,
    CAUSE_UNLOAD_CREDIT,
    CAUSE_CANCELATION,
    CAUSE_UNKNOWN
}
