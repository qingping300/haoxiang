<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteHeader from './components/SiteHeader.vue'
import SiteFooter from './components/SiteFooter.vue'

const route = useRoute()
const isAdmin = computed(() => route.path.startsWith('/admin'))
const isStandalone = computed(() => ['/login', '/register'].includes(route.path) || isAdmin.value)
const showFooter = computed(() => !isStandalone.value && !['/alipay', '/wxpay'].includes(route.path))
</script>

<template>
  <SiteHeader v-if="!isStandalone" />
  <RouterView v-slot="{ Component }">
    <component :is="Component" :key="route.fullPath" />
  </RouterView>
  <SiteFooter v-if="showFooter" />
</template>
