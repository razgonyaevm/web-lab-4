<script setup>

import {onMounted, ref, watch} from "vue";
import {pointService} from "../services/api.js";

const props = defineProps({
  currentRadius: {
    type: Number,
    default: 1
  },
  points: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['point-added'])

const canvas = ref(null)
const size = ref(400)
const ctx = ref(null)

// Tooltip состояние
const tooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  point: null
})

onMounted(() => {
  ctx.value = canvas.value.getContext('2d')
  drawGraph()
})

watch(() => props.currentRadius, () => {
  drawGraph()
})

watch(() => props.points, () => {
  drawGraph()
}, { immediate: true })

const drawGraph = () => {
  if (!ctx.value) {
    return
  }

  const canvasSize = size.value
  const center = canvasSize / 2
  const scale = (canvasSize / 2) / 6

  // Очистка canvas
  ctx.value.clearRect(0, 0, canvasSize, canvasSize)

  // Перерисовка фона
  ctx.value.fillStyle = '#f9f9f9'
  ctx.value.fillRect(0, 0, canvasSize, canvasSize)

  // Рисуем фон
  ctx.value.fillStyle = '#f9f9f9'
  ctx.value.fillRect(0, 0, canvasSize, canvasSize)

  // Рисуем оси координат
  ctx.value.strokeStyle = '#000'
  ctx.value.lineWidth = 2
  ctx.value.beginPath()

  // Ось X
  ctx.value.moveTo(0, center)
  ctx.value.lineTo(canvasSize, center)

  // Ось Y
  ctx.value.moveTo(center, 0)
  ctx.value.lineTo(center, canvasSize)
  ctx.value.stroke()

  // Рисуем засечки
  ctx.value.strokeStyle = '#666'
  ctx.value.lineWidth = 1
  ctx.value.beginPath()

  for (let i = -6; i <= 6; i++) {
    if (i === 0) continue

    // Засечки на оси X
    const xPos = center + i * scale
    ctx.value.moveTo(xPos, center - 5)
    ctx.value.lineTo(xPos, center + 5)

    // Засечки на оси Y
    const yPos = center - i * scale
    ctx.value.moveTo(center - 5, yPos)
    ctx.value.lineTo(center + 5, yPos)

    // Подписи
    ctx.value.fillStyle = '#000'
    ctx.value.font = '12px Arial'
    ctx.value.fillText(i.toString(), xPos - 5, center + 20)
    ctx.value.fillText(i.toString(), center + 10, yPos + 5)
  }
  ctx.value.stroke()

  // Рисуем область
  drawArea()

  // Рисуем точки
  drawPoints()
}

const drawArea = () => {
  if (!ctx.value || !props.currentRadius) return

  const canvasStyle = size.value
  const center = canvasStyle / 2
  const scale = (canvasStyle / 2) / 6
  const r = props.currentRadius

  ctx.value.fillStyle = 'rgba(100, 150, 255, 0.3)'
  ctx.value.strokeStyle = 'rgba(100, 150, 255, 0.7)'
  ctx.value.lineWidth = 2

  // Четверть круга в первой четверти с радиусом r/2
  ctx.value.beginPath()
  ctx.value.arc(center, center, (r/2) * scale, -Math.PI/2, 0)
  ctx.value.lineTo(center, center)
  ctx.value.closePath()
  ctx.value.fill()
  ctx.value.stroke()

  // Квадрат во второй четверти со стороной r
  ctx.value.beginPath()
  ctx.value.rect(center - r * scale, center, r * scale, -r * scale)
  ctx.value.fill()
  ctx.value.stroke()

  // Треугольник в четвертой четверти
  ctx.value.beginPath()
  ctx.value.moveTo(center, center)
  ctx.value.lineTo(center + r * scale, center)
  ctx.value.lineTo(center, center + r * scale)
  ctx.value.closePath()
  ctx.value.fill()
  ctx.value.stroke()
}

// Функция для определения, находится ли курсор над точкой
const getPointAtPosition = (mouseX, mouseY) => {
  const canvasSize = size.value
  const center = canvasSize / 2
  const scale = (canvasSize / 2) / 6

  // Ищем точку, над которой находится курсор (с небольшим радиусом попадания)
  for (let i = props.points.length - 1; i >= 0; i--) {
    const point = props.points[i]
    const pointX = center + point.x * scale
    const pointY = center - point.y * scale

    const distance = Math.sqrt((mouseX - pointX) ** 2 + (mouseY - pointY) ** 2)

    // Если курсор находится в радиусе 8 пикселей от центра точки
    if (distance <= 8) {
      return point
    }
  }

  return null
}

// Обработчик движения мыши
const handleMouseMove = (event) => {
  const rect = canvas.value.getBoundingClientRect()
  const mouseX = event.clientX - rect.left
  const mouseY = event.clientY - rect.top

  const point = getPointAtPosition(mouseX, mouseY)

  if (point) {
    tooltip.value.visible = true
    tooltip.value.x = event.clientX + 10 // Смещаем tooltip вправо от курсора
    tooltip.value.y = event.clientY + 10 // Смещаем tooltip вниз от курсора
    tooltip.value.point = point
  } else {
    tooltip.value.visible = false
  }
}

// Обработчик ухода мыши с canvas
const handleMouseLeave = () => {
  tooltip.value.visible = false
}

const drawPoints = () => {
  if (!ctx.value) return

  const canvasSize = size.value
  const center = canvasSize / 2
  const scale = (canvasSize / 2) / 6
  props.points.forEach((point) => {
    const x = center + point.x * scale
    const y = center - point.y * scale

    ctx.value.fillStyle = point.result ? '#4CAF50' : '#f44336'
    ctx.value.beginPath()
    ctx.value.arc(x, y, 5, 0, 2 * Math.PI)
    ctx.value.fill()
  })
}

// Метод для принудительной очистки графика
const clearGraph = () => {
  drawGraph()
}

// Экспортируем метод для внешнего вызова
defineExpose({
  clearGraph
})

// Доступные значения чекбоксов только для X
const xValues = [-3, -2, -1, 0, 1, 2, 3, 4, 5]

// Функция для поиска ближайшего значения из массива
const findNearestValue = (value, values) => {
  return values.reduce((prev, curr) => {
    return Math.abs(curr - value) < Math.abs(prev - value) ? curr : prev
  })
}

const handleCanvasClick = async (event) => {
  if (!props.currentRadius) return

  const canvasSize = size.value
  const center = canvasSize / 2
  const scale = (canvasSize / 2) / 6

  const rect = canvas.value.getBoundingClientRect()
  const x = event.clientX - rect.left
  const y = event.clientY - rect.top

  // Конвертируем координаты canvas в математические координаты
  const mathX = (x - center) / scale
  const mathY = (center - y) / scale

  // Ограничиваем значения в допустимых пределах
  const boundedX = Math.max(-3, Math.min(5, mathX))
  const boundedY = Math.max(-3, Math.min(3, mathY))

  // Округляем X до ближайших значений чекбоксов, Y оставляем как есть (округляем до 2 знаков после запятой)
  const snappedX = findNearestValue(boundedX, xValues)
  const snappedY = Math.round(boundedY * 100) / 100 // Округляем до 2 знаков

  console.log(`Canvas click: (${boundedX.toFixed(2)}, ${boundedY.toFixed(2)}) -> snapped to: (${snappedX}, ${snappedY})`)

  try {
    const pointData = {
      x: snappedX,
      y: snappedY,
      r: props.currentRadius
    }

    const result = await pointService.checkPoint(pointData)
    emit('point-added', result)
  } catch (error) {
    console.error('Error checking point from graph:', error)
  }
}
</script>

<template>
  <div class="graph-container card">
    <h2>Область на координатной плоскости</h2>
    <div class="graph-wrapper">
      <canvas
        ref="canvas"
        :width="size"
        :height="size"
        @click="handleCanvasClick"
        @mousemove="handleMouseMove"
        @mouseleave="handleMouseLeave"
      ></canvas>

      <!-- Tooltip для информации о точке -->
      <div
        v-if="tooltip.visible"
        class="point-tooltip"
        :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
      >
        <div class="tooltip-content">
          <div class="tooltip-row">
            <span class="tooltip-label">X:</span>
            <span class="tooltip-value">{{ tooltip.point.x }}</span>
          </div>
          <div class="tooltip-row">
            <span class="tooltip-label">Y:</span>
            <span class="tooltip-value">{{ tooltip.point.y }}</span>
          </div>
          <div class="tooltip-row">
            <span class="tooltip-label">Пользователь:</span>
            <span class="tooltip-value">{{ tooltip.point.username }}</span>
          </div>
        </div>
      </div>
    </div>
    <div class="graph-info">
      <p>Текущий радиус: {{ currentRadius }}</p>
      <p>Количество точек: {{ points.length }}</p>
    </div>
  </div>
</template>

<style scoped>
.graph-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

canvas {
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: crosshair;
}

.graph-info {
  text-align: center;
  color: #666;
}

.graph-info p {
  margin: 5px 0;
}

/* Стили для tooltip */
.point-tooltip {
  position: fixed;
  pointer-events: none;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
  max-width: 200px;
  white-space: nowrap;
}

.tooltip-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.tooltip-label {
  font-weight: 600;
  opacity: 0.8;
}

.tooltip-value {
  font-weight: 400;
}
</style>