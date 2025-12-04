<script setup>

import {onMounted, ref, watch, nextTick, computed} from "vue";
import {pointService} from "../services/api.js";
import {useAuthStore} from "../stores/authStore.js";

const emit = defineEmits(['data-updated', 'history-cleared'])

const authStore = useAuthStore()

const results = ref([])
const loading = ref(false)
const error = ref('')
const totalCount = ref(0)
const currentPage = ref(0)
const pageSize = ref(50)

const loadResults = async (page = 0) => {
  console.log('🔄 loadResults called (CALLED FROM:', new Error().stack.split('\n')[2])
  console.log('🔄 authStore.isAuthenticated:', authStore.isAuthenticated)
  console.log('🔄 Current token:', authStore.token ? 'present' : 'missing')

  // Не загружаем, если пользователь не аутентифицирован
  if (!authStore.isAuthenticated) {
    console.log('⏳ Skipping history load - user not authenticated')
    return
  }

  console.log('🔄 Loading results for authenticated user, page:', page)

  loading.value = true
  error.value = ''
  try {
    // Загружаем данные и количество параллельно
    const [history, count] = await Promise.all([
      pointService.getHistory(pageSize.value, page * pageSize.value),
      pointService.getHistoryCount()
    ])

    console.log('✅ History loaded successfully:', history)
    console.log('✅ Total count:', count)

    results.value = Array.isArray(history) ? history : []
    totalCount.value = count || 0
    currentPage.value = page
  } catch (err) {
    console.log('❌ Error loading history:', err)
    console.log('Error details:', {
      message: err.message,
      status: err.response?.status,
      data: err.response?.data
    })

    error.value = `❌ Произошла ошибка при загрузке данных - ${err.message || 'Неизвестная ошибка'}. Попробуйте обновить страницу.`

    results.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

const refreshResults = async () => {
  console.log('🔄 refreshResults called - EMITTING data-updated')
  await loadResults()
  // Сообщаем MainView об обновлении данных
  console.log('📤 EMITTING data-updated event')
  emit('data-updated')
}

const clearHistory = async () => {
  try {
    // Запрашиваем подтверждение пользователя
    const confirmed = window.confirm('Вы уверены, что хотите очистить всю историю проверок? Это действие нельзя отменить.')
    if (!confirmed) {
      return
    }

    loading.value = true
    error.value = ''

    // Очищаем данные на сервере
    await pointService.clearHistory()

    // Перезагружаем данные всех пользователей (включая текущего, у которого теперь 0 записей)
    await loadResults()

    // Уведомляем родительский компонент об обновлении данных
    emit('data-updated')

    loading.value = false

  } catch (err) {
    console.error('Ошибка при очистке истории:', err.message)

    error.value = `❌ Не удалось очистить историю - ${err.message || 'Неизвестная ошибка'}`
    loading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return
  const date = new Date(dateTime)
  return date.toLocaleString('ru-RU')
}

// Pagination functions
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    loadResults(page)
  }
}

const goToFirstPage = () => goToPage(0)
const goToLastPage = () => goToPage(totalPages.value - 1)
const goToPrevPage = () => goToPage(currentPage.value - 1)
const goToNextPage = () => goToPage(currentPage.value + 1)

defineExpose({
  refreshResults
})

// Загружаем историю только при успешной аутентификации
// Не загружаем автоматически при монтировании

// Загружаем результаты при монтировании компонента
onMounted(() => {
  console.log('📊 ResultsTable mounted, loading results...')
  if (authStore.isAuthenticated) {
    loadResults()
  }
})

console.log('👀 Auth watcher temporarily disabled for logout diagnosis')
</script>

<template>
  <div class="results-table card">
    <h2>История проверок</h2>

    <div v-if="loading" class="loading">Загрузка...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    <div v-else-if="results.length === 0" class="no-results">Нет результатов проверок</div>

    <div v-else class="table-container">
      <table class="results-table">
        <thead>
        <tr>
          <th>Пользователь</th>
          <th>X</th>
          <th>Y</th>
          <th>R</th>
          <th>Результат</th>
          <th>Время проверки</th>
          <th>Время выполнения (мс)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="result in results" :key="result.id" :class="{ 'hit': result.result, 'miss': !result.result }">
          <td>{{ result.username }}</td>
          <td>{{ result.x }}</td>
          <td>{{ result.y }}</td>
          <td>{{ result.r }}</td>
          <td>
              <span class="result-badge" :class="result.result ? 'hit' : 'miss'">
                {{ result.result ? 'Попадание' : 'Промах' }}
              </span>
          </td>
          <td>{{ formatDateTime(result.checkTime) }}</td>
          <td>{{ result.executionTime }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalCount > pageSize" class="pagination">
      <div class="pagination-info">
        Показано {{ (currentPage * pageSize) + 1 }}-{{ Math.min((currentPage + 1) * pageSize, totalCount) }}
        из {{ totalCount }} результатов
      </div>

      <div class="pagination-controls">
        <button @click="goToFirstPage" :disabled="currentPage === 0 || loading" class="btn btn-secondary">
          «« Первая
        </button>
        <button @click="goToPrevPage" :disabled="currentPage === 0 || loading" class="btn btn-secondary">
          « Предыдущая
        </button>

        <span class="page-info">
          Страница {{ currentPage + 1 }} из {{ totalPages }}
        </span>

        <button @click="goToNextPage" :disabled="currentPage >= totalPages - 1 || loading" class="btn btn-secondary">
          Следующая »
        </button>
        <button @click="goToLastPage" :disabled="currentPage >= totalPages - 1 || loading" class="btn btn-secondary">
          Последняя »»
        </button>
      </div>
    </div>

    <div class="table-actions">
      <button @click="refreshResults" class="btn btn-primary" :disabled="loading">
        Обновить
      </button>
      <button @click.prevent="clearHistory" type="button" class="btn btn-danger" :disabled="loading">
        Очистить историю
      </button>
    </div>
  </div>
</template>

<style scoped>
.table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.results-table th,
.results-table td {
  padding: 10px;
  text-align: center;
  border: 1px solid #ddd;
}

.results-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

.results-table tr.hit {
  background-color: #e8f5e8;
}

.results-table tr.miss {
  background-color: #ffebee;
}

.result-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.result-badge.hit {
  background-color: #4CAF50;
  color: white;
}

.result-badge.miss {
  background-color: #f44336;
  color: white;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.no-results {
  text-align: center;
  padding: 20px;
  color: #666;
  font-style: italic;
}

.error-message {
  text-align: center;
  padding: 15px 20px;
  margin: 15px 0;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.4;
  border: 1px solid;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
}

.error-message {
  color: #d32f2f;
  border-color: #ffcdd2;
  background-color: #ffebee;
}

.error-message:before {
  content: "⚠️ ";
  font-size: 16px;
}

.table-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-danger {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-danger:hover {
  background-color: #d32f2f;
}

.btn-danger:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 5px;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-info {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin: 0 10px;
  white-space: nowrap;
}

.pagination .btn {
  padding: 6px 12px;
  font-size: 12px;
  min-width: auto;
}

.pagination .btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>