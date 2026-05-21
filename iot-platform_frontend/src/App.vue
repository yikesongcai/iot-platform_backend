<template>
  <div class="app">
    <router-view></router-view>
    <NavBar v-if="showNavbar"/> 
  </div>
</template>

<script setup>
  import { computed } from 'vue';
  import { useRoute } from 'vue-router';
  import NavBar from "@/components/NavBar.vue"


  const route = useRoute();
  const showNavbar = computed(() => route.name !== 'login');
</script>

<style lang="less" scoped>
  
  
</style>