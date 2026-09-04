<template>
  <AppHeader v-if="!route.meta.bareLayout" />
  <router-view />
  <AppFooter v-if="!route.meta.bareLayout" />
</template>

<script setup>
import { useRoute } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'

const route = useRoute()
</script>
