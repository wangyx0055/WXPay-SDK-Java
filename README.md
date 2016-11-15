微信支付 Java SDK
------

对[微信支付开发者文档](https://pay.weixin.qq.com/wiki/doc/api/index.html)中给出的API进行了封装。

com.qq.weixin.pay.WXPay类下提供了对应的方法：

|方法名 | 说明 |
|--------|--------|
|microPay| 刷卡支付 |
|unifiedOrder | 统一下单|
|orderQuery | 查询订单 |
|reverse | 撤销订单 |
|closeOrder|关闭订单|
|refund|申请退款|
|refundQuery|查询退款|
|downloadBill|下载对账单|
|report|交易保障|
|shortUrl|转换短链接|
|authCodeToOpenid|授权码查询openid|

参数为`Map<String, String>`对象，返回类型也是`Map<String, String>`。
方法内部会将参数会转换成含有`appid`、`mch_id`、`nonce_str`和`sign`的XML；
通过HTTPS请求得到返回数据后会对其做必要的处理（例如验证签名，签名错误则抛出异常）。

对于downloadBill，无论是否成功都返回Map，且都含有`return_code`和`return_msg`。
若成功，其中`return_code`为`SUCCESS`，另外`data`对应对账单数据。


## 安装
通过maven，具体待定。

## 示例
MyConfig.java:
```java
import com.qq.weixin.pay.WXPayConfig;
import java.io.*;

public class MyConfig implements WXPayConfig{

    private byte[] certData;

    public MyConfig() throws Exception {
        String certPath = "/path/to/apiclient_cert.p12";
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    public String getAppID() {
        return "wx8888888888888888";
    }

    public String getMchID() {
        return "12888888";
    }

    public String getKey() {
        return "88888888888888888888888888888888";
    }

    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    public int getTimeOutMs() {
        return 10000;
    }
}
```

WXPayExample.java:
```java
import com.qq.weixin.pay.WXPay;

import java.util.HashMap;
import java.util.Map;

public class WXPayExample {

    private WXPay wxpay;
    private MyConfig config;

    public WXPayExample() throws Exception {
        config = new MyConfig();
        wxpay = new WXPay(config);
    }

    /**
     * 扫码支付  下单
     */
    public void doUnifiedOrder() {
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("body", "腾讯充值中心-QQ会员充值");
        data.put("out_trade_no", "2016090910595900000012");
        data.put("device_info", "");
        data.put("fee_type", "CNY");
        data.put("total_fee", ""+1);
        data.put("spbill_create_ip", "123.12.12.123");
        data.put("notify_url", "http://www.example.com/wxpay/notify");
        data.put("trade_type", "NATIVE");
        data.put("product_id", "12");

        try {
            Map<String, String> r = wxpay.unifiedOrder(data);
            System.out.println(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## Java Doc

见 。。。

## License
BSD