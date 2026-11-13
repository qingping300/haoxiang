package com.haoxiang.mall.core.service;
import java.util.function.Supplier;
public interface CacheService {
    <T> T get(String key, Class<T> type, Supplier<T> loader);
    void invalidate(String key);
}
