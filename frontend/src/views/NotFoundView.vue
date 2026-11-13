<template>
  <section class="not-found container">
    <p class="error-code">404</p>
    <h1>页面暂时找不到</h1>
    <p>地址可能已变更。返回首页继续逛逛吧。</p>
    <RouterLink class="button-primary" to="/">返回商城首页</RouterLink>
  </section>
</template>

<style scoped>
.not-found { min-height: 420px; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; }
.error-code { margin: 0; font-size: 84px; line-height: 1; font-weight: 800; color: var(--brand); }
h1 { margin: 20px 0 0; font-size: 26px; }
.not-found > p:not(.error-code) { margin: 14px 0 28px; color: var(--muted); }
</style>
