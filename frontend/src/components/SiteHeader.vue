<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { legacyRequest } from '../services/legacy.js'

const route = useRoute()
const router = useRouter()
const keyword = ref('')
const user = ref(null)
const cartCount = ref(0)
const showNav = computed(() => !['/shopCart', '/order-settle', '/pay-select', '/alipay', '/wxpay'].some(path => route.path === path || route.path.startsWith(`${path}/`)))
let lastFailure = 0

async function refreshHeader() {
  if (Date.now() - lastFailure < 15000) return
  const [userResult, countResult] = await Promise.allSettled([
    legacyRequest('/personal/info'),
    legacyRequest('/shopCart/getUserShopCartCount'),
  ])
  if (userResult.status === 'fulfilled') {
    const profile = userResult.value?.map?.userInfo
    user.value = profile ? { nickName: profile.nickName, loginName: profile.loginName } : null
  } else {
    user.value = null
    if (!userResult.reason?.code) lastFailure = Date.now()
  }
  if (countResult.status === 'fulfilled') cartCount.value = Number(countResult.value?.map?.count || 0)
  else cartCount.value = 0
}

function search() {
  const value = keyword.value.trim()
  if (!value) return
  router.push({ path: '/search', query: { keyword: value, ...(route.query.goodsCategoryId ? { goodsCategoryId: route.query.goodsCategoryId } : {}) } })
}

async function logout() {
  try { await legacyRequest('/logout') } catch { /* The header remains usable while backend is offline. */ }
  user.value = null
  cartCount.value = 0
  router.push('/')
}

watch(() => route.fullPath, () => {
  keyword.value = String(route.query.keyword || '')
  refreshHeader()
}, { immediate: true })
</script>

<template>
  <header id="header">
    <div class="center">
      <ul class="fl">
        <li><RouterLink to="/">haoxiang 官网</RouterLink></li>
        <li><RouterLink to="/admin">后台管理系统</RouterLink></li>
        <li><a href="https://github.com/qingping300/haoxiang" target="_blank" rel="noopener noreferrer">项目 GitHub</a></li>
      </ul>
      <div class="fr">
        <div v-if="user" class="user-login">
          <div class="user">
            <ul class="selector">
              <li><RouterLink to="/personal">个人中心</RouterLink></li>
              <li><RouterLink to="/orders">我的订单</RouterLink></li>
              <li><a href="#" @click.prevent="logout">退出登录</a></li>
              <div style="clear:both"></div>
            </ul>
            <a href="#" class="username" @click.prevent><span id="headNickName">{{ user.nickName || user.loginName }}</span><i class="iconfont icon-down"></i></a>
          </div>
          <div class="shopcart"><RouterLink to="/shopCart"><i class="iconfont icon-cart"></i> 购物车(<span id="shopCatCount">{{ cartCount }}</span>)</RouterLink></div>
        </div>
        <div v-else class="user-not-login">
          <ul class="login"><li><RouterLink to="/login">登录</RouterLink></li><li><RouterLink to="/register">注册</RouterLink></li></ul>
          <div class="shopcart"><RouterLink to="/login"><i class="iconfont icon-cart"></i> 购物车(0)</RouterLink></div>
        </div>
      </div>
    </div>
    <div class="saoyisao">
      <div class="item"><svg width="23" height="23" viewBox="0 0 24 24" fill="none" stroke="#333" stroke-width="1.7" aria-hidden="true"><path d="M4 13v-2a8 8 0 0 1 16 0v2M4 13h2a2 2 0 0 1 2 2v2a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-2a2 2 0 0 1 1-2Zm16 0h-2a2 2 0 0 0-2 2v2a2 2 0 0 0 2 2h1a2 2 0 0 0 2-2v-2a2 2 0 0 0-1-2Z"/></svg><span>在线客服</span></div>
    </div>
  </header>
  <nav v-if="showNav" id="nav">
    <div class="banner_x center">
      <RouterLink to="/" class="logo"><h1>haoxiang</h1></RouterLink>
      <ul class="list">
        <li><RouterLink to="/seckill/list">秒杀专区</RouterLink></li>
        <li><RouterLink to="/coupon">优惠券</RouterLink></li>
        <li><RouterLink :to="{ path: '/search', query: { keyword: '手机' } }">手机专区</RouterLink></li>
        <li><a href="#" @click.prevent>服务</a></li>
        <li><a href="#" @click.prevent>商城</a></li>
      </ul>
      <form class="fr" role="search" @submit.prevent="search">
        <div class="search"><label class="sr-only" for="keyword">搜索商品</label><input id="keyword" v-model="keyword" class="text" type="text" autocomplete="off"></div>
        <button class="button iconfont icon-search1" type="submit" aria-label="搜索商品"></button>
      </form>
    </div>
  </nav>
</template>
