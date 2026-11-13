<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const orders = ref([])
const page = ref({ current: 1, pages: 0 })
const error = ref('')

async function load() {
  try {
    const number = Math.max(1, Number(route.query.pageNumber) || 1)
    const result = (await legacyRequest('/orders?pageNumber=' + number)).map?.orderPageResult || {}
    orders.value = result.records || []
    page.value = { current: Number(result.current) || 1, pages: Number(result.pages) || 0 }
    error.value = ''
  } catch (cause) {
    error.value = cause.auth ? '请先登录后查看订单。' : cause.message || '订单暂时无法显示。'
  }
}

async function logout() {
  await legacyRequest('/logout')
  router.push('/login')
}

const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
watch(() => route.query.pageNumber, load, { immediate: true })
</script>

<template>
  <div id="personal">
    <div class="self-info center">
      <div class="lfnav fl"><div class="nav_title">个人中心</div><div class="title_list"><ul><li><RouterLink to="/personal">个人信息</RouterLink></li><li><RouterLink class="active" to="/orders">我的订单</RouterLink></li><li><a href="javascript:;" @click.prevent="logout">退出登录</a></li></ul></div></div>
      <div class="intro fr"><div class="uc-box uc-main-box"><div class="uc-content-box order-list-box">
        <div class="box-hd"><h1 class="title">我的订单 <small>请谨防钓鱼链接或诈骗电话，<a href="javascript:;">了解更多&gt;</a></small></h1></div>
        <div class="box-bd"><ul class="order-list">
          <li v-for="order in orders" :key="order.orderNo" class="uc-order-item uc-order-item-list">
            <div class="order-detail"><div class="order-summary"><div :class="order.orderStatus < 0 ? 'order-status-closed' : order.orderStatus === 0 ? 'order-status-need-pay' : 'order-status-success'"></div> {{ order.orderStatusString }}</div></div>
            <table class="order-detail-table"><thead><tr><th class="col-main"><p class="caption-info">{{ formatTime(order.createTime) }} <span class="sep">|</span>订单号：<RouterLink :to="`/orders/${order.orderNo}`">{{ order.orderNo }}</RouterLink><span class="sep">|</span>在线支付</p></th><th class="col-sub"><p class="caption-price">应付金额：<span class="num">{{ Number(order.totalPrice || 0).toFixed(2) }}</span>元</p></th></tr></thead>
              <tbody><tr><td class="order-items"><ul class="goods-list"><li v-for="item in order.haoxiangMallOrderItemVOS || []" :key="item.orderItemId"><div class="figure figure-thumb"><RouterLink :to="`/goods/detail/${item.goodsId}`"><img :src="legacyImageUrl(item.goodsCoverImg)" width="80" height="80" :alt="item.goodsName" /></RouterLink></div><p class="name"><RouterLink :to="`/goods/detail/${item.goodsId}`">{{ item.goodsName }}</RouterLink></p><p class="price">{{ item.sellingPrice }}元 x {{ item.goodsCount }}</p></li></ul></td><td class="order-actions"><RouterLink class="btn btn-small btn-line-gray" :to="`/orders/${order.orderNo}`">订单详情</RouterLink></td></tr></tbody>
            </table>
          </li>
        </ul><div v-if="!orders.length" style="text-align:center;padding:60px 0"><img src="/mall/image/null-content.png" alt="暂无订单" /></div></div>
        <div v-if="page.pages > 1" id="J_orderListPages"><div class="newbee-pagenavi"><RouterLink v-for="number in page.pages" :key="number" class="numbers" :class="{ current: page.current === number }" :to="{ path: '/orders', query: { pageNumber: number } }">{{ number }}</RouterLink></div></div>
      </div></div></div>
      <div class="clear"></div>
    </div>
    <p v-if="error" role="alert" class="legacy-error center">{{ error }} <RouterLink to="/login?redirect=/orders">登录</RouterLink> <button type="button" @click="load">重试</button></p>
  </div>
</template>
