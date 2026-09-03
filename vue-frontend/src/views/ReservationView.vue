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
          <div v-for="item in reservations" :key="item.id" class="reservation-card">
            <div class="enroll-thumb" :class="getThumbBg(item.event?.category)">
              <img :src="getThumbSrc(item.event)" :alt="item.event?.title" />
            </div>

            <div class="enroll-info">
              <span class="badge" :class="getBadge(item.event?.category)">
                {{ item.event?.category }}
              </span>
              <h3 class="enroll-title">{{ item.event?.title }}</h3>
              <p class="enroll-instructor">주관 기관: {{ item.event?.instructorName }}</p>
            </div>

            <div class="enroll-status">
              <span
                :class="[
                  'status-badge',
                  item.status === 'ACTIVE' ? 'status-active' : 'status-pending'
                ]"
              >
                {{ item.status === 'ACTIVE' ? '예약 확정' : '대기 중' }}
              </span>
              <router-link :to="`/events/${item.eventId}`" class="btn btn-ghost btn-sm">
                행사 보기
              </router-link>
            </div>
          </div>
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

const router = useRouter()
const auth = useAuthStore()

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

function getBadge(cat) {
  return categoryConfig[cat]?.badge || 'badge-gray'
}

function getThumbSrc(event) {
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
  padding: 56px 24px 100px;
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
  gap: 16px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 16px;
  transition: var(--transition);
}

.reservation-card:hover {
  box-shadow: var(--shadow-sm);
}

.enroll-thumb {
  width: 72px;
  height: 72px;
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
  object-fit: contain;
  padding: 8px;
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

.enroll-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.enroll-title {
  font-size: 15px;
  font-weight: 600;
}

.enroll-instructor {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.enroll-status {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: #e8f0fb;
  color: #174fbd;
}

.status-pending {
  background: #FAEEDA;
  color: #854F0B;
}

.btn-sm {
  padding: 7px 14px;
  font-size: 13px;
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