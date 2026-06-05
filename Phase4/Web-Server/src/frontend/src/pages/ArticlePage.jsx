import React, { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getArticleById } from '../services/api'

export default function ArticlePage() {
  const { id } = useParams()
  const [article, setArticle] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!id) {
      setError('No article id provided')
      setLoading(false)
      return
    }

    let canceled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const data = await getArticleById(id)
        if (!canceled) setArticle(data)
      } catch (err) {
        if (!canceled) setError(err.message || 'Failed to load article')
      } finally {
        if (!canceled) setLoading(false)
      }
    }

    load()
    return () => {
      canceled = true
    }
  }, [id])

  if (loading) return <div>Loading article...</div>
  if (error) return <div className="error">{error}</div>
  if (!article) return <div>No article data available.</div>

  const { title, abstract, body, references, referencedArticles } = article

  const referencesToDisplay = Array.isArray(referencedArticles) && referencedArticles.length > 0
    ? referencedArticles
    : Array.isArray(references)
      ? references
      : []

  return (
    <div className="article-detail">
      <h1 className="article-detail__title">{title}</h1>
      {abstract && <p className="article-detail__abstract">{abstract}</p>}
      {body && (
        <section className="article-detail__body">
          <h2>Article</h2>
          <p>{body}</p>
        </section>
      )}

      {referencesToDisplay.length > 0 && (
        <section className="article-detail__refs">
          <h3>Related Articles</h3>
          <ul className="reference-list">
            {referencesToDisplay.map((reference, idx) => {
              const referenceId = typeof reference === 'object' ? reference.id : reference
              const referenceTitle = typeof reference === 'object' ? reference.title : null
              const referenceLabel = referenceTitle ? referenceTitle : `Article #${referenceId}`

              return (
                <li key={referenceId ?? idx} className="reference-item">
                  <Link to={`/article/${referenceId}`}>
                    <span className="reference-item__title">{referenceLabel}</span>
                    <span className="reference-item__id">#{referenceId}</span>
                  </Link>
                </li>
              )
            })}
          </ul>
        </section>
      )}

    </div>
  )
}
