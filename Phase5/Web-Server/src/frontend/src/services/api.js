const ARTICLES_URL = '/api/articles'
const AUTH_URL = '/api/auth'
const USERS_URL = '/api/users/me'
const TOKEN_STORAGE_KEY = 'articleshare_token'

export function getToken() {
  return localStorage.getItem(TOKEN_STORAGE_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_STORAGE_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
}

function authHeaders() {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

async function handleResponse(response) {
  const json = await response.json()

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    if (json && (json.error || json.message)) {
      message = json.error || json.message
    }
    const error = new Error(message)
    error.status = response.status
    throw error
  }

  return json && Object.prototype.hasOwnProperty.call(json, 'data') ? json.data : json
}

export async function getArticles(searchQuery = '') {
  const url = searchQuery
    ? `${ARTICLES_URL}?q=${encodeURIComponent(searchQuery)}`
    : ARTICLES_URL
  const res = await fetch(url)
  return handleResponse(res)
}

export async function getArticleById(id) {
  if (!id) throw new Error('getArticleById: id is required')
  const res = await fetch(`${ARTICLES_URL}/${encodeURIComponent(id)}`)
  return handleResponse(res)
}

export async function createArticle(articleData) {
  if (!articleData) throw new Error('createArticle: articleData is required')
  const res = await fetch(ARTICLES_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(articleData),
  })
  return handleResponse(res)
}

export async function register(payload) {
  const res = await fetch(`${AUTH_URL}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return handleResponse(res)
}

export async function login(payload) {
  const res = await fetch(`${AUTH_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return handleResponse(res)
}

export async function getProfile() {
  const res = await fetch(USERS_URL, {
    headers: { ...authHeaders() },
  })
  return handleResponse(res)
}

export async function updateContact(payload) {
  const res = await fetch(`${USERS_URL}/contact`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(payload),
  })
  return handleResponse(res)
}

export async function changePassword(payload) {
  const res = await fetch(`${USERS_URL}/password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(payload),
  })
  return handleResponse(res)
}
