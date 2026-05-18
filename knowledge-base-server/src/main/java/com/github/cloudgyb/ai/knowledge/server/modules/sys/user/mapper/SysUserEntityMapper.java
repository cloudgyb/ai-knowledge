package com.github.cloudgyb.ai.knowledge.server.modules.sys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.user.entity.SysUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author cloudgyb
 * @since 2026/2/1 18:21
 */
@Mapper
public interface SysUserEntityMapper extends BaseMapper<SysUserEntity> {

    @Select("select * from sys_user where username=#{username} limit 1")
    SysUserEntity selectByUsername(String username);
}
