import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    {path: '/', component: () => import('../views/Manage.vue'),
      children: [
        { path: '', redirect: '/home' },
        { path: 'home', component: () => import('../views/Home.vue') },
        { path: 'group', component: () => import('../views/Group.vue') },
        { path: 'trash', component: () => import('../views/Trash.vue') },
      ]
    },
    {path: '/notFound', component: import('../views/404.vue'),},
    {path: '/:pathMatch(.*)', redirect: '/notFound' },
  ],
})

// 路由守卫：未登录跳转到登录页
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  if (to.path === '/login' || to.path === '/register') {
    next()
  } else if (!user) {
    next('/login')
  } else {
    next()
  }
})

export default router
