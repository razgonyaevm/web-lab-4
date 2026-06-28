<script setup>
import {useAuthStore} from "../stores/authStore.js";
import {useRouter} from "vue-router";

const props = defineProps({
  showAuthInfo: {
    type: Boolean,
    default: false
  }
})

const authStore = useAuthStore()
const router = useRouter()

const logout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="header">
    <div class="header-content">
      <div class="student-info">
        <h1>Лабораторная работа по веб-программированию №4</h1>
        <p>Разгоняев Максим Витальевич</p>
        <p>Группа P3231</p>
        <p>Вариант 4211</p>
      </div>
      <div v-if="showAuthInfo" class="auth-info">
        <p>Пользователь: {{ authStore.user?.username }}</p>
        <button @click="logout" class="btn btn-secondary">Выйти</button>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  padding: 20px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.08);
  margin-bottom: 20px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.student-info h1 {
  margin-bottom: 10px;
  color: #2c3e50;
}

.student-info p {
  margin: 5px 0;
  color: #666;
}

.auth-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}
</style>