<script setup>

import {computed, reactive, ref, watch} from "vue";
import {pointService} from "../services/api.js";

const emit = defineEmits(['point-checked', 'radius-changed'])

const loading = ref(false)

const xValues = [-3, -2, -1, 0, 1, 2, 3, 4, 5]
const rValues = [-3, -2, -1, 0, 1, 2, 3, 4, 5]

const form = reactive({
  x: [],
  y: '',
  r: []
})

const errors = reactive({
  x: '',
  y: '',
  r: ''
})

const isFormValid = computed(() => {
  const hasPositiveRadius = form.r.some(r => r > 0)
  return form.x.length > 0 &&
      form.y !== '' &&
      !errors.y &&
      form.r.length > 0 &&
      hasPositiveRadius &&
      parseFloat(form.y) >= -3 &&
      parseFloat(form.y) <= 3
})

const validateY = () => {
  // Сначала фильтруем ввод
  form.y = filterYInput(form.y)

  const yValue = parseFloat(form.y)
  if (isNaN(yValue)) {
    errors.y = 'Y должен быть числом'
  } else if (yValue < -3 || yValue > 3) {
    errors.y = 'Y должен быть в диапазоне от -3 до 3'
  } else {
    errors.y = ''
  }
}

// Функция для фильтрации ввода Y
const filterYInput = (value) => {
  // Заменяем запятую на точку
  let filtered = value.replace(/,/g, '.')

  // Разрешаем только цифры, минус в начале, и одну точку
  const parts = filtered.split('.')
  if (parts.length > 2) {
    // Если больше одной точки, оставляем только первую часть и первую точку
    filtered = parts[0] + '.' + parts.slice(1).join('')
  }

  // Убираем все символы кроме цифр, минуса и точки
  filtered = filtered.replace(/[^0-9.-]/g, '')

  // Убеждаемся, что минус только в начале
  const minusCount = (filtered.match(/-/g) || []).length
  if (minusCount > 1) {
    // Если больше одного минуса, оставляем только первый
    const firstMinusIndex = filtered.indexOf('-')
    filtered = filtered.substring(0, firstMinusIndex + 1) + filtered.substring(firstMinusIndex + 1).replace(/-/g, '')
  }

  // Если минус не в начале, убираем его
  if (filtered.includes('-') && !filtered.startsWith('-')) {
    filtered = filtered.replace(/-/g, '')
  }

  return filtered
}

const checkPoint = async () => {
  if (!isFormValid.value) return

  loading.value = true
  try {
    const y = parseFloat(form.y)
    const results = []

    // Отправляем только положительные радиусы
    const positiveRadii = form.r.filter(r => r > 0)

    // Отправляем все комбинации X и положительных R
    for (const x of form.x) {
      for (const r of positiveRadii) {
        const pointData = {
          x: x,
          y: y,
          r: r
        }

        const result = await pointService.checkPoint(pointData)
        results.push(result)
      }
    }

    emit('point-checked', results)
  } catch (error) {
    console.error('Error checking points:', error)
  } finally {
    loading.value = false
  }
}

const clearForm = () => {
  form.x = []
  form.y = ''
  form.r = []
  errors.x = ''
  errors.y = ''
  errors.r = ''
}

// Отслеживаем изменения радиусов и передаем максимальный положительный радиус
watch(() => form.r, (newRadii) => {
  const positiveRadii = newRadii.filter(r => r > 0)
  if (positiveRadii.length > 0) {
    const maxRadius = Math.max(...positiveRadii)
    emit('radius-changed', maxRadius)
  } else {
    emit('radius-changed', 1) // значение по умолчанию
  }
}, { deep: true })

</script>

<template>
  <div class="point-form card">
    <h2>Проверка точки</h2>

    <form @submit.prevent="checkPoint" class="form-grid">
      <!-- Координата X (Checkbox -->
      <div class="form-group">
        <label class="form-label">Координата X:</label>
        <div class="checkbox-group">
          <label v-for="xValue in xValues" :key="xValue" class="checkbox-label">
            <input
                type="checkbox"
                :value="xValue"
                v-model="form.x"
                class="checkbox-input"
            >
            {{ xValue }}
          </label>
        </div>
        <div v-if="errors.x" class="error-text">{{ errors.x }}</div>
      </div>

      <!-- Координата Y (Text input) -->
      <div class="form-group">
        <label for="y" class="form-label">Координата Y:</label>
        <input
            id="y"
            v-model="form.y"
            type="text"
            required
            class="form-input"
            placeholder="Введите Y от -3 до 3"
            @input="validateY"
        >
        <div v-if="errors.y" class="error-text">{{ errors.y }}</div>
      </div>

      <!-- Радиус R (Checkbox) -->
      <div class="form-group">
        <label class="form-label">Радиус R:</label>
        <div class="checkbox-group">
          <label v-for="rValue in rValues" :key="rValue" class="checkbox-label">
            <input
                type="checkbox"
                :value="rValue"
                v-model="form.r"
                class="checkbox-input"
            >
            {{ rValue }}
          </label>
        </div>
        <div v-if="errors.r" class="error-text">{{ errors.r }}</div>
      </div>

      <div class="form-actions">
        <button
          type="submit"
          :disabled="!isFormValid || loading"
          class="btn btn-primary"
        >
          {{ loading ? 'Проверка...' : 'Проверить' }}
        </button>
        <button
          type="button"
          @click="clearForm"
          class="btn btn-secondary"
        >
          Очистить
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

@media (min-width: 673px) {
  .form-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.checkbox-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 10px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.checkbox-input {
  margin: 0;
}

.error-text {
  color: #f44336;
  font-size: 14px;
  margin-top: 5px;
}

.form-actions {
  grid-column: 1 / -1;
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.form-actions .btn {
  flex: 1;
}

</style>