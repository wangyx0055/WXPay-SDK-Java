package com.github.wxpay;

import java.io.InputStream;

public interface WXPayConfig {


    /**
     * 获取App ID
     * @return App ID
     */
    public String getAppID();


    /**
     * 获取Mch ID
     * @return Mch ID
     */
    public String getMchID();


    /**
     * 获取API密钥
     * @return API密钥
     */
    public String getKey();


    /**
     * 获取商户证书内容
     * @return 商户证书内容
     */
    public InputStream getCertStream();


    /**
     * HTTP(S)请求超时时间，单位毫秒
     * @return 超时时间
     */
    public int getTimeOutMs();

}
