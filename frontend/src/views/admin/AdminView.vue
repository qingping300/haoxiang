<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { formBody, legacyImageUrl, legacyRequest } from '../../services/legacy.js'

const route = useRoute()
const router = useRouter()
const sidebarCollapsed = ref(false)
const mobileSidebarOpen = ref(false)
const rows = ref([])
const selectedIds = ref([])
const pageNumber = ref(1)
const pageSize = ref(10)
const total = ref(0)
const pages = ref(0)
const loading = ref(false)
const error = ref('')
const success = ref('')
const modal = ref(null)
const modalValues = reactive({})
const search = reactive({})
const profile = reactive({ loginUserName: '', nickName: '', originalPassword: '', newPassword: '' })
const goods = reactive({
  goodsId: 0, goodsName: '', goodsIntro: '', goodsCategoryId: '', originalPrice: '',
  sellingPrice: '', stockNum: '', tag: '', goodsSellStatus: '0',
  goodsCoverImg: '', goodsDetailContent: '',
})
const categoryOne = ref([])
const categoryTwo = ref([])
const categoryThree = ref([])
const levelOne = ref('')
const levelTwo = ref('')
const statisticsDays = ref(365)
const statisticsDates = ref([])
const statisticsValues = ref([])
let requestSerial = 0

const navGroups = [
  { heading: '工作台', items: [
    { label: '工作台', icon: 'fa-dashboard', path: '/admin', section: 'dashboard' },
    { label: '统计信息', icon: 'fa-area-chart', path: '/admin/statistics', section: 'statistics' },
  ] },
  { heading: '首页配置', items: [
    { label: '轮播图配置', icon: 'fa-file-image-o', path: '/admin/carousels', section: 'carousels' },
    { label: '热销商品配置', icon: 'fa-hand-o-up', path: '/admin/indexConfigs?configType=3', section: 'indexConfigs', type: 3 },
    { label: '新品上线配置', icon: 'fa-hand-o-up', path: '/admin/indexConfigs?configType=4', section: 'indexConfigs', type: 4 },
    { label: '为你推荐配置', icon: 'fa-hand-o-up', path: '/admin/indexConfigs?configType=5', section: 'indexConfigs', type: 5 },
  ] },
  { heading: '管理模块', items: [
    { label: '分类管理', icon: 'fa-list-alt', path: '/admin/categories?parentId=0&categoryLevel=1&backParentId=0', section: 'categories' },
    { label: '商品管理', icon: 'fa-archive', path: '/admin/goods', section: 'goods' },
    { label: '会员管理', icon: 'fa-users', path: '/admin/users', section: 'users' },
    { label: '订单管理', icon: 'fa-money', path: '/admin/orders', section: 'orders' },
  ] },
  { heading: '营销管理', items: [
    { label: '优惠券管理', icon: 'fa-money', path: '/admin/coupon', section: 'coupon' },
    { label: '秒杀管理', icon: 'fa-buysellads', path: '/admin/seckill', section: 'seckill' },
  ] },
  { heading: '系统管理', items: [
    { label: '修改密码', icon: 'fa-user-secret', path: '/admin/profile', section: 'profile' },
    { label: '安全退出', icon: 'fa-sign-out', path: '/admin/logout', section: 'logout' },
  ] },
]

const listConfigs = {
  goods: {
    title: '商品管理', endpoint: '/admin/goods/list', id: 'goodsId',
    search: [
      { label: '商品id', key: 'goodsId' }, { label: '商品名称', key: 'goodsName' },
      { label: '商品简介', key: 'goodsIntro' },
      { label: '上架状态', key: 'goodsSellStatus', options: [['0', '上架'], ['1', '下架']] },
    ],
    columns: [
      ['商品编号', 'goodsId'], ['商品名', 'goodsName'], ['商品简介', 'goodsIntro'],
      ['商品图片', 'goodsCoverImg', 'image'], ['商品库存', 'stockNum'],
      ['商品售价', 'sellingPrice'], ['上架状态', 'goodsSellStatus', 'goodsStatus'],
      ['创建时间', 'createTime'],
    ],
    actions: [
      ['添加商品', 'fa-plus', 'info', 'add'], ['修改商品', 'fa-pencil-square-o', 'info', 'edit'],
      ['上架商品', 'fa-arrow-up', 'success', 'putUp'], ['下架商品', 'fa-arrow-down', 'danger', 'putDown'],
      ['同步RS', 'fa-clone', 'warning', 'sync'],
    ],
  },
  categories: {
    title: '分类管理', endpoint: '/admin/categories/list', id: 'categoryId',
    columns: [
      ['id', 'categoryId'], ['分类名称', 'categoryName', 'categoryLink'],
      ['分类级别', 'categoryLevel', 'categoryLevel'], ['排序值', 'categoryRank'],
      ['添加时间', 'createTime'],
    ],
    actions: [
      ['新增分类', 'fa-plus', 'info', 'add'], ['修改分类', 'fa-pencil-square-o', 'info', 'edit'],
      ['删除分类', 'fa-trash-o', 'danger', 'delete'],
    ],
    form: [
      ['分类名称', 'categoryName', 'text'], ['排序值', 'categoryRank', 'number'],
    ],
  },
  carousels: {
    title: '轮播图管理', endpoint: '/admin/carousels/list', id: 'carouselId',
    columns: [
      ['轮播图', 'carouselUrl', 'image'], ['跳转链接', 'redirectUrl'],
      ['排序值', 'carouselRank'], ['添加时间', 'createTime'],
    ],
    actions: [
      ['添加', 'fa-plus', 'info', 'add'], ['修改', 'fa-pencil-square-o', 'info', 'edit'],
      ['删除', 'fa-trash-o', 'danger', 'delete'],
    ],
    form: [
      ['轮播图', 'carouselUrl', 'upload'], ['跳转链接', 'redirectUrl', 'text'],
      ['排序值', 'carouselRank', 'number'],
    ],
  },
  indexConfigs: {
    title: '首页配置管理', endpoint: '/admin/indexConfigs/list', id: 'configId',
    columns: [
      ['配置项名称', 'configName'], ['跳转链接', 'redirectUrl'],
      ['排序值', 'configRank'], ['商品编号', 'goodsId'], ['添加时间', 'createTime'],
    ],
    actions: [
      ['添加', 'fa-plus', 'info', 'add'], ['修改', 'fa-pencil-square-o', 'info', 'edit'],
      ['删除', 'fa-trash-o', 'danger', 'delete'],
    ],
    form: [
      ['配置名称', 'configName', 'text'], ['商品编号', 'goodsId', 'number'],
      ['跳转链接', 'redirectUrl', 'text'], ['排序值', 'configRank', 'number'],
    ],
  },
  users: {
    title: '会员管理', endpoint: '/admin/users/list', id: 'userId',
    search: [
      { label: '昵称', key: 'nickName' }, { label: '登录名', key: 'loginName' },
      { label: '身份状态', key: 'lockedFlag', options: [['0', '正常'], ['1', '锁定']] },
    ],
    columns: [
      ['昵称', 'nickName'], ['登录名', 'loginName'], ['身份状态', 'lockedFlag', 'lockStatus'],
      ['是否注销', 'isDeleted', 'deletedStatus'], ['注册时间', 'createTime'],
    ],
    actions: [
      ['解除锁定', 'fa-unlock', 'info', 'unlock'], ['锁定账户', 'fa-lock', 'danger', 'lock'],
    ],
  },
  orders: {
    title: '订单管理', endpoint: '/admin/orders/list', id: 'orderId',
    search: [
      { label: '订单号', key: 'orderNo' },
      { label: '订单状态', key: 'orderStatus', options: [['0', '待支付'], ['1', '已支付'], ['2', '配货完成'], ['3', '出库成功'], ['4', '交易成功']] },
      { label: '创建时间', key: 'createTime' },
    ],
    columns: [
      ['订单号', 'orderNo'], ['订单总价', 'totalPrice'], ['订单状态', 'orderStatus', 'orderStatus'],
      ['支付方式', 'payType', 'payType'], ['创建时间', 'createTime'],
      ['操作', 'orderId', 'orderAction'],
    ],
    actions: [
      ['修改订单', 'fa-pencil-square-o', 'info', 'edit'],
      ['配货完成', 'fa-truck', 'default', 'checkDone'],
      ['出库', 'fa-truck', 'warning', 'checkOut'],
      ['关闭订单', 'fa-trash-o', 'success', 'close'],
    ],
    form: [['订单价格', 'totalPrice', 'number'], ['收件人信息', 'userAddress', 'text']],
  },
  coupon: {
    title: '优惠券管理', endpoint: '/admin/coupon/list', id: 'couponId',
    search: [
      { label: '优惠券名称', key: 'name' },
      { label: '优惠券类型', key: 'couponType', options: [['0', '通用券'], ['1', '注册赠券'], ['2', '兑换券']] },
      { label: '状态', key: 'status', options: [['0', '上架'], ['1', '过期'], ['2', '下架']] },
      { label: '创建时间', key: 'createTime' },
    ],
    columns: [
      ['优惠券名称', 'name'], ['优惠券简介', 'couponDesc'], ['数量', 'couponTotal'],
      ['优惠金额', 'discount'], ['最低消费金额', 'min'], ['限制数量', 'couponLimit'],
      ['有效天数', 'days'], ['赠送类型', 'couponType'], ['优惠券状态', 'status', 'couponStatus'],
      ['创建时间', 'createTime'],
    ],
    actions: [
      ['添加', 'fa-plus', 'info', 'add'], ['修改', 'fa-pencil-square-o', 'info', 'edit'],
      ['删除', 'fa-trash-o', 'danger', 'delete'],
    ],
    form: [
      ['名称', 'name', 'text'], ['简介', 'couponDesc', 'text'], ['数量', 'couponTotal', 'number'],
      ['优惠金额', 'discount', 'number'], ['最低消费金额', 'min', 'number'],
      ['限制数量', 'couponLimit', 'number'], ['有效天数', 'days', 'number'],
      ['赠送类型', 'couponType', 'number'], ['优惠券状态', 'status', 'number'],
      ['商品类型', 'goodsType', 'number'], ['商品值', 'goodsValue', 'text'],
    ],
  },
  seckill: {
    title: '秒杀管理', endpoint: '/admin/seckill/list', id: 'seckillId',
    search: [{ label: '秒杀商品Id', key: 'goodsId' }, { label: '创建时间', key: 'createTime' }],
    columns: [
      ['序号', 'seckillId'], ['秒杀商品Id', 'goodsId'], ['秒杀价格', 'seckillPrice'],
      ['秒杀数量', 'seckillNum'], ['上架状态', 'status', 'seckillStatus'],
      ['秒杀开始', 'seckillBegin'], ['秒杀结束', 'seckillEnd'],
      ['商品排序', 'seckillRank'], ['创建时间', 'createTime'],
    ],
    actions: [
      ['添加', 'fa-plus', 'info', 'add'], ['修改', 'fa-pencil-square-o', 'info', 'edit'],
      ['删除', 'fa-trash-o', 'danger', 'delete'],
    ],
    form: [
      ['商品编号', 'goodsId', 'number'], ['秒杀价格', 'seckillPrice', 'number'],
      ['秒杀数量', 'seckillNum', 'number'], ['上架状态', 'status', 'number'],
      ['秒杀开始', 'seckillBegin', 'datetime-local'],
      ['秒杀结束', 'seckillEnd', 'datetime-local'], ['商品排序', 'seckillRank', 'number'],
    ],
  },
}

const section = computed(() => {
  const path = route.path
  if (path === '/admin') return 'dashboard'
  if (/^\/admin\/goods\/(?:add|new|edit\/|[^/]+\/edit)/.test(path)) return 'goods-editor'
  const segment = path.split('/')[2]
  return ({ 'index-configs': 'indexConfigs', members: 'users', coupons: 'coupon' })[segment] || segment || 'dashboard'
})
const configType = computed(() => Number(route.query.configType || ({ new: 4, recommend: 5 })[route.query.type] || 3))
const categoryLevel = computed(() => Number(route.query.categoryLevel || 1))
const categoryParent = computed(() => Number(route.query.parentId || 0))
const activeConfig = computed(() => listConfigs[section.value])
const isGoodsEdit = computed(() => /^\/admin\/goods\/edit\/|^\/admin\/goods\/[^/]+\/edit$/.test(route.path))
const goodsEditId = computed(() => String(route.params.id || route.path.match(/\/goods\/edit\/([^/]+)/)?.[1] || ''))
const heading = computed(() => section.value === 'goods-editor' ? '商品信息编辑' :
  activeConfig.value?.title || ({ dashboard: '工作台', statistics: '统计信息', profile: '个人资料' })[section.value] || '首页')
const indexConfigName = computed(() => ({ 3: '热销商品配置', 4: '新品上线配置', 5: '为你推荐配置' })[configType.value] || '首页配置')
const chartPoints = computed(() => {
  const values = statisticsValues.value.map(Number)
  const max = Math.max(1, ...values)
  if (values.length < 2) return ''
  return values.map((value, i) => (i * 760 / (values.length - 1)) + ',' + (280 - value * 250 / max)).join(' ')
})

function messageFor(errorValue) {
  return errorValue?.message || '请求失败'
}
function onRequestError(cause) {
  if (cause?.auth) {
    router.replace('/admin/login')
    return
  }
  error.value = cause instanceof TypeError ? '无法连接后端，请先启动项目' : messageFor(cause)
}
function clearMessages() {
  error.value = ''
  success.value = ''
}
function rowId(row) {
  return row?.[activeConfig.value?.id]
}
function selectedRows() {
  return rows.value.filter(row => selectedIds.value.some(id => String(id) === String(rowId(row))))
}
function requireSelection(single = false) {
  const selected = selectedRows()
  if (!selected.length || (single && selected.length !== 1)) {
    error.value = single ? '请选择一条记录' : '请选择记录'
    return null
  }
  return selected
}
function queryString(extra = {}) {
  const params = new URLSearchParams({ pageNumber: String(pageNumber.value), pageSize: String(pageSize.value) })
  Object.entries({ ...search, ...extra }).forEach(([key, value]) => {
    if (value === '' || value === undefined || value === null) return
    if (key === 'createTime' && ['orders', 'coupon', 'seckill'].includes(section.value)) {
      const dateRange = String(value).split(/\s+-\s+/)
      if (dateRange[0]) params.set('startTime', dateRange[0])
      if (dateRange[1]) params.set('endTime', dateRange[1])
    } else params.set(key, String(value))
  })
  return params.toString()
}
async function loadList() {
  const cfg = activeConfig.value
  if (!cfg) return
  const serial = ++requestSerial
  loading.value = true
  error.value = ''
  const extra = section.value === 'categories'
    ? { categoryLevel: categoryLevel.value, parentId: categoryParent.value }
    : section.value === 'indexConfigs' ? { configType: configType.value } : {}
  try {
    const result = await legacyRequest(cfg.endpoint + '?' + queryString(extra))
    if (serial !== requestSerial) return
    rows.value = Array.isArray(result.records) ? result.records : []
    total.value = Number(result.total || 0)
    pages.value = Number(result.pages || 0)
    selectedIds.value = []
  } catch (cause) {
    if (serial !== requestSerial) return
    rows.value = []
    total.value = 0
    pages.value = 0
    onRequestError(cause)
  } finally {
    if (serial === requestSerial) loading.value = false
  }
}
async function loadStatistics() {
  error.value = ''
  try {
    const result = await legacyRequest('/admin/statistics/transactionAmount/' + statisticsDays.value)
    statisticsDates.value = result.map?.xAxisData || []
    statisticsValues.value = result.map?.seriesData || []
  } catch (cause) {
    statisticsDates.value = []
    statisticsValues.value = []
    onRequestError(cause)
  }
}
async function loadProfile() {
  try {
    const data = (await legacyRequest('/admin/profile')).map || {}
    profile.loginUserName = data.loginUserName || ''
    profile.nickName = data.nickName || ''
  } catch (cause) {
    onRequestError(cause)
  }
}
async function loadGoodsEditor() {
  Object.assign(goods, {
    goodsId: isGoodsEdit.value ? goodsEditId.value : 0, goodsName: '', goodsIntro: '',
    goodsCategoryId: '', originalPrice: '', sellingPrice: '', stockNum: '',
    tag: '', goodsSellStatus: '0', goodsCoverImg: '', goodsDetailContent: '',
  })
  categoryOne.value = []
  categoryTwo.value = []
  categoryThree.value = []
  levelOne.value = ''
  levelTwo.value = ''
  try {
    const path = isGoodsEdit.value ? '/admin/goods/edit/' + goodsEditId.value : '/admin/goods/add'
    const data = (await legacyRequest(path)).map || {}
    Object.assign(goods, data.goods || {})
    goods.goodsSellStatus = String(goods.goodsSellStatus ?? '0')
    const options = list => (list || []).map(item => ({ id: item.categoryId, name: item.categoryName }))
    categoryOne.value = options(data.firstLevelCategories)
    categoryTwo.value = options(data.secondLevelCategories)
    categoryThree.value = options(data.thirdLevelCategories)
    levelOne.value = data.firstLevelCategoryId || categoryOne.value[0]?.id || ''
    levelTwo.value = data.secondLevelCategoryId || categoryTwo.value[0]?.id || ''
    goods.goodsCategoryId = data.thirdLevelCategoryId || goods.goodsCategoryId || categoryThree.value[0]?.id || ''
    if (!categoryTwo.value.length && levelOne.value) await onFirstCategoryChange()
  } catch (cause) {
    onRequestError(cause)
  }
}
async function onFirstCategoryChange() {
  if (!levelOne.value) return
  try {
    const result = await legacyRequest('/admin/categories/listForSelect?categoryId=' + levelOne.value)
    categoryTwo.value = (result.map?.data?.secondLevelCategories || []).map(item => ({ id: item.categoryId, name: item.categoryName }))
    categoryThree.value = (result.map?.data?.thirdLevelCategories || []).map(item => ({ id: item.categoryId, name: item.categoryName }))
    levelTwo.value = categoryTwo.value[0]?.id || ''
    goods.goodsCategoryId = categoryThree.value[0]?.id || ''
  } catch (cause) {
    onRequestError(cause)
  }
}
async function onSecondCategoryChange() {
  if (!levelTwo.value) return
  try {
    const result = await legacyRequest('/admin/categories/listForSelect?categoryId=' + levelTwo.value)
    categoryThree.value = (result.map?.data?.thirdLevelCategories || []).map(item => ({ id: item.categoryId, name: item.categoryName }))
    goods.goodsCategoryId = categoryThree.value[0]?.id || ''
  } catch (cause) {
    onRequestError(cause)
  }
}
async function uploadImage(event, target) {
  const file = event.target.files?.[0]
  if (!file) return
  if (!/^image\/(jpeg|png|gif)$/.test(file.type)) {
    error.value = '只支持 jpg、png、gif 格式的文件'
    return
  }
  const form = new FormData()
  form.append('file', file)
  clearMessages()
  try {
    const result = await legacyRequest('/common/upload', { method: 'POST', body: form })
    const url = result.map?.url
    if (!url) throw new Error('上传结果缺少图片地址')
    if (target === 'goods') goods.goodsCoverImg = url
    else modalValues.carouselUrl = url
  } catch (cause) {
    onRequestError(cause)
  }
  event.target.value = ''
}
async function saveGoods() {
  clearMessages()
  if (!goods.goodsCategoryId || !goods.goodsName.trim() || !goods.goodsIntro.trim()
    || !goods.tag.trim() || !goods.goodsCoverImg || !goods.goodsDetailContent.trim()) {
    error.value = '请完整填写分类、名称、简介、标签、图片和商品介绍'
    return
  }
  const payload = { ...goods, goodsCarousel: goods.goodsCoverImg }
  try {
    await legacyRequest('/admin/goods/' + (isGoodsEdit.value ? 'update' : 'save'), { method: 'POST', body: payload })
    router.push('/admin/goods')
  } catch (cause) {
    onRequestError(cause)
  }
}
function openModal(mode) {
  clearMessages()
  const cfg = activeConfig.value
  const selected = mode === 'edit' ? requireSelection(true) : []
  if (mode === 'edit' && !selected) return
  if (section.value === 'goods') {
    router.push(mode === 'edit' ? '/admin/goods/edit/' + selected[0].goodsId : '/admin/goods/add')
    return
  }
  const item = selected?.[0] || {}
  Object.keys(modalValues).forEach(key => delete modalValues[key])
  cfg.form?.forEach(([, key, kind]) => {
    let value = item[key] ?? ''
    if (kind === 'datetime-local' && value) value = String(value).replace(' ', 'T').slice(0, 16)
    modalValues[key] = value
  })
  if (mode === 'add' && section.value === 'coupon') {
    Object.assign(modalValues, { couponLimit: 0, couponType: 0, status: 0, goodsType: 0 })
  }
  if (mode === 'add' && section.value === 'seckill') modalValues.status = 0
  if (mode === 'edit') modalValues[cfg.id] = item[cfg.id]
  modal.value = { mode, title: mode === 'edit' ? '修改' : '添加', fields: cfg.form || [] }
}
async function saveModal() {
  if (!modal.value) return
  const cfg = activeConfig.value
  const payload = { ...modalValues }
  if (section.value === 'categories') {
    payload.categoryLevel = categoryLevel.value
    payload.parentId = categoryParent.value
  }
  if (section.value === 'indexConfigs') payload.configType = configType.value
  if (section.value === 'orders') {
    const selected = requireSelection(true)
    if (!selected) return
    payload.orderId = selected[0].orderId
  }
  try {
    await legacyRequest(cfg.endpoint.replace(/\/list$/, '') + '/' +
      (modal.value.mode === 'edit' ? 'update' : 'save'), { method: 'POST', body: payload })
    modal.value = null
    success.value = '保存成功'
    await loadList()
  } catch (cause) {
    onRequestError(cause)
  }
}
async function applySelected(action) {
  clearMessages()
  const cfg = activeConfig.value
  if (action === 'add' || action === 'edit') {
    openModal(action)
    return
  }
  if (action === 'sync') {
    try {
      await legacyRequest('/admin/goods/syncRs', { method: 'POST' })
      success.value = '同步完成'
    } catch (cause) { onRequestError(cause) }
    return
  }
  const selected = requireSelection()
  if (!selected) return
  const ids = selected.map(row => row[cfg.id])
  try {
    let path
    let method = 'POST'
    if (section.value === 'goods') {
      path = '/admin/goods/status/' + (action === 'putUp' ? '0' : '1')
      method = 'PUT'
    } else if (section.value === 'users') {
      path = '/admin/users/lock/' + (action === 'unlock' ? '0' : '1')
    } else if (section.value === 'orders') {
      path = '/admin/orders/' + action
    } else if (section.value === 'coupon' || section.value === 'seckill') {
      method = 'DELETE'
      for (const id of ids) await legacyRequest(cfg.endpoint.replace(/\/list$/, '') + '/' + id, { method })
      success.value = '操作成功'
      await loadList()
      return
    } else {
      path = cfg.endpoint.replace(/\/list$/, '') + '/delete'
    }
    await legacyRequest(path, { method, body: ids })
    success.value = '操作成功'
    await loadList()
  } catch (cause) {
    onRequestError(cause)
  }
}
function toggleAll(event) {
  selectedIds.value = event.target.checked ? rows.value.map(row => rowId(row)) : []
}
function searchList() {
  pageNumber.value = 1
  loadList()
}
function resetSearch() {
  Object.keys(search).forEach(key => { search[key] = '' })
  searchList()
}
function changePage(value) {
  if (value < 1 || value > pages.value) return
  pageNumber.value = value
  loadList()
}
function displayCell(row, column) {
  const value = row[column[1]]
  const kind = column[2]
  if (section.value === 'coupon' && column[1] === 'couponTotal' && Number(value) === 0) return '无限制'
  if (section.value === 'coupon' && column[1] === 'couponLimit') return Number(value) === 0 ? '不限制' : '限领一张'
  if (section.value === 'coupon' && column[1] === 'couponType') return ({ 0: '通用券', 1: '注册赠券', 2: '优惠码兑换券' })[value] || value
  if (kind === 'goodsStatus') return Number(value) === 0 ? '销售中' : '已下架'
  if (kind === 'lockStatus') return Number(value) === 0 ? '正常' : '锁定'
  if (kind === 'deletedStatus') return Number(value) === 0 ? '正常' : '注销'
  if (kind === 'orderStatus') return ({ 0: '待支付', 1: '已支付', 2: '配货完成', 3: '出库成功', 4: '交易成功', '-1': '手动关闭', '-2': '超时关闭', '-3': '商家关闭' })[value] || value
  if (kind === 'payType') return ({ 0: '未支付', 1: '支付宝', 2: '微信' })[value] || value
  if (kind === 'categoryLevel') return ({ 1: '一级分类', 2: '二级分类', 3: '三级分类' })[value] || value
  if (kind === 'couponStatus') return ({ 0: '上架', 1: '已过期', 2: '下架' })[value] || value
  if (kind === 'seckillStatus') return Number(value) === 1 ? '上架中' : '已下架'
  return value ?? ''
}
function radioOptions(key) {
  if (section.value === 'coupon') {
    return ({
      couponLimit: [[0, '无限制'], [1, '限领一张']],
      couponType: [[0, '通用券'], [1, '注册赠券'], [2, '优惠码兑券']],
      status: [[0, '可用'], [2, '下架']],
      goodsType: [[0, '全商品'], [1, '类目限制'], [2, '商品限制']],
    })[key]
  }
  if (section.value === 'seckill' && key === 'status') return [[0, '下架'], [1, '上架']]
  return null
}
function statusClass(row, column) {
  const kind = column[2]
  const value = Number(row[column[1]])
  if (kind === 'couponStatus') return value === 0 ? 'btn-success' : value === 1 ? 'btn-warning' : 'btn-danger'
  if (kind === 'seckillStatus') return value === 1 ? 'btn-success' : 'btn-danger'
  return value === 0 ? 'btn-success' : 'btn-secondary'
}
async function showOrderItems(row) {
  clearMessages()
  try {
    const result = await legacyRequest('/admin/orders/order-items/' + row.orderId)
    modal.value = { mode: 'items', title: '订单详情', items: result.map?.data || result.map?.orderItems || [] }
  } catch (cause) { onRequestError(cause) }
}
async function saveProfile(type) {
  clearMessages()
  const payload = type === 'name'
    ? { loginUserName: profile.loginUserName, nickName: profile.nickName }
    : { originalPassword: profile.originalPassword, newPassword: profile.newPassword }
  try {
    const result = await legacyRequest('/admin/profile/' + type, { method: 'POST', body: formBody(payload) })
    if (result !== 'success') throw new Error(String(result || '修改失败'))
    if (type === 'password') router.push('/admin/login')
    else success.value = '修改成功'
  } catch (cause) { onRequestError(cause) }
}
async function signOut() {
  try { await legacyRequest('/admin/logout') } catch { /* A stopped backend has no session to clear. */ }
  router.push('/admin/login')
}
function onNav(item) {
  mobileSidebarOpen.value = false
  if (item.section === 'logout') signOut()
}
function isActive(item) {
  return item.section === section.value &&
    (item.section !== 'indexConfigs' || item.type === configType.value)
}
watch(() => route.fullPath, async () => {
  ++requestSerial
  clearMessages()
  modal.value = null
  mobileSidebarOpen.value = false
  pageNumber.value = 1
  pageSize.value = section.value === 'goods' ? 20 : 10
  rows.value = []
  selectedIds.value = []
  Object.keys(search).forEach(key => delete search[key])
  await nextTick()
  if (activeConfig.value) loadList()
  else if (section.value === 'goods-editor') loadGoodsEditor()
  else if (section.value === 'statistics') loadStatistics()
  else if (section.value === 'profile') loadProfile()
  else if (section.value === 'dashboard') {
    try { await legacyRequest('/admin') } catch (cause) { onRequestError(cause) }
  }
}, { immediate: true })
</script>

<template>
  <div class="wrapper hold-transition sidebar-mini layout-fixed legacy-admin" :class="{ 'sidebar-collapse': sidebarCollapsed, 'sidebar-open': mobileSidebarOpen }">
    <nav class="main-header navbar navbar-expand navbar-white navbar-light">
      <ul class="navbar-nav">
        <li class="nav-item"><a class="nav-link" href="#" aria-label="展开或收起侧栏" @click.prevent="sidebarCollapsed = !sidebarCollapsed; mobileSidebarOpen = !mobileSidebarOpen"><i class="fa fa-bars"></i></a></li>
        <li class="nav-item d-none d-sm-inline-block"><RouterLink class="nav-link" to="/admin">工作台</RouterLink></li>
      </ul>
      <ul class="navbar-nav ml-auto">
        <li class="nav-item"><RouterLink class="nav-link" to="/" target="_blank"><i class="fa fa-paper-plane">&nbsp;&nbsp;haoxiang商城首页</i></RouterLink></li>
        <li class="nav-item"><RouterLink class="nav-link" to="/admin/profile"><i class="fa fa-user">&nbsp;&nbsp;管理员</i></RouterLink></li>
      </ul>
    </nav>

    <aside class="main-sidebar sidebar-dark-primary elevation-4">
      <RouterLink to="/admin" class="brand-link">
        <span class="brand-image img-circle elevation-3 legacy-admin__brand-mark">H</span>
        <span class="brand-text font-weight-light">haoxiang商城</span>
      </RouterLink>
      <div class="sidebar">
        <nav class="legacy-admin__sidebar-nav" aria-label="后台导航">
          <ul class="nav nav-pills nav-sidebar flex-column" role="menu">
            <template v-for="group in navGroups" :key="group.heading">
              <li class="nav-header">{{ group.heading }}</li>
              <li v-for="item in group.items" :key="item.path" class="nav-item">
                <a v-if="item.section === 'logout'" class="nav-link" href="#" @click.prevent="onNav(item)"><i class="nav-icon fa" :class="item.icon"></i><p>{{ item.label }}</p></a>
                <RouterLink v-else :to="item.path" class="nav-link" :class="{ active: isActive(item) }" @click="onNav(item)">
                  <i class="nav-icon fa" :class="item.icon"></i><p>{{ item.label }}</p>
                </RouterLink>
              </li>
            </template>
          </ul>
        </nav>
      </div>
    </aside>

    <div class="content-wrapper">
      <div class="content-header">
        <div class="container-fluid">
          <div class="row mb-2">
            <div class="col-sm-6"></div>
            <div class="col-sm-6">
              <ol class="breadcrumb float-sm-right">
                <li class="breadcrumb-item"><RouterLink to="/admin">Home</RouterLink></li>
                <li class="breadcrumb-item active">{{ section === 'indexConfigs' ? indexConfigName : heading }}</li>
              </ol>
            </div>
          </div>
        </div>
      </div>
      <section class="content">
        <div class="container-fluid">
          <div v-if="error" class="alert alert-danger" role="alert">{{ error }}</div>
          <div v-if="success" class="alert alert-success" role="status">{{ success }}</div>

          <div v-if="section === 'dashboard'" class="legacy-admin__dashboard">
            <section class="legacy-admin__welcome" aria-labelledby="dashboard-title">
              <div class="legacy-admin__welcome-copy">
                <h1 id="dashboard-title">好享商城管理工作台</h1>
                <p>从商品上架到订单处理，在这里管理商城内容、会员与营销活动。</p>
                <div class="legacy-admin__welcome-actions">
                  <RouterLink to="/admin/goods" class="legacy-admin__primary-link">管理商品 <i class="fa fa-arrow-right" aria-hidden="true"></i></RouterLink>
                  <RouterLink to="/" target="_blank" class="legacy-admin__secondary-link">查看商城首页 <i class="fa fa-external-link" aria-hidden="true"></i></RouterLink>
                </div>
              </div>
              <div class="legacy-admin__welcome-brand"><img src="/mall/image/haoxiang-logo.svg" alt="haoxiang mall" /></div>
            </section>

            <div class="legacy-admin__overview">
              <section class="legacy-admin__business" aria-labelledby="dashboard-business-title">
                <div class="legacy-admin__section-heading">
                  <h2 id="dashboard-business-title">商城功能</h2>
                  <p>日常运营所需的核心模块</p>
                </div>
                <div class="legacy-admin__feature-list">
                  <div class="legacy-admin__feature"><i class="fa fa-cubes" aria-hidden="true"></i><div><h3>商品与分类</h3><p>维护商品信息、库存和商品分类。</p></div></div>
                  <div class="legacy-admin__feature"><i class="fa fa-picture-o" aria-hidden="true"></i><div><h3>首页内容</h3><p>配置轮播图、热销商品、新品与推荐位。</p></div></div>
                  <div class="legacy-admin__feature"><i class="fa fa-shopping-bag" aria-hidden="true"></i><div><h3>订单与会员</h3><p>查看订单状态，管理商城会员。</p></div></div>
                  <div class="legacy-admin__feature"><i class="fa fa-ticket" aria-hidden="true"></i><div><h3>营销活动</h3><p>管理优惠券和秒杀商品。</p></div></div>
                </div>
              </section>

              <section class="legacy-admin__technology" aria-labelledby="dashboard-technology-title">
                <div class="legacy-admin__section-heading">
                  <h2 id="dashboard-technology-title">技术栈</h2>
                  <p>前后端分离的商城应用</p>
                </div>
                <dl class="legacy-admin__stack-list">
                  <div><dt>前端</dt><dd>Vue 3 · Vue Router · Vite</dd></div>
                  <div><dt>服务端</dt><dd>JDK 21 · Spring Boot 3</dd></div>
                  <div><dt>数据访问</dt><dd>MyBatis-Plus · MySQL 8</dd></div>
                  <div><dt>缓存与消息</dt><dd>Redis · RabbitMQ</dd></div>
                </dl>
                <RouterLink class="legacy-admin__statistics-link" to="/admin/statistics">查看经营统计 <i class="fa fa-long-arrow-right" aria-hidden="true"></i></RouterLink>
              </section>
            </div>
          </div>

          <div v-else-if="section === 'statistics'" class="card card-primary card-outline">
            <div class="card-header"><h3 class="card-title">统计信息</h3></div>
            <div class="search-content">
              <form class="form-inline" @submit.prevent="loadStatistics">
                <div class="form-group"><label for="statistics-days">查询天数</label><input id="statistics-days" v-model.number="statisticsDays" class="form-control" type="number" min="1" max="3650" /></div>
                <button type="submit" class="btn btn-success m-l">搜索</button>
                <button type="button" class="btn btn-info m-l" @click="statisticsDays = 365; loadStatistics()">重置</button>
              </form>
            </div>
            <div class="card-body">
              <div class="legacy-admin__chart" role="img" aria-label="交易金额统计图">
                <svg v-if="chartPoints" viewBox="0 0 800 320" preserveAspectRatio="none" aria-hidden="true">
                  <line v-for="y in [30, 92, 155, 218, 280]" :key="y" x1="20" :y1="y" x2="780" :y2="y" stroke="#e9ecef" />
                  <polyline :points="chartPoints" fill="none" stroke="#1baeae" stroke-width="3" />
                </svg>
                <span v-else>暂无数据</span>
              </div>
              <div v-if="statisticsDates.length" class="d-flex justify-content-between text-muted small">
                <span>{{ statisticsDates[0] }}</span><span>交易金额</span><span>{{ statisticsDates[statisticsDates.length - 1] }}</span>
              </div>
            </div>
          </div>

          <template v-else-if="section === 'profile'">
            <div class="row">
              <div class="col-md-6">
                <div class="card card-primary card-outline">
                  <div class="card-header"><h3 class="card-title">基本信息</h3></div>
                  <form @submit.prevent="saveProfile('name')">
                    <div class="card-body">
                      <div class="form-group"><label for="profile-login-name">登录名称</label><input id="profile-login-name" v-model="profile.loginUserName" class="form-control" required /></div>
                      <div class="form-group"><label for="profile-nick-name">昵称</label><input id="profile-nick-name" v-model="profile.nickName" class="form-control" required /></div>
                    </div>
                    <div class="card-footer"><button class="btn btn-info" type="submit">确认修改</button></div>
                  </form>
                </div>
              </div>
              <div class="col-md-6">
                <div class="card card-primary card-outline">
                  <div class="card-header"><h3 class="card-title">修改密码</h3></div>
                  <form @submit.prevent="saveProfile('password')">
                    <div class="card-body">
                      <div class="form-group"><label for="profile-old-password">原密码</label><input id="profile-old-password" v-model="profile.originalPassword" class="form-control" type="password" required /></div>
                      <div class="form-group"><label for="profile-new-password">新密码</label><input id="profile-new-password" v-model="profile.newPassword" class="form-control" type="password" required /></div>
                    </div>
                    <div class="card-footer"><button class="btn btn-info" type="submit">确认修改</button></div>
                  </form>
                </div>
              </div>
            </div>
          </template>

          <div v-else-if="section === 'goods-editor'" class="card card-primary card-outline">
            <div class="card-header"><h3 class="card-title">商品信息编辑</h3></div>
            <div class="card-body">
              <form @submit.prevent="saveGoods">
                <div class="form-group form-inline">
                  <label class="control-label">请选择分类：</label>
                  <select v-model="levelOne" class="form-control col-sm-3" @change="onFirstCategoryChange"><option v-for="item in categoryOne" :key="item.id" :value="item.id">{{ item.name }}</option></select>&nbsp;
                  <select v-model="levelTwo" class="form-control col-sm-3" @change="onSecondCategoryChange"><option v-for="item in categoryTwo" :key="item.id" :value="item.id">{{ item.name }}</option></select>&nbsp;
                  <select v-model="goods.goodsCategoryId" class="form-control col-sm-3"><option v-for="item in categoryThree" :key="item.id" :value="item.id">{{ item.name }}</option></select>
                </div>
                <div class="form-group form-inline">
                  <label for="goodsName" class="control-label">商品名称：</label>
                  <input id="goodsName" v-model="goods.goodsName" class="form-control col-sm-5" placeholder="*请输入商品名称(必填)" maxlength="100" required />
                  &nbsp;&nbsp;<label for="goodsIntro" class="control-label">商品简介：</label>
                  <input id="goodsIntro" v-model="goods.goodsIntro" class="form-control col-sm-5" placeholder="*请输入商品简介(100字以内)" maxlength="100" required />
                </div>
                <div class="form-group form-inline">
                  <label for="originalPrice" class="control-label">商品价格：</label>
                  <input id="originalPrice" v-model="goods.originalPrice" type="number" min="1" class="form-control col-sm-5" required />
                  &nbsp;&nbsp;<label for="sellingPrice" class="control-label">商品售价：</label>
                  <input id="sellingPrice" v-model="goods.sellingPrice" type="number" min="1" class="form-control col-sm-5" required />
                </div>
                <div class="form-group form-inline">
                  <label for="stockNum" class="control-label">商品库存：</label>
                  <input id="stockNum" v-model="goods.stockNum" type="number" min="0" class="form-control col-sm-5" required />
                  &nbsp;&nbsp;<label for="goodsTag" class="control-label">商品标签：</label>
                  <input id="goodsTag" v-model="goods.tag" class="form-control col-sm-5" placeholder="多个标签用 | 分隔" required />
                </div>
                <div class="form-group">
                  <label class="control-label">上架状态：</label>
                  <label><input v-model="goods.goodsSellStatus" name="goodsSellStatus" type="radio" value="0" />&nbsp;上架&nbsp;</label>
                  <label><input v-model="goods.goodsSellStatus" name="goodsSellStatus" type="radio" value="1" />&nbsp;下架&nbsp;</label>
                </div>
                <div class="form-group"><img :src="goods.goodsCoverImg ? legacyImageUrl(goods.goodsCoverImg) : '/admin/dist/img/img-upload.png'" alt="商品主图" class="legacy-admin__goods-cover" /></div>
                <div class="form-group"><label class="btn btn-info"><i class="fa fa-picture-o"></i>&nbsp;上传商品主图<input type="file" accept=".jpg,.jpeg,.png,.gif" hidden @change="uploadImage($event, 'goods')" /></label></div>
                <div class="form-group"><label for="goodsDetailContent">商品介绍</label><textarea id="goodsDetailContent" v-model="goods.goodsDetailContent" class="form-control legacy-admin__editor" required></textarea></div>
                <div class="form-group clearfix">
                  <button type="submit" class="btn btn-info float-right ml-1">保存商品</button>
                  <RouterLink to="/admin/goods" class="btn btn-secondary float-right">返回商品列表</RouterLink>
                </div>
              </form>
            </div>
          </div>

          <div v-else-if="activeConfig" class="card card-primary card-outline">
            <div class="card-header"><h3 class="card-title">{{ activeConfig.title }}</h3></div>
            <div v-if="section === 'indexConfigs'" class="legacy-admin__config-title">{{ indexConfigName }}</div>
            <div v-if="section === 'categories' && categoryLevel > 1" class="legacy-admin__category-back">
              <RouterLink :to="'/admin/categories?parentId=' + (route.query.backParentId || 0) + '&categoryLevel=' + (categoryLevel - 1) + '&backParentId=0'" class="btn btn-info btn-sm"><i class="fa fa-arrow-left"></i>&nbsp;返回上一级</RouterLink>
            </div>
            <div v-if="activeConfig.search" class="search-content">
              <form class="form-inline" @submit.prevent="searchList">
                <div v-for="field in activeConfig.search" :key="field.key" class="form-group">
                  <label :for="'search-' + field.key" class="m-l">{{ field.label }}</label>
                  <select v-if="field.options" :id="'search-' + field.key" v-model="search[field.key]" class="form-control">
                    <option value="">请选择</option>
                    <option v-for="option in field.options" :key="option[0]" :value="option[0]">{{ option[1] }}</option>
                  </select>
                  <input v-else :id="'search-' + field.key" v-model="search[field.key]" type="text" class="form-control" />
                </div>
                <button type="submit" class="btn btn-success m-l">搜索</button>
                <button type="button" class="btn btn-info m-l" @click="resetSearch">重置</button>
              </form>
            </div>
            <div class="card-body">
              <div class="grid-btn">
                <button v-for="action in activeConfig.actions" :key="action[3]" type="button" class="btn" :class="'btn-' + action[2]" @click="applySelected(action[3])">
                  <i class="fa" :class="action[1]"></i>&nbsp;{{ action[0] }}
                </button>
              </div>
              <div class="table-responsive">
                <table class="table table-bordered legacy-admin__table" aria-label="管理列表">
                  <thead><tr>
                    <th class="legacy-admin__select"><input type="checkbox" :checked="rows.length > 0 && selectedIds.length === rows.length" aria-label="全选" @change="toggleAll" /></th>
                    <th v-for="column in activeConfig.columns" :key="column[0]">{{ column[0] }}</th>
                  </tr></thead>
                  <tbody>
                    <tr v-for="row in rows" :key="rowId(row)">
                      <td><input v-model="selectedIds" type="checkbox" :value="rowId(row)" :aria-label="'选择 ' + rowId(row)" /></td>
                      <td v-for="column in activeConfig.columns" :key="column[0]">
                        <img v-if="column[2] === 'image' && row[column[1]]" :src="legacyImageUrl(row[column[1]])" :alt="column[0]" :class="section === 'carousels' ? 'legacy-admin__carousel-image' : 'legacy-admin__table-image'" />
                        <RouterLink v-else-if="column[2] === 'categoryLink' && categoryLevel < 3" :to="'/admin/categories?parentId=' + row.categoryId + '&categoryLevel=' + (categoryLevel + 1) + '&backParentId=' + categoryParent">{{ displayCell(row, column) }}</RouterLink>
                        <button v-else-if="['goodsStatus', 'lockStatus', 'deletedStatus', 'couponStatus', 'seckillStatus'].includes(column[2])" class="btn btn-block btn-sm" :class="statusClass(row, column)" type="button">{{ displayCell(row, column) }}</button>
                        <template v-else-if="column[2] === 'orderAction'"><a href="#" @click.prevent="showOrderItems(row)">查看订单信息</a><br /><a href="#" @click.prevent="modal = { mode: 'address', title: '收件人信息', address: row.userAddress }">查看收件人信息</a></template>
                        <span v-else>{{ displayCell(row, column) }}</span>
                      </td>
                    </tr>
                    <tr v-if="!rows.length"><td :colspan="activeConfig.columns.length + 1" class="text-center text-muted legacy-admin__no-rows">{{ loading ? '信息读取中...' : '暂无记录' }}</td></tr>
                  </tbody>
                </table>
              </div>
              <div class="legacy-admin__pager">
                <span>共 {{ total }} 条，每页 <select v-model.number="pageSize" aria-label="每页条数" @change="pageNumber = 1; loadList()"><option v-for="size in section === 'goods' ? [20, 50, 80] : [10, 20, 50]" :key="size" :value="size">{{ size }}</option></select> 条</span>
                <div class="btn-group">
                  <button class="btn btn-default btn-sm" type="button" :disabled="pageNumber <= 1" @click="changePage(pageNumber - 1)">上一页</button>
                  <span class="btn btn-default btn-sm disabled">{{ pageNumber }} / {{ Math.max(1, pages) }}</span>
                  <button class="btn btn-default btn-sm" type="button" :disabled="pageNumber >= pages" @click="changePage(pageNumber + 1)">下一页</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div v-if="modal" class="modal fade show legacy-admin__modal" role="dialog" aria-modal="true" :aria-label="modal.title" @click.self="modal = null">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header"><h6 class="modal-title">{{ modal.title }}</h6><button class="close" type="button" aria-label="关闭" @click="modal = null"><span aria-hidden="true">&times;</span></button></div>
          <div v-if="modal.mode === 'items'" class="modal-body">
            <table class="table table-bordered"><thead><tr><th>商品</th><th>数量</th><th>价格</th></tr></thead><tbody>
              <tr v-for="item in modal.items" :key="item.goodsId"><td>{{ item.goodsName }}</td><td>{{ item.goodsCount }}</td><td>{{ item.sellingPrice }}</td></tr>
              <tr v-if="!modal.items?.length"><td colspan="3" class="text-center text-muted">暂无记录</td></tr>
            </tbody></table>
          </div>
          <div v-else-if="modal.mode === 'address'" class="modal-body"><label class="control-label">收件信息：</label><span>{{ modal.address || '暂无记录' }}</span></div>
          <form v-else @submit.prevent="saveModal">
            <div class="modal-body">
              <div v-for="field in modal.fields" v-show="!(section === 'coupon' && field[1] === 'goodsValue' && Number(modalValues.goodsType) === 0)" :key="field[1]" class="form-group">
                <label :for="'modal-' + field[1]" class="control-label">{{ field[0] }}:</label>
                <div v-if="radioOptions(field[1])" class="btn-group btn-group-sm btn-group-toggle d-block">
                  <label v-for="option in radioOptions(field[1])" :key="option[0]" class="btn btn-info" :class="{ active: Number(modalValues[field[1]]) === option[0] }">
                    <input v-model.number="modalValues[field[1]]" type="radio" :name="field[1]" :value="option[0]" />{{ option[1] }}
                  </label>
                </div>
                <template v-else-if="field[2] === 'upload'">
                  <img v-if="modalValues[field[1]]" :src="legacyImageUrl(modalValues[field[1]])" :alt="field[0]" class="legacy-admin__upload-preview" />
                  <label class="btn btn-info d-block legacy-admin__upload-button"><i class="fa fa-picture-o"></i>&nbsp;上传图片<input type="file" accept=".jpg,.jpeg,.png,.gif" hidden @change="uploadImage($event, 'modal')" /></label>
                </template>
                <textarea v-else-if="field[1] === 'couponDesc'" :id="'modal-' + field[1]" v-model="modalValues[field[1]]" class="form-control" rows="2"></textarea>
                <input v-else :id="'modal-' + field[1]" v-model="modalValues[field[1]]" class="form-control" :type="field[2]" :required="!(section === 'coupon' && field[1] === 'goodsValue' && Number(modalValues.goodsType) === 0)" />
              </div>
            </div>
            <div class="modal-footer"><button type="button" class="btn btn-default" @click="modal = null">取消</button><button type="submit" class="btn btn-primary">确认</button></div>
          </form>
          <div v-if="['items', 'address'].includes(modal.mode)" class="modal-footer"><button type="button" class="btn btn-default" @click="modal = null">关闭</button></div>
        </div>
      </div>
    </div>
    <div v-if="modal" class="modal-backdrop fade show"></div>
  </div>
</template>

<style scoped>
.legacy-admin { min-height: 100vh; }
.legacy-admin__sidebar-nav { margin-top: 4px; }
.legacy-admin .nav-sidebar .nav-header { padding: 8px 12px 3px; font-size: 12px; line-height: 18px; }
.legacy-admin .nav-sidebar .nav-item > .nav-link { min-height: 36px; margin-bottom: 0; padding: 7px 12px; font-size: 14px; line-height: 20px; }
.legacy-admin .nav-sidebar .nav-icon { width: 20px; margin-right: 7px; font-size: 16px; line-height: 20px; }
.legacy-admin__brand-mark { width: 33px; height: 33px; line-height: 33px; margin-top: -2px; margin-right: .5rem; background: #1baeae; color: white; text-align: center; font-size: 24px; font-weight: 700; }
.legacy-admin__dashboard { max-width: 1440px; margin: 0 auto 32px; color: #26343d; }
.legacy-admin__welcome { display: grid; grid-template-columns: minmax(0, 1.4fr) minmax(240px, .6fr); align-items: center; gap: 48px; min-height: 286px; padding: 40px 48px; border-radius: 16px; background: #283740; color: #fff; }
.legacy-admin__welcome-copy h1 { margin: 0 0 16px; font-size: clamp(28px, 2.4vw, 38px); font-weight: 700; line-height: 1.3; }
.legacy-admin__welcome-copy p { max-width: 35em; margin: 0; color: #d8e3e5; font-size: 16px; line-height: 1.7; }
.legacy-admin__welcome-actions { display: flex; flex-wrap: wrap; align-items: center; gap: 12px 24px; margin-top: 28px; }
.legacy-admin__welcome-actions a { display: inline-flex; align-items: center; gap: 10px; min-height: 44px; font-weight: 600; text-decoration: none; }
.legacy-admin__welcome-actions a:focus-visible, .legacy-admin__statistics-link:focus-visible { outline: 3px solid #86e6df; outline-offset: 3px; }
.legacy-admin__primary-link { padding: 0 20px; border-radius: 7px; background: #0e7776; color: #fff; }
.legacy-admin__primary-link:hover { background: #075c5c; color: #fff; }
.legacy-admin__secondary-link { color: #e6f4f3; }
.legacy-admin__secondary-link:hover { color: #9aeee6; }
.legacy-admin__welcome-brand { display: grid; place-items: center; min-height: 168px; padding: 24px; border-radius: 14px; background: #fff; }
.legacy-admin__welcome-brand img { display: block; width: 100%; max-width: 300px; height: auto; }
.legacy-admin__overview { display: grid; grid-template-columns: minmax(0, 1.2fr) minmax(290px, .8fr); gap: 24px; margin-top: 24px; }
.legacy-admin__business, .legacy-admin__technology { min-width: 0; padding: 30px 32px; border-radius: 14px; background: #fff; }
.legacy-admin__technology { background: #edf7f6; }
.legacy-admin__section-heading h2 { margin: 0; font-size: 23px; font-weight: 700; line-height: 1.35; }
.legacy-admin__section-heading p { margin: 7px 0 0; color: #60717a; font-size: 14px; }
.legacy-admin__feature-list { margin-top: 20px; }
.legacy-admin__feature { display: flex; align-items: flex-start; gap: 18px; padding: 17px 0; border-top: 1px solid #e8edef; }
.legacy-admin__feature > .fa { display: grid; flex: 0 0 38px; place-items: center; width: 38px; height: 38px; border-radius: 9px; background: #e4f5f3; color: #168d8c; font-size: 17px; }
.legacy-admin__feature h3 { margin: 0 0 3px; font-size: 16px; font-weight: 700; line-height: 1.45; }
.legacy-admin__feature p { margin: 0; color: #60717a; font-size: 14px; line-height: 1.6; }
.legacy-admin__stack-list { margin: 20px 0 0; }
.legacy-admin__stack-list > div { display: grid; grid-template-columns: 84px minmax(0, 1fr); gap: 10px; padding: 17px 0; border-top: 1px solid #d5e8e5; }
.legacy-admin__stack-list dt { color: #55716e; font-size: 14px; font-weight: 600; }
.legacy-admin__stack-list dd { min-width: 0; margin: 0; font-size: 14px; font-weight: 600; line-height: 1.5; overflow-wrap: anywhere; }
.legacy-admin__statistics-link { display: inline-flex; align-items: center; gap: 10px; margin-top: 14px; color: #0b6b6b; font-weight: 700; text-decoration: none; }
.legacy-admin__statistics-link:hover { color: #0d6261; text-decoration: underline; text-underline-offset: 4px; }
.legacy-admin__chart { width: 100%; height: 360px; display: grid; place-items: center; color: #6c757d; }
.legacy-admin__chart svg { width: 100%; height: 100%; }
.legacy-admin__editor { min-height: 380px; resize: vertical; }
.legacy-admin__goods-cover { width: 160px; height: 160px; object-fit: contain; }
.legacy-admin__table { width: 100%; margin-bottom: 0; }
.legacy-admin__table th { white-space: nowrap; background: #f8f9fa; }
.legacy-admin__table td { vertical-align: middle; }
.legacy-admin__table-image { height: 65px; max-width: 100px; object-fit: contain; }
.legacy-admin__carousel-image { height: 120px; max-width: 200px; object-fit: contain; }
.legacy-admin__select { width: 36px; }
.legacy-admin__no-rows { height: 80px; vertical-align: middle; }
.legacy-admin__pager { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; padding: 10px 0 0; font-size: 13px; }
.legacy-admin__pager select { border: 1px solid #ced4da; background: white; padding: 2px; }
.legacy-admin__modal { display: block; overflow-y: auto; }
.legacy-admin__upload-preview { width: 128px; height: 128px; object-fit: contain; display: block; margin-bottom: 8px; }
.legacy-admin__upload-button { width: fit-content; }
.legacy-admin__config-title, .legacy-admin__category-back { padding: 15px 20px 0; }
.legacy-admin .grid-btn .btn { margin-right: 5px; margin-bottom: 5px; }
@media (max-width: 991px) {
  .legacy-admin__welcome { grid-template-columns: minmax(0, 1fr) 220px; gap: 24px; padding: 32px; }
  .legacy-admin__overview { grid-template-columns: 1fr; }
}
@media (max-width: 767px) {
  .legacy-admin__welcome { grid-template-columns: 1fr; padding: 28px 24px; }
  .legacy-admin__welcome-brand { min-height: 120px; }
  .legacy-admin__welcome-brand img { max-width: 240px; }
  .legacy-admin__business, .legacy-admin__technology { padding: 24px; }
}
</style>
