import axios from "axios"

// Backend URL - configured via environment variables
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
})

// Интерцептор для добавления JWT токена
apiClient.interceptors.request.use((config) => {
    const authData = localStorage.getItem('auth')
    console.log('Raw auth data from localStorage:', authData)

    const auth = JSON.parse(authData || '{}')
    console.log('Parsed auth object:', auth)

    console.log('🚀 Starting API request:', config.method?.toUpperCase(), config.url)

    // Добавляем заголовки для предотвращения кэширования и basic auth
    config.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    config.headers['Pragma'] = 'no-cache'
    config.headers['Expires'] = '0'
    config.headers['If-None-Match'] = '' // Prevent 304 responses
    config.headers['Cache-Control'] = config.headers['Cache-Control'] + ', no-transform' // Prevent transforms

    // Запрещаем использование credentials для предотвращения basic auth
    config.withCredentials = false

    // Добавляем timestamp и random для предотвращения кэширования
    const separator = config.url.includes('?') ? '&' : '?'
    config.url += separator + '_t=' + Date.now() + Math.random()

    if (auth.token) {
        config.headers['Authorization'] = `Bearer ${auth.token}`
        console.log('✅ API Request:', config.method?.toUpperCase(), config.url, '- Token added to headers')
        console.log('Full request config:', {
            url: config.url,
            method: config.method,
            headers: Object.keys(config.headers)
        })
    } else {
        console.log('❌ API Request:', config.method?.toUpperCase(), config.url, '- No token found in localStorage')
    }
    return config
})

// Интерцептор для обработки ответов
apiClient.interceptors.response.use(
    (response) => {
        // Убираем логирование успешных ответов для производительности
        // console.log('✅ API Response:', response.config.method?.toUpperCase(), response.config.url, '- Status:', response.status)
        return response
    },
    (error) => {
        console.log('❌ API Error:', error.config?.method?.toUpperCase(), error.config?.url, '- Status:', error.response?.status, '- Message:', error.message)

        if (error.response?.status === 401) {
            console.log('🚫 401 Unauthorized received!')
            console.log('🚫 Request URL:', error.config?.url)
            console.log('🚫 Request method:', error.config?.method)
            console.log('🚫 Response status:', error.response.status)
            console.log('🚫 Response data:', error.response.data)
            console.log('🚫 Stack trace:', new Error().stack)

            console.log('🚫 401 Unauthorized - clearing auth data')
            localStorage.removeItem('user')  // Исправлено: удаляем 'user' вместо 'auth'

            console.log('🔄 Redirecting to login page due to 401')
            window.location.replace('/')
            return Promise.reject(error)
        }

        // Обработка 500 ошибок - не очищаем auth данные, но логируем
        if (error.response?.status === 500) {
            console.log('🚫 500 Internal Server Error received!')
            console.log('🚫 Request URL:', error.config?.url)
            console.log('🚫 Request method:', error.config?.method)
            console.log('🚫 Response data:', error.response.data)
        }

        return Promise.reject(error)
    }
)

export const authService = {
    async login(username, password) {
        console.log('authService.login called with:', username, password ? '[password provided]' : '[no password]')
        try {
            const response = await apiClient.post('/api/auth/login', { username, password })
            console.log('authService.login - axios response.data:', response.data)
            console.log('authService.login - full axios response:', response)
            console.log('authService.login - response.data type:', typeof response.data)
            if (response.data) {
                console.log('authService.login - response.data keys:', Object.keys(response.data))
            }
            return response.data
        } catch (error) {
            console.log('Login API error:', error)
            console.log('Error response:', error.response)
            console.log('Error response status:', error.response?.status)
            console.log('Error response data:', error.response?.data)
            const errorData = error.response?.data
            console.log('Extracted errorData:', errorData)
            console.log('errorData.message:', errorData?.message)
            console.log('errorData.data:', errorData?.data)

            if (error.response?.status === 400 && errorData?.data && typeof errorData.data === 'object') {
                console.log('Validation errors detected, returning error object')
                const resultObject = {
                    success: false,
                    message: errorData.message || 'Validation failed',
                    fieldErrors: errorData.data
                }
                console.log('authService.login - about to return validation error object:', resultObject)
                // Возвращаем объект с ошибками вместо исключения
                return resultObject
            }
            // Для других ошибок выбрасываем исключение
            throw new Error(errorData?.message || 'Login failed')
        }
    },

    async register(username, password) {
        try {
            const response = await apiClient.post('/api/auth/register', { username, password })
            return response.data
        } catch (error) {
            console.log('Register API error:', error)
            console.log('Error response:', error.response)
            const errorData = error.response?.data
            console.log('Error data:', errorData)

            if (error.response?.status === 400 && errorData?.data && typeof errorData.data === 'object') {
                console.log('Validation errors detected, returning error object')
                const resultObject = {
                    success: false,
                    message: errorData.message || 'Validation failed',
                    fieldErrors: errorData.data
                }
                console.log('authService.login - about to return validation error object:', resultObject)
                // Возвращаем объект с ошибками вместо исключения
                return resultObject
            }
            // Для других ошибок выбрасываем исключение
            throw new Error(errorData?.message || 'Registration failed')
        }
    }
}

export const pointService = {
    async checkPoint(pointData) {
        console.log('🎯 Checking point:', pointData)
        try {
            const response = await apiClient.post('/api/points/check', pointData)
            console.log('✅ Point checked successfully:', response.data)
            return response.data
        } catch (error) {
            console.log('❌ Point check failed:', error)
            throw new Error(error.response?.data?.message || 'Point check failed')
        }
    },

    async getHistory(limit = 50, offset = 0) {
        console.log('📚 Fetching history with pagination...', { limit, offset }, '(CALLED FROM:', new Error().stack.split('\n')[2])
        try {
            const response = await apiClient.get('/api/points/history', {
                params: { limit, offset }
            })
            console.log('✅ History fetched successfully:', response.data)
            return response.data
        } catch (error) {
            console.log('❌ History fetch failed:', error)
            throw new Error(error.response?.data?.message || 'Failed to fetch history')
        }
    },

    async getHistoryCount() {
        console.log('🔢 Fetching history count...')
        try {
            const response = await apiClient.get('/api/points/history/count')
            console.log('✅ History count fetched successfully:', response.data)
            return response.data
        } catch (error) {
            console.log('❌ History count fetch failed:', error)
            throw new Error(error.response?.data?.message || 'Failed to fetch history count')
        }
    },

    async clearHistory() {
        console.log('🗑️ Clearing history... (CALLED FROM:', new Error().stack.split('\n')[2])
        try {
            const response = await apiClient.delete('/api/points/history')
            console.log('✅ History cleared successfully, response:', response)
            return response.data || { success: true }
        } catch (error) {
            console.log('❌ History clear failed:', error)
            console.log('Error response:', error.response)
            throw new Error(error.response?.data?.message || error.message || 'Failed to clear history')
        }
    }
}

export default apiClient