# 🎯 Point Checker - Веб-приложение для проверки точек

Современное веб-приложение для проверки попадания точек в заданные геометрические области с аутентификацией пользователей и интерактивным интерфейсом.

## ✨ Возможности

- 🔐 **Аутентификация** с JWT токенами и BCrypt хэшированием
- 📊 **Интерактивный график** с Canvas API и проверкой областей
- 📱 **Адаптивный дизайн** для всех устройств (desktop/tablet/mobile)
- 🚀 **Rate limiting** для защиты от слишком частых запросов
- ⚡ **Реактивный бэкенд** на Spring WebFlux

## 🛠️ Стек технологий

### Backend
- **Java 17** + **Spring Boot 3.5**
- **Spring WebFlux** (реактивное программирование)
- **Spring Security** + **JWT** (аутентификация)
- **R2DBC PostgreSQL** (реактивная БД)
- **Bucket4j** (rate limiting)

### Frontend
- **Vue.js 3** Composition API
- **Vite** (быстрая сборка)
- **Pinia** (управление состоянием)
- **Vue Router** (маршрутизация)
- **Axios** (HTTP клиент)
- **HTML5 Canvas** (интерактивный график)

## 🚀 Быстрый старт

### Требования
- Java 17+
- Node.js 20+
- PostgreSQL
- Redis

## 📡 API

### Аутентификация
```http
POST /api/auth/login
POST /api/auth/register
```

### Работа с точками
```http
POST /api/points/check    # Проверка точки
GET  /api/points/history  # История проверок
DEL  /api/points/history  # Очистка истории
```

## 🔧 Переменные окружения

### Основные переменные
| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `DATABASE_URL` | PostgreSQL URL | `r2dbc:postgresql://localhost:5432/studs` |
| `DATABASE_USERNAME` | Пользователь БД | `s467211` |
| `DATABASE_PASSWORD` | Пароль БД | *(требуется)* |
| `SERVER_PORT` | Порт сервера | `8308` |

### Настройки логирования и производительности
| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `LOG_LEVEL` | Уровень логов приложения | `DEBUG` (dev), `INFO` (prod) |
| `SPRING_LOG_LEVEL` | Уровень логов Spring | `WARN` |
| `RATE_LIMIT_ENABLED` | Включить rate limiting | `false` (dev), `true` (prod) |
| `RATE_LIMIT_REQUESTS` | Запросов в минуту | `60` (dev), `30` (prod) |
| `RATE_LIMIT_BURST` | Burst capacity | `100` (dev), `50` (prod) |
| `HEALTH_SHOW_DETAILS` | Детали health check | `when-authorized` (dev), `never` (prod) |

## 🚀 Деплой на Helios

```bash
# 1. Сборка
./build.sh

# 2. Загрузка на сервер
scp backend/target/backend-1.0.0.jar helios:~/web-lab-4/

# 3. Запуск
ssh helios './start-server.sh'
```
