<script setup>
import {useAuthStore} from "../stores/authStore.js";
import {useRouter} from "vue-router";
import {reactive, ref, onMounted} from "vue";

const authStore = useAuthStore()
const router = useRouter()

const isRegisterMode = ref(false)
const isSubmitting = ref(false)

// Предотвращаем автоматическую отправку формы при загрузке
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.replace('/main')
  }
})

const form = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: ''
})

  const handleSubmit = async () => {
  // Предотвращаем повторную отправку
  if (isSubmitting.value) {
    console.log('🚫 Login already in progress, ignoring duplicate submit')
    return
  }

  isSubmitting.value = true

  try {
    // Очищаем все предыдущие ошибки перед новой попыткой
    authStore.clearError()

    console.log('🚀 Login attempt with:', { username: form.username, passwordLength: form.password.length })
    const success = await authStore.login(form.username, form.password)
    console.log('Login result:', success)
    if (success) {
      console.log('✅ Redirecting to /main using router.replace')
      router.replace('/main')
    } else {
      console.log('❌ Login failed, staying on login page')
    }
  } finally {
    isSubmitting.value = false
  }
}

const handleRegister = async () => {
  // Очищаем все предыдущие ошибки перед новой попыткой
  authStore.clearError()

  const success = await authStore.register(registerForm.username, registerForm.password)
  if (success) {
    isRegisterMode.value = false
    registerForm.username = ''
    registerForm.password = ''
  }
}

const switchToRegister = () => {
  isRegisterMode.value = true
  authStore.clearError()
  // Очищаем форму входа при переключении
  form.username = ''
  form.password = ''
}

const switchToLogin = () => {
  isRegisterMode.value = false
  authStore.clearError()
  // Очищаем форму регистрации при переключении
  registerForm.username = ''
  registerForm.password = ''
}
</script>

<template>
  <div class="login-form">
    <form @submit.prevent="handleSubmit" class="card">
      <h2>Вход в систему</h2>

      <div class="form-group">
        <label for="username" class="form-label">Логин:</label>
        <input
            id="username"
            v-model="form.username"
            type="text"
            required
            class="form-input"
            placeholder="Введите логин"
        >
      </div>

      <div class="form-group">
        <label for="password" class="form-label">Пароль:</label>
        <input
            id="password"
            v-model="form.password"
            type="password"
            required
            class="form-input"
            placeholder="Введите пароль"
        >
      </div>


      <div class="form-actions">
        <button
          type="submit"
          :disabled="authStore.loading || isSubmitting"
          class="btn btn-primary"
        >
          {{ authStore.loading || isSubmitting ? 'Вход...' : 'Войти' }}
        </button>

        <button
          type="button"
          @click="switchToRegister"
          class="btn btn-secondary"
        >
          Регистрация
        </button>
      </div>
    </form>

    <!-- Форма регистрации -->
    <form v-if="isRegisterMode" @submit.prevent="handleRegister" class="card">
      <h2>Регистрация</h2>

      <div class="form-group">
        <label for="reg-username" class="form-label">Логин:</label>
        <input
            id="reg-username"
            v-model="registerForm.username"
            type="text"
            required
            class="form-input"
            placeholder="Введите логин"
        >
      </div>

      <div class="form-group">
        <label for="reg-password" class="form-label">Пароль:</label>
        <input
            id="reg-password"
            v-model="registerForm.password"
            type="password"
            required
            class="form-input"
            placeholder="Введите пароль"
        >
      </div>

      <div class="form-actions">
        <button
          type="submit"
          :disabled="authStore.loading"
          class="btn btn-primary"
        >
          {{ authStore.loading ? 'Регистрация...' : 'Зарегистрироваться' }}
        </button>

        <button
          type="button"
          @click="switchToLogin"
          class="btn btn-secondary"
        >
          Назад к входу
        </button>
      </div>
    </form>

    <!-- Общее окно ошибок -->
    <div v-if="authStore.error" class="common-errors card">
      <h3>Ошибка:</h3>
      <div class="error-message">
        {{ authStore.error }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-form {
  max-width: 400px;
  margin: 0 auto;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.form-actions .btn {
  flex: 1;
}

.common-errors {
  margin-top: 20px;
  border: 2px solid #ef4444;
  background-color: rgba(239, 68, 68, 0.1);
}

.common-errors h3 {
  margin: 0 0 10px 0;
  color: #dc2626;
  font-size: 16px;
}
</style>