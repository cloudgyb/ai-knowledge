package com.github.cloudgyb.ai.knowledge.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.entity.SysUserEntity;
import com.github.cloudgyb.ai.knowledge.server.mapper.SysUserEntityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cloudgyb
 * @since 2026/2/1 18:22
 */
@Service
@Transactional
public class SysUserService extends ServiceImpl<SysUserEntityMapper, SysUserEntity> {
    public SysUserEntity getUserById(Long id) {
        return getById(id);
    }
}
