package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GoodsStockMapperTest {
    private JdbcDataSource dataSource;
    private SqlSessionFactory sessions;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE haoxiang_mall_goods_info (goods_id BIGINT PRIMARY KEY, stock_num INT NOT NULL, update_time TIMESTAMP)");
            statement.execute("INSERT INTO haoxiang_mall_goods_info(goods_id, stock_num) VALUES (1, 1), (2, 10)");
        }
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        try (var xml = Resources.getResourceAsStream("mapper/GoodsMapper.xml")) {
            new XMLMapperBuilder(xml, configuration, "mapper/GoodsMapper.xml", configuration.getSqlFragments()).parse();
        }
        sessions = new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void rejectsQuantityLargerThanRemainingStock() throws Exception {
        try (var session = sessions.openSession(true)) {
            assertFalse(session.getMapper(GoodsMapper.class).reduceStock(1L, 2));
        }
        assertEquals(1, stock(1));
    }

    @Test
    void rejectsZeroAndNegativeQuantityAtSqlBoundary() throws Exception {
        try (var session = sessions.openSession(true)) {
            GoodsMapper mapper = session.getMapper(GoodsMapper.class);
            assertFalse(mapper.reduceStock(1L, 0));
            assertFalse(mapper.reduceStock(1L, -1));
        }
        assertEquals(1, stock(1));
    }

    @Test
    void deductsExactAvailableQuantity() throws Exception {
        try (var session = sessions.openSession(true)) {
            assertTrue(session.getMapper(GoodsMapper.class).reduceStock(2L, 10));
            assertFalse(session.getMapper(GoodsMapper.class).reduceStock(2L, 1));
        }
        assertEquals(0, stock(2));
    }

    @Test
    void concurrentBuyersCannotMakeStockNegative() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var results = new ArrayList<Future<Boolean>>();
            for (int i = 0; i < 20; i++) {
                results.add(executor.submit(() -> {
                    if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("start timeout");
                    try (var session = sessions.openSession(true)) {
                        return session.getMapper(GoodsMapper.class).reduceStock(2L, 2);
                    }
                }));
            }
            start.countDown();
            int successes = 0;
            for (var result : results) if (result.get(15, TimeUnit.SECONDS)) successes++;
            assertEquals(5, successes);
        }
        assertEquals(0, stock(2));
    }

    private int stock(long id) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT stock_num FROM haoxiang_mall_goods_info WHERE goods_id=?")) {
            statement.setLong(1, id);
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getInt(1);
            }
        }
    }
}
