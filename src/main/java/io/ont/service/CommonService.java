package io.ont.service;


import io.ont.controller.vo.InvokeDto;

import java.util.Map;

public interface CommonService {

    String getQrCode(String action, String id);

    Map<String, Object> login(String action) throws Exception;

    void loginCallback(String action, InvokeDto req) throws Exception;

    Map<String, Object> authorize(String action) throws Exception;

    void authorizeCallback(String action, InvokeDto req) throws Exception;
}
