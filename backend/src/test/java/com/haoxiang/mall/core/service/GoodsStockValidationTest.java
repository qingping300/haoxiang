package com.haoxiang.mall.core.service;

import com.haoxiang.mall.core.mapper.GoodsMapper;
import com.haoxiang.mall.core.service.impl.GoodsServiceImpl;
import com.haoxiang.mall.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GoodsStockValidationTest {
    @Test
    void invalidQuantitiesNeverReachMapper() {
        GoodsMapper mapper = mock(GoodsMapper.class);
        GoodsServiceImpl service = new GoodsServiceImpl();
        ReflectionTestUtils.setField(service, "goodsMapper", mapper);
        assertThrows(BusinessException.class, () -> service.reduceStock(1L, 0));
        assertThrows(BusinessException.class, () -> service.reduceStock(1L, -1));
        assertThrows(BusinessException.class, () -> service.reduceStock(1L, null));
        assertThrows(BusinessException.class, () -> service.reduceStock(null, 1));
        verifyNoInteractions(mapper);
    }
}
