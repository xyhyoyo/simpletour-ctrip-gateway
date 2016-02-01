package com.simpletour.gateway.ctrip.config;

/**
 * Created by Mario on 2016/1/16.
 */
public class SysConfig {

    public static final String ORDER_HANDLER = "orderhandler";

    public static final String TOURISM_HANDLER = "tourismHandler";

    public static final String VERIFY_ORDER_METHOD = "VerifyOrder";

    public static final String CREATE_ORDER_METHOD = "CreateOrder";

    public static final String CANCEL_ORDER_METHOD = "CancelOrder";

    public static final String QUERY_ORDER_METHOD = "QueryOrder";

    public static final String RESEND_METHOD = "ReSend";

    public static final String QUERY_TOURISM_METHOD = "queryTourism";
    //0代表门票---产品
    public static final String PRODUCT_TYPE = "1";
    //1代表巴士+门票---行程
    public static final String TOURISM_TYPE = "0";
}
