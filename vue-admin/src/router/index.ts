import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/dashboard',
      component: () => import('../views/Layout.vue'),
      children: [
        {
          path: '',
          component: () => import('../views/hotel/List.vue'),
          meta: { title: '酒店列表' }
        },
        {
          path: 'hotel-add',
          component: () => import('../views/hotel/Detail.vue'),
          meta: { title: '发布酒店' }
        },
        {
          path: 'hotel-edit/:id',
          component: () => import('../views/hotel/Detail.vue'),
          meta: { title: '编辑酒店' }
        },
        {
          path: 'room-manage/:hotelId',
          component: () => import('../views/room/List.vue'),
          meta: { title: '房型管理' }
        },
        {
          path: 'profile',
          component: () => import('../views/Profile.vue'),
          meta: { title: '个人中心' }
        },
        {
          path: 'user-manage',
          component: () => import('../views/user/List.vue'),
          meta: { title: '酒店账号管理' }
        },
        {
          path: 'consumer-manage',
          component: () => import('../views/user/ConsumerList.vue'),
          meta: { title: '消费者用户管理' }
        },
        {
          path: 'banner-manage',
          component: () => import('../views/banner/List.vue'),
          meta: { title: '广告轮播图管理' }
        }
      ]
    }
  ]
})

export default router
