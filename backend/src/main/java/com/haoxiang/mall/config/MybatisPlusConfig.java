package com.haoxiang.mall.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 配置 Mapper 扫描、事务、AOP 和 MyBatis-Plus 分页。
 * 数据源及 Hikari 连接池使用 spring.datasource.* 配置。
 */
@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@MapperScan(basePackages = "com.haoxiang.mall.core.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public TransactionDefinition transactionDefinition() {
        return new DefaultTransactionDefinition();
    }
}
