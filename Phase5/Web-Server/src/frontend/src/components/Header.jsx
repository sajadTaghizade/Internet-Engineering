import React from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Header() {
  const { user } = useAuth()

  return (
    <header className="app-header">
      <div className="app-header__inner">
        <div className="brand">ArticleShare</div>
        <nav className="nav">
          <Link to="/" className="nav-link">Home</Link>
          {user ? (
            <>
              <Link to="/add-article" className="nav-link">Add Article</Link>
              <Link to="/profile" className="nav-link">Profile ({user.username})</Link>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">Login</Link>
              <Link to="/register" className="nav-link">Register</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}
