package com.qq.weixin.pay;

/**
 * 常量
 */
public class WXPayConstants {

    public static final String FAIL     = "FAIL";
    public static final String SUCCESS  = "SUCCESS";
    public static final String SIGN     = "sign";

    public static String MICROPAY_URL     = "https://api.mch.weixin.qq.com/pay/micropay";
    public static String UNIFIEDORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
    public static String ORDERQUERY_URL   = "https://api.mch.weixin.qq.com/pay/orderquery";
    public static String REVERSE_URL      = "https://api.mch.weixin.qq.com/secapi/pay/reverse";
    public static String CLOSEORDER_URL   = "https://api.mch.weixin.qq.com/pay/closeorder";
    public static String REFUND_URL       = "https://api.mch.weixin.qq.com/secapi/pay/refund";
    public static String REFUNDQUERY_URL  = "https://api.mch.weixin.qq.com/pay/refundquery";
    public static String DOWNLOADBILL_URL = "https://api.mch.weixin.qq.com/pay/downloadbill";
    public static String REPORT_URL       = "https://api.mch.weixin.qq.com/pay/report";
    public static String SHORTURL_URL     = "https://api.mch.weixin.qq.com/tools/shorturl";
    public static String AUTHCODETOOPENID_URL = "https://api.mch.weixin.qq.com/tools/authcodetoopenid";

}