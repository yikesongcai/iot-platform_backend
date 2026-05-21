import { createRouter, createWebHashHistory } from "vue-router"

const router = createRouter({
  routes: [
    {
      path: "/",
      redirect: "/login"
    },
    {
      name: "login",
      path: "/login",
      component: () => import("@/components/Login.vue")
    },
    {
      name: "dashboard",
      path: "/dashboard",
      component: () => import("@/components/DashBoard.vue"),
      meta: {title: '控制台'}
    },
    {
      path: '/alarm/list',
      name: 'AlarmList',
      component: () => import('@/views/alarm/AlarmList.vue'),
      meta: { title: '告警列表' }
    },
    {
      path: '/alarm/detail/:id',
      name: 'AlarmDetail',
      component: () => import('@/views/alarm/AlarmDetail.vue'),
      meta: {
        title: '告警详情'
      }
    },
    {
      path: '/user-management',
      name: 'UserManagement',
      component: () => import('@/components/UserManagement.vue'),
      meta: { title: '用户管理' }
    },

    {
      path: '/systemLog',
      name: "SystemLog",
      component: () => import('@/components/SystemLog.vue'),
      meta: { title: '系统日志' }
    },
    {
      name: "home",
      path: "/home",
      component: () => import("@/views/home.vue")
    },
    {
      name: "management",
      path: "/management",
      component: () => import("@/components/DeviceManagement .vue")
    },

  ],
  history: createWebHashHistory()
})

export default router