import React from 'react'
import { Link } from 'react-router-dom'

export default function Header() {
  return (
    <header className="app-header">
      <div className="app-header__inner">
        <div className="brand">ArticleShare</div>
        <nav className="nav">
          <Link to="/" className="nav-link">Home</Link>
          <Link to="/add-article" className="nav-link">Add Article</Link>
        </nav>
      </div>
    </header>
  )
}
