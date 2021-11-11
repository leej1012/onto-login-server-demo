package io.ont.controller.vo;

import lombok.Data;

import java.util.Map;


@Data
public class InvokeDto {
    private String signer;
    private String signedTx;
    private Map<String,Object> extraData;

}
