package com.haoxiang.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Carousels;
import com.haoxiang.mall.core.entity.vo.GoodsVO;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.CarouselsService;
import com.haoxiang.mall.core.service.GoodsCategoryService;
import com.haoxiang.mall.core.service.IndexConfigService;
import com.haoxiang.mall.enums.IndexConfigTypeEnum;
import com.haoxiang.mall.recommend.service.RecommendService;
import com.haoxiang.mall.util.R;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import static com.haoxiang.mall.constant.Constants.*;

/**
 * 前台商城-首页
 */
@RestController
@AllArgsConstructor
public class MallIndexController extends BaseController {

    private GoodsCategoryService goodsCategoryService;
    private CarouselsService carouselsService;
    private IndexConfigService indexConfigService;
    private RecommendService recommendService;

    @GetMapping("index")
    public R index(HttpServletRequest request, HttpSession session) {
        LambdaQueryWrapper<Carousels> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Carousels::getIsDeleted, 0).orderByAsc(Carousels::getCarouselRank);
        // 分类数据
        request.setAttribute(CATEGORIES, goodsCategoryService.treeList());
        // 轮播图
        request.setAttribute(CAROUSELS, carouselsService.list(queryWrapper));
        // 热销商品
        request.setAttribute(HOT_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_HOT, INDEX_GOODS_HOT_NUMBER));
        // 新品
        request.setAttribute(NEW_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_NEW, INDEX_GOODS_NEW_NUMBER));
        // 推荐商品
        if (session.getAttribute(Constants.MALL_USER_SESSION_KEY) instanceof MallUserVO mallUserVO) {
            List<GoodsVO> goodsList = recommendService.recommendGoods(mallUserVO.getUserId(), INDEX_GOODS_RECOMMOND_NUMBER);
            if (goodsList.isEmpty()) {
                goodsList = indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND, INDEX_GOODS_RECOMMOND_NUMBER);
            }
            request.setAttribute(RECOMMEND_GOODSES, goodsList);
        } else {
            request.setAttribute(RECOMMEND_GOODSES, indexConfigService.listIndexConfig(IndexConfigTypeEnum.INDEX_GOODS_RECOMMOND, INDEX_GOODS_RECOMMOND_NUMBER));
        }
        return pageData(request);
    }
}
