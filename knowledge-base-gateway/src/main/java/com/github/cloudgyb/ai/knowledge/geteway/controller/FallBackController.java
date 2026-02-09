package com.github.cloudgyb.ai.knowledge.geteway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallBackController {

    @RequestMapping("/fallback")
    public String fallback() {
        return "服务异常";
    }
}
