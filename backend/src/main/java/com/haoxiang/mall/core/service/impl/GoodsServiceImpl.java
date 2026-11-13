package com.haoxiang.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.mapper.GoodsMapper;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.vo.GoodsVO;
import com.haoxiang.mall.core.entity.vo.SearchObjVO;
import com.haoxiang.mall.core.entity.vo.SearchPageGoodsVO;
import com.haoxiang.mall.core.service.GoodsService;
import com.haoxiang.mall.core.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.JedisSearch;
import com.haoxiang.mall.redis.RedisCache;
import org.springframework.stereotype.Service;
import redis.clients.jedis.search.Schema;

import java.util.List;

@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private JedisSearch jedisSearch;

    @Resource
    private RedisCache redisCache;

    @Resource
    private CacheService cacheService;

    @Override
    public IPage<Goods> selectPage(Page<Goods> page, Goods goods) {
        return goodsMapper.selectListPage(page, goods);
    }

    @Override
    public IPage<Goods> findMallGoodsListBySearch(Page<SearchPageGoodsVO> page, SearchObjVO searchObjVO) {
        return goodsMapper.findMallGoodsListBySearch(page, searchObjVO);
    }

    @Override
    public boolean syncRs() {
        jedisSearch.dropIndex(Constants.GOODS_IDX_NAME);
        Schema schema = new Schema()
                .addSortableTextField("goodsName", 1.0)
                .addSortableTextField("goodsNamePinyin", 0.5)
                .addSortableTextField("goodsIntro", 0.5)
                .addSortableNumericField("goodsId")
                .addSortableNumericField("goodsCategoryId")
                .addSortableNumericField("goodsSellStatus")
                .addSortableNumericField("sellingPrice")
                .addSortableNumericField("originalPrice")
                .addSortableTagField("tag", "|");
        // 创建索引
        jedisSearch.createIndex(Constants.GOODS_IDX_NAME, Constants.GOODS_IDX_PREFIX, schema);
        // 删除旧数据
        List<GoodsVO> list = goodsMapper.selectUpGoodsList();
        // 添加新数据
        jedisSearch.deleteGoodsList(Constants.GOODS_IDX_PREFIX);
        return jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
    }

    @Override
    public boolean saveGoods(Goods goods) {
        this.save(goods);
        invalidateAfterCommit(goods.getGoodsId());
        return jedisSearch.addGoodsIndex(Constants.GOODS_IDX_PREFIX, goods);
    }

    @Override
    public boolean updateGoods(Goods goods) {
        boolean updated = this.updateById(goods);
        if (!updated) return false;
        invalidateAfterCommit(goods.getGoodsId());
        redisCache.deleteObject(Constants.SECKILL_GOODS_LIST_DATA);
        return jedisSearch.addGoodsIndex(Constants.GOODS_IDX_PREFIX, goods);
    }

    @Override
    public boolean reduceStock(Long goodsId, Integer goodsCount) {
        if (goodsId == null || goodsCount == null || goodsCount <= 0) {
            throw new BusinessException("商品与购买数量不合法");
        }
        boolean updated = goodsMapper.reduceStock(goodsId, goodsCount);
        if (updated) invalidateAfterCommit(goodsId);
        return updated;
    }

    @Override
    public boolean changeSellStatus(List<Long> ids, int sellStatus) {
        boolean updated = lambdaUpdate().set(Goods::getGoodsSellStatus, sellStatus).in(Goods::getGoodsId, ids).update();
        if (!updated) return false;
        ids.forEach(this::invalidateAfterCommit);
        List<GoodsVO> list = goodsMapper.selectGoodsListByIds(ids);
        return jedisSearch.addGoodsListIndex(Constants.GOODS_IDX_PREFIX, list);
    }

    private void invalidateAfterCommit(Long goodsId) {
        if (goodsId == null) return;
        Runnable invalidate = () -> {
            try { cacheService.invalidate("goods:" + goodsId); }
            catch (RuntimeException e) { log.error("商品缓存失效失败，goodsId={}", goodsId, e); }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive() && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { invalidate.run(); }
            });
        } else invalidate.run();
    }
}

