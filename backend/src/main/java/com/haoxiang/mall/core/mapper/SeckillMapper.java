package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.SeckillVO;

import java.util.Date;

public interface SeckillMapper extends BaseMapper<Seckill> {

    IPage<Seckill> selectListPage(Page<Seckill> page, SeckillVO seckill);

    /**
     * 秒杀商品减扣库存
     *
     * @param seckillId 秒杀ID
     * @param now       当前时间戳
     * @return boolean
     */
    boolean reduceStock(Long seckillId, Date now);

    /**
     * 添加秒杀商品库存
     *
     * @param seckillId 秒杀ID
     * @return boolean
     */
    boolean addStock(Long seckillId);
}
