<template>
  <main class="landing">
    <section class="hero">
      <div class="hero-card">
        <img class="hero-image" src="@/assets/images/hero/korea-festival-hero.png" alt="한국 문화 축제 풍경" />
        <div class="hero-shade"></div>
        <div class="hero-copy">
          <span class="hero-badge">전국 행사 큐레이션</span>
          <h1>어디로, 어떤 행사에<br />참여해 볼까요?</h1>
          <p>전국 곳곳의 특별한 행사를 갈래에서 만나보세요.</p>
          <router-link to="/events" class="hero-cta">행사 찾기 <span>→</span></router-link>
        </div>
      </div>

      <div class="search-panel">
        <svg class="search-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
        <input placeholder="어디로, 어떤 행사를 찾고 있나요?" />
        <router-link to="/events">검색</router-link>
      </div>
    </section>

    <section class="quick-wrap">
      <div class="quick-grid">
        <router-link v-for="item in quickLinks" :key="item.title" to="/events" class="quick">
          <span class="quick-icon" v-html="item.icon"></span>
          <b>{{ item.title }}</b>
          <small>{{ item.sub }}</small>
        </router-link>
      </div>
    </section>

    <section class="content-section">
      <div class="heading">
        <div>
          <p>CURATION</p>
          <h2>갈래가 추천하는 특별한 행사</h2>
        </div>
        <router-link to="/events">전체 보기 →</router-link>
      </div>

      <div class="card-carousel">
        <button class="carousel-arrow prev" @click="scrollCards(-1)" aria-label="이전 카드">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 18l-6-6 6-6" /></svg>
        </button>

        <div class="card-track" ref="trackRef" @scroll="onTrackScroll">
          <router-link
            v-for="(event, i) in featuredEvents"
            :key="event.title"
            to="/events"
            class="feature-card"
            :class="event.tone"
          >
            <button class="heart-btn" :class="{ liked: likes[i] }" @click.prevent="toggleLike(i)" aria-label="찜하기">
              <svg viewBox="0 0 24 24" width="16" height="16" :fill="likes[i] ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M12 21s-7.5-4.6-10-9.3C.5 8 2 4.5 5.5 4c2-.3 3.8.6 6.5 3.4C14.7 4.6 16.5 3.7 18.5 4 22 4.5 23.5 8 22 11.7 19.5 16.4 12 21 12 21Z" /></svg>
            </button>
            <div class="feature-card-art">{{ event.icon }}</div>
            <div class="feature-card-overlay">
              <span class="feature-card-cat">{{ event.category }}</span>
              <h3>{{ event.title }}</h3>
              <p>⌖ {{ event.place }} · {{ event.price }}</p>
            </div>
          </router-link>
        </div>

        <button class="carousel-arrow next" @click="scrollCards(1)" aria-label="다음 카드">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 6l6 6-6 6" /></svg>
        </button>
      </div>

      <div class="carousel-dots">
        <span v-for="(event, i) in featuredEvents" :key="i" :class="{ active: activeIndex === i }"></span>
      </div>
    </section>

    <section class="region-section">
      <div>
        <p>REGION</p>
        <h2>이번 주말, 어디로 갈래요?</h2>
      </div>
      <div class="region-list">
        <router-link v-for="region in regions" :key="region" to="/events">{{ region }} <span>↗</span></router-link>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'

const trackRef = ref(null)
const activeIndex = ref(0)
const likes = ref({})

function toggleLike(i) {
  likes.value = { ...likes.value, [i]: !likes.value[i] }
}

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
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20l4.5-4.5" /><path d="M14.5 3.5a2 2 0 0 1 2.83 0l3.17 3.17a2 2 0 0 1 0 2.83L10 20 4 21l1-6L14.5 3.5Z" /><path d="M13 5l6 6" /></svg>',
    title: '축제', sub: '계절을 즐기는 여행'
  },
  {
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="14" rx="2" /><circle cx="9" cy="10" r="2" /><path d="M21 15l-5-4-4 3.5-3-2L3 16" /></svg>',
    title: '전시', sub: '새로운 영감 발견'
  },
  {
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3a3 3 0 0 1 3 3v6a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3Z" /><path d="M6 11a6 6 0 0 0 12 0" /><line x1="12" y1="19" x2="12" y2="22" /></svg>',
    title: '공연', sub: '가슴 뛰는 무대'
  },
  {
    icon: '<svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12a8 8 0 1 1 8 8" /><path d="M4 12l4 4m-4-4l4-4" /></svg>',
    title: '문화 체험', sub: '직접 만드는 추억'
  }
]
const featuredEvents = [
  { title: '서울 세계 불꽃축제', category: 'FESTIVAL', region: 'SEOUL', place: '여의도 한강공원', price: '무료', icon: '🎆', tone: 'night' },
  { title: '국립현대미술관 특별전', category: 'EXHIBITION', region: 'SEOUL', place: '국립현대미술관 서울', price: '₩5,000', icon: '🖼️', tone: 'pink' },
  { title: '부산 바다 불빛 축제', category: 'FESTIVAL', region: 'BUSAN', place: '광안리 해수욕장', price: '무료', icon: '🌊', tone: 'blue' },
  { title: '전주 한옥마을 전통 체험', category: 'CULTURE', region: 'JEONJU', place: '전주 한옥마을', price: '₩12,000', icon: '🏮', tone: 'gold' },
  { title: '대구 국제 재즈 페스티벌', category: 'PERFORMANCE', region: 'DAEGU', place: '수성못 야외무대', price: '₩8,000', icon: '🎷', tone: 'purple' },
  { title: '제주 감귤 마을 체험', category: 'CULTURE', region: 'JEJU', place: '서귀포 감귤 농원', price: '₩15,000', icon: '🍊', tone: 'orange' }
]
const regions = ['서울', '부산', '강원', '전주', '경주', '제주']
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
.hero-badge {
  display: inline-flex;
  width: fit-content;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.02em;
  margin-bottom: 18px;
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
  display: inline-flex;
  align-items: center;
  gap: 14px;
  margin-top: 26px;
  color: #0322ab;
  background: #fff;
  font-weight: 700;
  font-size: 14.5px;
  padding: 14px 22px;
  border-radius: 999px;
  width: fit-content;
  transition: var(--transition);
}
.hero-cta:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.search-panel {
  position: relative;
  z-index: 2;
  max-width: 900px;
  margin: -34px auto 0;
  height: 74px;
  background: #fff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 12px 0 26px;
  box-shadow: var(--shadow-lg);
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
.search-panel a {
  background: var(--color-primary);
  color: #fff;
  padding: 14px 26px;
  border-radius: 14px;
  font-size: 14.5px;
  font-weight: 600;
  transition: var(--transition);
}
.search-panel a:hover {
  background: var(--color-primary-dark);
}

/* 빠른 링크 */
.quick-wrap {
  max-width: 1100px;
  margin: 74px auto 0;
  padding: 0 24px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border: 1px solid var(--color-border);
  border-radius: 22px;
  overflow: hidden;
}
.quick {
  padding: 26px;
  color: var(--color-text-primary);
  border-right: 1px solid var(--color-border);
  display: grid;
  gap: 8px;
  background: #fff;
  transition: var(--transition);
}
.quick:hover {
  background: var(--color-bg-secondary);
}
.quick:last-child {
  border-right: 0;
}
.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-primary);
  display: grid;
  place-items: center;
  margin-bottom: 4px;
}
.quick b {
  font-size: 16.5px;
}
.quick small {
  color: var(--color-text-secondary);
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
.heading p,
.region-section p {
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  margin: 0;
}
.heading h2,
.region-section h2 {
  font-size: 30px;
  letter-spacing: -1.4px;
  margin: 7px 0 0;
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
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
  box-shadow: var(--shadow-md);
}
.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.feature-card-art {
  font-size: 72px;
  opacity: 0.9;
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
  z-index: 1;
  top: 14px;
  right: 14px;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(4px);
  color: #fff;
  display: grid;
  place-items: center;
  transition: var(--transition);
}
.heart-btn.liked {
  background: #fff;
  color: #e0355b;
}
.night { background: linear-gradient(160deg, #1d274d, #0322ab); }
.pink { background: linear-gradient(160deg, #c98fa0, #7a4a63); }
.blue { background: linear-gradient(160deg, #4facd6, #14507e); }
.gold { background: linear-gradient(160deg, #e8c675, #a9762f); }
.purple { background: linear-gradient(160deg, #9c8fd6, #4a3a8f); }
.orange { background: linear-gradient(160deg, #f5a35c, #c15a1f); }

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

/* 지역 */
.region-section {
  background: var(--color-bg-secondary);
  max-width: none;
  padding: 64px calc((100% - 1152px) / 2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 0;
}
.region-list {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  max-width: 600px;
}
.region-list a {
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
  padding: 13px 18px;
  background: #fff;
  border-radius: 999px;
  font-size: 14.5px;
  font-weight: 600;
  transition: var(--transition);
}
.region-list a:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.region-list span {
  color: var(--color-primary);
  margin-left: 10px;
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
  .search-panel a { order: 2; width: 100%; text-align: center; }
  .quick-wrap { margin-top: 56px; }
  .content-section, .region-section { margin: 64px auto; }
  .region-section { padding: 48px 24px; display: block; }
  .region-list { margin-top: 24px; }
  .feature-card { width: 210px; height: 300px; }
  .carousel-arrow { display: none; }
}
</style>
