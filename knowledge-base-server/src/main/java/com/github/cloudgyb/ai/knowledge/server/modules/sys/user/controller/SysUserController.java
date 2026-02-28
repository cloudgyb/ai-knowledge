package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.entity.SysUserEntity;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.service.SysUserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cloudgyb
 * @since 2026/2/1 18:36
 */
@RestController
@RequestMapping("/user")
public class SysUserController {
    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @RequestMapping("/{id}")
    public SysUserEntity getUserById(@PathVariable Long id) {
        return sysUserService.getUserById(id);
    }

    @RequestMapping("/list")
    public Iterable<SysUserEntity> list() {
        return sysUserService.list();
    }

    @RequestMapping("/list/{page}/{size}")
    public Page<SysUserEntity> list(@PathVariable int page, @PathVariable int size) {
        Page<SysUserEntity> page1 = Page.of(page, size, true);
        Page<SysUserEntity> page2 = sysUserService.page(page1);
        System.out.println(page2 == page1);
        return page1;
    }
}
