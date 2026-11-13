<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formBody, legacyRequest } from '../../services/legacy.js'

const router = useRouter()
const route = useRoute()
const loginName = ref('')
const password = ref('')
const rememberme = ref(true)
const error = ref('')
const busy = ref(false)
const captchaOpen = ref(false)
const captcha = ref(null)
const backgroundImage = ref('')
const templateImage = ref('')
const backgroundNode = ref(null)
const templateNode = ref(null)
const offset = ref(0)
let startX = 0
let startY = 0
let startTime = null
let track = []
let activePointerId = null

function stopDrag() {
  window.removeEventListener('pointermove', moveDrag)
  window.removeEventListener('pointerup', endDrag)
  window.removeEventListener('pointercancel', cancelDrag)
  activePointerId = null
  startTime = null
}

function cancelDrag(event) {
  if (event.pointerId !== activePointerId) return
  stopDrag()
  offset.value = 0
}

function closeCaptcha() {
  stopDrag()
  captchaOpen.value = false
}

async function openCaptcha() {
  error.value = ''
  if (!/^1(3|4|5|6|7|8|9)\d{9}$/.test(loginName.value)) { error.value = '请输入正确的登录名(即手机号)'; return }
  if (!/^[a-zA-Z0-9]{6,20}$/.test(password.value.trim())) { error.value = '请输入正确的密码格式(6-20位字符和数字组合)'; return }
  captchaOpen.value = true
  await refreshCaptcha()
}

async function refreshCaptcha() {
  stopDrag()
  offset.value = 0
  captcha.value = null
  try {
    const response = await legacyRequest('/tianai/gen')
    captcha.value = response
    backgroundImage.value = response.captcha.backgroundImage
    templateImage.value = response.captcha.templateImage
  } catch (cause) { error.value = cause.message || '验证码加载失败' }
}

function startDrag(event) {
  if (!captcha.value || activePointerId !== null || (event.pointerType === 'mouse' && event.button !== 0)) return
  event.preventDefault()
  activePointerId = event.pointerId
  startX = event.clientX
  startY = event.clientY
  startTime = new Date()
  track = [{ x: 0, y: 0, type: 'down', t: 0 }]
  window.addEventListener('pointermove', moveDrag)
  window.addEventListener('pointerup', endDrag)
  window.addEventListener('pointercancel', cancelDrag)
}

function moveDrag(event) {
  if (!startTime || event.pointerId !== activePointerId) return
  offset.value = Math.max(0, Math.min(206, Math.round(event.clientX - startX)))
  track.push({ x: Math.round(event.clientX - startX), y: Math.round(event.clientY - startY), type: 'move', t: Date.now() - startTime.getTime() })
}

async function endDrag(event) {
  if (!startTime || !captcha.value || event.pointerId !== activePointerId) return
  const endTime = new Date()
  track.push({ x: Math.round(event.clientX - startX), y: Math.round(event.clientY - startY), type: 'up', t: endTime.getTime() - startTime.getTime() })
  const payload = {
    bgImageWidth: backgroundNode.value?.getBoundingClientRect().width || 260,
    bgImageHeight: backgroundNode.value?.getBoundingClientRect().height || 159,
    templateImageWidth: templateNode.value?.getBoundingClientRect().width || 49,
    templateImageHeight: templateNode.value?.getBoundingClientRect().height || 159,
    startSlidingTime: startTime.getTime(),
    entSlidingTime: endTime.getTime(),
    trackList: track,
  }
  stopDrag()
  try {
    const valid = await legacyRequest('/tianai/check?id=' + encodeURIComponent(captcha.value.id), { method: 'POST', body: payload })
    if (valid === true) { closeCaptcha(); await submit() }
    else { error.value = '滑块验证失败，请重试'; await refreshCaptcha() }
  } catch (cause) { error.value = cause.message || '滑块验证失败'; await refreshCaptcha() }
}

onBeforeUnmount(stopDrag)

async function submit() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    await legacyRequest('/login?destPath=', { method: 'POST', body: formBody({ loginName: loginName.value, password: password.value, rememberme: rememberme.value }) })
    await router.push(typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/') ? route.query.redirect : '/')
  } catch (cause) {
    error.value = cause.message || '登录失败'
  } finally { busy.value = false }
}
</script>

<template>
  <main class="hx-auth">
    <section class="hx-auth__intro"><div class="hx-auth__art"><div class="hx-auth__message"><h1>发现心仪好物，<br>享受每一次生活。</h1><p>欢迎来到好享商城，发现值得喜欢的日常。</p></div></div></section>
    <section class="hx-auth__panel"><div class="hx-auth__form"><h2>欢迎回来</h2><p class="hx-auth__hint">登录账号，继续你的好享之旅</p>
      <form id="registerForm" @submit.prevent="openCaptcha">
        <label for="loginName">手机号</label><input id="loginName" v-model="loginName" type="text" name="loginName" autocomplete="username" placeholder="请输入手机号" required>
        <label for="password">登录密码</label><input id="password" v-model="password" type="password" name="password" autocomplete="current-password" placeholder="请输入密码" required>
        <div class="hx-auth__remember"><input id="check1" v-model="rememberme" type="checkbox" name="rememberme"><label for="check1">记住我的登录信息</label></div>
        <p v-if="error" role="alert" class="hx-auth__error">{{ error }}</p><button class="hx-auth__submit" type="submit" :disabled="busy">{{ busy ? '登录中…' : '登录' }}</button>
      </form><p class="hx-auth__switch">还没有账号？ <RouterLink to="/register">立即注册</RouterLink></p><RouterLink class="hx-auth__back" to="/">← 返回商城首页</RouterLink>
    </div></section>
  </main>
  <div class="captcha-modal" :style="{ display: captchaOpen ? 'block' : 'none' }"><div class="slider">
    <div class="content"><div class="bg-img-div"><img id="bg-img" ref="backgroundNode" :src="backgroundImage" alt="拼图背景"></div><div id="slider-img-div" class="slider-img-div" :style="{ transform: `translateX(${offset}px)` }"><img id="slider-img" ref="templateNode" :src="templateImage" alt="拼图滑块"></div></div>
    <div class="slider-move"><div class="slider-move-track">拖动滑块完成拼图</div><div id="slider-move-btn" class="slider-move-btn" :style="{ transform: `translateX(${offset}px)`, touchAction: 'none' }" @pointerdown="startDrag"></div></div>
    <div class="bottom"><div id="slider-close-btn" class="close-btn" @click="closeCaptcha"></div><div id="slider-refresh-btn" class="refresh-btn" @click="refreshCaptcha"></div></div>
  </div></div>
</template>
