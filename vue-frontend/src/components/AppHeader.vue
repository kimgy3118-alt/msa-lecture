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
            <img src="@/assets/images/profile/profile.png" alt="프로필 사진" />
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

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const isOrganizer = computed(() => auth.user?.role === 'ADMIN')

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #f5f5f5;
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
  gap: 30px;
  flex: 1;
}
.nav-link {
  padding: 9px 0;
  font-size: 15.5px;
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
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  background: var(--color-primary);
  transition: var(--transition);
  flex-shrink: 0;
}
.icon-btn:hover {
  background: var(--color-primary-dark);
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
  padding: 9px 20px;
  border-radius: 999px;
  background: var(--color-primary);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  transition: var(--transition);
  white-space: nowrap;
}
.btn-pill:hover {
  background: var(--color-primary-dark);
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
