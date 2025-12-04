import {defineStore} from "pinia";
import {ref} from "vue";
import {authService} from "@/services/api.js";

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(null)
    const isAuthenticated = ref(false)
    const loading = ref(false)
    const error = ref(null)

    const clearError = () => {
        error.value = null
    }

    const login = async (username, password) => {
        loading.value = true
        error.value = null
        try {
            const response = await authService.login(username, password)
            if (response.success && response.data?.token) {
                const authData = response.data
                token.value = authData.token
                user.value = { username: authData.username }
                isAuthenticated.value = true
                localStorage.setItem('auth', JSON.stringify({
                    token: authData.token,
                    user: { username: authData.username }
                }))
                return true
            }
            return false
        } catch (err) {
            error.value = err.message
            return false
        } finally {
            loading.value = false
        }
    }

    const register = async (username, password) => {
        loading.value = true
        error.value = null
        try {
            const response = await authService.register(username, password)
            return response.success
        } catch (err) {
            error.value = err.message
            return false
        } finally {
            loading.value = false
        }
    }

    const logout = () => {
        token.value = null
        user.value = null
        isAuthenticated.value = false
        localStorage.removeItem('auth')
    }

    const checkAuth = () => {
        const savedAuth = localStorage.getItem('auth')
        if (savedAuth) {
            try {
                const authData = JSON.parse(savedAuth)
                if (authData.token && authData.user) {
                    token.value = authData.token
                    user.value = authData.user
                    isAuthenticated.value = true
                } else {
                    clearAuthData()
                }
            } catch (e) {
                clearAuthData()
            }
        } else {
            clearAuthData()
        }
    }

    const clearAuthData = () => {
        token.value = null
        user.value = null
        isAuthenticated.value = false
        console.log('🔍 Auth data cleared')
    }

    return {
        user,
        token,
        isAuthenticated,
        loading,
        error,
        login,
        register,
        logout,
        checkAuth,
        clearAuthData,
        clearError
    }
})