<template>
  <div class="page-wrapper">
    <div class="page-shell">
      <section class="page-heading">
        <p>마이페이지</p>
        <h1>{{ auth.user?.name || '사용자' }}님, 안녕하세요</h1>
      </section>

      <main class="main-content">
        <!-- 프로필 카드 -->
        <div class="profile-card fade-in-up">
          <div class="profile-avatar"><img :src="avatarSrc" alt="프로필 사진" /></div>
          <div class="profile-info">
            <h2 class="profile-name">{{ auth.user?.name || '사용자' }}</h2>
            <p class="profile-email">{{ auth.user?.email || '-' }}</p>
            <span class="badge" :class="isOrganizer ? 'badge-amber' : 'badge-blue'">
              {{ isOrganizer ? '기관 담당자' : '일반 사용자' }}
            </span>
          </div>
        </div>

        <!-- 찜한 행사 -->
        <section class="favorite-section">
          <h3 class="section-title">찜한 행사</h3>
          <div v-if="favoriteEvents.length" class="recommend-grid fade-in">
            <EventCard v-for="c in favoriteEvents" :key="c.id" :event="c" />
          </div>
          <p v-else class="empty-text">아직 찜한 행사가 없습니다. 마음에 드는 행사에서 하트를 눌러보세요.</p>
        </section>

        <!-- 일반 사용자 화면 -->
        <section v-if="!isOrganizer" class="recommend-section">
          <h3 class="section-title">추천 행사</h3>

          <p v-if="recommendMessage" class="recommend-message">
            {{ recommendMessage }}
          </p>

          <div v-if="recommendLoading" class="loading-row">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-thumb"></div>
              <div class="skeleton-body">
                <div class="skeleton-line short"></div>
                <div class="skeleton-line"></div>
              </div>
            </div>
          </div>

          <div v-else-if="recommendations.length" class="recommend-grid fade-in">
            <EventCard v-for="c in recommendations" :key="c.id" :event="c" />
          </div>

          <p v-else-if="recommendError" class="empty-text">
            {{ recommendError }}
          </p>

          <p v-else class="empty-text">
            아직 추천할 행사가 없습니다.
          </p>
        </section>

        <!-- 기관 담당자 화면 -->
        <section v-else class="instructor-section">
          <div class="section-head">
            <h3 class="section-title">내가 등록한 행사</h3>
            <span class="section-subtitle">등록한 행사와 행사별 예약자 수를 확인할 수 있습니다.</span>
          </div>

          <div class="summary-cards">
            <div class="summary-card">
              <div class="summary-label">등록 행사 수</div>
              <div class="summary-value">{{ myEvents.length }}</div>
            </div>
            <div class="summary-card">
              <div class="summary-label">총 예약자 수</div>
              <div class="summary-value">{{ totalReservationCount }}</div>
            </div>
          </div>

          <div v-if="instructorLoading" class="loading-row instructor-loading">
            <div v-for="i in 3" :key="i" class="skeleton-card">
              <div class="skeleton-thumb"></div>
              <div class="skeleton-body">
                <div class="skeleton-line short"></div>
                <div class="skeleton-line"></div>
              </div>
            </div>
          </div>

          <div v-else-if="myEvents.length" class="instructor-event-list fade-in">
            <div
              v-for="event in myEvents"
              :key="event.id"
              class="instructor-event-card"
            >
              <div class="event-card-top">
                <div>
                  <h4 class="event-title">{{ event.title }}</h4>
                  <p class="event-desc">{{ event.description || '설명이 없습니다.' }}</p>
                </div>
                <span
                  class="status-badge"
                  :class="event.status === 'ACTIVE' ? 'status-active' : 'status-inactive'"
                >
                  {{ event.status || 'UNKNOWN' }}
                </span>
              </div>

              <div class="event-meta-grid">
                <div class="meta-box">
                  <div class="meta-label">카테고리</div>
                  <div class="meta-value">{{ event.category || '-' }}</div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">가격</div>
                  <div class="meta-value">{{ formatPrice(event.price) }}</div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">예약자 수</div>
                  <div class="meta-value">
                    {{ event.reservation_count ?? event.reservationCount ?? 0 }}명
                  </div>
                </div>
                <div class="meta-box">
                  <div class="meta-label">행사 ID</div>
                  <div class="meta-value">{{ event.id }}</div>
                </div>
              </div>

              <div class="event-card-actions">
                <router-link :to="`/events/${event.id}`" class="action-btn action-primary">
                  행사 보기
                </router-link>
              </div>
            </div>
          </div>

          <p v-else-if="organizerError" class="empty-text">
            {{ organizerError }}
          </p>

          <p v-else class="empty-text">
            아직 등록한 행사가 없습니다.
          </p>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import EventCard from '@/components/EventCard.vue'
import { useAuthStore } from '@/store/auth.js'
import { useEventStore } from '@/store/event.js'
import { useFavoriteStore } from '@/store/favorite.js'
import { reservationApi } from '@/api/reservation.js'
import { eventApi } from '@/api/event.js'
import profileImg from '@/assets/images/profile/profile.png'
import profileOrganizerImg from '@/assets/images/profile/profile2.png'

const auth = useAuthStore()
const eventStore = useEventStore()
const favoriteStore = useFavoriteStore()

const isOrganizer = computed(() => auth.user?.role === 'ADMIN')
const avatarSrc = computed(() => (isOrganizer.value ? profileOrganizerImg : profileImg))
const favoriteEvents = computed(() => eventStore.events.filter(e => favoriteStore.isFavorite(e.id)))

/* 일반 사용자용 */
const recommendations = ref([])
const recommendLoading = ref(true)
const recommendError = ref('')
const recommendMessage = ref('')

/* 기관 담당자용 */
const myEvents = ref([])
const instructorLoading = ref(true)
const organizerError = ref('')

const totalReservationCount = computed(() =>
  myEvents.value.reduce((sum, event) => {
    const count = Number(event.reservation_count ?? event.reservationCount ?? 0)
    return sum + (Number.isNaN(count) ? 0 : count)
  }, 0)
)

function formatPrice(price) {
  const value = Number(price ?? 0)
  if (Number.isNaN(value)) return '-'
  return `${value.toLocaleString()}원`
}

/**
 * event 객체에서 기관 담당자 식별자 추출
 */
function getEventOrganizerId(event) {
  return (
    event.organizerId ??
    event.organizer_id ??
    event.instructor ??
    event.teacherId ??
    event.teacher_id ??
    null
  )
}

async function loadStudentRecommendations() {
  try {
    if (!auth.user) {
      console.warn('[MyPage] auth.user is missing')
      recommendError.value = '추천 행사를 준비 중입니다.'
      return
    }

    if (!auth.user.id) {
      console.warn('[MyPage] auth.user.id is missing:', auth.user)
      recommendError.value = '추천 행사를 준비 중입니다.'
      return
    }

    const res = await reservationApi.getRecommendations(auth.user.id)
    console.log('[MyPage] recommendation response:', res.data)

    const payload = res.data

    if (Array.isArray(payload?.recommendedEvents)) {
      recommendations.value = payload.recommendedEvents
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload?.data)) {
      recommendations.value = payload.data
      recommendMessage.value = payload.message ?? ''
    } else if (Array.isArray(payload)) {
      recommendations.value = payload
      recommendMessage.value = ''
    } else {
      console.warn('[MyPage] unexpected recommendation response shape:', payload)
      recommendations.value = []
      recommendMessage.value = ''
    }
  } catch (error) {
    console.error('[MyPage] failed to load recommendations:', error)
    recommendError.value = '현재 추천 행사를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    recommendLoading.value = false
  }
}

async function loadOrganizerEvents() {
  try {
    if (!auth.user) {
      console.warn('[MyPage] instructor auth.user is missing')
      organizerError.value = '행사 정보를 불러오지 못했습니다.'
      return
    }

    if (!auth.user.id) {
      console.warn('[MyPage] instructor auth.user.id is missing:', auth.user)
      organizerError.value = '행사 정보를 불러오지 못했습니다.'
      return
    }

    const res = await eventApi.getEvents()
    console.log('[MyPage] event list response:', res.data)

    let events = []

    if (Array.isArray(res.data?.data)) {
      events = res.data.data
    } else if (Array.isArray(res.data)) {
      events = res.data
    } else {
      console.warn('[MyPage] unexpected event response shape:', res.data)
    }

    console.log('[MyPage] auth.user =', auth.user)
    console.log('[MyPage] events =', events)
    console.log('[MyPage] first event =', events[0])

    events.forEach(event => {
      console.log('[MyPage] instructor fields check:', {
        eventId: event.id,
        organizerId: event.organizerId,
        organizer_id: event.organizer_id,
        instructor: event.instructor,
        teacherId: event.teacherId,
        teacher_id: event.teacher_id,
        rawEvent: event
      })
    })

    const organizerId = Number(auth.user.id)

    myEvents.value = events.filter(event => {
      const eventOrganizerId = Number(getEventOrganizerId(event))
      return !Number.isNaN(eventOrganizerId) && eventOrganizerId === organizerId
    })

    console.log('[MyPage] filtered myEvents =', myEvents.value)
  } catch (error) {
    console.error('[MyPage] failed to load instructor events:', error)
    organizerError.value = '현재 행사 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    instructorLoading.value = false
  }
}

onMounted(async () => {
  if (!eventStore.events.length) eventStore.fetchEvents()

  if (isOrganizer.value) {
    recommendLoading.value = false
    await loadOrganizerEvents()
  } else {
    instructorLoading.value = false
    await loadStudentRecommendations()
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
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 28px;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--color-primary-light);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.profile-name {
  font-size: 20px;
  font-weight: 700;
}

.profile-email {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.badge-blue {
  background: #e8f1ff;
  color: #2563eb;
}

.badge-amber {
  background: #f7edd8;
  color: #9a6700;
}

.section-head {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
}

.section-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
}

.recommend-message {
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.loading-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.instructor-loading {
  margin-bottom: 20px;
}

.skeleton-card {
  background: var(--color-bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.skeleton-thumb {
  height: 110px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-body {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-line.short {
  width: 40%;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(160px, 220px));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
}

.summary-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.instructor-event-list {
  display: grid;
  gap: 22px;
}

.instructor-event-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}

.instructor-event-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.event-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.event-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.event-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  white-space: pre-line;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #e6f7ee;
  color: #1a8a4f;
}

.status-inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.event-meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 18px;
}

.meta-box {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 14px;
}

.meta-label {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 6px;
}

.meta-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.event-card-actions {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  border-radius: var(--radius-md);
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 600;
  transition: var(--transition);
}

.action-primary {
  background: var(--color-primary);
  color: white;
}

.action-primary:hover {
  opacity: 0.92;
}

.empty-text {
  color: var(--color-text-muted);
  font-size: 14px;
}

@keyframes shimmer {
  to {
    background-position: -200% 0;
  }
}

@media (max-width: 992px) {
  .recommend-grid,
  .loading-row,
  .event-meta-grid {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .page-shell {
    padding: 40px 18px 70px;
  }

  .page-heading h1 {
    font-size: 28px;
  }

  .profile-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .event-card-top {
    flex-direction: column;
  }

  .summary-cards {
    grid-template-columns: 1fr;
  }
}
</style>