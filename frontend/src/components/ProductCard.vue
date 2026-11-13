<script setup>
import { computed } from 'vue'
import { legacyImageUrl } from '../services/legacy.js'

const props = defineProps({
  product: { type: Object, required: true },
  variant: { type: String, default: 'new' },
})

const target = computed(() => props.product.href || (props.variant === 'seckill'
  ? `/seckill/detail/${props.product.id}`
  : `/goods/detail/${props.product.id}`))
const placeholder = '/admin/dist/img/no-img.png'
const cover = computed(() => {
  const image = legacyImageUrl(props.product.image)
  return image === '/mall/image/null-content.png' ? placeholder : image
})
const price = computed(() => props.variant === 'seckill' ? props.product.seckillPrice ?? props.product.price : props.product.price)

function onCoverError(event) {
  if (event.target.getAttribute('src') !== placeholder) event.target.src = placeholder
}
</script>

<template>
  <div v-if="variant === 'hot'" class="hot-image">
    <RouterLink :to="target">
      <img :src="cover" :alt="product.name" @error="onCoverError" />
      <p class="name text-truncate" :title="product.name">{{ product.name }}</p>
      <p class="grid-tips"><em class="thumb"><span class="color01" style="background-color:#FF6A6E">热卖商品</span></em></p>
    </RouterLink>
  </div>

  <li v-else-if="variant === 'new'">
    <RouterLink :to="target">
      <img :src="cover" :alt="product.name" @error="onCoverError" />
      <p class="name text-truncate" :title="product.name">{{ product.name }}</p>
      <p class="discount text-truncate" :title="product.intro">{{ product.intro }}</p>
      <p class="item_price">¥ {{ product.price }}</p>
      <p class="grid-tips"><em class="thumb"><span class="color01" style="background-color:#68BEFF">新品上线</span></em></p>
    </RouterLink>
  </li>

  <li v-else-if="variant === 'recommend'">
    <RouterLink :to="target">
      <img :src="cover" :alt="product.name" @error="onCoverError" />
      <p class="name text-truncate" :title="product.name">{{ product.name }}</p>
      <p class="item_price" :title="`¥ ${product.price}`">¥ {{ product.price }}</p>
      <p class="counter">猜你喜欢</p>
      <div class="comment"><p>好享精选</p><p>好物也可以不贵</p></div>
    </RouterLink>
  </li>

  <div v-else-if="variant === 'search'" class="item_card_frame">
    <div class="item_card"><RouterLink :to="target"><img :src="cover" :alt="product.name" @error="onCoverError" /></RouterLink></div>
    <div class="item_brand text-truncate"><RouterLink :to="target" :title="product.name">{{ product.name }}</RouterLink></div>
    <div class="item_sub_intro text-truncate" :title="product.intro">{{ product.intro }}</div>
    <div class="item_price">{{ Number(product.price || 0).toFixed(2) }}元</div>
  </div>

  <li v-else-if="variant === 'seckill'">
    <RouterLink :to="target">
      <img :src="cover" :alt="product.name" @error="onCoverError" />
      <p class="name">{{ product.name }}</p>
      <div class="priceMod">
        <div class="seckillPrice">￥<i>{{ price }}</i></div>
        <div class="originPrice">￥<del style="text-decoration:line-through">{{ product.originalPrice }}</del></div>
        <div class="seckillButton"><button type="button">立即抢购</button></div>
      </div>
    </RouterLink>
  </li>
</template>
