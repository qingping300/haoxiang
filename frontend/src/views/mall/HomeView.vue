<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import ProductCard from '../../components/ProductCard.vue'
import { legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const categories = ref([])
const featuredProducts = {
  20001: { name: 'Apple iPhone 18 Pro', intro: '2026 款 Pro 系列手机', tone: 'burgundy' },
  20002: { name: 'Apple iPhone 18 Pro Max', intro: '2026 款 Pro Max 系列手机', tone: 'graphite' },
  20003: { name: 'Apple iPhone 17e', intro: '2026 款 iPhone 17e', tone: 'sky' },
}
const banners = ref([20001, 20002, 20003].map((id, index) => ({
  image: `/mall/image/swiper/banner0${index + 1}.jpg`,
  href: `/goods/detail/${id}`,
  feature: featuredProducts[id],
})))
const hot = ref([])
const fresh = ref([])
const recommended = ref([])
const error = ref('')
const activeCategory = ref(-1)
const activeBanner = ref(0)
const slideIndex = ref(1)
const slideTransition = ref(true)
const displayBanners = computed(() => banners.value.length > 1
  ? [banners.value[banners.value.length - 1], ...banners.value, banners.value[0]]
  : banners.value)
let carouselTimer
let controller
let retryTimer
let retryCount = 0

const product = item => ({
  id: item.goodsId,
  image: legacyImageUrl(item.goodsCoverImg),
  name: item.goodsName,
  intro: item.goodsIntro,
  price: item.sellingPrice,
})

function readHome(data) {
  categories.value = (data.categories || []).map(category => ({
    id: category.categoryId,
    name: category.categoryName,
    levels: (category.subCategoryVOS || []).map(level => ({
      name: level.categoryName,
      children: (level.subCategoryVOS || []).map(child => ({
        name: child.categoryName,
        href: `/search?goodsCategoryId=${child.categoryId}`,
      })),
    })),
  }))
  banners.value = (data.carousels || []).map(item => {
    const image = legacyImageUrl(item.carouselUrl)
    const href = item.redirectUrl || '#'
    const id = Number(href.match(/^\/goods\/detail\/(\d+)$/)?.[1])
    return { image, href, feature: (image.startsWith('/mall/image/swiper/') || image.startsWith('/mall/image/products/')) ? featuredProducts[id] : null }
  })
  hot.value = (data.hotGoodses || []).map(product)
  fresh.value = (data.newGoodses || []).map(product)
  recommended.value = (data.recommendGoodses || []).map(product)
  activeBanner.value = 0
  slideIndex.value = banners.value.length > 1 ? 1 : 0
}

async function load() {
  controller?.abort()
  controller = new AbortController()
  try {
    readHome((await legacyRequest('/index', { signal: controller.signal })).map || {})
    retryCount = 0
    error.value = ''
  } catch (cause) {
    if (cause.name === 'AbortError') return
    if (retryCount < 10) {
      retryCount += 1
      retryTimer = window.setTimeout(load, 3000)
    } else {
      error.value = '商城服务暂不可用，请启动后端后重试。'
    }
  }
}

function changeBanner(delta) {
  if (banners.value.length < 2 || !slideTransition.value) return
  if (slideIndex.value === 0 || slideIndex.value === banners.value.length + 1) return
  slideIndex.value += delta
  activeBanner.value = (activeBanner.value + delta + banners.value.length) % banners.value.length
}

function selectBanner(index) {
  if (!slideTransition.value || slideIndex.value === 0 || slideIndex.value === banners.value.length + 1) return
  activeBanner.value = index
  slideIndex.value = banners.value.length > 1 ? index + 1 : 0
}

function finishBannerTransition(event) {
  if (event.propertyName !== 'transform' || banners.value.length < 2) return
  if (slideIndex.value !== 0 && slideIndex.value !== banners.value.length + 1) return
  slideTransition.value = false
  slideIndex.value = slideIndex.value === 0 ? banners.value.length : 1
  requestAnimationFrame(() => requestAnimationFrame(() => { slideTransition.value = true }))
}

onMounted(() => {
  load()
  carouselTimer = window.setInterval(() => changeBanner(1), 2000)
})
onUnmounted(() => {
  controller?.abort()
  window.clearTimeout(retryTimer)
  window.clearInterval(carouselTimer)
})
</script>

<template>
  <div id="content">
    <div id="banner">
      <div class="all-sort-list">
        <div v-for="(category, index) in categories" :key="category.id" class="item" @mouseenter="activeCategory = index" @mouseleave="activeCategory = -1">
          <a href="javascript:;">{{ category.name }}<i class="iconfont icon-right"></i></a>
          <div class="item-list clearfix" :style="{ display: activeCategory === index && category.levels.length ? 'block' : 'none' }">
            <div class="subitem">
              <dl v-for="(level, levelIndex) in category.levels" :key="levelIndex" class="fore1">
                <dt><a href="javascript:;">{{ level.name }}</a></dt>
                <dd><em v-for="(child, childIndex) in level.children" :key="childIndex"><RouterLink :to="child.href">{{ child.name }}</RouterLink></em></dd>
              </dl>
            </div>
          </div>
        </div>
      </div>
      <div class="swiper-container fl">
        <div class="swiper-wrapper" :style="{ transform: `translate3d(-${slideIndex * 100}%, 0, 0)`, transition: slideTransition ? 'transform .35s ease' : 'none' }" @transitionend.self="finishBannerTransition">
          <div v-for="(banner, index) in displayBanners" :key="`${banner.image}-${index}`" class="swiper-slide" :inert="index !== slideIndex">
            <RouterLink v-if="banner.feature" :to="banner.href" class="featured-slide" :class="`featured-${banner.feature.tone}`">
              <span class="featured-copy"><span class="featured-label">2026 新品</span><strong>{{ banner.feature.name }}</strong><span class="featured-intro">{{ banner.feature.intro }}</span><span class="featured-link">了解详情 <span aria-hidden="true">→</span></span></span>
              <img :src="banner.image" :alt="banner.feature.name" />
            </RouterLink>
            <RouterLink v-else-if="banner.href.startsWith('/goods/detail/') || banner.href.startsWith('/seckill/detail/')" :to="banner.href"><img :src="banner.image" :alt="`轮播商品 ${index + 1}`" /></RouterLink>
            <a v-else :href="banner.href" target="_blank" rel="noopener noreferrer"><img :src="banner.image" :alt="`轮播商品 ${index + 1}`" /></a>
          </div>
        </div>
        <div class="swiper-pagination swiper-pagination-clickable swiper-pagination-bullets">
          <button v-for="(_, index) in banners" :key="index" type="button" class="swiper-pagination-bullet" :class="{ 'swiper-pagination-bullet-active': activeBanner === index }" :aria-label="`查看第 ${index + 1} 张轮播图`" @click="selectBanner(index)"></button>
        </div>
        <div class="swiper-button-prev" role="button" tabindex="0" aria-label="上一张" @click="changeBanner(-1)" @keydown.enter="changeBanner(-1)"></div>
        <div class="swiper-button-next" role="button" tabindex="0" aria-label="下一张" @click="changeBanner(1)" @keydown.enter="changeBanner(1)"></div>
      </div>
    </div>
    <p v-if="error" class="legacy-error center" role="alert">{{ error }}</p>

    <div id="sub_banner"><ProductCard v-for="item in hot" :key="item.id" :product="item" variant="hot" /></div>

    <div id="flash"><h2>新品上线</h2><ul><ProductCard v-for="item in fresh" :key="item.id" :product="item" variant="new" /></ul></div>

    <div id="recommend"><h2>为你推荐</h2><ul><ProductCard v-for="item in recommended" :key="item.id" :product="item" variant="recommend" /></ul></div>
  </div>
</template>

<style scoped>
.featured-slide {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  color: #24242a;
  background: linear-gradient(120deg, #f2e5e7, #fff7f4);
}
.featured-graphite { color: #fff; background: linear-gradient(120deg, #242933, #5b6271); }
.featured-sky { background: linear-gradient(120deg, #e5eff6, #f8f9fc); }
.featured-copy {
  display: flex;
  flex: 0 0 53%;
  flex-direction: column;
  align-items: flex-start;
  box-sizing: border-box;
  padding: 104px 0 0 72px;
}
.featured-label { margin-bottom: 20px; font-size: 16px; letter-spacing: .14em; }
.featured-copy strong { font-size: 44px; line-height: 1.2; font-weight: 600; }
.featured-intro { margin-top: 20px; font-size: 20px; }
.featured-link { margin-top: 42px; padding-bottom: 8px; border-bottom: 1px solid currentColor; font-size: 17px; }
#content #banner .swiper-container .swiper-wrapper .swiper-slide .featured-slide img {
  width: 47%;
  height: 100%;
  box-sizing: border-box;
  padding: 28px 35px 28px 0;
  object-fit: contain;
}
</style>
