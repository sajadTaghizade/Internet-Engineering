import React, { useEffect, useState } from 'react'
import { getArticles } from '../services/api'
import ArticleCard from '../components/ArticleCard'

export default function HomePage() {
  const [articles, setArticles] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)

  async function fetchArticles(q = '') {
    setLoading(true)
    try {
      const data = await getArticles(q)
      setArticles(Array.isArray(data) ? data : [])
    } catch (err) {
      setArticles([])
      console.error('Failed to fetch articles', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchArticles()
  }, [])

  return (
    <div>
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search articles..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button onClick={() => fetchArticles(query)}>Search</button>
      </div>

      {loading && <div>Loading articles...</div>}

      {!loading && articles.length === 0 && (
        <div>No articles found.</div>
      )}

      <div className="article-list">
        {articles.map((a) => (
          <ArticleCard key={a.id} article={a} />
        ))}
      </div>
    </div>
  )
}
