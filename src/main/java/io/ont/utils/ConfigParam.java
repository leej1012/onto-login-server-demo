package io.ont.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service("ConfigParam")
public class ConfigParam {

    @Value("${service.restfulUrl}")
    public String RESTFUL_URL;
    @Value("${server.wif}")
    public String SERVER_WIF;
    @Value("${server.ontid}")
    public String SERVER_ONTID;
    @Value("${callback.url}")
    public String CALLBACK_URL;
    @Value("${main.net}")
    public Boolean MAIN_NET;

}