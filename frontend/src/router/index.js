import { createRouter, createWebHistory } from 'vue-router'
import {useAuthStore} from "../stores/authStore.js";
import LoginView from "../views/LoginView.vue";
import MainView from "../views/MainView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true }
    },
    {
      path: '/main',
      name: 'main',
      component: MainView,
      meta: { requiresAuth: true }
    },
    // Catch-all route - перенаправляет неизвестные маршруты для обработки guards
    {
      path: '/:pathMatch(.*)*',
      redirect: (to) => {
        // Guards сами разберутся с перенаправлением
        console.log('🛤️ Catch-all triggered for:', to.path)
        return '/'
      }
    }
  ]
})

// Навигационные guards
router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    // Проверяем аутентификацию для каждого перехода
    authStore.checkAuth()

    // Ждем завершения reactive обновлений
    await new Promise(resolve => setTimeout(resolve, 10))

    // Логика навигации
    if (authStore.isAuthenticated) {
        // Залогиненный пользователь - перенаправляем на main, НЕ разрешаем /
        if (to.path !== '/main') {
            next('/main')
        } else {
            next()
        }
    } else {
        // Незалогиненный пользователь
        if (to.path === '/') {
            // Разрешаем доступ к главной странице (логин)
            next()
        } else {
            // Любые другие страницы перенаправляем на главную
            next('/')
        }
    }
})

export default router
