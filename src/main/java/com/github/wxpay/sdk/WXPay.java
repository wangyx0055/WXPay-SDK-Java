package com.github.wxpay.sdk;

import com.github.kevinsawicki.http.HttpRequest;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class WXPay {

    private WXPayConfig config;

    public WXPay(final WXPayConfig config) {
        this.config = config;
    }


    /**
     * 根据map数据生成HTTP post数据（XML格式）
     * @param reqData 向wxpay post的请求数据
     * @return XML格式的请求数据
     */
    public String makeRequestBody(Map<String, String> reqData) {
        reqData.put("appid", config.getAppID());
        reqData.put("mch_id", config.getMchID());
        reqData.put("nonce_str", WXPayUtil.generateNonceStr());
        return WXPayUtil.generateSignedXml(reqData, config.getKey());
    }

    /**
     * 判断xml数据的sign是否有效
     * @param reqData 向wxpay post的请求数据
     * @return 签名是否有效
     * @throws Exception
     */
    public boolean isSignatureValid(Map<String, String> reqData) throws Exception {
        return WXPayUtil.isSignatureValid(reqData, this.config.getKey());
    }


    /**
     * 不需要证书的请求
     * @param strUrl String
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithoutCert(String strUrl, Map<String, String> reqData, int timeoutMs) throws Exception {
        String reqBody = this.makeRequestBody(reqData);
        HttpRequest request = HttpRequest.post(strUrl)
                .connectTimeout(timeoutMs)
                .readTimeout(timeoutMs)
                .send(reqBody);
        if (request.code() != 200) {
            throw new Exception("HTTP response code is not 200");
        }

        return request.body();
    }


    /**
     * 需要证书的请求
     * @param strUrl String
     * @param reqData 向wxpay post的请求数据  Map
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public String requestWithCert(String strUrl, Map<String, String> reqData, int timeoutMs) throws Exception {
        HttpRequest.setConnectionFactory(new HttpRequest.ConnectionFactory() {

            public HttpURLConnection create(URL url) throws IOException {
                return create(url, null);
            }

            public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
                try {
                    char[] password = config.getMchID().toCharArray();
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    ks.load(config.getCertStream(), password);

                    // 实例化密钥库 & 初始化密钥工厂
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, password);

                    // 创建SSLContext
                    SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                    if (proxy != null)
                        return (HttpURLConnection) url.openConnection(proxy);
                    else
                        return (HttpURLConnection) url.openConnection();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                }
                throw new IOException("Error when init SSL");
            }
        });

        String reqBody = this.makeRequestBody(reqData);
        HttpRequest request = HttpRequest.post(strUrl)
                .connectTimeout(timeoutMs)
                .readTimeout(timeoutMs)
                .send(reqBody);
        if (request.code() != 200) {
            throw new Exception("HTTP response code is not 200");
        }

        return request.body();  // 别忘了验证sign
    }

    /**
     * 处理API返回数据，转换成Map对象。若有必要，验证签名。
     * @param xmlStr API返回的XML格式数据
     * @return Map类型数据
     * @throws Exception
     */
    public Map<String, String> processResponseXml(String xmlStr) throws Exception {
        String RETURN_CODE = "return_code";
        String return_code;
        Map<String, String> respData = WXPayUtil.xmlToMap(xmlStr);
        if (respData.containsKey(RETURN_CODE)) {
            return_code = respData.get(RETURN_CODE);
        }
        else {
            throw new Exception("No `return_code` in the response XML reqData");
        }

        if (return_code.equals(WXPayConstants.FAIL)) {
            return respData;
        }
        else if (return_code.equals(WXPayConstants.SUCCESS)) {
           if (this.isSignatureValid(respData)) {
               return respData;
           }
           else {
               throw new Exception("Invalid signature in XML");
           }
        }
        else {
            throw new Exception("Invalid XML. return_code value " + return_code + " is invalid");
        }
    }

    /**
     * 作用：提交刷卡支付<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> microPay(Map<String, String> reqData) throws Exception {
        return this.microPay(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：提交刷卡支付<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> microPay(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.MICROPAY_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData) throws Exception {
        return this.unifiedOrder(reqData, config.getTimeOutMs());
    }


    /**
     * 作用：统一下单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> unifiedOrder(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.UNIFIEDORDER_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData) throws Exception {
        return this.orderQuery(reqData, config.getTimeOutMs());
    }


    /**
     * 作用：查询订单<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据 int
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> orderQuery(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.ORDERQUERY_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData) throws Exception {
        return this.reverse(reqData, config.getTimeOutMs());
    }


    /**
     * 作用：撤销订单<br>
     * 场景：刷卡支付<br>
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> reverse(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithCert(WXPayConstants.REVERSE_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData) throws Exception {
        return this.closeOrder(reqData, config.getTimeOutMs());
    }


    /**
     * 作用：关闭订单<br>
     * 场景：公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> closeOrder(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.CLOSEORDER_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData) throws Exception {
        return this.refund(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：申请退款<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：需要证书
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refund(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithCert(WXPayConstants.REFUND_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData) throws Exception {
        return this.refundQuery(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：退款查询<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> refundQuery(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.REFUNDQUERY_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：对账单下载（成功时返回对账单数据，失败时返回XML格式数据）<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData) throws Exception {
        return this.downloadBill(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：对账单下载<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付<br>
     * 其他：无论是否成功都返回Map。若成功，返回的Map中含有return_code、return_msg、data，
     *      其中return_code为`SUCCESS`，data为对账单数据。
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return 经过封装的API返回数据
     * @throws Exception
     */
    public Map<String, String> downloadBill(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respStr = this.requestWithoutCert(WXPayConstants.DOWNLOADBILL_URL, reqData, timeoutMs).trim();
        Map<String, String> ret;
        // 出现错误，返回XML数据
        if (respStr.indexOf("<") == 0) {
            ret = WXPayUtil.xmlToMap(respStr);
        }
        else {
            // 正常返回csv数据
            ret = new HashMap<String, String>();
            ret.put("return_code", WXPayConstants.SUCCESS);
            ret.put("return_msg", "ok");
            ret.put("data", respStr);
        }
        return ret;
    }


    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData) throws Exception {
        return this.report(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：交易保障<br>
     * 场景：刷卡支付、公共号支付、扫码支付、APP支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> report(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.REPORT_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData) throws Exception {
        return this.shortUrl(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：转换短链接<br>
     * 场景：刷卡支付、扫码支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> shortUrl(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.SHORTURL_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData) throws Exception {
        return this.authCodeToOpenid(reqData, this.config.getTimeOutMs());
    }


    /**
     * 作用：授权码查询OPENID接口<br>
     * 场景：刷卡支付
     * @param reqData 向wxpay post的请求数据
     * @param timeoutMs 超时时间，单位是毫秒
     * @return API返回数据
     * @throws Exception
     */
    public Map<String, String> authCodeToOpenid(Map<String, String> reqData, int timeoutMs) throws Exception {
        String respXml = this.requestWithoutCert(WXPayConstants.AUTHCODETOOPENID_URL, reqData, timeoutMs);
        return this.processResponseXml(respXml);
    }


} // end class
