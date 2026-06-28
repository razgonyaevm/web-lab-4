<script setup>
import {useAuthStore} from "./stores/authStore.js";

const authStore = useAuthStore()

console.log('🏠 App.vue loaded successfully - no onMounted checkAuth to avoid conflicts with router guard')
</script>

<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style scoped>
@import "@/styles/responsive.css";

#app {
  min-height: 100vh;
}
</style>
