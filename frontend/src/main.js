import {createApp} from "vue";
import App from "./App.vue";
import {createPinia} from "pinia";
import router from "./router/index.js";

console.log('🚀 Starting Vue application...')

try {
  const app = createApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(router)

  // Добавляем глобальный обработчик ошибок Vue
  app.config.errorHandler = (error, instance, info) => {
    console.error('🚨 Vue Global Error:', error)
    console.error('🚨 Error Info:', info)
    console.error('🚨 Component Instance:', instance)
    console.error('🚨 Component Name:', instance?.$?.type?.name || 'Unknown')
    console.error('🚨 Error Stack:', error.stack)
    // Не выбрасываем ошибку дальше, чтобы предотвратить перезагрузку страницы
    return false
  }

  // Добавляем обработчик необработанных обещаний
  window.addEventListener('unhandledrejection', event => {
    console.error('🚨 Unhandled Promise Rejection:', event.reason)
    console.error('🚨 Rejection Stack:', event.reason?.stack)
  })

  // Базовые обработчики для отладки (без модификации location)
  window.addEventListener('beforeunload', event => {
    console.log('🏁 Page beforeunload triggered')
  })

  // Добавляем обработчик ошибок JavaScript
  window.addEventListener('error', event => {
    console.error('🚨 JavaScript Error:', event.error)
    console.error('🚨 Error Message:', event.message)
    console.error('🚨 Error File:', event.filename, 'Line:', event.lineno)
    // Предотвращаем перезагрузку страницы при ошибках
    event.preventDefault()
    return false
  })

  // Предотвращаем необработанные rejections от перезагрузки страницы
  window.addEventListener('unhandledrejection', event => {
    console.error('🚨 Unhandled Promise Rejection:', event.reason)
    console.error('🚨 Rejection Stack:', event.reason?.stack)
    // Предотвращаем перезагрузку
    event.preventDefault()
  })

  console.log('✅ Vue app configured, mounting...')
  app.mount('#app')
  console.log('✅ Vue app mounted successfully')

  // Скрываем fallback контент после загрузки Vue.js
  const fallbackElement = document.getElementById('loading-fallback')
  if (fallbackElement) {
    console.log('🗑️ Hiding fallback content')
    fallbackElement.style.display = 'none'
  }
} catch (error) {
  console.error('❌ Error starting Vue application:', error)
  // Fallback: show basic error message
  const appElement = document.getElementById('app')
  if (appElement) {
    appElement.innerHTML = `
      <div style="padding: 20px; color: red; font-family: Arial, sans-serif; background: white; border-radius: 8px;">
        <h2>Ошибка загрузки приложения</h2>
        <p>Произошла ошибка при инициализации Vue приложения.</p>
        <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px;">${error.message}</pre>
        <p>Проверьте консоль браузера для подробностей.</p>
        <details>
          <summary>Показать stack trace</summary>
          <pre style="background: #f5f5f5; padding: 10px; border-radius: 4px; font-size: 12px;">${error.stack}</pre>
        </details>
      </div>
    `
  }
}