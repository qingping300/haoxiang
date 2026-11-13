<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { formBody, legacyRequest, legacyUrl } from '../../services/legacy.js'

const router = useRouter()
const loginName = ref('')
const password = ref('')
const repassword = ref('')
const verifyCode = ref('')
const captchaStamp = ref(Date.now())
const error = ref('')
const busy = ref(false)

async function submit() {
  if (busy.value) return
  error.value = ''
  if (!/^1(3|4|5|6|7|8|9)\d{9}$/.test(loginName.value)) { error.value = '请输入正确的登录名(即手机号)'; return }
  if (!/^[a-zA-Z0-9]{6,20}$/.test(password.value.trim())) { error.value = '请输入正确的密码格式(6-20位字符和数字组合)'; return }
  busy.value = true
  try {
    await legacyRequest('/register', { method: 'POST', body: formBody({ loginName: loginName.value, verifyCode: verifyCode.value, password: password.value, repassword: repassword.value }) })
    await router.push('/login')
  } catch (cause) {
    error.value = cause.message || '注册失败'
    captchaStamp.value = Date.now()
  } finally { busy.value = false }
}
</script>

<template>
  <main class="hx-auth">
    <section class="hx-auth__intro"><div class="hx-auth__art"><div class="hx-auth__message"><h1>发现心仪好物，<br>享受每一次生活。</h1><p>欢迎来到好享商城，发现值得喜欢的日常。</p></div></div></section>
    <section class="hx-auth__panel"><div class="hx-auth__form"><h2>创建账号</h2><p class="hx-auth__hint">注册好享账号，开启你的购物之旅</p>
      <form id="registerForm" @submit.prevent="submit">
        <label for="loginName">手机号</label><input id="loginName" v-model="loginName" type="text" name="loginName" autocomplete="username" placeholder="请输入手机号" required>
        <label for="password">登录密码</label><input id="password" v-model="password" type="password" name="password" autocomplete="new-password" placeholder="请输入密码" required>
        <label for="repassword">确认密码</label><input id="repassword" v-model="repassword" type="password" name="repassword" autocomplete="new-password" placeholder="请再次输入密码" required>
        <label for="verifyCode">验证码</label><div class="hx-auth__captcha"><input id="verifyCode" v-model="verifyCode" type="text" name="verifyCode" placeholder="请输入验证码" required><button type="button" aria-label="刷新验证码" @click="captchaStamp = Date.now()"><img alt="图形验证码" :src="legacyUrl('/common/captcha?d=' + captchaStamp)"></button></div>
        <p v-if="error" role="alert" class="hx-auth__error">{{ error }}</p><button class="hx-auth__submit" type="submit" :disabled="busy">{{ busy ? '注册中…' : '注册' }}</button>
      </form><p class="hx-auth__switch">已有账号？ <RouterLink to="/login">立即登录</RouterLink></p><RouterLink class="hx-auth__back" to="/">← 返回商城首页</RouterLink>
    </div></section>
  </main>
</template>
