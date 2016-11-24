package com.github.wxpay.sdk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.security.MessageDigest;

import org.jdom2.Element;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;


public class WXPayUtil {

    /**
     * XML格式字符串转换为Map
     * @param strXML XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        InputStream stream = null;
        stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(stream);
        Element root = document.getRootElement();
        if (!root.getName().equals("xml")) {
            throw new Exception("XML root \""+ root.getName() +"\" is invalid");
        }
        List<Element> list = root.getChildren();
        HashMap<String, String> data = new HashMap<String, String>();
        for (Element node : list) {
            data.put(node.getName(), node.getTextTrim());
        }
        return data;
    }


    /**
     * 将Map转换为XML格式的字符串
     * @param data Map类型数据
     * @return String
     */
    public static String mapToXml(Map<String, String> data) {
        Element root=new Element("xml");
        Document doc=new Document();
        for(String k:data.keySet()) {
            Element child=new Element(k);
            child.addContent(data.get(k));
            root.addContent(child);
        }
        doc.setRootElement(root);
        XMLOutputter xmOut = new XMLOutputter(Format.getPrettyFormat());
        return xmOut.outputString(doc);
    }


    /**
     * 生成带有sign的XML格式字符串
     * @param data Map类型数据
     * @param key API密钥
     * @return 含有sign字段的XML
     */
    public static String generateSignedXml(final Map<String, String> data, String key) {
        String sign = generateSignature(data, key);
        data.put(WXPayConstants.SIGN, sign);
        return mapToXml(data);
    }


    /**
     * 判断签名是否正确
     * @param xmlStr XML格式数据
     * @param key API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(String xmlStr, String key) throws Exception {
        Map<String, String> data = xmlToMap(xmlStr);
        if (!data.containsKey(WXPayConstants.SIGN) ) {
            return false;
        }
        String sign = data.get(WXPayConstants.SIGN);
        return generateSignature(data, key).equals(sign);
    }

    /**
     * 判断签名是否正确
     * @param data Map类型数据
     * @param key API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(Map<String, String> data, String key) throws Exception {
        if (!data.containsKey(WXPayConstants.SIGN) ) {
            return false;
        }
        String sign = data.get(WXPayConstants.SIGN);
        return generateSignature(data, key).equals(sign);
    }

    /**
     * 生成签名
     * @param data 待签名数据
     * @param key API密钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key) {
        Set<String> keySet = data.keySet();
        keySet.remove(WXPayConstants.SIGN);
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals(WXPayConstants.SIGN)) {
                continue;
            }
            if (data.get(k).length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k)).append("&");
        }
        sb.append("key=").append(key);
        return MD5(sb.toString()).toUpperCase();
    }


    /**
     * 获取随机字符串 Nonce Str
     * @return String 随机字符串
     */
    public static String generateNonceStr() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


    /**
     * 生成 MD5
     * @param s 待处理数据
     * @return MD5结果
     */
    public static String MD5(String s) {
        try {
            java.security.MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
