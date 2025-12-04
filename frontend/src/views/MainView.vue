<script setup>
import {onMounted, ref, computed, nextTick} from "vue";
import {pointService} from "../services/api.js";
import Header from "../components/Header.vue";
import PointForm from "../components/PointForm.vue";
import ResultsTable from "../components/ResultsTable.vue";
import CoordinateGraph from "../components/CoordinateGraph.vue";

const resultTable = ref(null)
const currentRadius = ref(1)
const points = ref([])
const selectedRadii = ref([])
const allHistoryPoints = ref([])
const graphKey = ref(0) // Для принудительного перерендеринга графика
const coordinateGraph = ref(null) // Ссылка на компонент графика

// Временно отключаем computed filteredPoints полностью для диагностики
// const filteredPoints = computed(() => {
//   console.log('🏠 Computed filteredPoints DISABLED: returning all points')
//   return allHistoryPoints.value
// })
// ВРЕМЕННО ОТКЛЮЧАЕМ computed filteredPoints для диагностики проблемы с логаутом
// const filteredPoints = computed(() => {
//   console.log('🏠 Computed filteredPoints: radius=', currentRadius.value, 'allPoints=', allHistoryPoints.value.length)
//   const result = allHistoryPoints.value.filter(point => point.r === currentRadius.value)
//   console.log('🏠 Filtered result:', result.length, 'points')
//   return result
// })

// Фильтруем точки по выбранному радиусу
const filteredPoints = computed(() => {
  console.log('🏠 Computed filteredPoints: radius=', currentRadius.value, 'allPoints=', allHistoryPoints.value.length)
  const filtered = allHistoryPoints.value.filter(point => point.r === currentRadius.value)
  console.log('🏠 Filtered result:', filtered.length, 'points for radius', currentRadius.value)
  return filtered
})

const handlePointChecked = (results) => {
  // results может быть массивом или одним объектом
  const resultsArray = Array.isArray(results) ? results : [results]

  // Добавляем все результаты в начало массива точек для таблицы
  points.value = [...resultsArray, ...points.value]

  // Также добавляем в историю для графика
  allHistoryPoints.value = [...resultsArray, ...allHistoryPoints.value]

  // Устанавливаем currentRadius как максимальный из положительных радиусов
  if (resultsArray.length > 0) {
    const positiveRadii = resultsArray.filter(r => r.r > 0).map(r => r.r)
    if (positiveRadii.length > 0) {
      currentRadius.value = Math.max(...positiveRadii)
    }
  }

  resultTable.value?.refreshResults()
}

const handlePointAdded = (point) => {
  points.value = [point, ...points.value]
  allHistoryPoints.value = [point, ...allHistoryPoints.value]
  // Обновляем currentRadius на радиус добавленной точки
  currentRadius.value = point.r
  resultTable.value?.refreshResults()
}

const handleRadiusChanged = (maxRadius) => {
  currentRadius.value = maxRadius
}

const handleDataUpdated = async () => {
  try {
    await loadHistory()
  } catch (error) {
    console.error('Ошибка при обновлении данных:', error)
  }
}

const handleHistoryClearedEvent = () => {
  console.log('🎯🎯🎯 EVENT RECEIVED: history-cleared from ResultsTable! 🎯🎯🎯')
  handleHistoryCleared()
}

const handleHistoryCleared = () => {
  console.log('🚨🚨🚨 handleHistoryCleared: CALLED DIRECTLY FROM RESULTS TABLE! 🚨🚨🚨')
  console.log('🏠 handleHistoryCleared: Starting, current points:', points.value.length, 'history:', allHistoryPoints.value.length)

  // Очищаем массивы путем присваивания новых пустых массивов
  allHistoryPoints.value = []
  points.value = []

  console.log('🏠 handleHistoryCleared: Arrays cleared by assignment, points:', points.value.length, 'history:', allHistoryPoints.value.length)

  // Обновляем key для принудительного перерендеринга графика
  graphKey.value++

  console.log('🏠 handleHistoryCleared: Graph key updated to:', graphKey.value)

  // ПРЯМАЯ перерисовка графика - гарантированное обновление
  console.log('🏠 handleHistoryCleared: Direct graph redraw - START')

  // Ждем следующего тика Vue для гарантии обновления реактивности
  nextTick(() => {
    console.log('🏠 handleHistoryCleared: nextTick - starting direct redraw')

    // Прямой доступ к canvas через ref
    if (coordinateGraph.value) {
      console.log('🏠 handleHistoryCleared: coordinateGraph ref exists')

      // Получаем canvas element
      const canvasElement = coordinateGraph.value.$el?.querySelector('canvas')
      if (canvasElement) {
        console.log('🏠 handleHistoryCleared: canvas element found')

        // Прямое получение контекста и очистка
        const ctx = canvasElement.getContext('2d')
        if (ctx) {
          console.log('🏠 handleHistoryCleared: clearing canvas directly')
          ctx.clearRect(0, 0, canvasElement.width, canvasElement.height)

          // Перерисовка фона
          ctx.fillStyle = '#f9f9f9'
          ctx.fillRect(0, 0, canvasElement.width, canvasElement.height)

          // Перерисовка осей и сетки
          const center = canvasElement.width / 2
          const scale = (canvasElement.width / 2) / 6

          ctx.strokeStyle = '#000'
          ctx.lineWidth = 2
          ctx.beginPath()
          ctx.moveTo(0, center)
          ctx.lineTo(canvasElement.width, center)
          ctx.moveTo(center, 0)
          ctx.lineTo(center, canvasElement.height)
          ctx.stroke()

          console.log('🏠 handleHistoryCleared: axes redrawn')
        } else {
          console.log('🏠 handleHistoryCleared: cannot get canvas context')
        }
      } else {
        console.log('🏠 handleHistoryCleared: canvas element not found')
      }
    } else {
      console.log('🏠 handleHistoryCleared: coordinateGraph ref is null')
    }

    // Также вызываем exposed метод если он есть
    if (coordinateGraph.value && coordinateGraph.value.clearGraph) {
      console.log('🏠 handleHistoryCleared: calling exposed clearGraph method')
      coordinateGraph.value.clearGraph()
    }
  })

  // Используем nextTick для гарантии реактивного обновления
  nextTick(() => {
    console.log('🏠 handleHistoryCleared: nextTick executed - final state:')
    console.log('🏠 - points:', points.value.length, 'history:', allHistoryPoints.value.length, 'graphKey:', graphKey.value)
  })
}


const loadHistory = async () => {
  try {
    const history = await pointService.getHistory()
    allHistoryPoints.value = Array.isArray(history) ? history : []
    points.value = allHistoryPoints.value
  } catch (error) {
    console.error('Ошибка при загрузке истории:', error.message)
    // Если ошибка 401, пользователь не авторизован
    if (error.response?.status === 401) {
      // Обработка ошибки аутентификации
    }
  }
}

// Очищаем все данные (вызывается из ResultsTable)
const clearAllData = () => {
  console.log('🏠 MainView: Clearing all local data with nextTick')
  console.log('🏠 Before clearing - allHistoryPoints:', allHistoryPoints.value.length, 'points:', points.value.length)

  // Используем nextTick для безопасного обновления reactive массивов
  nextTick(() => {
    allHistoryPoints.value = []
    points.value = []
    console.log('🏠 After clearing - allHistoryPoints:', allHistoryPoints.value.length, 'points:', points.value.length)
    console.log('🏠 Arrays cleared successfully')
  })
}

// Экспортируем методы для вызова из других компонентов
defineExpose({
  clearAllData,
  loadHistory
})

// Загружаем историю при монтировании компонента
onMounted(() => {
  console.log('🏠 MainView onMounted - starting loadHistory')
  loadHistory()
})
</script>

<template>
  <div class="main-view">
    <Header show-auth-info/>
    <div class="container">
      <div class="main-layout">
        <div class="left-panel">
          <PointForm @point-checked="handlePointChecked" @radius-changed="handleRadiusChanged"/>
          <ResultsTable ref="resultTable" @data-updated="handleDataUpdated"/>
        </div>
        <div class="right-panel">
          <CoordinateGraph
            ref="coordinateGraph"
            :key="'graph-' + graphKey"
            :current-radius="currentRadius"
            :points="filteredPoints"
            @point-added="handlePointAdded"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 0;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  display: flex;
  justify-content: center;
}
</style>