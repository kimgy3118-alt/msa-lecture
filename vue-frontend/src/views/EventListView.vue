<template>
  <div class="events-page">
    <main class="events-shell">
      <section class="page-heading"><p>행사 찾기</p><h1>{{ selectedCategory }}</h1></section>
      <div class="explore-layout">
        <section class="event-results">
          <div class="result-toolbar"><strong>총 <em>{{ regionFilteredEvents.length.toLocaleString() }}</em>건</strong><div class="sort-tabs"><button class="active">최신순</button><button>마감 임박순</button><button>인기순</button></div></div>
          <div v-if="loading" class="list-loading"><i v-for="i in 4" :key="i"></i></div>
          <div v-else-if="regionFilteredEvents.length" class="event-list"><router-link v-for="event in regionFilteredEvents" :key="event.id" :to="'/events/' + event.id" class="event-row"><div class="row-image"><img v-if="event.imageUrl" :src="event.imageUrl" :alt="event.title"><span v-else>{{ categorySymbol(event.category) }}</span></div><div class="row-copy"><p>{{ event.category || "행사" }}</p><h2>{{ event.title }}</h2><span>{{ event.venue || "장소 추후 공지" }} · {{ dateLabel(event.eventStartAt) }}</span><small>{{ event.description || "전국 곳곳에서 만나는 특별한 행사입니다." }}</small></div><b class="row-price">{{ priceLabel(event) }}</b></router-link></div>
          <div v-else class="empty"><b>아직 등록된 행사가 없어요.</b><p>새로운 행사가 곧 찾아옵니다.</p><router-link v-if="isOrganizer" to="/events/new">첫 행사 등록하기</router-link></div>
        </section>
        <aside class="filter-panel"><div class="filter-top"><b>행사 분류</b><router-link v-if="isOrganizer" to="/events/new">행사 등록</router-link></div><div class="filter-chips"><button v-for="cat in categories" :key="cat" :class="{active:selectedCategory===cat}" @click="selectCategory(cat)">{{ cat }}</button></div><hr><b>지역</b><div class="region-chips"><button v-for="region in regions" :key="region" :class="{active:selectedRegion===region}" @click="selectRegion(region)">{{ region }}</button></div></aside>
      </div>
    </main>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from "vue"
import { storeToRefs } from "pinia"
import { useEventStore } from "@/store/event.js"
import { useAuthStore } from "@/store/auth.js"
const eventStore = useEventStore()
const auth = useAuthStore()
const { loading } = storeToRefs(eventStore)
const categories = eventStore.categories
const selectedCategory = computed(() => eventStore.selectedCategory)
const selectedRegion = ref("전체")
const isOrganizer = computed(() => auth.user?.role === "ADMIN")
const filteredEvents = computed(() => !Array.isArray(eventStore.events) ? [] : selectedCategory.value === "전체" ? eventStore.events : eventStore.events.filter(e => e.category === selectedCategory.value))
const regionFilteredEvents = computed(() => filteredEvents.value.filter(e => selectedRegion.value === "전체" ? true : String(e.venue ?? "").includes(selectedRegion.value)))
const regions = ["전체","서울","부산","대구","인천","광주","대전","울산","세종","경기","강원","충북","충남","전북","전남","경북","경남","제주"]
const symbols = { 축제:"✦", 전시:"▣", 공연:"♪", "문화 체험":"◌", 스포츠:"●", 교육:"✎", 기타:"◇" }
function categorySymbol(category) { return symbols[category] || "◇" }
function selectCategory(cat) { eventStore.setCategory(cat) }
function selectRegion(region) { selectedRegion.value = region }
function dateLabel(value) { return value ? new Intl.DateTimeFormat("ko-KR", { year:"numeric", month:"long", day:"numeric" }).format(new Date(value)) : "일정 추후 공지" }
function priceLabel(event) { return event.eventType === "PAID_RESERVATION" ? Number(event.price || 0).toLocaleString() + "원" : "무료" }
onMounted(() => eventStore.fetchEvents())
</script>
<style scoped>
.events-page{min-height:100vh;background:#fff;color:#18263d}.events-shell{max-width:1200px;margin:0 auto;padding:72px 24px 100px}.page-heading{padding-bottom:42px;border-bottom:1px solid #bac6d7}.page-heading p{margin:0 0 10px;color:#667892;font-size:18px}.page-heading h1{margin:0;font-size:46px;letter-spacing:-2.8px}.explore-layout{display:grid;grid-template-columns:minmax(0,1fr) 325px;gap:64px;padding-top:30px}.result-toolbar{height:62px;display:flex;align-items:flex-start;justify-content:space-between;border-bottom:1px solid #dce5f1}.result-toolbar strong{font-size:21px}.result-toolbar em{font-style:normal;color:#174fbd}.sort-tabs{display:flex;gap:22px}.sort-tabs button{font:inherit;font-size:15px;font-weight:600;border:0;background:none;color:#748094;padding:0 0 10px;cursor:pointer}.sort-tabs button.active{color:#0322ab;border-bottom:3px solid #0322ab}.event-list{display:grid}.event-row{display:grid;grid-template-columns:210px minmax(0,1fr) auto;gap:24px;padding:22px 0;border-bottom:1px solid #dce5f1;text-decoration:none;color:inherit;transition:background .2s}.event-row:hover{background:#f5f8fd}.row-image{height:132px;border-radius:14px;overflow:hidden;background:#0322ab;display:grid;place-items:center;color:#fff;font-size:42px}.row-image img{width:100%;height:100%;object-fit:cover}.row-copy{min-width:0;display:flex;flex-direction:column;justify-content:center}.row-copy p{margin:0 0 7px;color:#174fbd;font-size:13px;font-weight:700}.row-copy h2{margin:0 0 7px;font-size:22px;letter-spacing:-1.1px}.row-copy span,.row-copy small{color:#68778b;font-size:14px;line-height:1.5}.row-copy small{margin-top:5px;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:1;overflow:hidden}.row-price{align-self:center;color:#0322ab;font-size:15px;white-space:nowrap}.filter-panel{align-self:start;background:#f7f9fc;border:1px solid #e1e8f2;border-radius:22px;padding:28px}.filter-panel b{font-size:16px}.filter-top{display:flex;justify-content:space-between;align-items:center}.filter-top a{font-size:13px;color:#0322ab;font-weight:700}.filter-chips,.region-chips{display:flex;gap:12px;row-gap:14px;flex-wrap:wrap;margin-top:22px}.filter-panel button{border:1px solid #dfe4ea;background:#fff;color:#536176;font:inherit;font-size:13.5px;padding:8px 14px;border-radius:999px;cursor:pointer;transition:.2s}.filter-panel button:hover{border-color:#0322ab;color:#0322ab}.filter-panel button.active{background:transparent;color:#0322ab;font-weight:700;border:1.5px solid #0322ab}.filter-panel hr{border:0;border-top:1px solid #dce5f1;margin:30px 0}.list-loading{display:grid;gap:20px;padding-top:22px}.list-loading i{display:block;height:132px;border-radius:14px;background:linear-gradient(100deg,#edf2fa,#fff,#edf2fa)}.empty{text-align:center;padding:100px 20px;background:#f7f9fc;border-radius:22px;color:#536176}.empty p{font-size:14px}.empty a{display:inline-block;margin-top:12px;padding:11px 17px;background:#0322ab;color:#fff;border-radius:999px;text-decoration:none;font-size:13px}@media(max-width:900px){.explore-layout{grid-template-columns:1fr;gap:30px}.filter-panel{order:-1}.page-heading{padding-bottom:28px}}@media(max-width:620px){.events-shell{padding:42px 18px 70px}.page-heading h1{font-size:36px}.event-row{grid-template-columns:110px 1fr;gap:15px}.row-image{height:100px}.row-price{display:none}.row-copy h2{font-size:18px}.sort-tabs{gap:12px}.sort-tabs button{font-size:13px}}
</style>
