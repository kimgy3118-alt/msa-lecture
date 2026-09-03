import { defineStore } from 'pinia'
import { ref } from 'vue'
import { eventApi } from '@/api/event.js'

export const useEventStore = defineStore('event', () => {
  const events = ref([])
  const selectedEvent = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const selectedCategory = ref('전체')

  const categories = ['전체', '축제', '전시', '공연', '기타']

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

  // 행사 사진 (백엔드 데이터가 없을 때 보여줄 예시 행사용)
  const eventPhotos = {
    FIREWORKS: new URL('../assets/images/events/fireworks.jpg', import.meta.url).href,
    EXHIBITION: new URL('../assets/images/events/exhibition.jpg', import.meta.url).href,
    SEA: new URL('../assets/images/events/sea-festival.jpg', import.meta.url).href,
    HANOK: new URL('../assets/images/events/hanok.jpg', import.meta.url).href,
    JAZZ: new URL('../assets/images/events/jazz.jpg', import.meta.url).href,
    CITRUS: new URL('../assets/images/events/citrus.jpg', import.meta.url).href,
    GYEONGJU: new URL('../assets/images/events/gyeongju.jpg', import.meta.url).href,
    ROCK: new URL('../assets/images/events/rock.jpg', import.meta.url).href,
  }

  // 백엔드에서 아직 행사 데이터를 받아오지 못했을 때 보여줄 예시 행사
  const sampleEvents = [
    { id: '9001', title: '서울 세계 불꽃축제', category: '축제', venue: '여의도 한강공원', eventType: 'FREE_VISIT', price: 0, imageUrl: eventPhotos.FIREWORKS, eventStartAt: '2026-10-03T19:00:00', organizerName: '서울특별시', reservationCount: 1240 },
    { id: '9002', title: '국립현대미술관 특별전', category: '전시', venue: '국립현대미술관 서울', eventType: 'PAID_RESERVATION', price: 5000, imageUrl: eventPhotos.EXHIBITION, eventStartAt: '2026-09-20T10:00:00', organizerName: '국립현대미술관', reservationCount: 320 },
    { id: '9003', title: '부산 바다 불빛 축제', category: '축제', venue: '광안리 해수욕장', eventType: 'FREE_VISIT', price: 0, imageUrl: eventPhotos.SEA, eventStartAt: '2026-09-26T18:00:00', organizerName: '부산광역시', reservationCount: 980 },
    { id: '9004', title: '전주 한옥마을 전통 체험', category: '문화 체험', venue: '전주 한옥마을', eventType: 'PAID_RESERVATION', price: 12000, imageUrl: eventPhotos.HANOK, eventStartAt: '2026-09-13T11:00:00', organizerName: '전주시', reservationCount: 156 },
    { id: '9005', title: '대구 국제 재즈 페스티벌', category: '공연', venue: '수성못 야외무대', eventType: 'PAID_RESERVATION', price: 8000, imageUrl: eventPhotos.JAZZ, eventStartAt: '2026-10-10T19:30:00', organizerName: '대구광역시', reservationCount: 410 },
    { id: '9006', title: '제주 감귤 마을 체험', category: '문화 체험', venue: '서귀포 감귤 농원', eventType: 'PAID_RESERVATION', price: 15000, imageUrl: eventPhotos.CITRUS, eventStartAt: '2026-11-02T10:00:00', organizerName: '서귀포시', reservationCount: 88 },
    { id: '9007', title: '경주 신라 문화 야행', category: '문화 체험', venue: '동궁과 월지', eventType: 'FREE_VISIT', price: 0, imageUrl: eventPhotos.GYEONGJU, eventStartAt: '2026-09-19T18:30:00', organizerName: '경주시', reservationCount: 640 },
    { id: '9008', title: '인천 펜타포트 락 페스티벌', category: '공연', venue: '송도달빛축제공원', eventType: 'PAID_RESERVATION', price: 99000, imageUrl: eventPhotos.ROCK, eventStartAt: '2026-09-27T15:00:00', organizerName: '인천광역시', reservationCount: 2130 }
  ]

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

      events.value = rawEvents.length ? rawEvents.map(normalizeEvent) : sampleEvents

      console.log('[EventStore] normalized events =', events.value)
    } catch (e) {
      console.error('[EventStore] fetchEvents failed:', e)
      events.value = sampleEvents
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
      const sample = sampleEvents.find(item => String(item.id) === String(id))
      selectedEvent.value = sample || null
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
