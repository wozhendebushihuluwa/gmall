package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "D:\\My_Code\\rsa\\rsa.pub";

    private static final String priKeyPath = "D:\\My_Code\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "asdfas234234ASDFfasdfa@^%#%$%$%");

    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1NzkwODg5OTd9.ZTcZ81LSNRC8Tu3RZgUrrLhP-PYEye6eXE1-gVNn5TsXx4F_zSl1eIGfp1Ydr2rf4CoZb3p1zg2XC4-vPMA2PHks_3THtifeLdGaj1FlqYw6j3BuxP6ZrIGf6h7ItcCtWtCXsJVJAsJivdgFvQWPlumec4xntV8THqL8-nCxjBmc6pAGRhe9Lih3ceWpFEW0ANdeI4GALOTtHnMjXS_lYcJfb1M3HDYhU8wlu4GLDfOqaXd6OfhdtF-ZP5ooY4uuf6O6clPMjBuDAJ3ZJcppo-3NVEnksCIXNpVjnzjF_DefzuV_s2sVsL05IKhU8qxCxr7qnM0ZnAwxW2hPZIvsuQ";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }

}
