import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', component: () => import('@/views/Home.vue') },
  { path: '/group', component: () => import('@/views/Group.vue') },
  { path: '/trash', component: () => import('@/views/Trash.vue') },
  { path: '/manage', component: () => import('@/views/Manage.vue') },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/404.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  if (to.path !== '/login' && to.path !== '/register' && !user) {
    next('/login')
  } else {
    next()
  }
})

export default router
