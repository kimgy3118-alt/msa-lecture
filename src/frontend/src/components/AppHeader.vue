<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">
        <img src="@/assets/images/logo/logo.png" alt="갈래" class="logo-img" />
      </router-link>

      <nav class="nav-links">
        <router-link to="/" class="nav-link" :class="{ active: route.path === '/' }">홈</router-link>
        <router-link to="/events" class="nav-link" :class="{ active: route.path.startsWith('/events') && route.name !== 'EventCreate' }">행사 찾기</router-link>
        <router-link v-if="isOrganizer" to="/events/new" class="nav-link" :class="{ active: route.name === 'EventCreate' }">행사 등록</router-link>
        <router-link v-if="auth.isAuthenticated && !isOrganizer" to="/reservations" class="nav-link" :class="{ active: route.path === '/reservations' }">내 예약</router-link>
      </nav>

      <div class="header-actions">
        <router-link to="/events" class="icon-btn" aria-label="행사 검색">
          <svg viewBox="0 0 24 24" width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
        </router-link>

        <template v-if="auth.isAuthenticated">
          <router-link to="/mypage" class="user-avatar" :title="auth.user?.name">
            <img :src="avatarSrc" alt="프로필 사진" />
          </router-link>
          <button class="text-link" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-pill">로그인</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/store/auth.js'
import { useRoute, useRouter } from 'vue-router'
import profileImg from '@/assets/images/profile/profile.png'
import profileOrganizerImg from '@/assets/images/profile/profile2.png'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const isOrganizer = computed(() => auth.user?.role === 'ADMIN')
const avatarSrc = computed(() => (isOrganizer.value ? profileOrganizerImg : profileImg))

async function handleLogout() {
  await auth.logout(false)
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 76px;
  display: flex;
  align-items: center;
  gap: 36px;
}
.logo {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.logo-img {
  height: 32px;
  width: auto;
  display: block;
}
.nav-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 34px;
  flex: 1;
}
.nav-link {
  padding: 9px 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text-primary);
  background: transparent;
  transition: var(--transition);
  white-space: nowrap;
}
.nav-link:hover {
  color: var(--color-primary);
}
.nav-link.active {
  color: var(--color-primary);
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
  flex-shrink: 0;
}
.icon-btn {
  position: relative;
  overflow: hidden;
  width: 38px;
  height: 38px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(135deg, #6a8dff, #0322ab);
  box-shadow: 0 8px 16px -6px rgba(3, 34, 171, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: var(--transition);
  flex-shrink: 0;
}
.icon-btn::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.icon-btn > svg {
  position: relative;
  z-index: 1;
}
.icon-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px -6px rgba(3, 34, 171, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.text-link {
  background: none;
  border: none;
  font-family: var(--font-sans);
  font-size: 13.5px;
  font-weight: 600;
  color: var(--color-text-secondary);
  padding: 8px 6px;
}
.text-link:hover {
  color: var(--color-text-primary);
}
.btn-pill {
  position: relative;
  overflow: hidden;
  padding: 9px 20px;
  border-radius: 999px;
  background: linear-gradient(135deg, #6a8dff, #0322ab);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  transition: var(--transition);
  white-space: nowrap;
  box-shadow: 0 8px 16px -6px rgba(3, 34, 171, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.btn-pill::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0) 55%);
  pointer-events: none;
}
.btn-pill:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px -6px rgba(3, 34, 171, 0.55), inset 0 1px 0 rgba(255, 255, 255, 0.4);
}
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  overflow: hidden;
  display: grid;
  place-items: center;
  flex-shrink: 0;
  border: 2px solid #fff;
  transition: var(--transition);
}
.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.user-avatar:hover {
  border-color: var(--color-primary);
}

@media (max-width: 760px) {
  .nav-links {
    display: none;
  }
  .header-inner {
    gap: 16px;
  }
}
</style>
