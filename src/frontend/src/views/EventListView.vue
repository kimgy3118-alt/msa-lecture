<template>
  <div class="events-page">
    <main class="events-shell">
      <section class="page-heading">
        <p>행사 찾기</p>
        <h1>{{ selectedCategory }}</h1>
      </section>

      <form class="search-bar" @submit.prevent>
        <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
        <input v-model.trim="searchText" placeholder="행사명, 장소로 검색해 보세요" />
        <button v-if="searchText" type="button" class="clear-btn" @click="searchText = ''" aria-label="검색어 지우기">✕</button>
      </form>

      <section class="filter-bar">
        <div class="filter-row">
          <span class="filter-label">분류</span>
          <div class="chip-scroll">
            <button v-for="cat in categories" :key="cat" :class="{ active: selectedCategory === cat }" @click="selectCategory(cat)">{{ cat }}</button>
          </div>
        </div>
      </section>

      <div class="result-toolbar">
        <strong>총 <em>{{ sortedEvents.length.toLocaleString() }}</em>건</strong>
        <div class="sort-tabs">
          <button v-for="opt in sortOptions" :key="opt.value" :class="{ active: sortBy === opt.value }" @click="sortBy = opt.value">{{ opt.label }}</button>
        </div>
        <router-link v-if="isOrganizer" to="/events/new" class="create-link">행사 등록 +</router-link>
      </div>

      <div v-if="loading" class="grid-loading">
        <i v-for="i in 8" :key="i"></i>
      </div>

      <div v-else-if="sortedEvents.length" class="event-grid">
        <router-link v-for="event in sortedEvents" :key="event.id" :to="'/events/' + event.id" class="event-card">
          <div class="event-card-photo">
            <img v-if="event.imageUrl" :src="event.imageUrl" :alt="event.title" />
            <span v-else class="event-card-fallback">{{ categorySymbol(event.category) }}</span>
            <span class="event-card-badge">{{ event.category || "행사" }}</span>
            <span class="event-card-dday" :class="{ ended: isEnded(event.eventStartAt) }">{{ ddayLabel(event.eventStartAt) }}</span>
          </div>
          <div class="event-card-body">
            <h2>{{ event.title }}</h2>
            <p>⌖ {{ event.venue || "장소 추후 공지" }}</p>
            <div class="event-card-bottom">
              <span>{{ dateLabel(event.eventStartAt) }}</span>
              <b>{{ priceLabel(event) }}</b>
            </div>
          </div>
        </router-link>
      </div>

      <div v-else class="empty">
        <b>아직 등록된 행사가 없어요.</b>
        <p>새로운 행사가 곧 찾아옵니다.</p>
        <router-link v-if="isOrganizer" to="/events/new">첫 행사 등록하기</router-link>
      </div>
    </main>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from "vue"
import { useRoute } from "vue-router"
import { storeToRefs } from "pinia"
import { useEventStore } from "@/store/event.js"
import { useAuthStore } from "@/store/auth.js"
const route = useRoute()
const eventStore = useEventStore()
const auth = useAuthStore()
const { loading } = storeToRefs(eventStore)
const categories = eventStore.categories
const selectedCategory = computed(() => eventStore.selectedCategory)
const searchText = ref(typeof route.query.q === "string" ? route.query.q : "")
const isOrganizer = computed(() => auth.user?.role === "ADMIN")
const filteredEvents = computed(() => {
  const keyword = searchText.value.trim().toLowerCase()
  const byCategory = !Array.isArray(eventStore.events) ? [] : selectedCategory.value === "전체" ? eventStore.events : eventStore.events.filter(e => e.category === selectedCategory.value)
  return byCategory.filter(e => !keyword || `${e.title ?? ""} ${e.venue ?? ""} ${e.description ?? ""}`.toLowerCase().includes(keyword))
})
const sortOptions = [
  { value: "latest", label: "최신순" },
  { value: "deadline", label: "마감 임박순" },
  { value: "popular", label: "인기순" }
]
const sortBy = ref("latest")
const sortedEvents = computed(() => {
  const list = [...filteredEvents.value]
  if (sortBy.value === "deadline") {
    return list.sort((a, b) => new Date(a.registrationEndAt ?? a.eventStartAt ?? 0) - new Date(b.registrationEndAt ?? b.eventStartAt ?? 0))
  }
  if (sortBy.value === "popular") {
    return list.sort((a, b) => Number(b.reservationCount ?? 0) - Number(a.reservationCount ?? 0))
  }
  return list.sort((a, b) => new Date(b.createdAt ?? b.eventStartAt ?? 0) - new Date(a.createdAt ?? a.eventStartAt ?? 0))
})
const symbols = { 축제:"✦", 전시:"▣", 공연:"♪", "문화 체험":"◌", 스포츠:"●", 교육:"✎", 기타:"◇" }
function categorySymbol(category) { return symbols[category] || "◇" }
function selectCategory(cat) { eventStore.setCategory(cat) }
function dateLabel(value) { return value ? new Intl.DateTimeFormat("ko-KR", { year:"numeric", month:"long", day:"numeric" }).format(new Date(value)) : "일정 추후 공지" }
function priceLabel(event) { return event.eventType === "PAID_RESERVATION" ? Number(event.price || 0).toLocaleString() + "원" : "무료" }
function ddayLabel(eventStartAt) {
  if (!eventStartAt) return "일정 미정"
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const target = new Date(eventStartAt); target.setHours(0, 0, 0, 0)
  const diffDays = Math.round((target - today) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return "D-DAY"
  if (diffDays > 0) return `D-${diffDays}`
  return "종료된 행사"
}
function isEnded(eventStartAt) {
  if (!eventStartAt) return false
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const target = new Date(eventStartAt); target.setHours(0, 0, 0, 0)
  return target < today
}
onMounted(() => {
  if (typeof route.query.category === "string" && eventStore.categories.includes(route.query.category)) {
    eventStore.setCategory(route.query.category)
  }
  eventStore.fetchEvents()
})
</script>
<style scoped>
.events-page{min-height:100vh;background:#fff;color:#18263d}
.events-shell{max-width:1200px;margin:0 auto;padding:32px 24px 100px}
.page-heading{padding-bottom:32px;border-bottom:1px solid #e5e9f0}
.page-heading p{margin:0 0 10px;color:#667892;font-size:16px}
.page-heading h1{margin:0;font-size:42px;letter-spacing:-2.4px}

.search-bar{margin-top:26px;display:flex;align-items:center;gap:10px;background:#f5f7fa;border:1px solid #e5e9f0;border-radius:14px;padding:12px 16px;color:#8893a3}
.search-bar input{flex:1;border:0;outline:0;background:transparent;font:inherit;font-size:14.5px;color:#18263d}
.search-bar input::placeholder{color:#9aa4b2}
.clear-btn{border:0;background:none;color:#8893a3;cursor:pointer;font-size:14px;padding:2px}

.filter-bar{display:flex;flex-direction:column;gap:14px;padding:26px 0;border-bottom:1px solid #e5e9f0}
.filter-row{display:flex;align-items:center;gap:18px}
.filter-label{flex-shrink:0;width:36px;font-size:13px;font-weight:700;color:#8893a3}
.chip-scroll{display:flex;gap:10px;flex-wrap:wrap}
.chip-scroll button{border:1px solid #dfe4ea;background:#fff;color:#536176;font:inherit;font-size:13.5px;padding:8px 14px;border-radius:999px;cursor:pointer;transition:.2s;white-space:nowrap}
.chip-scroll button:hover{border-color:#0322ab;color:#0322ab}
.chip-scroll button.active{background:transparent;color:#0322ab;font-weight:700;border:1.5px solid #0322ab}

.result-toolbar{margin-top:26px;padding-bottom:18px;display:flex;align-items:center;justify-content:space-between;gap:16px}
.result-toolbar strong{font-size:19px}
.result-toolbar em{font-style:normal;color:#174fbd}
.sort-tabs{display:flex;gap:20px;margin-right:auto;margin-left:32px}
.sort-tabs button{font:inherit;font-size:14px;font-weight:600;border:0;background:none;color:#748094;padding:0 0 8px;cursor:pointer}
.sort-tabs button.active{color:#0322ab;border-bottom:3px solid #0322ab}
.create-link{font-size:13px;font-weight:700;color:#0322ab}

.event-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:20px}
.event-card{display:block;color:inherit;border-radius:18px;overflow:hidden;background:#fff;border:1px solid #e5e9f0;transition:.2s}
.event-card:hover{transform:translateY(-4px);box-shadow:0 16px 32px rgba(24,38,63,.12);border-color:transparent}
.event-card-photo{position:relative;height:170px;background:#0322ab;display:grid;place-items:center;overflow:hidden}
.event-card-photo img{width:100%;height:100%;object-fit:cover}
.event-card-fallback{font-size:42px;color:#fff8e9}
.event-card-badge{position:absolute;left:11px;top:11px;background:rgba(255,255,255,.92);border-radius:999px;padding:5px 10px;font-size:11px;font-weight:700;color:#18263d}
.event-card-dday{position:absolute;left:11px;bottom:11px;background:rgba(3,34,171,.88);border-radius:999px;padding:5px 10px;font-size:11px;font-weight:700;color:#fff}
.event-card-dday.ended{background:rgba(107,114,128,.85)}
.heart-btn{position:absolute;overflow:hidden;right:11px;top:11px;width:30px;height:30px;border:0;border-radius:11px;background:linear-gradient(135deg,rgba(255,255,255,.95),rgba(255,255,255,.75));color:#18263d;display:grid;place-items:center;cursor:pointer;transition:.2s;box-shadow:0 6px 14px -6px rgba(0,0,0,.35),inset 0 1px 0 rgba(255,255,255,.6)}
.heart-btn::after{content:"";position:absolute;inset:0;background:linear-gradient(135deg,rgba(255,255,255,.5),rgba(255,255,255,0) 55%);pointer-events:none}
.heart-btn > svg{position:relative;z-index:1}
.heart-btn.liked{background:linear-gradient(135deg,#ff9a9e,#e0355b);color:#fff}
.event-card-body{padding:16px}
.event-card-body h2{margin:0 0 8px;font-size:15.5px;letter-spacing:-.3px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.event-card-body p{margin:0 0 14px;color:#727b77;font-size:12.5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.event-card-bottom{border-top:1px solid #eef1f5;padding-top:12px;display:flex;align-items:center;justify-content:space-between;font-size:12px;color:#7f8884}
.event-card-bottom b{font-size:13.5px;color:#0322ab}

.grid-loading{display:grid;grid-template-columns:repeat(4,1fr);gap:20px;margin-top:26px}
.grid-loading i{display:block;height:260px;border-radius:18px;background:linear-gradient(100deg,#edf2fa,#fff,#edf2fa)}

.empty{margin-top:26px;text-align:center;padding:100px 20px;background:#f7f9fc;border-radius:22px;color:#536176}
.empty p{font-size:14px}
.empty a{display:inline-block;margin-top:12px;padding:11px 17px;background:#0322ab;color:#fff;border-radius:999px;text-decoration:none;font-size:13px}

@media(max-width:1024px){.event-grid,.grid-loading{grid-template-columns:repeat(3,1fr)}}
@media(max-width:760px){.event-grid,.grid-loading{grid-template-columns:repeat(2,1fr)}.filter-row{align-items:flex-start}.sort-tabs{margin-left:0}}
@media(max-width:620px){.events-shell{padding:42px 18px 70px}.page-heading h1{font-size:34px}.result-toolbar{flex-wrap:wrap}.event-grid,.grid-loading{grid-template-columns:1fr 1fr}}
</style>
