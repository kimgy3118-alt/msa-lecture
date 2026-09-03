<template>
  <main class="landing">
    <section class="hero">
      <div class="hero-card">
        <img class="hero-image" src="@/assets/images/hero/korea-festival-hero.png" alt="한국 문화 축제 풍경" />
        <div class="hero-shade"></div>
        <div class="hero-copy">
          <h1>어디로, 어떤 행사에<br />참여해 볼까요?</h1>
          <p>전국 곳곳의 특별한 행사를 갈래에서 만나보세요.</p>
          <router-link to="/events" class="hero-cta">행사 찾기 <span>→</span></router-link>
        </div>
      </div>

      <form class="search-panel" @submit.prevent="submitSearch">
        <svg class="search-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
        <input v-model.trim="searchText" placeholder="어디로, 어떤 행사를 찾고 있나요?" />
        <button type="submit">검색</button>
      </form>
    </section>

    <section class="quick-wrap">
      <div class="quick-grid">
        <router-link v-for="item in quickLinks" :key="item.title" :to="{ path: '/events', query: { category: item.title } }" class="quick">
          <span class="quick-icon" :class="item.tint" v-html="item.icon"></span>
          <b>{{ item.title }}</b>
          <small>{{ item.sub }}</small>
        </router-link>
      </div>
    </section>

    <section class="content-section">
      <div class="heading">
        <h2>갈래가 추천하는 특별한 행사</h2>
        <router-link to="/events">전체 보기 →</router-link>
      </div>

      <div class="card-carousel">
        <button class="carousel-arrow prev" @click="scrollCards(-1)" aria-label="이전 카드">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6" /></svg>
        </button>

        <div class="card-track" ref="trackRef" @scroll="onTrackScroll">
          <router-link
            v-for="event in featuredEvents"
            :key="event.id"
            :to="'/events/' + event.id"
            class="feature-card"
          >
            <img class="feature-card-photo" :src="event.imageUrl" :alt="event.title" />
            <button class="heart-btn" :class="{ liked: favoriteStore.isFavorite(event.id) }" @click.prevent="favoriteStore.toggle(event.id)" aria-label="찜하기">
              <svg viewBox="0 0 24 24" width="16" height="16" :fill="favoriteStore.isFavorite(event.id) ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21s-7.5-4.6-10-9.3C.5 8 2 4.5 5.5 4c2-.3 3.8.6 6.5 3.4C14.7 4.6 16.5 3.7 18.5 4 22 4.5 23.5 8 22 11.7 19.5 16.4 12 21 12 21Z" /></svg>
            </button>
            <div class="feature-card-overlay">
              <span class="feature-card-cat">{{ event.category }}</span>
              <h3>{{ event.title }}</h3>
              <p>⌖ {{ event.venue }} · {{ priceLabel(event) }}</p>
            </div>
          </router-link>
        </div>

        <button class="carousel-arrow next" @click="scrollCards(1)" aria-label="다음 카드">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6" /></svg>
        </button>
      </div>

      <div class="carousel-dots">
        <span v-for="(event, i) in featuredEvents" :key="event.id" :class="{ active: activeIndex === i }"></span>
      </div>
    </section>

    <section class="region-section">
      <h2>갈래에서 전국 여행의 갈래를 찾아보세요</h2>
    </section>
  </main>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useEventStore } from '@/store/event.js'
import { useFavoriteStore } from '@/store/favorite.js'
import { useAuthStore } from '@/store/auth.js'
import { reservationApi } from '@/api/reservation.js'

const router = useRouter()
const eventStore = useEventStore()
const favoriteStore = useFavoriteStore()
const auth = useAuthStore()

const trackRef = ref(null)
const activeIndex = ref(0)
const searchText = ref('')
const ruleBasedEvents = ref(null)

// 로그인 사용자는 recommend-service의 규칙 기반 추천(참여 이력의 최빈 카테고리 → 미예약 인기 행사)을 그대로 보여준다.
// 추천 결과가 없거나(신규 사용자 등) 비로그인 상태면 전체 행사 목록으로 대체한다.
const featuredEvents = computed(() => {
  if (ruleBasedEvents.value?.length) return ruleBasedEvents.value
  return eventStore.events.slice(0, 6)
})

async function loadRuleBasedRecommendations() {
  if (!auth.isAuthenticated || !auth.user?.id) return
  try {
    const res = await reservationApi.getRecommendations(auth.user.id)
    const list = res.data?.recommendedEvents ?? res.data?.data?.recommendedEvents ?? []
    ruleBasedEvents.value = list.map(eventStore.normalizeEvent)
  } catch (e) {
    console.error('[Landing] failed to load rule-based recommendations:', e)
  }
}

function priceLabel(event) {
  return event.eventType === 'PAID_RESERVATION' ? `${Number(event.price || 0).toLocaleString()}원` : '무료'
}

function submitSearch() {
  router.push({ path: '/events', query: searchText.value ? { q: searchText.value } : {} })
}

onMounted(() => {
  if (!eventStore.events.length) eventStore.fetchEvents()
  loadRuleBasedRecommendations()
})

function scrollCards(dir) {
  const el = trackRef.value
  if (!el) return
  el.scrollBy({ left: dir * el.clientWidth * 0.7, behavior: 'smooth' })
}

function onTrackScroll() {
  const el = trackRef.value
  if (!el || !el.children.length) return
  const cardWidth = el.children[0].offsetWidth + 18
  activeIndex.value = Math.round(el.scrollLeft / cardWidth)
}

const quickLinks = [
  {
    icon: '<svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2.5v3.2" /><path d="M12 18.3v3.2" /><path d="M2.5 12h3.2" /><path d="M18.3 12h3.2" /><path d="M5.4 5.4l2.3 2.3" /><path d="M16.3 16.3l2.3 2.3" /><path d="M5.4 18.6l2.3-2.3" /><path d="M16.3 7.7l2.3-2.3" /><circle cx="12" cy="12" r="2.6" /></svg>',
    title: '축제', sub: '계절을 즐기는 여행', tint: 'tint-warm'
  },
  {
    icon: '<svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="14" rx="2" /><circle cx="9" cy="10" r="2" /><path d="M21 15l-5-4-4 3.5-3-2L3 16" /></svg>',
    title: '전시', sub: '새로운 영감 발견', tint: 'tint-violet'
  },
  {
    icon: '<svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3Z" /><path d="M6 11a6 6 0 0 0 12 0" /><line x1="12" y1="19" x2="12" y2="22" /></svg>',
    title: '공연', sub: '가슴 뛰는 무대', tint: 'tint-teal'
  }
]
</script>

<style scoped>
.landing {
  color: var(--color-text-primary);
  background: #fff;
  font-family: var(--font-sans);
}

/* 히어로 */
.hero {
  position: relative;
}
.hero-card {
  position: relative;
  height: 520px;
  overflow: hidden;
  color: #fff;
}
.hero-image,
.hero-shade {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.hero-image {
  object-fit: cover;
}
.hero-shade {
  background: linear-gradient(100deg, rgba(7, 26, 54, 0.86), rgba(16, 42, 86, 0.5) 55%, rgba(16, 42, 86, 0.12));
}
.hero-copy {
  position: relative;
  z-index: 1;
  max-width: 560px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 max(24px, calc((100% - 1200px) / 2 + 24px));
}
.hero-copy h1 {
  font-size: 46px;
  line-height: 1.25;
  letter-spacing: -1.6px;
  margin: 0 0 14px;
}
.hero-copy > p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.86);
}
.hero-cta {
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  gap: 14px;
  margin-top: 26px;
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px) saturate(160%);
  -webkit-backdrop-filter: blur(14px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-weight: 700;
  font-size: 14.5px;
  padding: 14px 14px 14px 24px;
  border-radius: 999px;
  width: fit-content;
  box-shadow: 0 8px 24px -8px rgba(0, 0, 0, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.35);
  transition: var(--transition);
}
.hero-cta::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.hero-cta span {
  position: relative;
  display: grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #0322ab;
  font-size: 14px;
}
.hero-cta:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.24);
  box-shadow: 0 12px 28px -8px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.search-panel {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: -34px auto 0;
  height: 74px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 12px 0 26px;
  box-shadow: 0 20px 44px -14px rgba(3, 34, 171, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.6);
}
.search-icon {
  color: var(--color-text-muted);
  flex-shrink: 0;
}
.search-panel input {
  border: 0;
  outline: 0;
  flex: 1;
  font-size: 15.5px;
  font-family: var(--font-sans);
  color: var(--color-text-primary);
  background: transparent;
}
.search-panel input::placeholder {
  color: var(--color-text-muted);
}
.search-panel button {
  position: relative;
  overflow: hidden;
  border: 0;
  cursor: pointer;
  background: linear-gradient(135deg, #4a63ff, #0322ab);
  color: #fff;
  padding: 14px 26px;
  border-radius: 14px;
  font-size: 14.5px;
  font-weight: 600;
  font-family: var(--font-sans);
  box-shadow: 0 8px 18px -6px rgba(3, 34, 171, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.35);
  transition: var(--transition);
}
.search-panel button::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.35), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.search-panel button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px -6px rgba(3, 34, 171, 0.6), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 빠른 링크 */
.quick-wrap {
  max-width: 1100px;
  margin: 74px auto 0;
  padding: 0 24px;
}
.quick-grid {
  display: flex;
  justify-content: center;
  gap: 56px;
}
.quick {
  color: var(--color-text-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  transition: var(--transition);
}
.quick:hover .quick-icon {
  transform: translateY(-3px);
  box-shadow: 0 14px 28px -10px rgba(0, 0, 0, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.quick-icon {
  position: relative;
  overflow: hidden;
  width: 64px;
  height: 64px;
  border-radius: 22px;
  color: #fff;
  display: grid;
  place-items: center;
  box-shadow: 0 10px 22px -8px rgba(0, 0, 0, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: var(--transition);
}
.quick-icon::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.quick-icon > svg {
  position: relative;
  z-index: 1;
}
.quick-icon.tint-warm { background: linear-gradient(135deg, #ff9a8b, #ff5f6d); }
.quick-icon.tint-violet { background: linear-gradient(135deg, #b39ddb, #7c4dff); }
.quick-icon.tint-teal { background: linear-gradient(135deg, #7ee0d4, #26a69a); }
.quick b {
  font-size: 15.5px;
}
.quick small {
  color: var(--color-text-secondary);
  font-size: 12.5px;
}

/* 공통 섹션 */
.content-section,
.region-section {
  max-width: 1200px;
  margin: 96px auto;
  padding: 0 24px;
}
.heading {
  display: flex;
  align-items: end;
  justify-content: space-between;
  margin-bottom: 28px;
}
.heading h2,
.region-section h2 {
  font-size: 30px;
  letter-spacing: -1.4px;
  margin: 0;
}
.heading a {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
}

/* 추천 행사 카드 캐러셀 */
.card-carousel {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}
.card-track {
  flex: 1;
  display: flex;
  gap: 18px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scroll-behavior: smooth;
  padding-bottom: 4px;
  scrollbar-width: none;
}
.card-track::-webkit-scrollbar {
  display: none;
}
.feature-card {
  position: relative;
  flex: 0 0 auto;
  width: 260px;
  height: 360px;
  border-radius: 22px;
  overflow: hidden;
  scroll-snap-align: start;
  color: #fff;
  display: block;
  background: var(--color-bg-tertiary);
  transition: var(--transition);
  box-shadow: var(--shadow-md);
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.feature-card:hover .feature-card-photo {
  transform: scale(1.05);
}
.feature-card-photo {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}
.feature-card-overlay {
  position: absolute;
  inset: auto 0 0 0;
  padding: 20px 18px;
  background: linear-gradient(0deg, rgba(0, 0, 0, 0.72) 0%, rgba(0, 0, 0, 0.32) 60%, transparent 100%);
}
.feature-card-cat {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  opacity: 0.85;
}
.feature-card-overlay h3 {
  font-size: 18px;
  margin: 6px 0 4px;
  letter-spacing: -0.4px;
}
.feature-card-overlay p {
  font-size: 12.5px;
  opacity: 0.85;
}
.heart-btn {
  position: absolute;
  overflow: hidden;
  z-index: 1;
  top: 14px;
  right: 14px;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
  color: #fff;
  display: grid;
  place-items: center;
  transition: var(--transition);
}
.heart-btn::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.35), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.heart-btn > svg {
  position: relative;
  z-index: 1;
}
.heart-btn.liked {
  background: linear-gradient(135deg, #ff9a9e, #e0355b);
  color: #fff;
}

.carousel-arrow {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: #fff;
  color: var(--color-text-primary);
  display: grid;
  place-items: center;
  transition: var(--transition);
}
.carousel-arrow:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
}
.carousel-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-border-hover);
  transition: var(--transition);
}
.carousel-dots span.active {
  width: 18px;
  border-radius: 3px;
  background: var(--color-primary);
}

/* 클로징 태그라인 */
.region-section {
  background: var(--color-bg-secondary);
  max-width: none;
  margin: 0;
  min-height: 140px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}
.region-section h2 {
  margin: 0;
}
.region-section h2 {
  font-size: 28px;
  letter-spacing: -1.2px;
  margin: 0;
}

@media (max-width: 900px) {
  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 760px) {
  .hero-card { height: 460px; }
  .hero-copy { padding: 0 28px; max-width: 100%; }
  .hero-copy h1 { font-size: 34px; }
  .search-panel { width: calc(100% - 48px); height: auto; flex-wrap: wrap; padding: 16px; border-radius: 18px; }
  .search-panel input { min-width: 0; width: 100%; order: 1; padding: 10px 0; }
  .search-panel button { order: 2; width: 100%; text-align: center; }
  .quick-wrap { margin-top: 56px; }
  .content-section { margin: 64px auto; }
  .region-section h2 { font-size: 22px; }
  .feature-card { width: 210px; height: 300px; }
  .carousel-arrow { display: none; }
}
</style>
