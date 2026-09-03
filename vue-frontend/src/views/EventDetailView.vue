<template>
  <div class="page-wrapper">
    <div class="detail-layout" v-if="event">
      <div class="detail-hero">
        <div class="detail-hero-inner">
          <!-- 좌측 상세 정보 -->
          <div class="detail-info fade-in-up">
            <span class="badge" :class="badgeClass">{{ displayCategory }}</span>
            <h1 class="detail-title">{{ event.title }}</h1>
            <p class="detail-desc">
              {{ event.description || '지역의 매력을 가까이에서 만나보는 특별한 행사입니다.' }}
            </p>

            <div class="detail-meta">
              <span>주관 기관: {{ displayInstructorName }}</span>
              <span v-if="showCapacity">예약 인원: {{ displayReservationCount }}/{{ displayCapacity }}</span>
              <span v-else>예약자: {{ displayReservationCount }}명</span>
            </div>

            <div v-if="mapEmbedUrl" class="detail-map">
              <p class="event-place">⌖ {{ event.venue }}</p>
              <iframe
                :src="mapEmbedUrl"
                title="행사 장소 지도"
                loading="lazy"
                referrerpolicy="no-referrer-when-downgrade"
              ></iframe>
            </div>
          </div>

          <!-- 우측 결제/참여 카드 -->
          <div class="enroll-card fade-in">
            <div class="enroll-thumb" :class="thumbBg">
              <img v-if="event.imageUrl || thumbSrc" :src="event.imageUrl || thumbSrc" :alt="event.title" />
            </div>

            <div class="enroll-body">
              <div class="enroll-price">{{ priceLabel }}</div>
              <p class="event-place">⌖ {{ event.venue }} · {{ displayDate }}</p>

              <button
                class="btn btn-primary btn-full"
                @click="handlePrimaryAction"
                :disabled="buttonDisabled"
                :class="{ 'btn-disabled': buttonDisabled }"
              >
                <span v-if="enrolling">처리 중...</span>
                <span v-else>{{ buttonLabel }}</span>
              </button>

              <div v-if="enrollError" class="error-msg">{{ enrollError }}</div>

              <p class="helper-text" v-if="helperText">
                {{ helperText }}
              </p>

              <ul class="enroll-info-list"><li>✓ {{ event.eventType === 'FREE_VISIT' ? '예약 없이 자유롭게 방문' : '선착순 예약' }}</li><li>✓ 행사 시작 전까지 예약 가능</li></ul>

              <div v-if="reservationStatus === 'CONFIRMED'" class="my-reservation-box">
                <div class="my-reservation-head">
                  <b>내 예약 정보</b>
                  <span class="payment-status-badge" :class="{ paid: myPayment }">{{ myPayment ? '결제 완료' : '예약 확정' }}</span>
                </div>
                <div class="my-reservation-row"><span>행사일까지</span><strong class="dday">{{ ddayLabel }}</strong></div>
                <div v-if="myPayment" class="my-reservation-row"><span>결제 금액</span><strong>{{ Number(myPayment.amount).toLocaleString() }}원</strong></div>
                <div v-if="myPayment" class="my-reservation-row"><span>결제 시간</span><strong>{{ new Date(myPayment.createdAt).toLocaleString('ko-KR') }}</strong></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="loading" class="loading-center">
      <div class="spinner"></div>
    </div>

    <div v-else class="loading-center">
      <p class="empty-text">행사 정보를 불러오지 못했습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useEventStore } from '@/store/event.js'
import { reservationApi } from '@/api/reservation.js'
import { paymentApi } from '@/api/payment.js'
import { useAuthStore } from '@/store/auth.js'

const route = useRoute()
const router = useRouter()
const eventStore = useEventStore()
const auth = useAuthStore()

const enrolling = ref(false)
const enrollError = ref('')
const reservationStatus = ref('NONE') // NONE | PAYMENT_PENDING | CONFIRMED
const myPayment = ref(null)

const ddayLabel = computed(() => {
  if (!event.value?.eventStartAt) return '일정 미정'
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const target = new Date(event.value.eventStartAt); target.setHours(0, 0, 0, 0)
  const diffDays = Math.round((target - today) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return 'D-DAY'
  if (diffDays > 0) return `D-${diffDays}`
  return '종료된 행사'
})

async function loadMyPayment() {
  myPayment.value = null
  if (!auth.user?.id || event.value?.eventType !== 'PAID_RESERVATION') return
  try {
    const res = await paymentApi.getMyPayments(auth.user.id)
    const list = Array.isArray(res.data?.data) ? res.data.data : Array.isArray(res.data) ? res.data : []
    myPayment.value = list.find(p => Number(p.eventId) === Number(event.value.id) && p.status === 'COMPLETED') || null
  } catch (e) {
    console.error('[EventDetail] failed to load payment info:', e)
  }
}

const event = computed(() => eventStore.selectedEvent)
const loading = computed(() => eventStore.loading)
const isOrganizer = computed(() => auth.user?.role === 'ADMIN')

const categoryConfig = {
  '백엔드': { badge: 'badge-teal', bg: 'thumb-teal', thumb: 'spring_boot' },
  '프론트엔드': { badge: 'badge-teal', bg: 'thumb-teal', thumb: 'vue_js' },
  'DevOps': { badge: 'badge-blue', bg: 'thumb-blue', thumb: 'kubernetes' },
  '데이터': { badge: 'badge-purple', bg: 'thumb-purple', thumb: 'python' },
  'AI': { badge: 'badge-pink', bg: 'thumb-pink', thumb: 'generative_ai' },
}

const config = computed(() => categoryConfig[event.value?.category] || {})
const badgeClass = computed(() => config.value.badge || 'badge-gray')
const thumbBg = computed(() => config.value.bg || 'thumb-gray')

const displayCategory = computed(() => event.value?.category || '-')

const displayInstructorName = computed(() => event.value?.organizerName || '주관 기관 정보 없음')

const mapEmbedUrl = computed(() => {
  if (!event.value?.venue) return null
  return `https://www.google.com/maps?q=${encodeURIComponent(event.value.venue)}&output=embed`
})

const displayReservationCount = computed(() => {
  const value = Number(
    event.value?.reservationCount ??
    event.value?.reservation_count ??
    0
  )
  return Number.isNaN(value) ? 0 : value.toLocaleString()
})

const displayCapacity = computed(() => {
  const value = Number(event.value?.capacity ?? 0)
  return Number.isNaN(value) ? 0 : value.toLocaleString()
})

const showCapacity = computed(() => event.value?.eventType !== 'FREE_VISIT' && Number(event.value?.capacity) > 0)

const displayPrice = computed(() => {
  const value = Number(event.value?.price ?? 0)
  return Number.isNaN(value) ? '0' : value.toLocaleString()
})
const priceLabel = computed(() => event.value?.eventType === 'FREE_VISIT' || event.value?.eventType === 'FREE_RESERVATION' ? '무료' : `₩${displayPrice.value}`)
const displayDate = computed(() => event.value?.eventStartAt ? new Date(event.value.eventStartAt).toLocaleString('ko-KR') : '일정 미정')

const thumbSrc = computed(() => {
  const key = event.value?.thumbnail || config.value.thumb
  if (!key) return null

  try {
    return new URL(`../assets/images/events/${key}.png`, import.meta.url).href
  } catch {
    return null
  }
})

const buttonLabel = computed(() => {
  if (isOrganizer.value) return '기관 담당자 계정은 신청 불가'
  if (event.value?.eventType === 'FREE_VISIT') return '자유 방문 행사'
  if (reservationStatus.value === 'CONFIRMED') return '내 예약 목록으로 이동'
  if (reservationStatus.value === 'PAYMENT_PENDING') return '결제 처리 중'
  return event.value?.eventType === 'PAID_RESERVATION' ? '결제하고 예약하기' : '무료 예약하기'
})

const buttonDisabled = computed(() => {
  if (enrolling.value) return true
  if (isOrganizer.value) return true
  if (event.value?.eventType === 'FREE_VISIT' || reservationStatus.value === 'PAYMENT_PENDING') return true
  return false
})

const helperText = computed(() => {
  if (isOrganizer.value) {
    return '기관 담당자 계정은 본인 행사를 참여 신청할 수 없습니다.'
  }

  if (event.value?.eventType === 'FREE_VISIT') return '예약과 결제 없이 행사 기간에 맞춰 방문하세요.'
  if (reservationStatus.value === 'CONFIRMED') {
    return '예약이 확정되었습니다. 내 예약 목록에서 확인할 수 있습니다.'
  }

  if (reservationStatus.value === 'PAYMENT_PENDING') {
    return '결제가 처리되면 예약이 확정됩니다.'
  }

  return event.value?.eventType === 'PAID_RESERVATION' ? '결제를 진행하면 예약이 확정됩니다.' : '예약 즉시 확정됩니다.'
})

async function loadReservationStatus() {
  if (!auth.user?.id || !event.value?.id || isOrganizer.value) {
    reservationStatus.value = 'NONE'
    return
  }

  try {
    const res = await reservationApi.getMyReservations()
    console.log('[EventDetail] my reservations response =', res.data)

    const reservations = Array.isArray(res.data?.data)
      ? res.data.data
      : Array.isArray(res.data)
        ? res.data
        : []

    const matched = reservations.find(item => Number(item.eventId) === Number(event.value.id))

    if (!matched) {
      reservationStatus.value = 'NONE'
      return
    }

    reservationStatus.value = matched.status
    if (reservationStatus.value === 'CONFIRMED') await loadMyPayment()
  } catch (e) {
    console.error('[EventDetail] failed to load reservation status:', e)
    reservationStatus.value = 'NONE'
  }
}

async function handlePrimaryAction() {
  enrollError.value = ''

  if (!event.value?.id) {
    enrollError.value = '행사 정보가 올바르지 않습니다.'
    return
  }

  if (isOrganizer.value) {
    enrollError.value = '기관 담당자 계정은 본인 행사를 참여 신청할 수 없습니다.'
    return
  }

  if (reservationStatus.value === 'CONFIRMED') {
    router.push('/reservations')
    return
  }

  if (reservationStatus.value === 'PAYMENT_PENDING') {
    return
  }

  enrolling.value = true

  try {
    await reservationApi.reserve(event.value.id)
    reservationStatus.value = event.value.eventType === 'PAID_RESERVATION' ? 'PAYMENT_PENDING' : 'CONFIRMED'

    // 유료 예약은 결제 완료 → 예약 확정(CONFIRMED)까지 Kafka를 통한 비동기 처리라
    // 확정될 때까지 잠시 상태를 다시 확인한다.
    if (reservationStatus.value === 'PAYMENT_PENDING') {
      await pollUntilConfirmed()
    }
  } catch (e) {
    console.error('[EventDetail] enroll failed:', e)
    enrollError.value = e.response?.data?.message || '결제/참여 신청에 실패했습니다.'
  } finally {
    enrolling.value = false
  }
}

async function pollUntilConfirmed(attempts = 8, delayMs = 1500) {
  for (let i = 0; i < attempts; i++) {
    await new Promise(resolve => setTimeout(resolve, delayMs))
    await loadReservationStatus()
    if (reservationStatus.value === 'CONFIRMED') return
  }
}

onMounted(async () => {
  await eventStore.fetchEvent(route.params.id)
  console.log('[EventDetail] selectedEvent =', eventStore.selectedEvent)
  await loadReservationStatus()
})

watch(
  () => eventStore.selectedEvent,
  async (value) => {
    console.log('[EventDetail] selectedEvent changed =', value)
    if (value?.id) {
      await loadReservationStatus()
    }
  },
  { deep: true }
)
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #fff;
}

.detail-hero {
  padding:72px 0 88px;
}

.detail-hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns:minmax(0,1fr) 360px;
  gap:64px;
  align-items: start;
}

.detail-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 14px;
}

.detail-map {
  width: 100%;
  margin-top: 8px;
}

.detail-map .event-place {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.detail-map iframe {
  width: 100%;
  height: 280px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.detail-title {
  font-size:48px;
  font-weight: 700;
  line-height: 1.3;
}

.detail-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}

.detail-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: var(--color-text-secondary);
  flex-wrap: wrap;
}

.enroll-card {
  background:#fff;
  border:1px solid #dce5f1;
  border-radius:24px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.enroll-thumb {
  height:210px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.enroll-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-teal { background: #E1F5EE; }
.thumb-blue { background: #E6F1FB; }
.thumb-purple { background: #EEEDFE; }
.thumb-pink { background: #FBEAF0; }
.thumb-gray { background: #F1EFE8; }

.enroll-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.enroll-price {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-primary);
}

.btn-full {
  width: 100%;
  padding: 13px;
  font-size: 15px;
  justify-content: center;
}

.btn-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.enroll-info-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.enroll-info-list li {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.my-reservation-box {
  margin-top: 4px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--color-bg-secondary);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.my-reservation-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.my-reservation-box b {
  font-size: 13px;
  color: var(--color-text-primary);
}

.payment-status-badge {
  padding: 4px 11px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  background: #e8f0fb;
  color: #174fbd;
}

.payment-status-badge.paid {
  background: #e6f7ee;
  color: #1a8a4f;
}

.my-reservation-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-text-secondary);
}

.my-reservation-row strong {
  color: var(--color-text-primary);
  font-weight: 600;
}

.my-reservation-row .dday {
  color: var(--color-primary);
  font-weight: 700;
}

.error-msg {
  font-size: 13px;
  color: #dc2626;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: var(--radius-sm);
}

.helper-text {
  font-size: 12px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.empty-text {
  font-size: 14px;
  color: var(--color-text-muted);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: 100px 0;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.badge-gray {
  background: #f3f4f6;
  color: #6b7280;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 900px) {
  .detail-hero-inner {
    grid-template-columns: 1fr;
  }
}
</style>
