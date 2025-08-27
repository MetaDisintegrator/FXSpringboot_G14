<template>
  <div class="booking-form-page">
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
      <el-breadcrumb-item>填写订单</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="booking-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="酒店名称">
          <span>{{ hotel.name }}</span>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="form.roomTypeId" placeholder="请选择房型">
            <el-option
              v-for="rt in hotel.roomTypes"
              :key="rt.roomTypeId"
              :label="rt.name"
              :value="rt.roomTypeId" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住 - 离店" prop="dates">
          <el-date-picker
            v-model="form.dates"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="入住人姓名" prop="guestName">
          <el-input v-model="form.guestName" placeholder="请输入姓名"/>
        </el-form-item>
        <el-form-item label="联系方式" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitBooking">提交订单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchHotelDetail, createBooking } from '../api/hotel'

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
const formRef = ref(null)
const hotel = reactive({ name: '', roomTypes: [] })

const form = reactive({
  roomTypeId: '',
  dates: [],
  guestName: '',
  phone: ''
})
const rules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  dates: [{ type: 'array', required: true, message: '请选择日期范围', trigger: 'change' }],
  guestName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const goHome = () => router.push('/')

onMounted(async () => {
  // 编辑或新建
  const id = route.query.hotelId || route.params.bookingId && (await getBooking()).hotelId
  const res = await fetchHotelDetail(id)
  Object.assign(hotel, res.data || res)
})

const submitBooking = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    const [checkIn, checkOut] = form.dates
    const payload = {
      userId: 'U1001',
      hotelId: route.query.hotelId || hotel.mealId,
      roomTypeId: form.roomTypeId,
      checkInDate: checkIn,
      checkOutDate: checkOut
    }
    await createBooking(payload)
    router.push('/orders')
  })
}
</script>

<style scoped>
.booking-form-page { padding: 20px; }
.booking-card { max-width: 600px; margin: 0 auto; }

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
