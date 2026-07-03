import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', component: () => import('@/views/Login.vue') },
  { path: '/register', component: () => import('@/views/Register.vue') },
  {
    path: '/',
    component: () => import('@/views/Manage.vue'),
    children: [
      { path: '/home', component: () => import('@/views/Home.vue') },
      { path: '/group', component: () => import('@/views/Group.vue') },
      { path: '/trash', component: () => import('@/views/Trash.vue') },
    ]
  },
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
  