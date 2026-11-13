package com.haoxiang.mall.core.service.impl;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.haoxiang.mall.core.mapper.AdminUserMapper;
import com.haoxiang.mall.core.entity.AdminUser;
import com.haoxiang.mall.core.service.AdminUserService;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
}
