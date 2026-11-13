<script setup>
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formBody, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const orderNo = String(route.query.orderNo || route.params.orderNo || '')
const totalPrice = ref(null)
const error = ref('')
const loading = ref(false)
const paying = ref(false)
const dialogOpen = ref(false)
const paymentMethod = ref('')
const password = ref('')
const confirmPassword = ref('')
const dialogError = ref('')
const dialog = ref(null)
const passwordInput = ref(null)
let trigger = null

async function load() {
  if (!orderNo) { error.value = '缺少订单号。'; return }
  loading.value = true
  try {
    const result = await legacyRequest('/selectPayType?orderNo=' + encodeURIComponent(orderNo))
    const amount = Number(result.map?.totalPrice)
    if (result.map?.totalPrice == null || !Number.isFinite(amount)) {
      throw new Error('订单金额暂时无法获取。')
    }
    totalPrice.value = amount
    error.value = ''
  } catch (cause) {
    totalPrice.value = null
    error.value = cause.auth ? '请先登录后查看订单。' : cause.message || '支付方式页面暂时无法显示。'
  } finally {
    loading.value = false
  }
}

async function openPayment(method, event) {
  if (totalPrice.value === null || paying.value) return
  trigger = event.currentTarget
  paymentMethod.value = method
  password.value = ''
  confirmPassword.value = ''
  dialogError.value = ''
  dialogOpen.value = true
  await nextTick()
  dialog.value?.showModal()
  passwordInput.value?.focus()
}

function closePayment() {
  if (paying.value) return
  dialog.value?.close()
}

function onDialogClose() {
  dialogOpen.value = false
  password.value = ''
  confirmPassword.value = ''
  dialogError.value = ''
  trigger?.focus()
}

async function submitPayment() {
  if (paying.value) return
  if (!password.value || !confirmPassword.value) {
    dialogError.value = '请填写并再次确认登录密码。'
    return
  }
  if (password.value !== confirmPassword.value) {
    dialogError.value = '两次输入的密码不一致，请重新确认。'
    return
  }
  paying.value = true
  dialogError.value = ''
  try {
    await legacyRequest('/payPage', {
      method: 'POST',
      body: formBody({ orderNo, password: password.value, payType: 1 }),
    })
  } catch (cause) {
    dialogError.value = cause.message || '支付确认失败，请重试。'
    password.value = ''
    confirmPassword.value = ''
    await nextTick()
    passwordInput.value?.focus()
    paying.value = false
    return
  }
  paying.value = false
  dialog.value?.close()
  await router.push('/orders/' + encodeURIComponent(orderNo))
}

onBeforeUnmount(() => dialog.value?.close())
load()
</script>

<template>
  <div class="page-main">
    <div class="container confirm-box">
      <div class="section section-order">
        <div class="order-info clearfix">
          <div class="fl">
            <h2 class="title">订单提交成功！去付款咯～</h2>
            <p class="order-time">订单号：{{ orderNo || '—' }}</p>
          </div>
          <div class="fr">
            <p class="total">应付总额：<span class="money"><em>{{ totalPrice === null ? '—' : totalPrice.toFixed(2) }}</em>元</span></p>
          </div>
        </div>
      </div>
      <div class="section section-payment">
        <div class="cash-title">选择以下支付方式付款</div>
        <div class="payment-box">
          <div class="payment-header clearfix"><h3 class="title">支付平台</h3></div>
          <div class="payment-body">
            <ul class="clearfix payment-list J_paymentList">
              <li class="J_alipay"><button type="button" class="payment-method" :disabled="loading || totalPrice === null" aria-label="选择支付宝支付" @click="openPayment('支付宝', $event)"><img src="/mall/image/pay/payOnline_zfb.png" alt="支付宝"></button></li>
              <li id="J_weixin"><button type="button" class="payment-method" :disabled="loading || totalPrice === null" aria-label="选择微信支付" @click="openPayment('微信支付', $event)"><img src="/mall/image/pay/weixinpay.png" alt="微信支付"></button></li>
              <li class="J_bank"><button type="button" class="payment-method" aria-label="选择银联支付" @click="error = '该支付方式暂不支持，请选择支付宝或微信支付。'"><img src="/mall/image/pay/unionpay.png" alt="银联"></button></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <p v-if="error" role="alert" class="legacy-error center">{{ error }} <a :href="'/orders/' + encodeURIComponent(orderNo)">查看订单</a> <button type="button" @click="load">重试</button></p>
    <Teleport to="body">
      <dialog v-if="dialogOpen" ref="dialog" class="payment-dialog" aria-labelledby="payment-dialog-title" @close="onDialogClose" @cancel="paying && $event.preventDefault()">
        <form class="payment-dialog-content" novalidate @submit.prevent="submitPayment">
          <button type="button" class="payment-dialog-close" aria-label="关闭支付确认" :disabled="paying" @click="closePayment">×</button>
          <h2 id="payment-dialog-title">确认支付</h2>
          <p class="payment-dialog-subtitle">已选择{{ paymentMethod }} · 使用登录密码完成验证</p>
          <div class="payment-dialog-summary"><span>本次应付</span><strong>¥{{ totalPrice?.toFixed(2) }}</strong></div>
          <label for="payment-password">登录密码</label>
          <input id="payment-password" ref="passwordInput" v-model="password" type="password" autocomplete="current-password" placeholder="请输入登录密码" :disabled="paying" required>
          <label for="payment-confirm-password">再次确认密码</label>
          <input id="payment-confirm-password" v-model="confirmPassword" type="password" autocomplete="off" placeholder="请再次输入登录密码" :disabled="paying" required>
          <p v-if="dialogError" class="payment-dialog-error" role="alert">{{ dialogError }}</p>
          <div class="payment-dialog-actions">
            <button type="button" class="payment-cancel" :disabled="paying" @click="closePayment">取消</button>
            <button type="submit" class="payment-confirm" :disabled="paying">{{ paying ? '验证中…' : '确认支付' }}</button>
          </div>
        </form>
      </dialog>
    </Teleport>
  </div>
</template>

<style scoped>
.payment-method { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; padding: 0; border: 0; background: transparent; cursor: pointer; }
.payment-method:disabled { cursor: not-allowed; opacity: .55; }
.payment-method:focus-visible { outline: 2px solid #1baeae; outline-offset: -3px; }
.payment-method img { max-width: 90%; max-height: 44px; margin: 0; }
.payment-dialog { position: fixed; inset: 0; width: min(420px, calc(100vw - 32px)); max-height: calc(100vh - 32px); margin: auto; padding: 0; overflow-y: auto; border: 0; border-radius: 12px; background: #fff; color: #242a2a; font: inherit; box-shadow: 0 18px 48px rgba(19, 39, 40, .22); }
.payment-dialog::backdrop { background: rgba(20, 28, 31, .5); }
.payment-dialog-content { position: relative; padding: 32px; }
.payment-dialog-close { position: absolute; top: 17px; right: 18px; width: 32px; height: 32px; padding: 0; border: 0; background: transparent; color: #697477; font-size: 26px; line-height: 1; cursor: pointer; }
.payment-dialog h2 { margin: 0; font-size: 22px; font-weight: 600; line-height: 1.35; }
.payment-dialog-subtitle { margin: 8px 0 24px; color: #5d696b; font-size: 14px; line-height: 1.5; }
.payment-dialog-summary { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; margin-bottom: 25px; padding: 15px 17px; border-radius: 8px; background: #f5f8f8; font-size: 14px; }
.payment-dialog-summary strong { color: #1a9898; font-size: 23px; font-weight: 600; }
.payment-dialog label { display: block; margin: 0 0 8px; font-size: 14px; font-weight: 600; }
.payment-dialog input { box-sizing: border-box; width: 100%; height: 44px; margin: 0 0 20px; padding: 0 13px; border: 1px solid #cbd3d4; border-radius: 6px; background: #fff; color: #242a2a; font: inherit; }
.payment-dialog input::placeholder { color: #657073; }
.payment-dialog input:focus { border-color: #1baeae; outline: 2px solid rgba(27, 174, 174, .16); }
.payment-dialog-error { margin: -3px 0 18px; color: #b42318; font-size: 13px; line-height: 1.5; }
.payment-dialog-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 7px; }
.payment-dialog-actions button { min-width: 96px; height: 42px; padding: 0 16px; border-radius: 6px; font-size: 14px; font-weight: 600; cursor: pointer; }
.payment-dialog-actions button:disabled { cursor: wait; opacity: .6; }
.payment-cancel { border: 1px solid #cbd3d4; background: #fff; color: #354042; }
.payment-confirm { border: 1px solid #1baeae; background: #1baeae; color: #fff; }
.payment-confirm:hover:not(:disabled) { background: #169f9f; }
.payment-dialog button:focus-visible { outline: 2px solid #1baeae; outline-offset: 2px; }
@media (max-width: 480px) { .payment-dialog-content { padding: 25px 22px; } }
</style>

