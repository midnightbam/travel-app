import { createRouter, createWebHistory } from 'vue-router'
import authService from './services/auth.js'
import Home from './views/Home.vue'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Dashboard from './views/Dashboard.vue'
import AddDestination from './views/AddDestination.vue'
import TripDetail from './views/TripDetail.vue'
import Profile from './views/Profile.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/destination/add',
    name: 'AddDestination',
    component: AddDestination,
    meta: { requiresAuth: true }
  },
  {
    path: '/destination/edit/:id',
    name: 'EditDestination',
    component: AddDestination,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/trip/:id',
    name: 'TripDetail',
    component: TripDetail,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard for authentication
router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authService.isAuthenticated()) {
    return '/login'
  }
})

export default router