import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { useAuth } from '../context/AuthContext'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()
    try {
      setSubmitting(true)
      await register({ username, password, email, phone })
      toast.success('Registered successfully')
      navigate('/', { replace: true })
    } catch (err) {
      toast.error(err.message || 'Registration failed')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form className="article-form" onSubmit={onSubmit}>
      <h1>Register</h1>
      <div>
        <label>Username</label>
        <input value={username} onChange={(e) => setUsername(e.target.value)} required />
      </div>

      <div>
        <label>Password</label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <small>At least 6 characters, with lowercase, uppercase, and a number.</small>
      </div>

      <div>
        <label>Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      </div>

      <div>
        <label>Phone</label>
        <input value={phone} onChange={(e) => setPhone(e.target.value)} />
        <small>At least one of email or phone is required.</small>
      </div>

      <div>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Registering...' : 'Register'}
        </button>
      </div>
    </form>
  )
}
