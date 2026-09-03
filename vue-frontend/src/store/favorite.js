import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const STORAGE_KEY = 'galrae_favorite_events'

export const useFavoriteStore = defineStore('favorite', () => {
  const ids = ref(JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]'))

  watch(ids, (value) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  }, { deep: true })

  function isFavorite(id) {
    return ids.value.includes(String(id))
  }

  function toggle(id) {
    const key = String(id)
    ids.value = isFavorite(key)
      ? ids.value.filter(item => item !== key)
      : [...ids.value, key]
  }

  return { ids, isFavorite, toggle }
})
