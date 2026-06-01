import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createArticle, getArticles } from '../services/api'
import { toast } from 'react-toastify'

export default function AddArticlePage() {
  const navigate = useNavigate()
  const [existingArticles, setExistingArticles] = useState([])
  const [title, setTitle] = useState('')
  const [abstract, setAbstract] = useState('')
  const [body, setBody] = useState('')
  const [selectedReferenceIds, setSelectedReferenceIds] = useState([])
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    let canceled = false

    async function loadArticles() {
      try {
        const articles = await getArticles()
        if (!canceled) {
          setExistingArticles(Array.isArray(articles) ? articles : [])
        }
      } catch (err) {
        if (!canceled) {
          setExistingArticles([])
          toast.error(err.message || 'Failed to load existing articles')
        }
      }
    }

    loadArticles()

    return () => {
      canceled = true
    }
  }, [])

  const toggleReference = (articleId) => {
    setSelectedReferenceIds((currentSelected) => {
      if (currentSelected.includes(articleId)) {
        return currentSelected.filter((selectedId) => selectedId !== articleId)
      }

      return [...currentSelected, articleId]
    })
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    const payload = { title, abstract, body, references: selectedReferenceIds }

    try {
      setSubmitting(true)
      await createArticle(payload)
      toast.success('Article created')
      navigate('/')
    } catch (err) {
      console.error('Create article failed', err)
      toast.error(err.message || 'Failed to create article')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="article-form" onSubmit={onSubmit}>
      <div>
        <label>Title</label>
        <input value={title} onChange={(e) => setTitle(e.target.value)} required />
      </div>

      <div>
        <label>Abstract</label>
        <input value={abstract} onChange={(e) => setAbstract(e.target.value)} required />
      </div>

      <div>
        <label>Body</label>
        <textarea value={body} onChange={(e) => setBody(e.target.value)} required />
      </div>

      <div>
        <label>References from existing articles</label>
        <div className="checkbox-list">
          {existingArticles.length === 0 ? (
            <p className="checkbox-list__empty">No existing articles available yet.</p>
          ) : (
            existingArticles.map((article) => (
              <label key={article.id} className="checkbox-list__item">
                <input
                  type="checkbox"
                  checked={selectedReferenceIds.includes(article.id)}
                  onChange={() => toggleReference(article.id)}
                />
                <span>{article.title}</span>
              </label>
            ))
          )}
        </div>
      </div>

      <div>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Submitting...' : 'Create Article'}
        </button>
      </div>
    </form>
  )
}
