<template>
  <div class="page-wrapper">
    <div class="page-shell">
      <section class="page-heading">
        <p>행사 등록</p>
        <h1>새로운 행사를 등록해 주세요</h1>
      </section>

      <main class="main-content">
        <div class="form-card">
          <form class="event-form" @submit.prevent="handleSubmit">
            <section class="photo-section">
              <div class="section-heading"><span>대표 이미지</span><small>권장 비율 16:9 · JPG, PNG, WEBP (최대 5MB)</small></div>
              <label class="photo-dropzone" :class="{ populated: photoPreview }" for="eventImage">
                <img v-if="photoPreview" :src="photoPreview" alt="선택한 행사 대표 이미지 미리보기" />
                <div v-else class="photo-empty"><span class="photo-plus">+</span><strong>행사를 돋보이게 할 사진을 올려주세요</strong><span>클릭해서 파일 선택</span></div>
                <span v-if="photoPreview" class="photo-change">사진 변경</span>
              </label>
              <input id="eventImage" class="sr-only" type="file" accept="image/png,image/jpeg,image/webp" @change="handleImageChange" />
              <button v-if="photoPreview" type="button" class="photo-remove" @click="removeImage">사진 삭제</button>
              <p v-if="imageError" class="image-error">{{ imageError }}</p>
            </section>
            <div class="form-group">
              <label class="form-label" for="title">행사명</label>
              <input
                id="title"
                v-model.trim="form.title"
                type="text"
                class="form-input"
                placeholder="예: 홀랑이랑 스칼라투어"
                maxlength="100"
              />
            </div>

            <div class="form-group">
              <label class="form-label" for="description">행사 설명</label>
              <textarea
                id="description"
                v-model.trim="form.description"
                class="form-textarea"
                rows="6"
                placeholder="행사 소개, 프로그램, 참여 대상 등을 입력해 주세요."
              ></textarea>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="category">카테고리</label>
                <select id="category" v-model="form.category" class="form-select">
                  <option disabled value="">카테고리를 선택하세요</option>
                  <option
                    v-for="option in categoryOptions"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ option.label }}
                  </option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label" for="eventType">운영 유형</label>
                <select id="eventType" v-model="form.eventType" class="form-select">
                  <option value="FREE_VISIT">자유 방문형 (예약·결제 없음)</option>
                  <option value="FREE_RESERVATION">무료 예약형</option>
                  <option value="PAID_RESERVATION">유료 예약형</option>
                </select>
              </div>

              <div class="form-group">
                <label class="form-label" for="price">가격</label>
                <input
                  id="price"
                  v-model.number="form.price"
                  type="number"
                  min="0"
                  step="1000"
                  class="form-input"
                  placeholder="예: 50000"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label" for="venue">행사 장소</label>
                <input id="venue" v-model.trim="form.venue" class="form-input" placeholder="예: 서울시립미술관" />
              </div>
              <div class="form-group">
                <label class="form-label" for="organizerName">주관 기관명</label>
                <input id="organizerName" v-model.trim="form.organizerName" class="form-input" placeholder="예: 서울특별시" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group"><label class="form-label" for="eventStartAt">행사 시작</label><input id="eventStartAt" v-model="form.eventStartAt" type="datetime-local" class="form-input" /></div>
              <div class="form-group"><label class="form-label" for="eventEndAt">행사 종료</label><input id="eventEndAt" v-model="form.eventEndAt" type="datetime-local" class="form-input" /></div>
            </div>
            <div class="form-row" v-if="form.eventType !== 'FREE_VISIT'">
              <div class="form-group"><label class="form-label" for="registrationStartAt">예약 시작</label><input id="registrationStartAt" v-model="form.registrationStartAt" type="datetime-local" class="form-input" /></div>
              <div class="form-group"><label class="form-label" for="registrationEndAt">예약 종료</label><input id="registrationEndAt" v-model="form.registrationEndAt" type="datetime-local" class="form-input" /></div>
            </div>
            <div class="form-group" v-if="form.eventType !== 'FREE_VISIT'"><label class="form-label" for="capacity">예약 정원</label><input id="capacity" v-model.number="form.capacity" type="number" min="1" class="form-input" /></div>

            <div v-if="validationError" class="error-box">
              {{ validationError }}
            </div>

            <div v-if="submitError" class="error-box">
              {{ submitError }}
            </div>

            <div v-if="submitSuccess" class="success-box">
              {{ submitSuccess }}
            </div>

            <div class="form-actions">
              <router-link to="/events" class="btn btn-ghost">
                취소
              </router-link>

              <button type="submit" class="btn btn-primary" :disabled="submitting">
                <span v-if="submitting">등록 중...</span>
                <span v-else>행사 등록</span>
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue"
import { useRouter } from 'vue-router'
import { eventApi } from '@/api/event.js'
import { useAuthStore } from '@/store/auth.js'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  title: '',
  description: '',
  category: '',
  eventType: 'FREE_VISIT',
  venue: '',
  organizerName: '',
  imageUrl: "",
  price: 0,
  eventStartAt: '',
  eventEndAt: '',
  registrationStartAt: '',
  registrationEndAt: '',
  capacity: 100
})

const photoPreview = ref("")
const imageError = ref("")
const submitting = ref(false)
const validationError = ref('')
const submitError = ref('')
const submitSuccess = ref('')

const categoryOptions = [
  { label: '축제', value: 'FESTIVAL' }, { label: '전시', value: 'EXHIBITION' },
  { label: '공연', value: 'PERFORMANCE' }, { label: '기타', value: 'OTHER' }
]

// 원본 파일을 그대로 base64로 저장하면 문자열이 너무 커져
// 서비스 간 내부 통신(reservation-service → event-service)이 응답 크기 제한에 걸려 실패한다.
// 그래서 캔버스로 리사이즈·압축한 뒤 저장한다.
function resizeImage(file, maxSize = 1000, quality = 0.75) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onerror = reject
    reader.onload = () => {
      const img = new Image()
      img.onerror = reject
      img.onload = () => {
        const scale = Math.min(1, maxSize / Math.max(img.width, img.height))
        const canvas = document.createElement('canvas')
        canvas.width = Math.round(img.width * scale)
        canvas.height = Math.round(img.height * scale)
        const ctx = canvas.getContext('2d')
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height)
        resolve(canvas.toDataURL('image/jpeg', quality))
      }
      img.src = String(reader.result)
    }
    reader.readAsDataURL(file)
  })
}

async function handleImageChange(event) {
  const file = event.target.files?.[0]
  imageError.value = ""
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { imageError.value = "이미지는 5MB 이하만 등록할 수 있습니다."; return }

  try {
    const resized = await resizeImage(file)
    photoPreview.value = resized
    form.imageUrl = resized
  } catch {
    imageError.value = "이미지를 처리하지 못했습니다. 다른 파일로 시도해 주세요."
  }
}

function removeImage() { photoPreview.value = ""; form.imageUrl = "" }

function validateForm() {
  validationError.value = ''

  if (!auth.user || auth.user.role !== 'ADMIN') {
    validationError.value = '기관 담당자 계정만 행사를 등록할 수 있습니다.'
    return false
  }

  if (!form.title) {
    validationError.value = '행사명을 입력해 주세요.'
    return false
  }

  if (!form.description) {
    validationError.value = '행사 설명을 입력해 주세요.'
    return false
  }

  if (!form.category) {
    validationError.value = '카테고리를 선택해 주세요.'
    return false
  }

  if (!form.imageUrl) { validationError.value = "행사 대표 사진을 등록해 주세요."; return false }

  if (!form.venue || !form.organizerName) {
    validationError.value = '행사 장소와 주관 기관명을 입력해 주세요.'
    return false
  }
  if (!form.eventStartAt || !form.eventEndAt || new Date(form.eventStartAt) >= new Date(form.eventEndAt)) {
    validationError.value = '행사 시작·종료 일시를 올바르게 입력해 주세요.'
    return false
  }
  if (form.eventType === 'PAID_RESERVATION' && Number(form.price) <= 0) { validationError.value = '유료 예약형 행사는 가격이 필요합니다.'; return false }
  if (form.eventType !== 'PAID_RESERVATION') form.price = 0
  if (form.eventType !== 'FREE_VISIT' && (!form.registrationStartAt || !form.registrationEndAt || new Date(form.registrationStartAt) >= new Date(form.registrationEndAt) || new Date(form.registrationEndAt) > new Date(form.eventStartAt))) { validationError.value = '예약 기간은 행사 시작 전에 종료되어야 합니다.'; return false }

  return true
}

async function handleSubmit() {
  submitError.value = ''
  submitSuccess.value = ''

  if (!validateForm()) return

  submitting.value = true

  try {
    const payload = {
      title: form.title,
      description: form.description,
      category: form.category,
      eventType: form.eventType, venue: form.venue, organizerName: form.organizerName, imageUrl: form.imageUrl,
      price: Number(form.price), eventStartAt: form.eventStartAt, eventEndAt: form.eventEndAt,
      registrationStartAt: form.eventType === 'FREE_VISIT' ? form.eventStartAt : form.registrationStartAt,
      registrationEndAt: form.eventType === 'FREE_VISIT' ? form.eventStartAt : form.registrationEndAt,
      capacity: form.eventType === 'FREE_VISIT' ? 1 : Number(form.capacity)
    }

    const res = await eventApi.create(payload)
    console.log('[EventCreate] create response =', res.data)

    submitSuccess.value = '행사가 성공적으로 등록되었습니다.'

    const createdEventId =
      res.data?.data?.id ??
      res.data?.id

    if (createdEventId) {
      setTimeout(() => {
        router.push(`/events/${createdEventId}`)
      }, 500)
    } else {
      setTimeout(() => {
        router.push('/events')
      }, 500)
    }
  } catch (error) {
    console.error('[EventCreate] create failed:', error)
    submitError.value =
      error.response?.data?.message ||
      '행사 등록에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  background: #fff;
}

.page-shell {
  max-width: 1000px;
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

.section-heading { display:flex; justify-content:space-between; align-items:center; margin-bottom:10px; font-size:14px; font-weight:700; }
.section-heading small { color:var(--color-text-muted); font-size:12px; font-weight:400; }
.photo-dropzone { position:relative; display:flex; min-height:220px; overflow:hidden; align-items:center; justify-content:center; border:1px dashed var(--color-border); border-radius:16px; cursor:pointer; background:transparent; }
.photo-dropzone:hover { border-color:var(--color-primary); background:var(--color-bg-secondary); }
.photo-dropzone img { position:absolute; inset:0; width:100%; height:100%; object-fit:cover; }
.photo-empty { display:grid; justify-items:center; gap:7px; text-align:center; color:var(--color-text-secondary); }
.photo-plus { position:relative; overflow:hidden; display:grid; place-items:center; width:44px; height:44px; margin-bottom:2px; border-radius:15px; background:linear-gradient(135deg,#8ea9ff,#0322ab); color:#fff; font-size:22px; font-weight:600; line-height:1; box-shadow:0 8px 16px -6px rgba(3,34,171,.4), inset 0 1px 0 rgba(255,255,255,.4); }
.photo-plus::after { content:""; position:absolute; inset:0; background:linear-gradient(135deg,rgba(255,255,255,.4),rgba(255,255,255,0) 55%); }
.photo-empty strong { font-size:15px; color:var(--color-text-primary); }
.photo-empty span { font-size:13px; color:var(--color-text-muted); }
.photo-change { position:absolute; right:12px; bottom:12px; padding:7px 10px; border-radius:8px; background:rgba(11,31,51,.78); color:#fff; font-size:12px; }
.photo-remove { margin-top:9px; border:0; background:none; color:#d14343; cursor:pointer; font-size:13px; padding:0; }
.sr-only { position:absolute; width:1px; height:1px; padding:0; margin:-1px; overflow:hidden; clip:rect(0,0,0,0); white-space:nowrap; border:0; }

.form-card {
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 32px;
}

.event-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg-primary);
  padding: 12px 14px;
  font-size: 14px;
  font-family: inherit;
  color: var(--color-text-primary);
  outline: none;
  transition: var(--transition);
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}

.form-textarea {
  resize: vertical;
  min-height: 140px;
  line-height: 1.5;
}

.error-box {
  background: #fef2f2;
  color: #dc2626;
  border-radius: var(--radius-md);
  padding: 12px 14px;
  font-size: 13px;
}

.success-box {
  background: #ecfdf3;
  color: #174fbd;
  border-radius: var(--radius-md);
  padding: 12px 14px;
  font-size: 13px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 6px;
}

@media (max-width: 992px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .page-shell {
    padding: 40px 18px 70px;
  }

  .page-heading h1 {
    font-size: 28px;
  }

  .form-card {
    padding: 20px;
  }
}
</style>
