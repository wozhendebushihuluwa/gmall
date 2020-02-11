package com.atguigu.gmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;

import com.atguigu.gmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2016101800713393";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCBl12MP3oTqpnMW+oVrTZ03RCptU9dG0PYtNgARoegWkTZqmMLWjKUz6CL6uPu2hJvLddNhNkPExe835zBoJUrINQNtDvwffZstRbvOcGDiYMfVraEoCfc1BZOxbqAk8G9kduhcRbKIfVN7JHISXMNWHcKVciWpHTwY3oVrsfBTVtGCueI1QV8C/ZCT6zLXn7lghnv6AUEEum1ehIi4Hq+7NmPEdcH9i3AotoldDGWK5Prm5WV+c33q/UR/8SHMiCiAASnN5c6xSatUSIrtoeQQLpGs3SNeqA85+q2tAk7peU1JZvUrnhtYYBxs/wn15bSlGtE1qCVWMJC+wHwRnATAgMBAAECggEAHs86yWYqAtTzfbd4frDQmpYYKRDXDn618aaFGAOsqP3tEobEx2UpU2HNfJZanGWyLkaKwn5MEJ5sbVYIcaxY6okCgUORYIrB9HRVQIjNrhUaXHAEMqHU+FZf+1hdD8aRMB2mRsqZLXNHW8BbpG957/lRS967Mr1ko+Yyl0cXbKg96Hx89xXEEEzTHBjvF0IPo+7sCTrWEQs3cudiFq1GZmNjGG4EjgPH4bipsFfP379hChqTOBM+vA5c5naVarZSLgGzsTkJGan9xbjiUXns0srIzpIVJEpZ2NYPcJz6R5pvyR55+pCqawfumJxSF8azfNrnywPT50hKni8KvwHaqQKBgQC2n+BPzUn0zlLhPOHqZn3aYajazgJ+g7cIVONbpLplDFTJRr8XeklUyB0kkEJVY7cKBtsLvmRx+zFNudvp0fVqlfH2wjjZ+rNXoxoRAPG9fzyYva+8wQ5tF2ACDygdt4oY7E6+hlm/1yh5KLz+477Ab+kjY1xBaZfCFVTzJGdTTwKBgQC1qKx3cAzxqDyywMhJewXkR+9DgBfpgx1Sf2oncyrZKFps/C761VJzLVH65uki3vPT0mOvifWPyTLaqKxb5PBDSu97hLwPV0BMms6egr+w5mLTQW3+YHe7SOPuIZE6jXIVVz6N5dccpvFqQIVg/wLZcuEYf0fRMfYa3qQexAp1/QKBgQCIYkdTcX1svHT6zsvskJQmdc5zcWw9bUJQj4ynkRK+igH7usDU2LdRAI17E9zmBizY6RvG3m4HDU/ZuxZ3vRvpeIeV/6ATcf1jUmQSFMF0AkSP0QEgFpEeeVly0DzmTH5udHOuJ4l5EkagjQLv28dF0Y2rpvVryF7US7gASMx9UwKBgQCvlfPuFl5MjqLs3K47aDAPsQAxrHRwfw0umn2O2CDIedq9kRwP03W4YkvlSqB51iqzyZ4VJDI44u7Gr1a6FBWv1ZSZPXlRErLR2KhySkriwC0xqQKaZ8ATRKcbm1J22BU3T3blTQ2ZYjviya6iTJsCd6nfNdfC+pm46w6TtkWuOQKBgQCjeCkM9Xb5h/lBSW9yJ+zt5L1rtoLoRictLcwnVChrBiClyDfGpVZH40/JJNyN7N01Hx5IxFgz3KzFcgZs+7uBNtnzScG4CstL2q6jZ1iwi/v0gxfNf3GCb1AIdracWEeVLs/s1dV8+yoKkMWUwxLaskIZAppCNyRDFwle+cCmLw==";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnlmIZ0yZ4f/kc7xoGlbibarTm6+/wX9PvXj6iX+WeBgGpdDGckGXMMcmHc/tARYMZTUzieYknwAb73dN+8qgoKg5X1cK6v2qeAL+8nE5Uujhix1q+putWJ6DKU17HnLFiSVaxtqGU6OJ1pbmuKHx3jTcgzY7L/tiQESJuEi4Bf4clR0nHsftYE6GjQ/WgP2VReIb6ptQhtXRxgkELIGV9cdluKLkRLb9zv9v+vSdzFSqNiR3ewssxbG0Xbx+9tc0+bOPpFcLZjwIbCxwMdZJ9xssdXPXdNVRpvuw3X0uA7hGKKfMhLAwRAwejIVfq8eg1mAz51/UMqiudlBb3vvzGwIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody(); // 二维码支付表单

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
