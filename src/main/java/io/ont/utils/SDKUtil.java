package io.ont.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.ontio.OntSdk;
import com.github.ontio.account.Account;
import com.github.ontio.common.*;
import com.github.ontio.common.Helper;
import com.github.ontio.core.DataSignature;
import com.github.ontio.core.asset.Sig;
import com.github.ontio.core.transaction.Transaction;
import com.github.ontio.crypto.Digest;
import com.github.ontio.crypto.SignatureScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class SDKUtil {

    @Autowired
    private ConfigParam configParam;
    private OntSdk wm;

    private OntSdk getOntSdk() {
        if (wm == null) {
            wm = OntSdk.getInstance();
            wm.setRestful(configParam.RESTFUL_URL);
            wm.openWalletFile("wallet.json");
        }
        if (wm.getWalletMgr() == null) {
            wm.openWalletFile("wallet.json");
        }
        return wm;
    }

    public String signData(String data, String privateKey) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Account account = new Account(Account.getPrivateKeyFromWIF(privateKey), ontSdk.getWalletMgr().getSignatureScheme());
        DataSignature sign = new DataSignature(SignatureScheme.SHA256WITHECDSA, account, data.getBytes());
        return Helper.toHexString(sign.signature());
    }

    public boolean verifySignature(String data, String publicKey, String signature) throws Exception {
        Account account = new Account(false, Helper.hexToBytes(publicKey));
        return account.verifySignature(data.getBytes(), Helper.hexToBytes(signature));
    }

    public boolean verifySignature(String ontId, String signedTx) throws Exception {
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(signedTx));
        byte[] sigBytes = transaction.sigs[0].sigData[0];
        String signature = Helper.toHexString(sigBytes);
        if (!signature.startsWith("01")) {
            signature = String.format("01%s", signature);
        }

        transaction.sigs = new Sig[0];
        String hex = transaction.toHexString();
        String tx = hex.substring(0, hex.length() - 2);
        String data = Helper.toHexString(Digest.hash256(Helper.hexToBytes(tx)));
        String publicKeys = getPublicKeys(ontId);
        if ("".equals(publicKeys)) {
            return false;
        } else {
            JSONArray jsonArray = JSONObject.parseArray(publicKeys);
            Boolean verify = Boolean.FALSE;

            for (int i = 0; i < jsonArray.size(); ++i) {
                String pubKey = jsonArray.getJSONObject(i).getString("publicKeyHex");
                verify = verifyHex(pubKey, data, signature);
                if (verify) {
                    break;
                }
            }

            return verify;
        }
    }

    public String getAddressByPublicKey(String publicKey) throws Exception {
        Address address = Address.addressFromPubKey(publicKey);
        return address.toBase58();
    }

    public Map<String, Object> verifyCallbackSignature(String ontId, String signedTx) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(signedTx));
        byte[] sigBytes = transaction.sigs[0].sigData[0];
        String signature = Helper.toHexString(sigBytes);
        log.info("signature:{}", signature);
        if (!signature.startsWith("01")) {
            signature = String.format("01%s", signature);
        }
        map.put("signature", signature);
        transaction.sigs = new Sig[0];
        String hex = transaction.toHexString();
        String tx = hex.substring(0, hex.length() - 2);
        String data = Helper.toHexString(Digest.hash256(Helper.hexToBytes(tx)));
        map.put("data", tx);
        String publicKeys = getPublicKeys(ontId);
        if ("".equals(publicKeys)) {
            map.put("verify", false);
            return map;
        } else {
            JSONArray jsonArray = JSONObject.parseArray(publicKeys);
            Boolean verify = Boolean.FALSE;

            for (int i = 0; i < jsonArray.size(); ++i) {
                String pubKey = jsonArray.getJSONObject(i).getString("publicKeyHex");
                map.put("publicKey", pubKey);
                verify = verifyHex(pubKey, data, signature);
                if (verify) {
                    break;
                }
            }
            map.put("verify", verify);
            return map;
        }
    }

    public String getPublicKeys(String ontId) throws Exception {
        return this.getOntSdk().nativevm().ontId().sendGetPublicKeys(ontId);
    }

    public Boolean verifyHex(String publicKey, String hex, String signature) throws Exception {
        Account account = new Account(false, Helper.hexToBytes(publicKey));
        boolean b = account.verifySignature(Helper.hexToBytes(hex), Helper.hexToBytes(signature));
        return b;
    }

    public String signTransaction(String tx, String privateKey) throws Exception {
        OntSdk ontSdk = getOntSdk();
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(tx));
        Account account = new Account(Helper.hexToBytes(privateKey), ontSdk.getWalletMgr().getSignatureScheme());
        ontSdk.addSign(transaction, account);
        return transaction.toHexString();
    }

    public String sendTransaction(String signedTx) throws Exception {
        OntSdk ontSdk = this.getOntSdk();
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(signedTx));
        ontSdk.getConnect().sendRawTransaction(transaction.toHexString());
        return transaction.hash().toString();
    }

    public String sendSyncTransaction(String signedTx) throws Exception {
        OntSdk ontSdk = this.getOntSdk();
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(signedTx));
        ontSdk.getConnect().sendRawTransactionSync(signedTx);
        return transaction.hash().toString();
    }

    public Object sendTransactionPreExecute(String signedTx) throws Exception {
        OntSdk ontSdk = this.getOntSdk();
        Transaction transaction = Transaction.deserializeFrom(Helper.hexToBytes(signedTx));
        Object o = ontSdk.getConnect().sendRawTransactionPreExec(transaction.toHexString());
        return o;
    }

    public long getOntBalance(String address) throws Exception {
        return this.getOntSdk().nativevm().ont().queryBalanceOf(address);
    }

    public long getOngBalance(String address) throws Exception {
        return this.getOntSdk().nativevm().ong().queryBalanceOf(address);
    }


    public String getPublicKeyNew(String ontId) throws Exception {
        OntSdk ontSdk = getOntSdk();
        String publicKeys = ontSdk.nativevm().ontId().sendGetPublicKeys(ontId);
        JSONArray jsonArray = JSONObject.parseArray(publicKeys);
        JSONObject jsonObject = jsonArray.getJSONObject(0);
        return jsonObject.getString("publicKeyHex");
    }

    public String getPublicKeyOld(String ontId) throws Exception {
        OntSdk ontSdk = getOntSdk();
        String ddo = ontSdk.nativevm().ontId().sendGetDDO(ontId);
        JSONObject jsonObject = JSONObject.parseObject(ddo);
        JSONArray owners = jsonObject.getJSONArray("Owners");
        return owners.getJSONObject(0).getString("Value");
    }

    public boolean verifyCredential(String publicKey, String headerAndPayload, byte[] signature) throws Exception {
        Account account = new Account(false, Helper.hexToBytes(publicKey));
        boolean b = account.verifySignature(headerAndPayload.getBytes(), signature);
        return b;
    }

    public Account newAccountFromPk(String privateKey) throws Exception {
        OntSdk ontSdk = getOntSdk();
        return new Account(Helper.hexToBytes(privateKey), ontSdk.getWalletMgr().getSignatureScheme());
    }

}
