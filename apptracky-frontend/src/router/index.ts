// Composables
import { RouteLocationNormalized, createRouter, createWebHashHistory } from 'vue-router'
import Home from '@/views/Home.vue';
import Login from '@/views/Login.vue';
import Settings from '@/views/Settings.vue';
import ForgotPassword from '@/views/ForgotPassword.vue';
import ResetPassword from '@/views/ResetPassword.vue';
import ValidateEmail from '@/views/ValidateEmail.vue';
import { getUser } from '@/utils/user';

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/default/Default.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: Home,
      },
      {
        path: 'login',
        name: 'Login',
        component: Login,
      },
      {
        path: 'settings',
        name: 'Settings',
        component: Settings,
      },
      {
        path: 'forgot-password/reset',
        name: 'ForgotPwReset',
        component: ResetPassword,
        beforeEnter: (to: RouteLocationNormalized) => {
          return !!to.query.uuid;
        }
      },
      {
        path: 'forgot-password',
        name: 'ForgotPw',
        component: ForgotPassword,
      },
      {
        path: 'validate-email',
        name: 'ValidateEmail',
        component: ValidateEmail,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(/* process.env.BASE_URL */),
  routes,
});

const noAuthRoutes = ['Login', 'ForgotPw', 'ForgotPwReset', 'ValidateEmail'];
router.beforeEach((to) => {
  if (!getUser() && !noAuthRoutes.includes(to.name as string)) {
    return { name: 'Login' };
  }
});

export default router
