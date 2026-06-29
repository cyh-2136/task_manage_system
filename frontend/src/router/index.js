import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'
import TaskList from '../views/TaskList.vue'
import News from '../views/News.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/dashboard', name: 'Dashboard', component: Dashboard },
  { path: '/tasks', name: 'TaskList', component: TaskList },
  { path: '/news', name: 'News', component: News }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path !== '/login' && to.path !== '/register' && !localStorage.getItem('logged_in')) {
    return next('/login')
  }
  next()
})

export default router
