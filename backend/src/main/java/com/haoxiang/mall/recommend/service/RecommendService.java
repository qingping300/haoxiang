package com.haoxiang.mall.recommend.service;

import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.vo.GoodsVO;
import com.haoxiang.mall.core.entity.vo.IndexConfigGoodsVO;
import com.haoxiang.mall.recommend.dto.ProductDTO;
import com.haoxiang.mall.recommend.dto.RelateDTO;

import java.util.List;

public interface RecommendService {

    List<ProductDTO> getProductData();

    List<RelateDTO> getRelateData();

    List<GoodsVO> recommendGoods(Long userId, Integer num);
}
