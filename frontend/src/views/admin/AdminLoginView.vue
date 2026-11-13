<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { formBody, legacyRequest, legacyUrl } from '../../services/legacy.js'

const router = useRouter()
const userName = ref('')
const password = ref('')
const verifyCode = ref('')
const captchaStamp = ref(Date.now())
const error = ref('')
const submitting = ref(false)
const injectedScripts = []

function refreshCaptcha() {
  captchaStamp.value = Date.now()
}

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.onload = resolve
    script.onerror = reject
    document.body.appendChild(script)
    injectedScripts.push(script)
  })
}

onMounted(async () => {
  try {
    if (!window.particlesJS) await loadScript('/admin/dist/js/plugins/particles.js')
    await loadScript('/admin/dist/js/plugins/login-bg-particles.js')
  } catch {
    // The light original login background remains usable without particles.
  }
})

onUnmounted(() => {
  try {
    window.pJSDom?.forEach(instance => instance?.pJS?.fn?.vendors?.destroypJS?.())
  } catch {
    // The old vendor animation may already have stopped.
  }
  injectedScripts.forEach(script => script.remove())
})

async function submit() {
  if (submitting.value) return
  error.value = ''
  submitting.value = true
  try {
    await legacyRequest('/admin/login', {
      method: 'POST',
      body: formBody({
        userName: userName.value,
        password: password.value,
        verifyCode: verifyCode.value,
      }),
    })
    password.value = ''
    await router.push('/admin')
  } catch (cause) {
    error.value = cause.message || '登录请求失败'
    verifyCode.value = ''
    refreshCaptcha()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="legacy-admin-login">
    <div id="particles" aria-hidden="true"></div>
    <div class="login-box">
      <div class="login-logo">
        <span class="legacy-admin-login__mark" aria-hidden="true">H</span>
        <h1>管理系统登陆</h1>
      </div>
      <div class="card">
        <div class="card-body login-card-body">
          <p class="login-box-msg">haoxiang MALL , Let's Go !</p>
          <form @submit.prevent="submit">
            <div v-if="error" class="form-group">
              <div class="alert alert-danger" role="alert">{{ error }}</div>
            </div>
            <div class="form-group has-feedback">
              <span class="fa fa-user form-control-feedback" aria-hidden="true"></span>
              <label class="sr-only" for="admin-user-name">账号</label>
              <input id="admin-user-name" v-model.trim="userName" name="userName" type="text" class="form-control" placeholder="请输入账号" autocomplete="username" required />
            </div>
            <div class="form-group has-feedback">
              <span class="fa fa-lock form-control-feedback" aria-hidden="true"></span>
              <label class="sr-only" for="admin-password">密码</label>
              <input id="admin-password" v-model="password" name="password" type="password" class="form-control" placeholder="请输入密码" autocomplete="current-password" required />
            </div>
            <div class="row">
              <div class="col-6">
                <label class="sr-only" for="admin-verify-code">验证码</label>
                <input id="admin-verify-code" v-model.trim="verifyCode" name="verifyCode" type="text" class="form-control" placeholder="请输入验证码" required />
              </div>
              <div class="col-6">
                <button class="legacy-admin-login__captcha" type="button" title="点击刷新验证码" aria-label="刷新验证码" @click="refreshCaptcha">
                  <img :src="legacyUrl('/common/captcha') + '?d=' + captchaStamp" alt="图形验证码，点击刷新" />
                </button>
              </div>
            </div>
            <div class="form-group has-feedback"></div>
            <div class="row">
              <div class="col-8"></div>
              <div class="col-4">
                <button type="submit" class="btn btn-primary btn-block btn-flat" :disabled="submitting">{{ submitting ? '登录中' : '登录' }}</button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.legacy-admin-login { min-height: 100vh; width: 100%; position: relative; display: flex; align-items: center; justify-content: center; background: #f7fafc; }
.legacy-admin-login #particles { background-color: #f7fafc; position: absolute; inset: 0; z-index: 0; overflow: hidden; }
.legacy-admin-login .login-box { position: relative; z-index: 1; width: 360px; max-width: calc(100% - 32px); margin: 7% auto; }
.legacy-admin-login .login-logo { color: #1baeae; min-height: 58px; display: flex; align-items: center; gap: 10px; }
.legacy-admin-login .login-logo h1 { color: #1baeae; font-size: 32px; font-weight: 400; margin: 0; white-space: nowrap; }
.legacy-admin-login__mark { width: 58px; height: 58px; flex: none; display: grid; place-items: center; color: #1baeae; border: 2px solid #1baeae; font-size: 38px; font-weight: 700; line-height: 1; }
.legacy-admin-login__captcha { border: 0; background: none; padding: 0; width: 100%; cursor: pointer; }
.legacy-admin-login__captcha img { height: 38px; max-width: 100%; object-fit: contain; }
.legacy-admin-login .form-control { min-height: 38px; }
@media (max-width: 420px) { .legacy-admin-login .login-logo h1 { font-size: 27px; } .legacy-admin-login__mark { width: 48px; height: 48px; font-size: 32px; } }
</style>
