package com.youchip.youmobile.controller.chipIO;

public interface SimpleProgressListener {

    /**
     * 
     * @param total the maximum number of steps
     * @param current -1 on error, otherwise from 0 up to the value of total
     */
    void listen(int total, int current);
}
