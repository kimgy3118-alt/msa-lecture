import { defineStore } from 'pinia'
import { ref } from 'vue'
import { eventApi } from '@/api/event.js'

export const useEventStore = defineStore('event', () => {
  const events = ref([])
  const selectedEvent = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const selectedCategory = ref('전체')

  const categories = ['전체', '축제', '전시', '공연', '문화 체험', '스포츠', '교육', '기타']

  // 백엔드 카테고리 → 프론트 표시용 카테고리
  const categoryLabelMap = {
    FESTIVAL: '축제', EXHIBITION: '전시', PERFORMANCE: '공연',
    CULTURE_EXPERIENCE: '문화 체험', SPORTS: '스포츠', EDUCATION: '교육', OTHER: '기타'
  }

  // 썸네일 이미지 매핑
  const thumbnailMap = {
    SPRING: new URL('../assets/images/events/spring_boot.png', import.meta.url).href,
    VUE: new URL('../assets/images/events/vue_js.png', import.meta.url).href,
    DOCKER: new URL('../assets/images/events/docker.png', import.meta.url).href,
    KUBERNETES: new URL('../assets/images/events/kubernetes.png', import.meta.url).href,
    PYTHON: new URL('../assets/images/events/python.png', import.meta.url).href,
    AI: new URL('../assets/images/events/generative_ai.png', import.meta.url).href,
  }

  const categoryThumbnailMap = {
    '백엔드': thumbnailMap.SPRING,
    '프론트엔드': thumbnailMap.VUE,
    'DevOps': thumbnailMap.KUBERNETES,
    '데이터': thumbnailMap.PYTHON,
    'AI': thumbnailMap.AI
  }

  function normalizeCategory(category) {
    if (!category) return ''
    return categoryLabelMap[category] || category
  }

  function normalizeEvent(event) {
    if (!event || typeof event !== 'object') return event

    return {
      ...event,
      category: normalizeCategory(event.category)
    }
  }

  function getThumbnail(event) {
    const thumbKey = event?.thumbnail?.toUpperCase?.() || ''
    if (thumbKey && thumbnailMap[thumbKey]) {
      return thumbnailMap[thumbKey]
    }

    return categoryThumbnailMap[event?.category] || null
  }

  async function fetchEvents() {
    loading.value = true
    error.value = null

    try {
      const res = await eventApi.getAll()
      console.log('[EventStore] fetchEvents response =', res.data)

      const rawEvents = Array.isArray(res.data?.data)
        ? res.data.data
        : Array.isArray(res.data)
          ? res.data
          : []

      events.value = rawEvents.map(normalizeEvent)

      console.log('[EventStore] normalized events =', events.value)
    } catch (e) {
      console.error('[EventStore] fetchEvents failed:', e)
      error.value = e.message || '행사 목록을 불러오지 못했습니다.'
      events.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchEvent(id) {
    loading.value = true
    error.value = null

    try {
      const res = await eventApi.getById(id)
      console.log('[EventStore] fetchEvent response =', res.data)

      const rawEvent =
        res.data?.data && typeof res.data.data === 'object'
          ? res.data.data
          : res.data

      selectedEvent.value = normalizeEvent(rawEvent)

      console.log('[EventStore] normalized selectedEvent =', selectedEvent.value)
    } catch (e) {
      console.error('[EventStore] fetchEvent failed:', e)
      error.value = e.message || '행사 정보를 불러오지 못했습니다.'
      selectedEvent.value = null
    } finally {
      loading.value = false
    }
  }

  function setCategory(cat) {
    selectedCategory.value = cat
  }

  return {
    events,
    selectedEvent,
    loading,
    error,
    categories,
    selectedCategory,
    thumbnailMap,
    categoryLabelMap,
    normalizeCategory,
    normalizeEvent,
    getThumbnail,
    fetchEvents,
    fetchEvent,
    setCategory
  }
})
