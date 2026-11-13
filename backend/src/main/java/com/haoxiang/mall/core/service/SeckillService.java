package com.haoxiang.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.ExposerVO;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.SeckillSuccessVO;
import com.haoxiang.mall.core.entity.vo.SeckillVO;

import java.util.Date;

public interface SeckillService extends IService<Seckill> {

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param seckillVO 秒杀对象
     * @return 分页数据
     */
    IPage<Seckill> selectPage(Page<Seckill> page, SeckillVO seckillVO);

    /**
     * 执行秒杀最终逻辑
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    用户VO
     * @return 用户秒杀成功VO
     */
    SeckillSuccessVO executeSeckillFinal(Long seckillId, MallUserVO userVO);

    /**
     * 秒杀地址暴露接口
     *
     * @param seckillId 秒杀商品ID
     * @return 秒杀服务接口地址暴露类
     */
    ExposerVO exposerUrl(Long seckillId);

    /**
     * 添加秒杀商品库存
     *
     * @param seckillId 秒杀商品ID
     * @return boolean
     */
    boolean addStock(Long seckillId);

    /**
     * 扣减库存
     *
     * @param seckillId 秒杀商品ID
     * @param now       当前时间
     * @return boolean
     */
    boolean reduceStock(Long seckillId, Date now);

}
