package io.ont.controller;

import io.ont.bean.Result;
import io.ont.controller.vo.InvokeDto;
import io.ont.service.CommonService;
import io.ont.utils.ErrorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/common")
@CrossOrigin
public class CommonController {
    @Autowired
    private CommonService commonService;

    /**
     * get qr-code params
     * @param id
     * @return
     */
    @GetMapping("/qr-code/{id}")
    public String getQrCode(@PathVariable String id) {
        String action = "getQrCode";
        return commonService.getQrCode(action, id);
    }

    /**
     * get login qr code
     * @return
     * @throws Exception
     */
    @GetMapping("/login")
    public Result login() throws Exception {
        String action = "login";
        Map<String, Object> result = commonService.login(action);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), result);
    }

    /**
     * loginCallback by ONTO
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/login/callback")
    public Result loginCallback(@RequestBody InvokeDto req) throws Exception {
        String action = "flash-pool-login-callback";
        commonService.loginCallback(action, req);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), true);
    }

    /**
     * get authorize credential qr code
     * @return
     * @throws Exception
     */
    @GetMapping("/authorize")
    public Result authorize() throws Exception {
        String action = "authorizeCredential";
        Map<String, Object> result = commonService.authorize(action);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), result);
    }

    /**
     * authorize credential callback by ONTO
     * @param req
     * @return
     * @throws Exception
     */
    @PostMapping("/authorize/callback")
    public Result authorizeCallback(@RequestBody InvokeDto req) throws Exception {
        String action = "authorizeCallback";
        commonService.authorizeCallback(action, req);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), true);
    }

}
