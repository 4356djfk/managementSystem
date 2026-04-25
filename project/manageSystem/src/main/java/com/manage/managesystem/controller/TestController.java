package com.manage.managesystem.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {  // 绫诲悕蹇呴』澶ч┘宄帮紒

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}
