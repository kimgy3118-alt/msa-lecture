<template>
  <div class="page-wrapper">
    <div class="page-shell">
      <section class="page-heading">
        <p>마이페이지</p>
        <h1>내 예약 목록</h1>
      </section>

      <main class="main-content">
        <div v-if="loading" class="loading-center">
          <div class="spinner"></div>
        </div>

        <div v-else-if="reservations.length" class="reservation-list fade-in">
          <router-link v-for="item in reservations" :key="item.id" :to="`/events/${item.eventId}`" class="reservation-card">
            <div class="enroll-thumb" :class="getThumbBg(item.event?.category)">
              <img :src="getThumbSrc(item.event)" :alt="item.event?.title" />
            </div>

            <div class="enroll-info">
              <span class="enroll-category">{{ eventStore.normalizeCategory(item.event?.category) }}</span>
              <h3 class="enroll-title">{{ item.event?.title }}</h3>
              <p class="enroll-venue">⌖ {{ item.event?.venue || '장소 추후 공지' }}</p>
            </div>

            <div class="enroll-right">
              <b class="enroll-price">{{ priceLabel(item.event) }}</b>
              <span
                :class="[
                  'status-badge',
                  item.status === 'CONFIRMED' ? 'status-active' : item.status === 'CANCELLED' ? 'status-cancelled' : 'status-pending'
                ]"
              >
                {{ statusLabel(item.status) }}
              </span>
            </div>
          </router-link>
        </div>

        <div v-else class="empty-state">
          <span class="empty-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="7" width="18" height="13" rx="2" /><path d="M3 7l9 6 9-6" /></svg>
          </span>
          <p>예약 확정인 행사가 없습니다.</p>
          <router-link to="/events" class="btn btn-primary" style="margin-top:10px;">
            행사 둘러보기
          </router-link>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { reservationApi } from '@/api/reservation.js'
import { useAuthStore } from '@/store/auth.js'
import { useEventStore } from '@/store/event.js'

const router = useRouter()
const auth = useAuthStore()
const eventStore = useEventStore()

const reservations = ref([])
const loading = ref(true)

const isOrganizer = computed(() => auth.user?.role === 'ADMIN')

const categoryConfig = {
  '백엔드': { bg: 'thumb-teal', badge: 'badge-teal', thumb: 'spring_boot' },
  '프론트엔드': { bg: 'thumb-teal', badge: 'badge-teal', thumb: 'vue_js' },
  'DevOps': { bg: 'thumb-blue', badge: 'badge-blue', thumb: 'kubernetes' },
  '데이터': { bg: 'thumb-purple', badge: 'badge-purple', thumb: 'python' },
  'AI': { bg: 'thumb-pink', badge: 'badge-pink', thumb: 'generative_ai' },
}

function getThumbBg(cat) {
  return categoryConfig[cat]?.bg || 'thumb-gray'
}

function priceLabel(event) {
  const value = Number(event?.price ?? 0)
  return value > 0 ? `${value.toLocaleString()}원` : '무료'
}

function statusLabel(status) {
  if (status === 'CONFIRMED') return '예약 확정'
  if (status === 'CANCELLED') return '취소됨'
  return '결제 처리 중'
}

function getThumbSrc(event) {
  const eventImage = eventStore.getEventImage(event)
  if (eventImage) return eventImage

  const key = event?.thumbnail || categoryConfig[event?.category]?.thumb
  if (!key) return ''
  try {
    return new URL(`../assets/images/events/${key}.png`, import.meta.url).href
  } catch {
    return ''
  }
}

onMounted(async () => {
  // 기관 담당자는 이 페이지 접근 불가 → 마이페이지로 이동
  if (isOrganizer.value) {
    console.warn('[ReservationView] instructor tried to access /reservations, redirect to /mypage')
    router.replace('/mypage')
    return
  }

  try {
    const res = await reservationApi.getMyReservations()
    console.log('[ReservationView] my reservations response:', res.data)

    if (Array.isArray(res.data?.data)) {
      reservations.value = res.data.data
    } else if (Array.isArray(res.data)) {
      reservations.value = res.data
    } else {
      reservations.value = []
    }
  } catch (error) {
    console.error('[ReservationView] failed to load reservations:', error)
    reservations.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #fff;
}

.page-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 100px;
}

.page-heading {
  padding-bottom: 32px;
  margin-bottom: 36px;
  border-bottom: 1px solid var(--color-border);
}

.page-heading p {
  margin: 0 0 10px;
  color: var(--color-text-secondary);
  font-size: 15px;
  font-weight: 600;
}

.page-heading h1 {
  margin: 0;
  font-size: 36px;
  letter-spacing: -1.4px;
  color: var(--color-text-primary);
}

.main-content {
  min-width: 0;
}

.reservation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reservation-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px;
  transition: var(--transition);
  text-decoration: none;
  color: inherit;
}

.reservation-card:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-hover);
}

.enroll-thumb {
  width: 108px;
  height: 108px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.enroll-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.enroll-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.enroll-category {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 999px;
  background: var(--color-primary-light);
  color: var(--color-primary);
  font-size: 11.5px;
  font-weight: 700;
}

.enroll-venue {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.enroll-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
}

.enroll-price {
  font-size: 15px;
  color: var(--color-text-primary);
}

.thumb-teal {
  background: #e8f0fb;
}

.thumb-blue {
  background: #E6F1FB;
}

.thumb-purple {
  background: #EEEDFE;
}

.thumb-pink {
  background: #FBEAF0;
}

.thumb-gray {
  background: #F1EFE8;
}

.enroll-title {
  width: 100%;
  font-size: 16.5px;
  font-weight: 700;
  letter-spacing: -0.3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-badge {
  flex-shrink: 0;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12.5px;
  font-weight: 600;
}

.status-active {
  background: #e8f0fb;
  color: #174fbd;
}

.status-pending {
  background: #FAEEDA;
  color: #854F0B;
}

.status-cancelled {
  background: #f3f4f6;
  color: #6b7280;
}

.empty-state {
  min-height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-muted);
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 700px) {
  .page-shell {
    padding: 40px 18px 70px;
  }

  .page-heading h1 {
    font-size: 28px;
  }
}
</style>
