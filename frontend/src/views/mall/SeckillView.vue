<script setup>
import { onUnmounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const products = ref([])
let controller

function readSeckillList(data) {
  products.value = (data.seckillList || []).map(item => ({
    id: item.seckillId,
    image: legacyImageUrl(item.goodsCoverImg),
    name: item.goodsName,
    seckillPrice: item.seckillPrice,
    originalPrice: item.originalPrice,
  }))
}

async function load() {
  controller?.abort()
  controller = new AbortController()
  products.value = []
  try {
    readSeckillList((await legacyRequest('/seckill/list', { signal: controller.signal })).map || {})
  } catch (error) {
    if (error.name !== 'AbortError') console.info('商城秒杀列表暂不可用。')
  }
}

watch(() => route.fullPath, load, { immediate: true })
onUnmounted(() => controller?.abort())
</script>

<template>
  <div id="content">
    <div id="seckill">
      <h2>秒杀专区</h2>
      <ul><ProductCard v-for="item in products" :key="item.id" :product="item" variant="seckill" /></ul>
    </div>
  </div>
</template>
