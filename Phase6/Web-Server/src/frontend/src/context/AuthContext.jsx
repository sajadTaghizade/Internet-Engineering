import React, { createContext, useContext, useEffect, useState, useCallback } from 'react'
import * as api from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let canceled = false

    async function loadProfile() {
      if (!api.getToken()) {
        setLoading(false)
        return
      }

      try {
        const profile = await api.getProfile()
        if (!canceled) setUser(profile)
      } catch (err) {
        if (!canceled) {
          api.clearToken()
          setUser(null)
        }
      } finally {
        if (!canceled) setLoading(false)
      }
    }

    loadProfile()
    return () => {
      canceled = true
    }
  }, [])

  const login = useCallback(async (username, password) => {
    const { token, user: loggedInUser } = await api.login({ username, password })
    api.setToken(token)
    setUser(loggedInUser)
    return loggedInUser
  }, [])

  const register = useCallback(async (payload) => {
    const { token, user: newUser } = await api.register(payload)
    api.setToken(token)
    setUser(newUser)
    return newUser
  }, [])

  const logout = useCallback(() => {
    api.clearToken()
    setUser(null)
  }, [])

  const refreshProfile = useCallback(async () => {
    const profile = await api.getProfile()
    setUser(profile)
    return profile
  }, [])

  const value = { user, loading, login, register, logout, refreshProfile }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
