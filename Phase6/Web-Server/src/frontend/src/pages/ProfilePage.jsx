import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'
import * as api from '../services/api'

export default function ProfilePage() {
  const { user, logout, refreshProfile } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState(user?.email || '')
  const [phone, setPhone] = useState(user?.phone || '')
  const [savingContact, setSavingContact] = useState(false)

  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [savingPassword, setSavingPassword] = useState(false)

  if (!user) return null

  const onSaveContact = async (e) => {
    e.preventDefault()
    try {
      setSavingContact(true)
      await api.updateContact({ email, phone })
      await refreshProfile()
      toast.success('Contact info updated')
    } catch (err) {
      toast.error(err.message || 'Failed to update contact info')
    } finally {
      setSavingContact(false)
    }
  }

  const onChangePassword = async (e) => {
    e.preventDefault()
    try {
      setSavingPassword(true)
      await api.changePassword({ currentPassword, newPassword })
      setCurrentPassword('')
      setNewPassword('')
      toast.success('Password changed successfully')
    } catch (err) {
      toast.error(err.message || 'Failed to change password')
    } finally {
      setSavingPassword(false)
    }
  }

  const onLogout = () => {
    // Navigate away from this protected route *before* clearing the user so
    // ProtectedRoute never renders with user=null while still mounted on
    // /profile -- otherwise it redirects to /login with a "from: /profile"
    // state that then hijacks the next login's redirect.
    navigate('/')
    logout()
  }

  return (
    <div className="article-detail">
      <h1>Profile</h1>
      <p><strong>Username:</strong> {user.username}</p>

      <form className="article-form" onSubmit={onSaveContact}>
        <h2>Edit Contact Info</h2>
        <div>
          <label>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div>
          <label>Phone</label>
          <input value={phone} onChange={(e) => setPhone(e.target.value)} />
        </div>
        <div>
          <button type="submit" disabled={savingContact}>
            {savingContact ? 'Saving...' : 'Save Contact Info'}
          </button>
        </div>
      </form>

      <form className="article-form" onSubmit={onChangePassword}>
        <h2>Change Password</h2>
        <div>
          <label>Current Password</label>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            required
          />
        </div>
        <div>
          <label>New Password</label>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
          />
        </div>
        <div>
          <button type="submit" disabled={savingPassword}>
            {savingPassword ? 'Saving...' : 'Change Password'}
          </button>
        </div>
      </form>

      <div>
        <button className="btn-primary" onClick={onLogout}>Logout</button>
      </div>

      <section>
        <h2>My Articles</h2>
        {(!user.articles || user.articles.length === 0) && <p>You haven't published any articles yet.</p>}
        <ul className="reference-list">
          {(user.articles || []).map((article) => (
            <li key={article.id} className="reference-item">
              <Link to={`/article/${article.id}`}>
                <span className="reference-item__title">{article.title}</span>
              </Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
