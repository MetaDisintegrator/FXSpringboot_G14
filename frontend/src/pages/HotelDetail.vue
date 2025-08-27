<template>
  <div class="hotel-detail">
    <header class="top-nav">
      <div class="logo-section" @click="goHome">
        <img class="logo" src="/images/ICON.png" alt="风行旅行" />
        <span class="site-name">风行旅行</span>
      </div>
      <nav class="nav-links">
        <template v-if="!isLoggedIn">
          <a href="#" @click.prevent="go('/auth')">登录/注册</a>
        </template>
        <template v-else>
          <div class="user-info">
            <img :src="avatarUrl" class="avatar" />
            <span class="nickname">{{ username }}</span>
            <div class="dropdown">
              <a href="#" @click.prevent="go('/orders')">我的订单</a>
              <a href="#" @click.prevent="logout">退出登录</a>
            </div>
          </div>
        </template>
        <a href="#" @click.prevent="go('/messages')" class="message-link">消息中心</a>
        <a href="#" @click.prevent="go('/aboutUs')">关于我们</a>
      </nav>
    </header>
    
    <el-breadcrumb separator="/">
      <el-breadcrumb-item @click.native="goHome">首页</el-breadcrumb-item>
      <el-breadcrumb-item>酒店详情</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="detail-card" v-if="hotel">
      <el-row :gutter="20">
        <el-col :span="12">
          <img :src="hotel.img" class="detail-img" />
        </el-col>
        <el-col :span="12">
          <h2>{{ hotel.name }}</h2>
          <div class="stars">
            <i v-for="i in hotel.stars" :key="i" class="iconfont">&#xe60a;</i>
            <span>{{ hotel.stars }}星</span>
          </div>
          <p class="address">{{ hotel.address }}</p>
          <p class="price">￥{{ hotel.price }} 起</p>
          <el-button type="primary" @click="goBooking">立即预订</el-button>
        </el-col>
      </el-row>
      <div class="description">
        <h3>酒店简介</h3>
        <p>{{ hotel.description }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchHotelDetail } from '../api/hotel'
import { ElMessage } from 'element-plus'

// 用户状态管理
const username = ref('未登录')
const isLoggedIn = ref(false)
const avatarUrl = ref('../assets/default-avatar.png')

const logout = () => {
  localStorage.removeItem('username')
  localStorage.removeItem('avatar')
  isLoggedIn.value = false
  username.value = '未登录'
  avatarUrl.value = '/images/default-avatar.png'
  ElMessage.success('已退出登录')
  router.push('/auth')
}

const route = useRoute()
const router = useRouter()
const hotel = ref(null)

const goHome = () => router.push('/')
const goBooking = () => router.push({ name: 'BookingForm', params: { bookingId: null, hotelId: hotel.value.mealId } })

onMounted(async () => {
  const id = route.params.mealId
  const res = await fetchHotelDetail(id)
  hotel.value = res.data || res
})
</script>

<style scoped>
.hotel-detail { padding: 20px; }
.detail-card { max-width: 900px; margin: 0 auto; }
.detail-img { width: 100%; border-radius: 8px; }
.stars { color: #fbbf24; margin: 10px 0; }
.address { color: #666; margin-bottom: 10px; }
.price { font-size: 24px; color: #e91e63; margin-bottom: 20px; }
.description { margin-top: 24px; }

/* 顶部导航样式 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}
.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.logo {
  height: 40px;
  width: auto;
}
.site-name {
  margin-left: 10px;
  font-size: 26px;
  color: #1677ff;
}
.nav-links {
  display: flex;
  align-items: center;
}
.nav-links a {
  margin-left: 24px;
  color: #333;
  text-decoration: none;
}
.user-info {
  position: relative;
  display: flex;
  align-items: center;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}
.dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 8px 0;
  min-width: 120px;
  display: none;
}
.user-info:hover .dropdown {
  display: block;
}
.dropdown a {
  display: block;
  padding: 8px 16px;
  color: #333;
}
.dropdown a:hover {
  background: #f5f7fa;
}
</style>