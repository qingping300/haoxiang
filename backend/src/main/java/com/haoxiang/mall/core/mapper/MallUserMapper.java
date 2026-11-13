package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.MallUser;

public interface MallUserMapper extends BaseMapper<MallUser> {

    IPage<MallUser> selectListPage(Page<MallUser> page, MallUser mallUser);
}
