<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const discount = ref(0)
const error = ref('')
const busy = ref(false)

async function load() {
  try {
    const data = (await legacyRequest('/orders/' + encodeURIComponent(route.params.orderNo))).map || {}
    order.value = data.orderDetailVO || null
    discount.value = Number(data.discount) || 0
    error.value = ''
  } catch (cause) {
    error.value = cause.auth ? '请先登录后查看订单。' : cause.message || '订单详情暂时无法显示。'
  }
}

async function action(endpoint) {
  if (!order.value || busy.value) return
  if (endpoint === 'pay') { router.push('/pay-select/' + encodeURIComponent(order.value.orderNo)); return }
  if (endpoint === 'cancel' && !window.confirm('确认要取消订单吗？')) return
  busy.value = true
  try {
    await legacyRequest(`/orders/${encodeURIComponent(order.value.orderNo)}/${endpoint}`, { method: 'PUT' })
    await load()
  } catch (cause) {
    error.value = cause.message || '操作失败。'
  } finally {
    busy.value = false
  }
}

async function logout() {
  await legacyRequest('/logout')
  router.push('/login')
}

const formatTime = value => value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : ''
watch(() => route.params.orderNo, load, { immediate: true })
</script>

<template>
  <div id="personal">
    <div class="self-info center">
      <div class="lfnav fl"><div class="nav_title">个人中心</div><div class="title_list"><ul><li><RouterLink to="/personal">个人信息</RouterLink></li><li><RouterLink class="active" to="/orders">我的订单</RouterLink></li><li><a href="javascript:;" @click.prevent="logout">退出登录</a></li></ul></div></div>
      <div class="intro fr"><div class="uc-box uc-main-box"><div class="uc-content-box order-view-box">
        <div class="box-hd"><h1 class="title">订单详情 <small>请谨防钓鱼链接或诈骗电话，<a href="javascript:;">了解更多&gt;</a></small></h1><div class="more clearfix"><h2 class="subtitle">订单号：{{ order?.orderNo || route.params.orderNo }}</h2><div v-if="order" class="actions"><a v-if="order.orderStatus > 0 && order.orderStatus < 3" class="btn btn-small btn-line-gray" href="javascript:;" title="取消订单" @click.prevent="action('cancel')">取消订单</a><a v-if="order.orderStatus === 0" class="btn btn-small btn-primary" href="javascript:;" title="去支付" @click.prevent="action('pay')">去支付</a><a v-if="order.orderStatus === 3" class="btn btn-small btn-primary" href="javascript:;" title="确认收货" @click.prevent="action('finish')">确认收货</a></div></div></div>
        <div v-if="order" class="box-bd"><div class="uc-order-item uc-order-item-pay"><div class="order-detail">
          <div class="order-summary"><div class="order-status">{{ order.orderStatusString }}</div><div class="order-desc">{{ order.orderStatus < 0 ? '交易已关闭~' : order.orderStatus === 0 ? '请尽快完成支付哦~' : order.orderStatus === 4 ? '交易成功，感谢您对好享商城的支持~' : '订单正在处理中~' }}</div><div v-if="order.orderStatus >= 0" class="order-progress"><ol class="progress-list clearfix progress-list-5"><li v-for="(step, index) in ['下单', '付款', '配货', '出库', '交易成功']" :key="step" class="step" :class="order.orderStatus === index ? 'step-active' : order.orderStatus > index ? 'step-done' : ''"><div class="progress"><span class="text">{{ step }}</span></div><div class="info">{{ index === 0 ? formatTime(order.createTime) : '' }}</div></li></ol></div></div>
          <table class="order-items-table"><tbody><tr v-for="item in order.haoxiangMallOrderItemVOS || []" :key="item.orderItemId"><td class="col col-thumb"><div class="figure figure-thumb"><RouterLink :to="`/goods/detail/${item.goodsId}`"><img :src="legacyImageUrl(item.goodsCoverImg)" width="80" height="80" :alt="item.goodsName" /></RouterLink></div></td><td class="col col-name"><p class="name"><RouterLink :to="`/goods/detail/${item.goodsId}`">{{ item.goodsName }}</RouterLink></p></td><td class="col col-price"><p class="price">{{ item.sellingPrice }}元 x {{ item.goodsCount }}</p></td></tr></tbody></table>
        </div><div id="editAddr" class="order-detail-info"><h3>收货信息</h3><table class="info-table"><tbody><tr><td>{{ order.userAddress }}</td></tr></tbody></table></div><div class="order-detail-info"><h3>支付方式</h3><table class="info-table"><tbody><tr><td>{{ order.payTypeString || '在线支付' }}</td></tr></tbody></table></div><div class="order-detail-total"><table class="total-table"><tbody><tr v-if="discount"><th>优惠券抵扣：</th><td><span class="num">{{ discount.toFixed(2) }}</span>元</td></tr><tr><th>应付金额：</th><td><span class="num">{{ Number(order.totalPrice || 0).toFixed(2) }}</span>元</td></tr></tbody></table></div></div></div>
      </div></div></div>
      <div class="clear"></div>
    </div>
    <p v-if="error" role="alert" class="legacy-error center">{{ error }} <RouterLink to="/login?redirect=/orders">登录</RouterLink> <button type="button" @click="load">重试</button></p>
  </div>
</template>
