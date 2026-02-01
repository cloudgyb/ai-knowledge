package com.github.cloudgyb.ai.knowledge.geteway.service;

import com.github.cloudgyb.ai.knowledge.geteway.entity.SysUserEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author cloudgyb
 * @since 2026/2/1 21:57
 */
@FeignClient(name = "knowledge-base-server", path = "/user")
public interface SysUserClient {

    @GetMapping("/{id}")
    SysUserEntity getUserById(@PathVariable Long id);
}
