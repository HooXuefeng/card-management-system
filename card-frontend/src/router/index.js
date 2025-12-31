import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Home from '@/views/Home.vue'
import Password from '@/views/Password.vue'
import CardDetail from '@/views/CardDetail.vue'
import Reports from '@/views/Reports.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { 
    path: '/home', 
    name: 'Home', 
    component: Home,
    meta: { requiresAuth: true }
  },
  {
    path: '/card/:cardId',
    name: 'CardDetail',
    component: CardDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/changePwd',
    name: 'Password',
    component: Password,
    meta: { requiresAuth: true }
  },
  {
    path: '/reports',
    name: 'Reports',
    component: Reports,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫：校验登录状态
router.beforeEach((to, from, next) => {
  // 同时检查localStorage和sessionStorage中的token
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  
  // 只有当页面需要认证且没有token时，才提示登录
  if (to.meta.requiresAuth && !token) {
    alert('请先登录！')
    next('/login')
  } else {
    next()
  }
})

export default router