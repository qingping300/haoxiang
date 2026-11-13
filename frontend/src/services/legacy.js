// The Spring Boot application keeps its original routes. Vite maps /legacy/*
// to those routes so Vue pages and legacy session cookies share one origin.
export const legacyUrl = (path = '/') => `/legacy${path.startsWith('/') ? path : `/${path}`}`

export class LegacyResponseError extends Error {
  constructor(message, { status = 0, code = null, auth = false } = {}) {
    super(message)
    this.name = 'LegacyResponseError'
    this.status = status
    this.code = code
    this.auth = auth
  }
}

export async function legacyRequest(path, { method = 'GET', body, signal, headers = {} } = {}) {
  const requestHeaders = { 'X-Requested-With': 'XMLHttpRequest', ...headers }
  let requestBody = body
  if (body && typeof body === 'object' && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
    requestBody = JSON.stringify(body)
    requestHeaders['Content-Type'] ||= 'application/json'
  }
  const response = await fetch(legacyUrl(path), {
    method,
    body: requestBody,
    signal,
    headers: requestHeaders,
    credentials: 'include',
  })
  const contentType = response.headers.get('content-type') || ''
  const isJson = contentType.includes('application/json')
  const value = isJson ? await response.json() : await response.text()
  if (response.status === 401) {
    const currentPath = window.location.pathname + window.location.search
    const loginPath = /^\/admin(?:\/|$)/.test(path)
      ? '/admin/login'
      : `/login?redirect=${encodeURIComponent(currentPath)}`
    window.location.assign(loginPath)
  }
  if (!response.ok) throw new LegacyResponseError(isJson ? value?.msg || '请求失败' : `请求失败（${response.status}）`, { status: response.status, auth: response.status === 401 })
  if (isJson && typeof value?.code === 'number' && value.code !== 200) {
    throw new LegacyResponseError(value.msg || '操作失败', { status: response.status, code: value.code, auth: /登录|登陆|login/i.test(value.msg || '') })
  }
  if (typeof value === 'string' && /<html|<!doctype/i.test(value) && !/\/login(?:\?|$)/.test(path) && /(?:id=["']loginName["']|class=["'][^"']*login-box)/i.test(value)) {
    throw new LegacyResponseError('请先登录', { status: response.status, auth: true })
  }
  return value
}

export async function legacyDocument(path, options) {
  const value = await legacyRequest(path, options)
  if (typeof value !== 'string') throw new LegacyResponseError('旧页面没有返回 HTML')
  return new DOMParser().parseFromString(value, 'text/html')
}

export function formBody(values) {
  const form = new URLSearchParams()
  for (const [key, value] of Object.entries(values)) if (value !== undefined && value !== null) form.set(key, String(value))
  return form
}

export function legacyImageUrl(value) {
  if (!value) return '/mall/image/null-content.png'
  if (/^https?:\/\//i.test(value)) return value
  return value.startsWith('/') ? value : `/${value}`
}

// Preserve server-rendered page markup when a legacy endpoint has no JSON
// equivalent. Scripts and inline handlers are removed; Vue owns actions.
export function legacyFragment(document, selector) {
  const source = document.querySelector(selector)
  if (!source) return ''
  const copy = source.cloneNode(true)
  copy.querySelectorAll('script, iframe, object, embed, meta[http-equiv], link').forEach(node => node.remove())
  const nodes = [copy, ...copy.querySelectorAll('*')]
  for (const node of nodes) {
    for (const attribute of [...node.attributes]) {
      const name = attribute.name.toLowerCase()
      const value = attribute.value.trim()
      if (name.startsWith('on') || name.startsWith('th:') || /^(javascript|data):/i.test(value)) {
        node.removeAttribute(attribute.name)
      } else if (['href', 'src', 'action', 'poster'].includes(name) && value && !value.startsWith('#') && !/^(?:https?:|mailto:|tel:|\/)/i.test(value)) {
        node.setAttribute(attribute.name, `/${value.replace(/^\.\//, '')}`)
      }
    }
  }
  return copy.innerHTML
}
