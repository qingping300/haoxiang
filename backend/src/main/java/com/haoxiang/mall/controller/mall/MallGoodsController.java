package com.haoxiang.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.GoodsCategory;
import com.haoxiang.mall.core.entity.ShopCat;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.SearchObjVO;
import com.haoxiang.mall.core.entity.vo.SearchPageCategoryVO;
import com.haoxiang.mall.core.entity.vo.SearchPageGoodsVO;
import com.haoxiang.mall.core.service.GoodsCategoryService;
import com.haoxiang.mall.core.service.GoodsService;
import com.haoxiang.mall.core.service.CacheService;
import com.haoxiang.mall.core.service.ShopCatService;
import com.haoxiang.mall.redis.JedisSearch;
import com.haoxiang.mall.util.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.haoxiang.mall.util.R;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.SearchResult;

import java.util.*;

/**
 * 前台商城-商品模块控制器
 */
@Slf4j
@RestController
public class MallGoodsController extends BaseController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private GoodsCategoryService goodsCategoryService;

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private JedisSearch jedisSearch;

    @GetMapping("/search")
    public R rsSearch(SearchObjVO searchObjVO, HttpServletRequest request) {
        log.info("search begin searchObjVO:{}", searchObjVO);
        Page<SearchPageGoodsVO> page = getPage(request, Constants.GOODS_SEARCH_PAGE_LIMIT);
        String keyword = searchObjVO.getKeyword();
        long size = page.getSize();
        SearchResult query;
        IPage<Goods> iPage = new Page<>(page.getCurrent(), size);
        try {
            query = jedisSearch.search(Constants.GOODS_IDX_NAME, searchObjVO, page);
            List<Goods> list = new ArrayList<>();
            for (Document document : query.getDocuments()) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, Object> property : document.getProperties()) {
                    String key = property.getKey();
                    Object value = property.getValue();
                    map.put(key, value);
                }
                Goods goods = MyBeanUtil.toBean(map, Goods.class);
                list.add(goods);
            }
            long totalResults = query.getTotalResults();
            iPage.setTotal(totalResults);
            iPage.setRecords(list);
            iPage.setPages((long) Math.ceil(totalResults / (double) size));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            iPage = goodsService.findMallGoodsListBySearch(page, searchObjVO);
        }
        Long goodsCategoryId = searchObjVO.getGoodsCategoryId();
        request.setAttribute("keyword", keyword);
        request.setAttribute("pageResult", iPage);
        request.setAttribute("orderBy", searchObjVO.getSidx());
        request.setAttribute("order", searchObjVO.getOrder());
        return pageData(request);
    }

    private R goodsCategoryHandler(HttpServletRequest request, Long goodsCategoryId) {
        if (goodsCategoryId != null) {
            Goods goods = new Goods();
            goods.setGoodsCategoryId(goodsCategoryId);
            SearchPageCategoryVO searchPageCategoryVO = new SearchPageCategoryVO();
            GoodsCategory thirdCategory = goodsCategoryService.getById(goodsCategoryId);
            GoodsCategory secondCategory = goodsCategoryService.getById(thirdCategory.getParentId());
            List<GoodsCategory> thirdCateGoryList = goodsCategoryService.list(new QueryWrapper<GoodsCategory>()
                    .eq("parent_id", thirdCategory.getParentId()));
            searchPageCategoryVO.setCurrentCategoryName(thirdCategory.getCategoryName());
            searchPageCategoryVO.setSecondLevelCategoryName(secondCategory.getCategoryName());
            searchPageCategoryVO.setThirdLevelCategoryList(thirdCateGoryList);
            request.setAttribute("goodsCategoryId", goodsCategoryId);
            request.setAttribute("searchPageCategoryVO", searchPageCategoryVO);
        }
        return pageData(request);
    }

    @GetMapping("/goods/detail/{goodsId}")
    public R detail(@PathVariable("goodsId") Long goodsId, HttpServletRequest request) {
        Goods goods = cacheService.get("goods:" + goodsId, Goods.class, () -> goodsService.getById(goodsId));
        request.setAttribute("cartItemId", 0);
        request.setAttribute("goodsCount", 0);
        // 查询购物车中是否有该商品
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (userVO != null) {
            ShopCat shopCat = shopCatService.selectByUserIdAndGoodsId(userVO.getUserId(), goodsId);
            if (Objects.nonNull(shopCat)) {
                request.setAttribute("cartItemId", shopCat.getCartItemId());
                request.setAttribute("goodsCount", shopCat.getGoodsCount());
            }
        }
        request.setAttribute("goodsDetail", goods);
        return pageData(request);
    }
}
