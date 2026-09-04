<template>
  <main class="login-page">
    <div class="login-visual" aria-hidden="true"></div>
    <header class="login-header"><router-link to="/" class="brand"><img src="@/assets/images/logo/logo.png" alt="갈래" /></router-link><router-link to="/" class="home-link">홈으로</router-link></header>
    <section class="login-card">
      <template v-if="!showRegister">
        <h1>가고 싶은 행사를<br>한곳에서 만나보세요.</h1><p class="intro">전국의 축제, 전시, 공연과 문화 체험을 갈래에서 찾아보세요.</p>
        <form class="form" @submit.prevent="handleOAuth"><label>이메일<input v-model.trim="loginForm.username" type="email" required placeholder="name@example.com" /></label><label>비밀번호<input v-model="loginForm.password" type="password" required placeholder="비밀번호" /></label><p v-if="loginError" class="message error">{{ loginError }}</p><button class="oauth-button" :disabled="loginLoading">{{ loginLoading ? '로그인 중…' : '로그인' }} <span>→</span></button></form><p class="notice">데모 계정: owner@test.com / password1234</p><div class="switch">처음이신가요? <button @click="showRegister = true">회원가입</button></div>
      </template>
      <template v-else>
        <button class="back-button" @click="showRegister = false">← 로그인으로</button><h1>갈래 회원가입</h1><p class="intro">행사를 예약하거나 기관 행사를 등록할 수 있어요.</p>
        <form @submit.prevent="handleRegister" class="form"><label>이름<input v-model.trim="registerForm.name" required placeholder="홍길동" /></label><label>이메일<input v-model.trim="registerForm.email" required type="email" placeholder="name@example.com" /></label><label>비밀번호<input v-model="registerForm.password" required type="password" minlength="8" placeholder="8자 이상" /></label><label>이용 목적<select v-model="registerForm.role"><option value="STANDARD">행사 참여</option><option value="ADMIN">기관 행사 운영</option></select></label><p v-if="error" class="message error">{{ error }}</p><p v-if="success" class="message success">{{ success }}</p><button class="oauth-button" :disabled="loading">{{ loading ? '가입 처리 중…' : '회원가입하기' }}</button></form>
      </template>
    </section>
  </main>
</template>
<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'
const auth = useAuthStore(); const route = useRoute(); const showRegister = ref(false), loading = ref(false), error = ref(''), success = ref(''), loginLoading = ref(false)
const loginError = ref(route.query.error !== undefined ? '이메일 또는 비밀번호가 올바르지 않습니다.' : '')
const registerForm = ref({ name: '', email: '', password: '', role: 'STANDARD' })
const loginForm = ref({ username: '', password: '' })
async function handleOAuth() { const params=new URLSearchParams({response_type:'code',client_id:import.meta.env.VITE_CLIENT_ID,redirect_uri:import.meta.env.VITE_REDIRECT_URI,scope:'openid profile read write'}); loginError.value=''; loginLoading.value=true; try { await fetch(`/oauth2/authorize?${params}`,{credentials:'include',redirect:'manual'}); const response=await fetch('/auth/login',{method:'POST',credentials:'include',redirect:'manual',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams(loginForm.value)}); if (!response.ok && response.type !== 'opaqueredirect') throw new Error('로그인 정보를 확인해 주세요.'); window.location.assign(`/oauth2/authorize?${params}`) } catch(e) { loginError.value=e.message || '로그인에 실패했습니다.' } finally { loginLoading.value=false } }
async function handleRegister() {
  error.value=''; success.value=''; loading.value=true
  const credentials = { username: registerForm.value.email, password: registerForm.value.password }
  try {
    await authApi.register(registerForm.value)
    // 가입 시 입력한 값을 그대로 사용해 바로 인증한다. 가입 뒤 비밀번호를
    // 다시 입력하면서 발생하는 오입력 때문에 로그인에 실패하지 않게 한다.
    loginForm.value = credentials
    registerForm.value={name:'',email:'',password:'',role:'STANDARD'}
    showRegister.value = false
    await handleOAuth()
  } catch(e) {
    error.value=e.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value=false
  }
}
</script>
<style scoped>
.login-page{min-height:100vh;position:relative;display:grid;place-items:center;padding:96px 24px 40px;background:#f5f8fd;overflow:hidden}.login-visual{position:absolute;inset:0;background:linear-gradient(90deg,rgba(10,22,30,.58),rgba(10,22,30,.08)),url('@/assets/images/hero/korea-festival-hero.png') center/cover;filter:saturate(.9)}.login-header{position:absolute;z-index:1;top:0;left:0;right:0;height:76px;padding:0 max(28px,calc((100% - 1180px)/2));display:flex;align-items:center;justify-content:space-between;color:#fff}.brand{display:flex;align-items:center;text-decoration:none}.brand img{height:26px;width:auto;display:block;filter:brightness(0) invert(1)}.home-link{color:#fff;text-decoration:none;padding:10px 18px;border:1px solid rgba(255,255,255,.55);border-radius:999px;font-size:14px}.login-card{position:relative;z-index:1;width:min(100%,460px);background:rgba(255,255,255,.96);border-radius:30px;padding:42px;box-shadow:0 24px 70px rgba(0,0,0,.22)}.eyebrow{font-size:11px;font-weight:800;letter-spacing:.14em;color:#0322ab;margin:0 0 14px}.login-card h1{margin:0;font-size:34px;line-height:1.25;letter-spacing:-1.5px;color:#1c292b}.intro{margin:16px 0 28px;line-height:1.7;color:#697274;font-size:14px}.oauth-button{position:relative;overflow:hidden;width:100%;border:0;border-radius:14px;background:linear-gradient(135deg,#4a63ff,#0322ab);color:#fff;padding:16px 20px;font-weight:700;font-size:15px;cursor:pointer;display:flex;justify-content:space-between;align-items:center;box-shadow:0 10px 22px -8px rgba(3,34,171,.5),inset 0 1px 0 rgba(255,255,255,.35);transition:var(--transition)}.oauth-button::after{content:"";position:absolute;inset:0;background:linear-gradient(135deg,rgba(255,255,255,.35),rgba(255,255,255,0) 55%);pointer-events:none}.oauth-button:hover:not(:disabled){transform:translateY(-1px);box-shadow:0 14px 26px -8px rgba(3,34,171,.6),inset 0 1px 0 rgba(255,255,255,.4)}.oauth-button > span{position:relative;z-index:1}.oauth-button:disabled{opacity:.6}.oauth-button span{font-size:22px;line-height:12px}.notice{font-size:12px;color:#8c9290;margin:15px 0 22px;line-height:1.5}.switch{text-align:center;color:#697274;font-size:13px}.switch button,.back-button{border:0;background:none;color:#0322ab;font-weight:700;cursor:pointer;font-size:13px}.back-button{margin:0 0 22px;padding:0}.form{display:grid;gap:14px}.form label{font-size:12px;font-weight:700;color:#485153;display:grid;gap:7px}.form input,.form select{font:inherit;border:1px solid #d9ded8;border-radius:12px;padding:12px;background:#fff;outline:none}.form input:focus,.form select:focus{border-color:#0322ab;box-shadow:0 0 0 3px #e8f0fb}.message{padding:11px 13px;border-radius:12px;font-size:13px;margin:0}.error{color:#a13030;background:#fcebea}.success{color:#276447;background:#e9f6ed}@media(max-width:600px){.login-card{padding:32px 25px;border-radius:24px}.login-card h1{font-size:29px}.login-header{padding:0 22px}}
</style>
