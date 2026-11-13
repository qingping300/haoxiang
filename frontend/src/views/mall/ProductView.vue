<script setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const isSeckill = computed(() => route.path.startsWith('/seckill/detail/'))
const product = ref(null)
const cartItemId = ref(0)
const goodsCount = ref(1)
const description = ref('')
const notice = ref('')
const seckillStatus = ref('')
const seckillReady = ref(false)
const seckillBusy = ref(false)
const seckillStart = ref(0)
const seckillEnd = ref(0)
const seckillMd5 = ref('')
let controller
let countdownTimer

function fallback() {
  product.value = null
  cartItemId.value = 0
  goodsCount.value = 1
  description.value = ''
  seckillStatus.value = '秒杀状态暂不可用'
  seckillReady.value = false
  seckillMd5.value = ''
  seckillStart.value = 0
  seckillEnd.value = 0
}

function readDetail(data) {
  const goods = data.goodsDetail
  if (!goods) throw new Error('商品不存在')
  product.value = {
    id: route.params.id,
    name: goods.goodsName,
    intro: goods.goodsIntro,
    image: legacyImageUrl(goods.goodsCoverImg),
    price: goods.sellingPrice,
    seckillPrice: goods.seckillPrice,
    originalPrice: goods.originalPrice,
  }
  cartItemId.value = Number(data.cartItemId) || 0
  goodsCount.value = Number(data.goodsCount) || 1
  description.value = goods.goodsDetailContent || ''
  if (isSeckill.value) {
    seckillStart.value = Number(goods.startDate) || 0
    seckillEnd.value = Number(goods.endDate) || 0
  }
}

function formatRemaining(milliseconds) {
  const seconds = Math.max(0, Math.floor(milliseconds / 1000))
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const rest = seconds % 60
  return `秒杀倒计时：${String(days).padStart(2, '0')}天 ${String(hours).padStart(2, '0')}时 ${String(minutes).padStart(2, '0')}分 ${String(rest).padStart(2, '0')}秒`
}

async function exposeSeckill() {
  if (!isSeckill.value || !product.value || !seckillStart.value || seckillReady.value || seckillBusy.value) return
  seckillBusy.value = true
  seckillStatus.value = '秒杀开始'
  try {
    const result = await legacyRequest(`/seckill/${route.params.id}/exposer`, { method: 'POST' })
    const exposer = result?.map?.exposer
    if (!exposer) throw new Error('秒杀状态暂不可用')
    if (exposer.exposed) {
      seckillMd5.value = exposer.md5 || ''
      seckillReady.value = Boolean(seckillMd5.value)
    } else {
      const now = Number(exposer.now) || Date.now()
      seckillStart.value = Number(exposer.start) || seckillStart.value
      seckillEnd.value = Number(exposer.end) || seckillEnd.value
      seckillStatus.value = now > seckillEnd.value ? '秒杀结束!' : formatRemaining(seckillStart.value - now)
    }
  } catch (error) {
    seckillStatus.value = error.message || '秒杀状态暂不可用'
  } finally {
    seckillBusy.value = false
  }
}

function updateSeckillClock() {
  if (!isSeckill.value || !seckillStart.value || seckillReady.value) return
  const now = Date.now()
  if (now > seckillEnd.value) seckillStatus.value = '秒杀结束!'
  else if (now < seckillStart.value) seckillStatus.value = formatRemaining(seckillStart.value - now)
  else exposeSeckill()
}

async function prepareSeckill() {
  if (!isSeckill.value || !seckillStart.value) return
  try {
    const result = await legacyRequest('/seckill/time/now')
    const serverNow = Number(result?.map?.now) || Date.now()
    if (serverNow > seckillEnd.value) seckillStatus.value = '秒杀结束!'
    else if (serverNow < seckillStart.value) seckillStatus.value = formatRemaining(seckillStart.value - serverNow)
    else await exposeSeckill()
    if (serverNow < seckillStart.value) countdownTimer = window.setInterval(updateSeckillClock, 1000)
  } catch (error) {
    seckillStatus.value = error.message || '秒杀状态暂不可用'
  }
}

async function load() {
  controller?.abort()
  window.clearInterval(countdownTimer)
  controller = new AbortController()
  notice.value = ''
  fallback()
  try {
    const path = isSeckill.value ? `/seckill/detail/${route.params.id}` : `/goods/detail/${route.params.id}`
    readDetail((await legacyRequest(path, { signal: controller.signal })).map || {})
    if (isSeckill.value) await prepareSeckill()
  } catch (error) {
    if (error.name !== 'AbortError') notice.value = '商城服务暂不可用，请启动后端后重试。'
  }
}

async function saveToCart(goToCart) {
  if (!product.value) return
  notice.value = ''
  try {
    await legacyRequest('/shopCart', {
      method: 'POST',
      body: { goodsId: Number(product.value.id), goodsCount: goodsCount.value, cartItemId: cartItemId.value },
    })
    if (goToCart) router.push('/shopCart')
    else {
      await load()
      notice.value = '添加成功'
    }
  } catch (error) {
    notice.value = error.message || '操作失败'
    if (error.auth) router.push({ path: '/login', query: { redirect: route.fullPath } })
  }
}

async function executeSeckill() {
  if (!seckillReady.value || !seckillMd5.value || seckillBusy.value) return
  seckillBusy.value = true
  notice.value = ''
  try {
    const result = await legacyRequest(`/seckill/${route.params.id}/${seckillMd5.value}/executionFour`, { method: 'POST' })
    const success = result?.map?.seckillSuccess
    if (!success?.seckillSuccessId || !success?.md5) throw new Error('秒杀结果不完整')
    router.push({ path: '/order-settle', query: { legacyPath: `/seckill/${success.seckillSuccessId}/${success.md5}/settle` } })
  } catch (error) {
    notice.value = error.message || '操作失败'
    if (error.auth) router.push({ path: '/login', query: { redirect: route.fullPath } })
  } finally {
    seckillBusy.value = false
  }
}

watch(() => route.fullPath, load, { immediate: true })
onUnmounted(() => {
  controller?.abort()
  window.clearInterval(countdownTimer)
})
</script>

<template>
  <div id="detail">
    <div class="dc">
      <div class="content w">
        <div class="title fl">商品详情</div>
        <nav v-if="isSeckill" class="fr">
          <li><a href="#detail">概述</a></li><li>|</li><li><a href="#goods-description">详情</a></li><li>|</li><li><a href="javascript:;">用户评价</a></li>
          <div class="clear"></div>
        </nav>
        <div class="clear"></div>
      </div>
    </div>

    <div v-if="product" class="intro mt20 w clearfix">
      <div class="left fl" style="position:relative">
        <div class="swiper-container fl"><img :src="legacyImageUrl(product.image)" :alt="product.name" /></div>
      </div>
      <div class="right fr">
        <h2 v-if="isSeckill" class="text-danger" style="padding:0 0 0 20px"><span class="glyphicon glyphicon-time"></span><span id="seckillBox" class="glyphicon">{{ seckillStatus }}</span></h2>
        <div class="h3 ml20 mt20">{{ product.name }}</div>
        <div class="sub_title mr40 ml20 mt10">{{ product.intro }}</div>
        <div v-if="isSeckill" class="item_price mr40 ml20 mt10" style="color:red;width:315px">秒杀价：<span style="font-size:28px">{{ product.seckillPrice }}.00 元</span><del>{{ product.originalPrice }}.00 元</del></div>
        <div v-else class="item_price mr40 ml20 mt10">{{ product.price }}.00 元 <del>{{ product.originalPrice }}.00 元</del></div>

        <div class="order">
          <template v-if="isSeckill"><button id="killBtn" class="btn btn-success btn-lg" type="button" :disabled="!seckillReady || seckillBusy" @click="executeSeckill">开始秒杀</button></template>
          <template v-else>
            <input id="cartItemId" type="hidden" :value="cartItemId" />
            <input id="goodsCount" type="hidden" :value="goodsCount" />
            <input class="car" type="button" value="立即选购" @click="saveToCart(true)" />
            <input class="car" type="button" value="加入购物车" @click="saveToCart(false)" />
          </template>
        </div>
        <div id="J_tbExtra" class="tb-extra ml20" :style="isSeckill ? { marginTop: '0' } : undefined">
          <dl><dt>承诺</dt><dd><a class="J_Cont" title="满足7天无理由退换货申请的前提下，包邮商品需要买家承担退货邮费，非包邮商品需要买家承担发货和退货邮费。" href="javascript:;"><img src="/mall/image/7d.jpg" alt="" />7天无理由</a></dd></dl>
          <dl><dt>支付</dt><dd><a href="javascript:;"><img src="/mall/image/hua.png" alt="" />蚂蚁花呗</a><a href="javascript:;"><img src="/mall/image/card.png" alt="" />信用卡支付</a><a href="javascript:;"><img src="/mall/image/ji.png" alt="" />集分宝</a></dd></dl>
          <dl><dt>支持</dt><dd>折旧变现，买新更省钱。<a style="float:none;text-decoration:underline" href="javascript:;">详情</a></dd></dl>
        </div>
        <p v-if="notice" role="status" style="clear:both;padding:10px 20px;color:#c43d32">{{ notice }}</p>
      </div>
      <div class="clear"></div>
    </div>
    <div v-else class="intro mt20 w clearfix">商品不存在</div>
    <div id="goods-description" class="goods mt20 w clearfix" v-html="description"></div>
  </div>
</template>
