package io.ont.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.ont.controller.vo.InvokeDto;
import io.ont.exception.OntoLoginException;
import io.ont.service.CommonService;
import io.ont.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;


@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private ConfigParam configParam;
    @Autowired
    private SDKUtil sdkUtil;

    private Map<String, String> storedQrCodeMap = new HashMap<>();

    @Override
    public String getQrCode(String action, String id) {
        log.info("getQrCode:{}", id);
        String qrCode = storedQrCodeMap.get(id);
        if (StringUtils.isEmpty(qrCode)) {
            throw new OntoLoginException(action, ErrorInfo.NOT_FOUND.descEN(), ErrorInfo.NOT_FOUND.code());
        }
        return qrCode;
    }

    @Override
    public Map<String, Object> login(String action) throws Exception {
        // construct qrCode
        String id = UUID.randomUUID().toString();
        String callbackUrl = String.format(configParam.CALLBACK_URL, Constant.LOGIN_URI);
        List<Map<String, Object>> argsList = new ArrayList<>();
        Map<String, Object> arg0 = new HashMap<>();
        arg0.put("name", "Login");
        arg0.put("value", "String:ontId");
        argsList.add(arg0);
        String params = HelperUtil.constructMessage(argsList);
        // sign data
        String sig = sdkUtil.signData(params, configParam.SERVER_WIF);
        Map<String, Object> desc = new HashMap<>();
        Map<String, Object> qrCodeMap = HelperUtil.generateQrCode(id, configParam.SERVER_ONTID, "", callbackUrl, params, sig, desc, configParam.MAIN_NET);

        // store qrCode, recommend database
        storedQrCodeMap.put(id, JSON.toJSONString(qrCodeMap));

        // construct result to frontend
        // QR_CODE_URI to get stored qrCode from server
        Map<String, String> simpleQRCode = HelperUtil.generateSimpleQRCode(String.format(configParam.CALLBACK_URL, Constant.QR_CODE_URI) + id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("qrCode", simpleQRCode);
        return result;
    }

    @Override
    public void loginCallback(String action, InvokeDto req) throws Exception {
        String ontId = req.getSigner();
        String signedTx = req.getSignedTx();
        boolean verified = sdkUtil.verifySignature(ontId, signedTx);
        if (verified) {
            // handle business with ontId
            log.info("onrId:{} verified success", ontId);
        } else {
            log.error("onrId:{} verified failed", ontId);
            throw new OntoLoginException(action, ErrorInfo.VERIFY_FAILED.descEN(), ErrorInfo.VERIFY_FAILED.code());
        }
    }

    @Override
    public Map<String, Object> authorize(String action) throws Exception {
        // construct qrCode
        String id = UUID.randomUUID().toString();
        String callbackUrl = String.format(configParam.CALLBACK_URL, Constant.AUTHORIZE_URI);
        List<Map<String, Object>> authorizeArgsList = new ArrayList<>();

        // the credential that server required
        List<String> contextList = new ArrayList<>();
        contextList.add(Constant.SFP_DL_AUTHENTICATION);
        contextList.add(Constant.SFP_PASSPORT_AUTHENTICATION);
        contextList.add(Constant.SFP_IDCARD_AUTHENTICATION);

        Map<String, Object> arg0 = new HashMap<>();
        Map<String, Object> arg1 = new HashMap<>();
        Map<String, Object> arg2 = new HashMap<>();
        Map<String, Object> arg3 = new HashMap<>();
        Map<String, Object> arg4 = new HashMap<>();
        Map<String, Object> arg5 = new HashMap<>();
        arg0.put("name", "dappName");
        arg0.put("value", "String:Your dapp name");
        arg1.put("name", "dappIcon");
        arg1.put("value", "Icon:");
        arg2.put("name", "message");
        arg2.put("value", "String:Dapp Authorize");
        arg3.put("name", "issueBy");
        arg3.put("value", "String:");
        arg4.put("name", "condition");
        arg4.put("value", "");
        arg5.put("name", "@context");
        arg5.put("value", contextList);
        authorizeArgsList.add(arg0);
        authorizeArgsList.add(arg1);
        authorizeArgsList.add(arg2);
        authorizeArgsList.add(arg3);
        authorizeArgsList.add(arg4);
        authorizeArgsList.add(arg5);
        String params = HelperUtil.constructAuthorizeCredential(authorizeArgsList);
        // sign data
        String sig = sdkUtil.signData(params, configParam.SERVER_WIF);
        Map<String, Object> desc = new HashMap<>();
        Map<String, Object> qrCodeMap = HelperUtil.generateQrCode(id, configParam.SERVER_ONTID, "", callbackUrl, params, sig, desc, configParam.MAIN_NET);

        // store qrCode, recommend database
        storedQrCodeMap.put(id, JSON.toJSONString(qrCodeMap));

        // construct result to frontend
        // QR_CODE_URL to get stored qrCode from server
        Map<String, String> simpleQRCode = HelperUtil.generateSimpleQRCode(String.format(configParam.CALLBACK_URL, Constant.QR_CODE_URI) + id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("qrCode", simpleQRCode);
        return result;
    }

    @Override
    public void authorizeCallback(String action, InvokeDto req) throws Exception {
        String ontId = req.getSigner();
        String signedTx = req.getSignedTx();
        String id = (String) req.getExtraData().get("id");
        List<String> credentialList = (List<String>) req.getExtraData().get("credential");
        boolean verified = sdkUtil.verifySignature(ontId, signedTx);
        if (verified) {
            log.info("onrId:{} verified success", ontId);
            log.info("onrId:{} credentialList:{}", ontId, credentialList);
            dealWithOntoCredentials(action, ontId, credentialList);
        } else {
            log.error("onrId:{} verified failed", ontId);
            throw new OntoLoginException(action, ErrorInfo.VERIFY_FAILED.descEN(), ErrorInfo.VERIFY_FAILED.code());
        }

    }

    private void dealWithOntoCredentials(String action, String ontId, List<String> credentialList) throws Exception {
        for (String credential : credentialList) {
            log.info("credential:{}", credential);
            String[] split = credential.split("\\.");
            String header = split[0];
            String payload = split[1];
            String signature = split[2];
            String payloadStr = Base64ConvertUtil.decode(payload);
            log.info("credential payload:{}", payloadStr);
            JSONObject payloadObj = JSONObject.parseObject(payloadStr);

            // verify kyc signature
            if (signature.endsWith("\\")) {
                signature = signature.substring(0, signature.length() - 1);
            }
            String headerAndPayload = header + "." + payload;
            String iss = payloadObj.getString("iss");
            String publicKey;
            try {
                publicKey = sdkUtil.getPublicKeyNew(iss);
            } catch (Exception e) {
                try {
                    publicKey = sdkUtil.getPublicKeyOld(iss);
                } catch (Exception ex) {
                    throw new OntoLoginException(action, ErrorInfo.VERIFY_FAILED.descEN(), ErrorInfo.VERIFY_FAILED.code());
                }
            }
            byte[] signatureBytes = Base64.getDecoder().decode(signature.getBytes("utf-8"));
            boolean signatureFlag = sdkUtil.verifyCredential(publicKey, headerAndPayload, signatureBytes);
            if (!signatureFlag) {
                throw new OntoLoginException(action, ErrorInfo.VERIFY_FAILED.descEN(), ErrorInfo.VERIFY_FAILED.code());
            }

            // check owner match ontId
            String owner = payloadObj.getString("sub");
            if (owner != null && !ontId.equals(owner)) {
                throw new OntoLoginException(action, ErrorInfo.KYC_OWNER_NOT_MATCH.descEN(), ErrorInfo.KYC_OWNER_NOT_MATCH.code());
            }

            // get @context
            Object contextObj = payloadObj.get("@context");
            if (contextObj == null) {
                contextObj = payloadObj.getJSONObject("vc").get("@context");
            }
            String context = null;
            if (contextObj instanceof String) {
                context = (String) contextObj;
            } else if (contextObj instanceof JSONArray) {
                JSONArray contextArray = (JSONArray) contextObj;
                context = contextArray.getString(contextArray.size() - 1);
            }

            // check credential expiration
            Integer issueTime = payloadObj.getInteger("iat");
            Integer expireTime = payloadObj.getInteger("exp");
            long current = System.currentTimeMillis() / 1000;
            if (expireTime < current) {
                throw new OntoLoginException(action, ErrorInfo.KYC_EXPIRE.descEN(), ErrorInfo.KYC_EXPIRE.code());
            }
            log.info("verify credential success");
        }
    }
}
