const BASE_URL = '/api/articles'

async function handleResponse(response) {
  const json = await response.json()

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`
    if (json && (json.error || json.message)) {
      message = json.error || json.message
    }
    throw new Error(message)
  }

  return json && Object.prototype.hasOwnProperty.call(json, 'data') ? json.data : json
}

export async function getArticles(searchQuery = '') {
  const url = searchQuery
    ? `${BASE_URL}?q=${encodeURIComponent(searchQuery)}`
    : BASE_URL
  try {
    const res = await fetch(url)
    return await handleResponse(res)
  } catch (err) {
    throw err
  }
}

export async function getArticleById(id) {
  if (!id) throw new Error('getArticleById: id is required')
  try {
    const res = await fetch(`${BASE_URL}/${encodeURIComponent(id)}`)
    return await handleResponse(res)
  } catch (err) {
    throw err
  }
}

export async function createArticle(articleData) {
  if (!articleData) throw new Error('createArticle: articleData is required')
  try {
    const res = await fetch(BASE_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(articleData),
    })
    return await handleResponse(res)
  } catch (err) {
    throw err
  }
}
