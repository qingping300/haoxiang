<script setup>
import { ref, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const products = ref([])
const pageLinks = ref([])
let controller

function currentQuery(patch = {}) {
  const query = {
    keyword: route.query.keyword || undefined,
    goodsCategoryId: route.query.goodsCategoryId || undefined,
    sidx: route.query.sidx || undefined,
    order: route.query.order || undefined,
    pageNumber: route.query.pageNumber || undefined,
    ...patch,
  }
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== undefined && value !== ''))
}

function legacyPath() {
  const params = new URLSearchParams(currentQuery())
  return `/search${params.size ? `?${params}` : ''}`
}

function readSearch(data) {
  const page = data.pageResult || {}
  products.value = (page.records || []).map(item => ({
    id: item.goodsId,
    image: legacyImageUrl(item.goodsCoverImg),
    name: item.goodsName,
    intro: item.goodsIntro,
    price: item.sellingPrice,
  }))
  const current = Number(page.current) || 1
  const pages = Number(page.pages) || 0
  pageLinks.value = Array.from({ length: Math.min(pages, 5) }, (_, index) => {
    const number = Math.max(1, Math.min(current - 2, pages - 4)) + index
    return { label: number, href: { path: '/search', query: currentQuery({ pageNumber: number }) }, active: number === current }
  })
}

async function load() {
  controller?.abort()
  controller = new AbortController()
  products.value = []
  pageLinks.value = []
  try {
    readSearch((await legacyRequest(legacyPath(), { signal: controller.signal })).map || {})
  } catch (error) {
    if (error.name !== 'AbortError') console.info('商城搜索暂不可用。')
  }
}

watch(() => route.fullPath, load, { immediate: true })
onUnmounted(() => controller?.abort())
</script>

<template>
  <div class="sort">
    <div class="list">
      <RouterLink :to="{ path: '/search', query: currentQuery({ sidx: undefined, order: undefined, pageNumber: undefined }) }"><div :class="{ active: !route.query.sidx }">推荐</div></RouterLink>
      <RouterLink :to="{ path: '/search', query: currentQuery({ sidx: 'goodsId', order: undefined, pageNumber: undefined }) }"><div :class="{ active: route.query.sidx === 'goodsId' }">新品</div></RouterLink>
      <RouterLink :to="{ path: '/search', query: currentQuery({ sidx: 'sellingPrice', order: route.query.order === 'asc' ? 'desc' : 'asc', pageNumber: undefined }) }">
        <div :class="{ active: route.query.sidx === 'sellingPrice' }">价格 <i :class="route.query.sidx === 'sellingPrice' ? route.query.order === 'asc' ? 'fa fa-sort-up' : 'fa fa-sort-down' : 'fa fa-sort'"></i></div>
      </RouterLink>
    </div>
  </div>

  <div class="goods_item center">
    <div class="main center">
      <template v-if="products.length"><ProductCard v-for="item in products" :key="item.id" :product="item" variant="search" /></template>
      <template v-else>未查询到商品</template>
      <div class="clear"></div>
    </div>
    <div class="pages"><div class="page_wrap"><span v-if="pageLinks.length" class="page_span1">
      <template v-for="(link, index) in pageLinks" :key="index">
        <RouterLink v-if="link.href && link.href !== '##' && link.href !== '#'" :to="link.href" :class="{ active: link.active }">{{ link.label }}</RouterLink>
        <a v-else href="javascript:;" :class="{ active: link.active }">{{ link.label }}</a>
      </template>
    </span></div></div>
  </div>
</template>
