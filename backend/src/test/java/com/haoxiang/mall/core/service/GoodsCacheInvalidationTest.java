package com.haoxiang.mall.core.service;

import com.haoxiang.mall.core.mapper.GoodsMapper;
import com.haoxiang.mall.core.service.impl.GoodsServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoodsCacheInvalidationTest {
    private GoodsServiceImpl goods;
    private GoodsMapper mapper;
    private CacheService cache;
    @BeforeEach void setup() {
        goods=new GoodsServiceImpl();mapper=mock(GoodsMapper.class);cache=mock(CacheService.class);
        ReflectionTestUtils.setField(goods,"goodsMapper",mapper);ReflectionTestUtils.setField(goods,"cacheService",cache);
    }
    @AfterEach void cleanup(){TransactionSynchronizationManager.clear();}
    @Test void transactionOnlyInvalidatesAfterCommit() {
        when(mapper.reduceStock(1L,2)).thenReturn(true);
        TransactionSynchronizationManager.setActualTransactionActive(true);TransactionSynchronizationManager.initSynchronization();
        assertTrue(goods.reduceStock(1L,2));verifyNoInteractions(cache);
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCommit());
        verify(cache).invalidate("goods:1");
    }
    @Test void failedDeductionDoesNotInvalidate() {
        when(mapper.reduceStock(1L,2)).thenReturn(false);
        assertFalse(goods.reduceStock(1L,2));verifyNoInteractions(cache);
    }
    @Test void rollbackDoesNotInvalidate() {
        when(mapper.reduceStock(1L,2)).thenReturn(true);
        TransactionSynchronizationManager.setActualTransactionActive(true);TransactionSynchronizationManager.initSynchronization();
        assertTrue(goods.reduceStock(1L,2));
        TransactionSynchronizationManager.getSynchronizations().forEach(s->s.afterCompletion(1));
        verifyNoInteractions(cache);
    }
}
