package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.CouponUser;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class OptimisticVersionTest {
    private SqlSessionFactory sessions;
    @BeforeEach void setup() throws Exception {
        JdbcDataSource ds=new JdbcDataSource();ds.setURL("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1");
        try(var c=ds.getConnection();var s=c.createStatement()){
            s.execute("CREATE TABLE haoxiang_mall_order(order_id BIGINT PRIMARY KEY,order_status TINYINT,version BIGINT NOT NULL)");
            s.execute("CREATE TABLE haoxiang_mall_coupon_user(coupon_user_id BIGINT PRIMARY KEY,status TINYINT,version BIGINT NOT NULL)");
            s.execute("INSERT INTO haoxiang_mall_order VALUES(1,0,0)");s.execute("INSERT INTO haoxiang_mall_coupon_user VALUES(1,0,0)");
        }
        MybatisConfiguration config=new MybatisConfiguration();config.setEnvironment(new Environment("test",new JdbcTransactionFactory(),ds));
        MybatisPlusInterceptor mp=new MybatisPlusInterceptor();mp.addInnerInterceptor(new OptimisticLockerInnerInterceptor());config.addInterceptor(mp);
        config.addMapper(OrderMapper.class);config.addMapper(CouponUserMapper.class);sessions=new MybatisSqlSessionFactoryBuilder().build(config);
    }
    @Test void oldOrderVersionCannotOverwriteSuccessfulUpdate(){
        try(var session=sessions.openSession(true)){
            Order first=new Order();first.setOrderId(1L);first.setOrderStatus((byte)1);first.setVersion(0L);
            Order stale=new Order();stale.setOrderId(1L);stale.setOrderStatus((byte)-2);stale.setVersion(0L);
            assertEquals(1,session.getMapper(OrderMapper.class).updateById(first));assertEquals(1L,first.getVersion());
            assertEquals(0,session.getMapper(OrderMapper.class).updateById(stale));
        }
    }
    @Test void oldCouponVersionCannotBeUsedTwice(){
        try(var session=sessions.openSession(true)){
            CouponUser first=new CouponUser();first.setCouponUserId(1L);first.setStatus((byte)1);first.setVersion(0L);
            CouponUser stale=new CouponUser();stale.setCouponUserId(1L);stale.setStatus((byte)1);stale.setVersion(0L);
            assertEquals(1,session.getMapper(CouponUserMapper.class).updateById(first));assertEquals(1L,first.getVersion());
            assertEquals(0,session.getMapper(CouponUserMapper.class).updateById(stale));
        }
    }
}
