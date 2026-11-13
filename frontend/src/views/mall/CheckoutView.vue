<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const items = ref([])
const coupons = ref([])
const user = ref(null)
const settlement = ref({})
const baseTotal = ref(0)
const selectedCoupon = ref(null)
const address = ref('')
const error = ref('')
const loginRequired = ref(false)
const busy = ref(false)
const modalOpen = ref(false)
const total = computed(() => baseTotal.value - Number(selectedCoupon.value?.discount || 0))

function settlePath() {
  const path = typeof route.query.legacyPath === 'string' ? route.query.legacyPath : ''
  return /^\/seckill\/\d+\/[0-9a-zA-Z]+\/settle$/.test(path) ? path : '/shopCart/settle'
}

async function load() {
  try {
    const [settleResult, infoResult] = await Promise.all([legacyRequest(settlePath()), legacyRequest('/personal/info')])
    const data = settleResult.map || {}
    settlement.value = data
    items.value = data.myShoppingCartItems || []
    coupons.value = data.coupons || []
    baseTotal.value = Number(data.priceTotal) || 0
    user.value = infoResult.map?.userInfo || null
    address.value = user.value?.address || ''
    error.value = ''
    loginRequired.value = false
  } catch (cause) {
    error.value = cause.auth ? '请先登录后结算。' : cause.message || '结算页面暂时无法显示。'
    loginRequired.value = Boolean(cause.auth)
  }
}

function chooseCoupon(coupon) {
  if (selectedCoupon.value?.couponUserId === coupon.couponUserId) {
    selectedCoupon.value = null
    return
  }
  if (baseTotal.value - Number(coupon.discount) <= 0) { error.value = '订单价格不能小于等于 0 元。'; return }
  selectedCoupon.value = coupon
  error.value = ''
}

async function saveAddress() {
  if (busy.value) return
  if (address.value.trim().length < 6) { error.value = '请输入正确的收货信息。'; return }
  busy.value = true
  try {
    await legacyRequest('/personal/updateInfo', { method: 'POST', body: { userId: user.value.userId, address: address.value.trim() } })
    modalOpen.value = false
    await load()
  } catch (cause) {
    error.value = cause.message || '修改收货地址失败。'
    loginRequired.value = Boolean(cause.auth)
  } finally {
    busy.value = false
  }
}

async function submitOrder() {
  if (busy.value) return
  if ((user.value?.address || '').trim().length < 6) { error.value = '请先填写正确的收货信息。'; return }
  const data = settlement.value
  const path = data.isSeckillSettle && data.seckillSuccessId && data.seckillSecretKey
    ? `/seckillSaveOrder/${encodeURIComponent(data.seckillSuccessId)}/${encodeURIComponent(data.seckillSecretKey)}`
    : `/saveOrder?couponUserId=${encodeURIComponent(selectedCoupon.value?.couponUserId || '')}`
  busy.value = true
  error.value = ''
  loginRequired.value = false
  try {
    const orderNo = (await legacyRequest(path)).map?.orderNo
    if (!orderNo) throw new Error('下单成功，但未返回订单号。')
    for (let attempt = 0; attempt < 15; attempt++) {
      await new Promise(resolve => setTimeout(resolve, 200))
      try {
        await legacyRequest('/saveOrder/result/' + encodeURIComponent(orderNo))
        await router.push('/orders/' + encodeURIComponent(orderNo))
        return
      } catch (cause) {
        if (cause.message !== '正在处理') throw cause
      }
    }
    error.value = '订单仍在处理，请前往我的订单查看。'
  } catch (cause) {
    error.value = cause.message || '提交订单失败。'
    loginRequired.value = Boolean(cause.auth)
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <div id="personal">
    <div class="self-info center">
      <div class="lfnav fl"><div class="nav_title">个人中心</div><div class="title_list"><ul><li><RouterLink to="/personal">个人信息</RouterLink></li><li><RouterLink to="/orders">我的订单</RouterLink></li><li><RouterLink to="/coupon">优惠券</RouterLink></li></ul></div></div>
      <div class="intro fr"><div class="uc-box uc-main-box"><div class="uc-content-box order-view-box">
        <div class="box-hd"><h1 class="title">填写并核对订单信息</h1><div class="more clearfix"><div class="actions"><button id="saveOrder" type="button" class="btn btn-small btn-primary" title="提交订单" :disabled="busy" @click="submitOrder">{{ busy ? '提交中…' : '提交订单' }}</button></div></div></div>
        <p v-if="error" role="alert" class="checkout-error">{{ error }} <RouterLink v-if="loginRequired" :to="{ path: '/login', query: { redirect: route.fullPath } }">登录</RouterLink></p>
        <div class="box-bd"><div class="uc-order-item uc-order-item-pay"><div class="order-detail">
          <div class="order-summary"><div class="order-progress"><ol class="progress-list clearfix progress-list-5"><li v-for="(step, index) in ['购物车', '下单', '付款', '出库', '交易成功']" :key="step" class="step" :class="index === 0 ? 'step-done' : index === 1 ? 'step-active' : ''"><div class="progress"><span class="text">{{ step }}</span></div><div class="info"></div></li></ol></div></div>
          <table class="order-items-table"><tbody><tr v-for="item in items" :key="item.cartItemId || item.goodsId"><td class="col col-thumb"><div class="figure figure-thumb"><RouterLink :to="`/goods/detail/${item.goodsId}`"><img :src="legacyImageUrl(item.goodsCoverImg)" width="80" height="80" :alt="item.goodsName" /></RouterLink></div></td><td class="col col-name"><p class="name"><RouterLink :to="`/goods/detail/${item.goodsId}`">{{ item.goodsName }}</RouterLink></p></td><td class="col col-price"><p class="price">{{ item.sellingPrice }}元 x {{ item.goodsCount }}</p></td></tr></tbody></table>
        </div>
        <div v-if="coupons.length" class="order-detail-info"><h3>可用优惠券</h3><div id="my-coupon"><ul><li v-for="coupon in coupons" :key="coupon.couponUserId" :class="{ active: selectedCoupon?.couponUserId === coupon.couponUserId }"><a href="javascript:;" @click.prevent="chooseCoupon(coupon)"><div class="left-desc"><div class="q-price"><em>¥</em><strong>{{ coupon.discount }}</strong><span class="q-limit">满{{ coupon.min }}元可用</span></div><div class="q-name">{{ coupon.name }}</div><div class="q-desc"><div :title="coupon.couponDesc">{{ coupon.couponDesc }}</div><div>有效期：{{ coupon.endTime?.slice(0, 10) }}</div></div></div></a></li></ul></div></div>
        <div id="editAddr" class="order-detail-info"><h3>收货信息</h3><table class="info-table"><tbody><tr><th>收货地址：</th><td class="user_address_label">{{ user?.address || '无' }}</td></tr></tbody></table><div class="actions"><a class="btn btn-small btn-line-gray J_editAddr" href="javascript:;" @click.prevent="modalOpen = true">修改</a></div></div>
        <div id="editTime" class="order-detail-info"><h3>支付方式</h3><table class="info-table"><tbody><tr><th>支付方式：</th><td>在线支付</td></tr></tbody></table></div>
        <div class="order-detail-total"><table class="total-table"><tbody><tr v-if="selectedCoupon"><th>优惠券抵扣：</th><td><span class="num">{{ Number(selectedCoupon.discount).toFixed(2) }}</span>元</td></tr><tr><th>商品总价：</th><td><span class="num">{{ baseTotal.toFixed(2) }}</span>元</td></tr><tr><th>运费：</th><td><span class="num">0</span>元</td></tr><tr><th class="total">应付金额：</th><td class="total"><span id="priceTotal" class="num">{{ total.toFixed(2) }}</span>元</td></tr></tbody></table></div>
      </div></div></div></div></div>
      <div class="clear"></div>
    </div>
    <div v-if="modalOpen" id="personalInfoModal" class="modal fade in show" style="display:block" role="dialog" aria-modal="true"><div class="modal-dialog" role="document"><div class="modal-content"><div class="modal-header"><button type="button" class="close" @click="modalOpen = false">×</button><h6 class="modal-title">收货信息修改</h6></div><div class="modal-body"><div class="form-group"><label for="address">收货信息:</label><input id="address" v-model="address" type="text" class="form-control" /></div></div><div class="modal-footer"><button type="button" class="btn btn-default" @click="modalOpen = false">取消</button><button id="saveButton" type="button" class="btn btn-primary" :disabled="busy" @click="saveAddress">确认</button></div></div></div></div>
    <div v-if="modalOpen" class="modal-backdrop fade in" @click="modalOpen = false"></div>
  </div>
</template>

<style scoped>
.checkout-error {
  margin: 14px 0 0;
  padding: 10px 14px;
  background: #fff4f4;
  color: #b92f2f;
}

#saveOrder:disabled {
  cursor: wait;
  opacity: .75;
}
</style>
