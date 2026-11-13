<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const router = useRouter()
const items = ref([])
const itemsTotal = ref(0)
const priceTotal = ref(0)
const error = ref('')
const busy = ref(false)

async function load() {
  try {
    const data = (await legacyRequest('/shopCart')).map || {}
    items.value = data.myShoppingCartItems || []
    itemsTotal.value = Number(data.itemsTotal) || 0
    priceTotal.value = Number(data.priceTotal) || 0
    error.value = ''
  } catch (cause) {
    error.value = cause.auth ? '请先登录后查看购物车。' : cause.message || '购物车暂时无法显示。'
  }
}

async function changeCount(item, event) {
  if (busy.value) return
  const goodsCount = Number(event.target.value)
  if (!Number.isInteger(goodsCount) || goodsCount < 1 || goodsCount > 5) {
    await load()
    error.value = '单个商品最多购买 5 件，数量不能少于 1 件。'
    return
  }
  busy.value = true
  try {
    await legacyRequest('/shopCart', { method: 'PUT', body: { cartItemId: item.cartItemId, goodsCount } })
    await load()
  } catch (cause) {
    error.value = cause.message || '修改数量失败。'
  } finally {
    busy.value = false
  }
}

async function remove(item) {
  if (busy.value || !window.confirm('确认要删除商品吗？')) return
  busy.value = true
  try {
    await legacyRequest('/shopCart/' + item.cartItemId, { method: 'DELETE' })
    await load()
  } catch (cause) {
    error.value = cause.message || '删除失败。'
  } finally {
    busy.value = false
  }
}

function settle() {
  if (itemsTotal.value) router.push('/order-settle')
  else error.value = '购物车中无商品，无法结算。'
}

onMounted(load)
</script>

<template>
  <div id="cart">
    <div class="banner_x center">
      <RouterLink to="/"><div class="logo fl"><img src="/mall/image/new-bee-logo-3.png" alt="好享商城" /></div></RouterLink>
      <div class="wdgwc fl ml20">购物车</div>
      <div class="wxts fl ml20">温馨提示：产品是否购买成功，以最终下单为准哦，请尽快结算</div>
      <div class="clear"></div>
    </div>
    <div class="cart_line"></div>
    <div class="cart_bg">
      <div class="list center">
        <img v-if="!items.length" style="position:absolute;margin-top:16px;left:45%" src="/mall/image/null-content.png" alt="购物车为空" />
        <template v-else>
          <div class="top2 center"><div class="sub_top fl"></div><div class="sub_top fl">商品名称</div><div class="sub_top fl">单价</div><div class="sub_top fl">数量</div><div class="sub_top fl">小计</div><div class="sub_top fr">操作</div><div class="clear"></div></div>
          <div v-for="item in items" :key="item.cartItemId" class="content2 center">
            <div class="sub_content fl"></div>
            <div class="sub_content cover fl"><img :src="legacyImageUrl(item.goodsCoverImg)" :alt="item.goodsName" /></div>
            <div class="sub_content fl ft20"><RouterLink :to="`/goods/detail/${item.goodsId}`">{{ item.goodsName }}</RouterLink></div>
            <div class="sub_content fl">{{ item.sellingPrice }}元</div>
            <div class="sub_content fl"><input class="goods_count" :id="`goodsCount${item.cartItemId}`" type="number" :value="item.goodsCount" step="1" min="1" max="5" @change="changeCount(item, $event)" /></div>
            <div class="sub_content fl">{{ item.goodsCount * item.sellingPrice }}元</div>
            <div class="sub_content fl"><a href="javascript:;" @click.prevent="remove(item)">×</a></div>
            <div class="clear"></div>
          </div>
        </template>
      </div>
      <div class="pre_order mt20 center">
        <div class="tips fl ml20"><ul><li><RouterLink to="/">继续购物</RouterLink></li><li>|</li><li>共<span>{{ itemsTotal }}</span>件商品</li><div class="clear"></div></ul></div>
        <div class="order_div fr"><div class="order_total fl">合计（不含运费）：<span>{{ priceTotal.toFixed(2) }}元</span></div><div class="order_button fr"><input :class="itemsTotal ? 'order_button_d' : 'order_button_c'" type="button" value="去结算" @click="settle" /></div><div class="clear"></div></div>
        <div class="clear"></div>
      </div>
    </div>
    <p v-if="error" role="alert" class="legacy-error center">{{ error }} <RouterLink to="/login?redirect=/shopCart">登录</RouterLink> <button type="button" @click="load">重试</button></p>
  </div>
</template>
