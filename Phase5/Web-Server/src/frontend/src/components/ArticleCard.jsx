import React from 'react'
import { Link } from 'react-router-dom'

export default function ArticleCard({ article }) {
  if (!article) return null

  const { id, title, abstract, citationCount } = article

  return (
    <article className="article-card">
      <h3 className="article-card__title">{title}</h3>
      <p className="article-card__abstract">{abstract}</p>
      <div className="article-card__meta">
        <span> Citations: {citationCount ?? 0}</span>
        <Link to={`/article/${id}`} className="article-card__readmore">
          Read More
        </Link>
      </div>
    </article>
  )
}
