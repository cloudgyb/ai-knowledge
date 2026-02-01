package com.github.cloudgyb.ai.knowledge.geteway.controller;

import com.github.cloudgyb.ai.knowledge.geteway.entity.SysUserEntity;
import com.github.cloudgyb.ai.knowledge.geteway.service.SysUserClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

/**
 * @author cloudgyb
 * @since 2026/2/1 22:08
 */
@RestController
@RequestMapping("/user")
public class SysUserManageController {
    private final SysUserClient sysUserClient;
    private final RestTemplate restTemplate;

    public SysUserManageController(SysUserClient sysUserClient, RestTemplate restTemplate) {
        this.sysUserClient = sysUserClient;
        this.restTemplate = restTemplate;
    }

    /*@RequestMapping("/{id}")
    public SysUserEntity getUserById(@PathVariable Long id) {
        return sysUserClient.getUserById(id);
    }*/

    @RequestMapping("/{id}")
    public SysUserEntity getUserById2(@PathVariable Long id) {
        return restTemplate.getForObject("http://knowledge-base-server/user/" + id, SysUserEntity.class);
    }

    @RequestMapping("/list")
    public String test() {
        return "hello world";
    }
}
