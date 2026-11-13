import { createRouter, createWebHistory } from 'vue-router'
import { loadLegacyStyles } from '../styles/legacyStyles.js'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/mall/HomeView.vue'), alias: '/index' },
  { path: '/search', name: 'search', component: () => import('../views/mall/SearchView.vue') },
  { path: '/goods/detail/:id', name: 'product', component: () => import('../views/mall/ProductView.vue') },
  { path: '/seckill/list', name: 'seckill', component: () => import('../views/mall/SeckillView.vue') },
  { path: '/seckill/detail/:id', name: 'seckill-detail', component: () => import('../views/mall/ProductView.vue') },
  { path: '/shopCart', name: 'cart', component: () => import('../views/mall/CartView.vue') },
  { path: '/shopCart/settle', name: 'checkout', component: () => import('../views/mall/CheckoutView.vue'), alias: '/order-settle' },
  { path: '/orders', name: 'orders', component: () => import('../views/mall/OrdersView.vue') },
  { path: '/orders/:orderNo', name: 'order-detail', component: () => import('../views/mall/OrderDetailView.vue'), alias: '/order/detail/:orderNo' },
  { path: '/personal', name: 'personal', component: () => import('../views/mall/PersonalView.vue') },
  { path: '/coupon', name: 'coupons', component: () => import('../views/mall/CouponsView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/mall/LoginView.vue'), meta: { standalone: true } },
  { path: '/register', name: 'register', component: () => import('../views/mall/RegisterView.vue'), meta: { standalone: true } },
  { path: '/selectPayType', name: 'payment', component: () => import('../views/mall/PaymentView.vue') },
  { path: '/pay-select/:orderNo', name: 'payment-compat', component: () => import('../views/mall/PaymentView.vue') },
  { path: '/payPage', name: 'pay-page', component: () => import('../views/mall/PaymentView.vue') },
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/admin/AdminLoginView.vue') },
  { path: '/admin/goods/add', name: 'admin-goods-add', component: () => import('../views/admin/AdminView.vue') },
  { path: '/admin/goods/edit/:id', name: 'admin-goods-edit-original', component: () => import('../views/admin/AdminView.vue') },
  { path: '/admin/goods/new', name: 'admin-goods-new', component: () => import('../views/admin/AdminView.vue') },
  { path: '/admin/goods/:id/edit', name: 'admin-goods-edit', component: () => import('../views/admin/AdminView.vue') },
  { path: '/admin', name: 'admin', component: () => import('../views/admin/AdminView.vue') },
  { path: '/admin/:section', name: 'admin-section', component: () => import('../views/admin/AdminView.vue') },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  },
})

router.beforeResolve(async to => {
  await loadLegacyStyles(to.path)
})

router.afterEach((to) => {
  document.body.className = to.path === '/admin/login' ? 'hold-transition login-page'
    : to.path === '/login' || to.path === '/register' ? 'body_login' : ''
  document.title = `${to.meta.title || '好享商城'} · haoxiang`
})

export default router
