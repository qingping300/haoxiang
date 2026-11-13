package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.haoxiang.mall.core.entity.Order;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class MySqlOptimisticVersionIntegrationTest {
    @Test void twoTransactionsWithSameVersionCannotBothWin() throws Exception {
        String password=System.getenv("HAOXIANG_TEST_MYSQL_ROOT_PASSWORD");Assumptions.assumeTrue(password!=null&&!password.isBlank());
        MysqlDataSource ds=new MysqlDataSource();ds.setUrl("jdbc:mysql://127.0.0.1:23306/haoxiang_version_test_20261113?useSSL=false&allowPublicKeyRetrieval=true");ds.setUser("root");ds.setPassword(password);
        long id;String orderNo=UUID.randomUUID().toString().replace("-","").substring(0,20);
        try(var c=ds.getConnection();var p=c.prepareStatement("INSERT INTO haoxiang_mall_order(order_no,user_id,total_price,version) VALUES(?,1,100,0)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,orderNo);p.executeUpdate();try(var keys=p.getGeneratedKeys()){assertTrue(keys.next());id=keys.getLong(1);}
        }
        try {
            MybatisConfiguration config=new MybatisConfiguration();config.setEnvironment(new Environment("mysql",new JdbcTransactionFactory(),ds));
            MybatisPlusInterceptor mp=new MybatisPlusInterceptor();mp.addInnerInterceptor(new OptimisticLockerInnerInterceptor());config.addInterceptor(mp);config.addMapper(OrderMapper.class);
            var factory=new MybatisSqlSessionFactoryBuilder().build(config);var gate=new CountDownLatch(1);
            try(var pool=Executors.newFixedThreadPool(2)){
                Callable<Integer> change=()->{gate.await();try(var session=factory.openSession(true)){Order patch=new Order();patch.setOrderId(id);patch.setVersion(0L);patch.setOrderStatus((byte)1);return session.getMapper(OrderMapper.class).updateById(patch);}};
                var a=pool.submit(change);var b=pool.submit(change);gate.countDown();assertEquals(1,a.get(10,TimeUnit.SECONDS)+b.get(10,TimeUnit.SECONDS));
            }
            try(var c=ds.getConnection();var p=c.prepareStatement("SELECT version FROM haoxiang_mall_order WHERE order_id=?")){p.setLong(1,id);try(var result=p.executeQuery()){assertTrue(result.next());assertEquals(1,result.getLong(1));}}
        } finally {
            try(var c=ds.getConnection();var p=c.prepareStatement("DELETE FROM haoxiang_mall_order WHERE order_id=? AND order_no=?")){p.setLong(1,id);p.setString(2,orderNo);p.executeUpdate();}
        }
    }
}
