<template>
  <router-link :to="`/events/${event.id}`" class="event-card">
    <div class="card-thumb" :class="`theme-${event.category || 'OTHER'}`"><img v-if="imageSrc" :src="imageSrc" :alt="event.title" class="thumb-img" /><span v-else class="thumb-placeholder">{{ categoryIcon }}</span><span class="type-chip">{{ typeLabel }}</span></div>
    <div class="card-body"><p class="category">{{ categoryLabel }}</p><h3>{{ event.title }}</h3><p class="place">⌖ {{ event.venue || '장소 추후 공지' }}</p><div class="card-bottom"><span>{{ dateLabel }}</span><strong>{{ priceLabel }}</strong></div></div>
  </router-link>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({ event: { type: Object, required: true } })
const categories={FESTIVAL:['축제','✦'],EXHIBITION:['전시','◫'],PERFORMANCE:['공연','♪'],CULTURE_EXPERIENCE:['문화 체험','◎'],SPORTS:['스포츠','◌'],EDUCATION:['교육','✎'],OTHER:['기타','◈']}
const categoryLabel=computed(()=>categories[props.event.category]?.[0] || props.event.category || '행사'); const categoryIcon=computed(()=>categories[props.event.category]?.[1] || '◈'); const imageSrc=computed(()=>props.event.imageUrl || null)
const typeLabel=computed(()=>({FREE_VISIT:'자유 방문',FREE_RESERVATION:'무료 예약',PAID_RESERVATION:'유료 예약'})[props.event.eventType] || '행사'); const priceLabel=computed(()=>props.event.eventType==='FREE_VISIT'||props.event.eventType==='FREE_RESERVATION'?'무료':`${Number(props.event.price||0).toLocaleString()}원`); const dateLabel=computed(()=>props.event.eventStartAt?new Intl.DateTimeFormat('ko-KR',{month:'long',day:'numeric'}).format(new Date(props.event.eventStartAt)):'일정 추후 공지')
</script>
<style scoped>
.event-card{display:block;background:#fff;border-radius:20px;overflow:hidden;color:#202a2b;text-decoration:none;border:1px solid #dce5f1;box-shadow:0 5px 16px rgba(37,42,31,.04);transition:.22s}.event-card:hover{transform:translateY(-4px);box-shadow:0 16px 32px rgba(37,42,31,.13)}.card-thumb{height:190px;position:relative;display:grid;place-items:center;overflow:hidden}.thumb-img{width:100%;height:100%;object-fit:cover}.thumb-placeholder{font-size:54px;color:#fff8e9}.theme-FESTIVAL{background:#d56735}.theme-EXHIBITION{background:#607c72}.theme-PERFORMANCE{background:#7b4d58}.theme-CULTURE_EXPERIENCE{background:#bb8035}.theme-SPORTS{background:#416f8e}.theme-EDUCATION{background:#7466a1}.theme-OTHER{background:#77847c}.type-chip{position:absolute;left:13px;top:13px;background:rgba(255,255,255,.9);border-radius:999px;padding:6px 9px;font-size:11px;font-weight:700}.card-body{padding:18px}.category{font-size:12px;color:#174fbd;font-weight:800;margin:0 0 8px}.card-body h3{margin:0;font-size:17px;line-height:1.4;letter-spacing:-.5px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.place{margin:10px 0 17px;color:#727b77;font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.card-bottom{border-top:1px solid #dce5f1;padding-top:13px;display:flex;align-items:center;justify-content:space-between;font-size:12px;color:#7f8884}.card-bottom strong{font-size:14px;color:#0322ab}
</style>
