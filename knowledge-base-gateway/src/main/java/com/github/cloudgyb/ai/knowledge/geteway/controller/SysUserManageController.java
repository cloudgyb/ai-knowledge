package com.github.cloudgyb.ai.knowledge.geteway.controller;

import com.github.cloudgyb.ai.knowledge.geteway.entity.SysUserEntity;
import com.github.cloudgyb.ai.knowledge.geteway.service.SysUserClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author cloudgyb
 * @since 2026/2/1 22:08
 */
@RestController
@RequestMapping("/user")
public class SysUserManageController {
    private final SysUserClient sysUserClient;
    private final RestTemplate restTemplate;
    private final Scheduler scheduler = Schedulers.boundedElastic();


    public SysUserManageController(SysUserClient sysUserClient, RestTemplate restTemplate) {
        this.sysUserClient = sysUserClient;
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/test/{id}")
    public Mono<ResponseEntity<SysUserEntity>> getUserById(@PathVariable Long id) {
        Mono<ResponseEntity<SysUserEntity>> mono = Mono
                .fromCallable(() -> sysUserClient.getUserById(id))
                .subscribeOn(scheduler).map(ResponseEntity::ok)
                .doOnError(throwable -> ResponseEntity.notFound().build());
        return mono;
    }

    @RequestMapping("/{id}")
    public Mono<ResponseEntity<SysUserEntity>> getUserById2(@PathVariable Long id) {
        Mono<ResponseEntity<SysUserEntity>> mono = Mono
                .fromCallable(() -> restTemplate.getForObject("http://knowledge-base-server/user/" + id, SysUserEntity.class))
                .subscribeOn(scheduler).map(ResponseEntity::ok)
                .doOnError(throwable -> ResponseEntity.notFound().build());
        return mono;
    }

    @RequestMapping("/list")
    public String test() {
        return "hello world";
    }
}
