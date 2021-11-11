package io.ont.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class HelperUtil {

    public static String constructMessage(List<Map<String, Object>> argsList) {
        return getData("signMessage", "0000000000000000000000000000000000000000", "signMessage", argsList, "", (Long) null, (Long) null);
    }

    public static String constructAuthorizeCredential(List<Map<String, Object>> argsList) {
        return getData("authorizeCredential", "0000000000000000000000000000000000000000", "authorizeCredential", argsList, "", (Long) null, (Long) null);
    }

    public static String getData(String action, String contractHash, String method, List<Map<String, Object>> argsList, String payer, Long gasLimit, Long gasPrice) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> invokeConfig = new HashMap<>();
        List<Map<String, Object>> functions = new ArrayList<>();
        Map<String, Object> function = new HashMap<>();
        function.put("operation", method);
        function.put("args", argsList);
        functions.add(function);
        invokeConfig.put("contractHash", contractHash);
        invokeConfig.put("functions", functions);
        invokeConfig.put("payer", payer);
        invokeConfig.put("gasLimit", gasLimit == null ? 20000L : gasLimit);
        invokeConfig.put("gasPrice", gasPrice == null ? 2500L : gasPrice);
        params.put("invokeConfig", invokeConfig);
        map.put("action", action);
        map.put("params", params);
        return JSON.toJSONString(map);
    }


    public static Map<String, Object> generateQrCode(String id, String requester, String signer, String callbackUrl, String data, String signature, Map<String, Object> desc, boolean mainNet) {
        long expireTime = (System.currentTimeMillis() + Constant.QRCODE_EXPIRE) / 1000L;
        return generateQrCode(id, requester, signer, expireTime, callbackUrl, data, "", desc, signature, mainNet);
    }

    private static Map<String, Object> generateQrCode(String id, String requester, String signer, Long expire, String callbackUrl, String data, String contractType, Map<String, Object> description, String signature, Boolean mainNet) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("requester", requester);
        resp.put("signer", signer);
        resp.put("data", data);
        resp.put("callback", callbackUrl);
        resp.put("exp", expire);
        resp.put("ver", "v2.0.0");
        resp.put("signature", signature);
        resp.put("desc", description);
        resp.put("chain", mainNet ? "Mainnet" : "Testnet");
        if (contractType != null && !"".equals(contractType)) {
            resp.put("contractType", contractType);
        }
        return resp;
    }

    public static Map<String, String> generateSimpleQRCode(String getQRCodeParamUrl) {
        Map<String, String> simpleQRCode = new HashMap<>();
        simpleQRCode.put("ONTAuthScanProtocol", getQRCodeParamUrl);
        return simpleQRCode;
    }

    public static String getOwnerByCredential(String claim) throws Exception {
        String[] split = claim.split("\\.");
        String payload = split[1];
        String payloadStr = new String(Base64.getDecoder().decode(payload.getBytes("utf-8")));
        return JSONObject.parseObject(payloadStr).getString("sub");
    }

    public static String getIssuerByCredential(String claim) throws Exception {
        String[] split = claim.split("\\.");
        String payload = split[1];
        String payloadStr = new String(Base64.getDecoder().decode(payload.getBytes("utf-8")));
        return JSONObject.parseObject(payloadStr).getString("iss");
    }

}
