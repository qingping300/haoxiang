<script setup>
import { onMounted, ref } from 'vue'
import { legacyRequest } from '../../services/legacy.js'

const coupons = ref([])
const error = ref('')
const success = ref('')
const busy = ref(false)

async function load() {
  try {
    coupons.value = (await legacyRequest('/coupon')).map?.coupons || []
    error.value = ''
  } catch (cause) {
    error.value = cause.message || '优惠券暂时无法显示。'
  }
}

async function receive(coupon) {
  if (busy.value || coupon.saleOut || coupon.hasReceived) return
  busy.value = true
  error.value = ''
  success.value = ''
  try {
    await legacyRequest('/coupon/' + coupon.couponId, { method: 'POST' })
    success.value = '领取成功'
    await load()
  } catch (cause) {
    error.value = cause.message || '领取失败。'
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <div id="content">
    <div id="coupon">
      <h2>为你推荐优惠券</h2>
      <ul>
        <li v-for="coupon in coupons" :key="coupon.couponId">
          <a href="javascript:;" @click.prevent="receive(coupon)">
            <div class="left-desc">
              <div class="q-price"><em>¥</em><strong>{{ coupon.discount }}</strong><span class="q-limit">满{{ coupon.min }}元可用</span></div>
              <div class="q-name">{{ coupon.name }}</div>
              <div class="q-desc"><div :title="coupon.couponDesc">{{ coupon.couponDesc }}</div><div>有效期：{{ coupon.days }}天</div></div>
            </div>
            <div class="right-desc" :style="coupon.saleOut ? { backgroundColor: 'gray' } : coupon.hasReceived ? { backgroundColor: 'lightgray' } : {}">
              {{ coupon.saleOut ? '领完了' : coupon.hasReceived ? '已领取' : '立即领取' }}
            </div>
          </a>
        </li>
      </ul>
      <p v-if="success" role="status" class="legacy-error center">{{ success }}</p>
      <p v-if="error" role="alert" class="legacy-error center">{{ error }} <button type="button" @click="load">重试</button></p>
    </div>
  </div>
</template>
