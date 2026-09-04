import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/LandingView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true, bareLayout: true }
  },
  {
    path: '/callback',
    name: 'Callback',
    component: () => import('@/views/CallbackView.vue'),
    meta: { bareLayout: true }
  },
  {
    path: '/events',
    name: 'EventList',
    component: () => import('@/views/EventListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/events/new',
    name: 'EventCreate',
    component: () => import('@/views/EventCreateView.vue'),
    meta: { requiresAuth: true, instructorOnly: true }
  },
  {
    path: '/events/:id(\\d+)',
    name: 'EventDetail',
    component: () => import('@/views/EventDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reservations',
    name: 'Reservation',
    component: () => import('@/views/ReservationView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/views/MyPageView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 인증/권한 가드
router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'EventList' }
  }

  if (to.meta.instructorOnly && auth.user?.role !== 'ADMIN') {
    return { name: 'EventList' }
  }
})

export default router