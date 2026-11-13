const mallBase = [
  '/mall/css/swiper.min.css',
  '/mall/css/iconfont.css',
  '/mall/css/common.css',
  '/mall/css/green/pace-theme-minimal.css',
  '/mall/styles/header.css',
  '/mall/styles/haoxiang-brand.css',
]

const adminBase = [
  '/admin/dist/css/font-awesome.min.css',
  '/admin/dist/css/ionicons.min.css',
  '/admin/plugins/bootstrap/css/bootstrap.css',
  '/admin/plugins/sweetalert/sweetalert.css',
  '/admin/plugins/jqgrid/ui.jqgrid-bootstrap4.css',
  '/admin/dist/css/adminlte.min.css',
  '/admin/dist/css/main.css',
]

export function stylesForPath(path) {
  if (path.startsWith('/admin')) {
    if (path === '/admin/login') return [
      '/admin/dist/css/font-awesome.min.css',
      '/admin/dist/css/ionicons.min.css',
      '/admin/dist/css/adminlte.min.css',
      '/admin/dist/css/login-original.css',
    ]
    const section = path.includes('/goods') ? 'goods' : path.split('/')[2]
    const extra = {
      goods: '/admin/dist/css/goods-original.css',
      orders: '/admin/dist/css/order-original.css',
      users: '/admin/dist/css/member-original.css',
      coupon: '/admin/dist/css/coupon-original.css',
      seckill: '/admin/dist/css/seckill-original.css',
      indexConfigs: '/admin/dist/css/index-configs-original.css',
    }[section]
    return extra ? [...adminBase, extra] : adminBase
  }
  if (path === '/login' || path === '/register') return [
    '/mall/css/common.css',
    ...(path === '/login' ? ['/mall/css/captcha.css'] : []),
    '/admin/plugins/sweetalert/sweetalert.css',
    path === '/login' ? '/mall/styles/login2-original.css' : '/mall/styles/register2-original.css',
  ]
  const section = path === '/' || path === '/index' || path.startsWith('/seckill/list') ? 'index'
    : path.startsWith('/search') ? 'search'
    : path.startsWith('/goods/detail') || path.startsWith('/seckill/detail') ? 'detail'
    : path.startsWith('/shopCart/settle') || path.startsWith('/order-settle') ? 'order-detail'
    : path.startsWith('/shopCart') ? 'cart'
    : path.startsWith('/orders/') ? 'order-detail'
    : path.startsWith('/orders') ? 'my-orders'
    : path.startsWith('/personal') ? 'personal'
    : path.startsWith('/coupon') ? 'index'
    : path.startsWith('/pay-select') || path.startsWith('/selectPayType') ? 'pay-select'
    : path.startsWith('/wxpay') || path.startsWith('/alipay') || path.startsWith('/payPage') ? 'pay'
    : 'index'
  const extra = []
  if (section === 'search') extra.push('/admin/dist/css/font-awesome.min.css')
  if (path.startsWith('/coupon')) extra.push('/mall/styles/coupon.css')
  if (path.startsWith('/order-settle') || path.startsWith('/shopCart/settle')) extra.push('/mall/styles/coupon.css')
  if (path.startsWith('/personal') || path.startsWith('/order-settle') || path.startsWith('/shopCart/settle')) extra.push('/mall/css/bootstrap-modal.css')
  if (path.startsWith('/seckill/detail')) extra.push('/mall/css/bootstrap.min.css')
  return [...mallBase, `/mall/styles/${section}.css`, ...extra]
}

export async function loadLegacyStyles(path) {
  const oldLinks = [...document.querySelectorAll('link[data-legacy-style]')]
  const urls = [...new Set([...stylesForPath(path), '/mall/styles/haoxiang-ui.css'])]
  const fresh = urls.map(href => {
    const link = document.createElement('link')
    link.rel = 'stylesheet'
    link.href = href
    link.dataset.legacyStyle = ''
    const ready = new Promise(resolve => {
      link.addEventListener('load', resolve, { once: true })
      link.addEventListener('error', resolve, { once: true })
    })
    document.head.append(link)
    return ready
  })
  await Promise.all(fresh)
  oldLinks.forEach(link => link.remove())
}
