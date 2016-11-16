package com.github.wxpay;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestWXPayUtil {


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void isSignValid() throws Exception {
        // 数据来自  https://pay.weixin.qq.com/wiki/doc/api/native.php?chapter=4_3
        String xmlStr = "<xml>\n" +
                "<appid>wxd930ea5d5a258f4f</appid>\n" +
                "<mch_id>10000100</mch_id>\n" +
                "<device_info>1000</device_info>\n" +
                "<body>test</body>\n" +
                "<nonce_str>ibuaiVcKdpRxkhJA</nonce_str>\n" +
                "<sign>9A0A8659F005D6984697E2CA0A9CF3B7</sign>\n" +
                "</xml>";
        String key = "192006250b4c09247ec02edce69f6a2d";
        assertEquals(true, WXPayUtil.isSignatureValid(xmlStr, key));
    }

    @Test
    public void genNonceStr() throws Exception {
        String r = WXPayUtil.generateNonceStr();
        assertFalse( r.contains("-") );
        assertTrue( r.length() <= 32 );
    }

    @Test
    public void MD5() throws Exception {
        assertEquals("47BCE5C74F589F4867DBD57E9CA9F808", WXPayUtil.MD5("aaa"));
    }

}