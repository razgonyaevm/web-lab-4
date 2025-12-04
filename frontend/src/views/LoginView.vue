<script setup>

import Header from "../components/Header.vue";
import LoginForm from "../components/LoginForm.vue";
</script>

<template>
  <div class="login-view">
    <Header />
    <div class="container">
      <LoginForm />
    </div>
  </div>
</template>

<style scoped>
.login-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px 0;
}
</style>