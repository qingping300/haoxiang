<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { legacyRequest } from '../../services/legacy.js'

const router = useRouter()
const user = ref(null)
const form = reactive({ nickName: '', introduceSign: '', address: '' })
const error = ref('')
const modalOpen = ref(false)
const busy = ref(false)

async function load() {
  try {
    user.value = (await legacyRequest('/personal/info')).map?.userInfo || null
    error.value = ''
  } catch (cause) {
    error.value = cause.auth ? '请先登录后查看个人资料。' : cause.message || '个人资料暂时无法显示。'
  }
}

function openModal() {
  if (!user.value) return
  Object.assign(form, { nickName: user.value.nickName || '', introduceSign: user.value.introduceSign || '', address: user.value.address || '' })
  modalOpen.value = true
}

async function save() {
  if (busy.value) return
  if (form.nickName.trim().length < 2) { error.value = '请输入正确的昵称。'; return }
  if (form.introduceSign.trim().length < 4) { error.value = '请输入正确的个性签名。'; return }
  if (form.address.trim().length < 10) { error.value = '请输入正确的收货信息。'; return }
  busy.value = true
  try {
    await legacyRequest('/personal/updateInfo', { method: 'POST', body: { userId: user.value.userId, ...form } })
    modalOpen.value = false
    await load()
  } catch (cause) {
    error.value = cause.message || '保存个人信息失败。'
  } finally {
    busy.value = false
  }
}

async function logout() {
  await legacyRequest('/logout')
  router.push('/login')
}

onMounted(load)
</script>

<template>
  <div id="personal">
    <div class="self-info center">
      <div class="lfnav fl"><div class="nav_title">个人中心</div><div class="title_list"><ul><li><RouterLink class="active" to="/personal">个人信息</RouterLink></li><li><RouterLink to="/orders">我的订单</RouterLink></li><li><a href="javascript:;" @click.prevent="logout">退出登录</a></li></ul></div></div>
      <div class="intro fr">
        <div class="grzlbt ml40">我的资料 <a href="javascript:;" style="color:#1baeae;margin-left:600px" @click.prevent="openModal">更改个人信息</a></div>
        <div class="info_item ml40"><span>昵称</span><span>{{ user?.nickName || '—' }}</span></div>
        <div class="info_item ml40"><span>手机号</span><span>{{ user?.loginName || '—' }}</span></div>
        <div class="info_item ml40"><span>密码</span><span>******</span></div>
        <div class="info_item ml40"><span>个性签名</span><span>{{ user?.introduceSign || '无' }}</span></div>
        <div class="info_item ml40"><span>收货信息</span><span>{{ user?.address || '无' }}</span></div>
      </div>
      <div class="clear"></div>
    </div>
    <div v-if="modalOpen" class="modal fade in show" style="display:block" role="dialog" aria-modal="true">
      <div class="modal-dialog" role="document"><div class="modal-content">
        <div class="modal-header"><button type="button" class="close" aria-label="关闭" @click="modalOpen = false"><span aria-hidden="true">×</span></button><h6 class="modal-title">个人信息修改</h6></div>
        <div class="modal-body"><form id="personalInfoForm" @submit.prevent="save">
          <div class="form-group"><label for="nickName" class="control-label">昵称:</label><input id="nickName" v-model="form.nickName" type="text" class="form-control" required /></div>
          <div class="form-group"><label for="introduceSign" class="control-label">个性签名:</label><input id="introduceSign" v-model="form.introduceSign" type="text" class="form-control" required /></div>
          <div class="form-group"><label for="address" class="control-label">收货信息:</label><input id="address" v-model="form.address" type="text" class="form-control" required /></div>
        </form></div>
        <div class="modal-footer"><button type="button" class="btn btn-default" @click="modalOpen = false">取消</button><button id="saveButton" type="button" class="btn btn-primary" :disabled="busy" @click="save">确认</button></div>
      </div></div>
    </div>
    <div v-if="modalOpen" class="modal-backdrop fade in" @click="modalOpen = false"></div>
    <p v-if="error" role="alert" class="legacy-error center">{{ error }} <RouterLink to="/login?redirect=/personal">登录</RouterLink> <button type="button" @click="load">重试</button></p>
  </div>
</template>
