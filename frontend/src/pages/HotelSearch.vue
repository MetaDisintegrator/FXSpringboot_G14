<template>
  <div class="hotel-app">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <ul>
        <li :class="{ active: currentMenu === 'hotel' }" @click="goHome">
          <i class="iconfont">&#xe6b6;</i>酒店
        </li>
        <li :class="{ active: currentMenu === 'train' }" @click="goTrain">
          <i class="iconfont">&#xe608;</i>火车票
        </li>
      </ul>
    </aside>
    <!-- 右侧内容区 -->
    <div class="content-wrapper">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="logo-section" @click="goHome">
          <img class="logo" src="/images/ICON.png" alt="风行旅行" />
          <span class="site-name">风行旅行</span>
        </div>
        <nav class="nav-links">
          <template v-if="!userStore.isLoggedIn">
            <a href="#" @click.prevent="go('/auth')">登录/注册</a>
          </template>
          <template v-else>
            <div class="user-info">
              <img :src="userStore.userInfo.avatar || '/images/default-avatar.png'" class="avatar" />
              <span class="nickname">{{ userStore.username }}</span>
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

      <!-- 主体内容 -->
      <div class="content-body">
        <main class="main-content">
          <h2 class="page-title">酒店搜索</h2>

          <!-- 搜索区域 -->
          <div class="search-section">
            <el-card shadow="hover" class="search-card">
              <div class="search-fields">
                <div class="search-field">
                  <label>目的地</label>
                  <el-input
                    v-model="params.destination"
                    placeholder="输入城市"
                    clearable
                    required
                  />
                </div>

                <div class="search-field">
                  <label>酒店名称</label>
                  <el-input
                    v-model="params.namePattern"
                    placeholder="输入酒店名称(可选)"
                    clearable
                  />
                </div>

                <div class="search-action">
                  <el-button
                    type="primary"
                    @click="onSearch"
                    class="search-btn">
                    <i class="iconfont">&#xe615;</i>搜索
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

            <div class="form-box">
              <div class="label">选择入住日期</div>
              <input type="date" v-model="bookingForm.checkInDate" />
            </div>

          <div class="form-box">
            <div class="label">选择离店日期</div>
            <input type="date" v-model="bookingForm.checkOutDate" />
          </div>
          <div class="results">
            <h3>查询到的酒店</h3>
            <div
              v-for="hotel in hotels"
              :key="hotel.id"
              class="hotel-item"
            >
              <el-card class="hotel-card" shadow="hover">
                <div class="info">
                  <p><strong>酒店名称：</strong>{{ hotel.hotel.name }}</p>
                  <p><strong>酒店所在城市：</strong>{{ hotel.hotel.destination }}</p>
                  <p><strong>酒店详细地址 </strong>{{ hotel.hotel.address }}</p>
                  <p><strong>酒店简介</strong> {{ hotel.hotel.description }}</p>
                </div>
                <div v-if="hotel.rooms && hotel.rooms.length" class="seat-info">
                  <h4>房型 & 每晚房价：</h4>
                  <ul>
                    <li v-for="roomItem in hotel.rooms" :key="roomItem.id" class="seat-item">
                      <span>
                        {{ roomItem.name }} – ¥{{ roomItem.pricePerNight }} – 剩余：{{ roomItem.remain }}
                      </span>
                      <button
                        class="book-btn"
                        @click="startBookRoom(hotel, roomItem, bookingForm.checkInDate, bookingForm.checkOutDate)"
                        :disabled="roomItem.remain === 0"
                      >
                        {{ roomItem.remain > 0 ? '预订' : '售罄' }}
                      </button>
                  </li>
                </ul>
                </div>
              </el-card>
            </div>
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import hotel, { searchHotels, bookRoom } from '../api/hotel'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'

const userStore = useUserStore()

const bookingDialogVisible = ref(false)
const selectedHotel = ref(null)
const bookingForm = ref({
  checkInDate: dayjs().format('YYYY-MM-DD'),
  checkOutDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
  roomCount: 1
})

const userId = userStore.userInfo?.id
async function startBookRoom(hotel, roomItem, checkInDate, checkOutDate) {
  console.log('预订响应', hotel, roomItem, checkInDate, checkOutDate)

  if (!checkInDate || !checkOutDate) {
    ElMessage.error("请选择入住和离店日期");
    return;
  }
  const payload = {
    hotelId: roomItem.hotelId,
    roomId: roomItem.id,
    userId: userId,
    checkInDate: checkInDate,
    checkOutDate: checkOutDate
  }

  try {
    const { data } = await bookRoom(payload)

    if (data.message) {
      ElMessage.success(data.message)
    }

    const orderId = data.id
    const orderNumber = data.number

    if (!orderId || !orderNumber) {
      ElMessage.error('下单失败：未获取订单信息')
      return
    }

    // 关键修改点：确保参数名称与支付页接收的名称完全一致
    await router.push({
      name: 'HotelBooking',
      query: {
        id: orderId,
        number: orderNumber,
        name: hotel.hotel.name, // 支付页接收的是name
        destination: hotel.hotel.destination,
        address: hotel.hotel.address,
        roomName: roomItem.name, // 支付页接收的是roomName（原roomItem）
        checkInDate: checkInDate,
        checkOutDate: checkOutDate,
        pricePerNight: roomItem.pricePerNight,
        totalAmount: data.totalAmount
      }
    })
  } catch (err) {
    console.error('startPayment 异常：', err)
    const msg = err.response?.data?.message || '下单失败，请稍后重试'
    ElMessage.error(msg)
  }
}

function calculateTotalPrice() {
  if (!selectedHotel.value) return 0
  const nights = dayjs(bookingForm.value.checkOutDate).diff(
    dayjs(bookingForm.value.checkInDate),
    'day'
  )
  // 默认选最低价房型
  return (selectedHotel.value.lowestPrice || 0) * nights * bookingForm.value.roomCount
}

async function handleBooking() {
  try {
    const bookingData = {
      hotelId: selectedHotel.value.id,
      // 这里可弹出让用户选房型，简化为默认选最低价
      roomType: selectedHotel.value.rooms?.[0]?.typeId || 'standard',
      checkInDate: bookingForm.value.checkInDate,
      checkOutDate: bookingForm.value.checkOutDate,
      guestCount: bookingForm.value.roomCount,
      totalPrice: calculateTotalPrice()
    }
    const response = await bookRoom(bookingData)
    if (!response.data?.orderId) throw new Error('无效的预订响应')
    router.push({
      path: '/payment',
      query: {
        orderId: response.data.orderId,
        amount: response.data.totalPrice
      }
    })
    bookingDialogVisible.value = false
    ElMessage.success('预订成功，请完成支付')
  } catch (error) {
    ElMessage.error('预订失败: ' + error.message)
  }
}

const logout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/auth')
}

onMounted(async () => {
  await userStore.fetchCurrentUser()
  if (params.value.destination) {
    await loadHotels()
  }
})

const router = useRouter()
const route = useRoute()

const currentMenu = ref('hotel')
function go(path) {
  router.push(path)
}
function goHome() {
  currentMenu.value = 'hotel'
  router.push({ name: 'HotelHome' })
}
function goTrain() {
  currentMenu.value = 'train'
  router.push({ name: 'train' })
}
function goMessages() {
  currentMenu.value = 'messages'
  router.push({ name: 'MessageCenter' })
}

const params = ref({
  destination: route.query.destination || '',
  namePattern: route.query.namePattern || ''
})

const hotels = ref([])
const defaultImg = '/images/default-hotel.jpg'

watch(
  () => route.query,
  (newQuery) => {
    params.value.destination = newQuery.destination || ''
    params.value.namePattern = newQuery.namePattern || ''
  }
)

async function loadHotels() {
  if (!params.value.destination) {
    hotels.value = []
    return
  }
  try {
    const paramsJson = {
      destination: params.value.destination.trim(),
      namePattern: params.value.namePattern?.trim() || null
    }
    console.log('begin')
    const response = await searchHotels(paramsJson)
    if (!response?.data) throw new Error('无效的响应格式')
    console.log('middle')
    hotels.value = response.data?.data
    console.log(hotels)
  } catch (err) {
    ElMessage.error('搜索酒店失败: ' + (err.response?.data?.message || err.message))
    hotels.value = []
  }
}



async function onSearch() {
  await loadHotels()
}

function goDetail(hotelId) {
  router.push({ name: 'HotelDetail', params: { id: hotelId } })
}
</script>


<style scoped>


@import url('https://fonts.googleapis.com/css2?family=Pacifico&family=Montserrat:wght@400;600&display=swap');
@import url('//at.alicdn.com/t/font_2738890_1x2k9b0b1c6.css');

.hotel-app {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'Montserrat', Arial, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 160px;
  background: #fff;
  border-right: 1px solid #ececec;
  padding-top: 20px;
}
.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sidebar li {
  display: flex;
  align-items: center;
  padding: 14px 26px;
  margin-bottom: 8px;
  font-size: 16px;
  color: #222;
  border-radius: 5px 0 0 5px;
  cursor: pointer;
  position: relative;
  transition: background 0.2s, transform 0.2s;
}
.sidebar li::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #1677ff;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
}
.sidebar li:hover::after,
.sidebar li.active::after {
  transform: translateX(0);
}
.sidebar li.active,
.sidebar li:hover {
  background: #e6f6ff;
  color: #1677ff;
}
.sidebar .iconfont {
  font-size: 20px;
  margin-right: 12px;
}
/* 侧边栏小红点 */
.red-dot {
  position: absolute;
  top: 12px;
  right: 16px;
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
}

/* 右侧内容区 */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}
.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.logo-section:hover {
  transform: scale(1.05);
}
.logo {
  height: 40px;
  width: auto;
}
.site-name {
  margin-left: 10px;
  font-family: 'Pacifico', cursive;
  font-size: 26px;
  color: #1677ff;
  transition: color 0.2s;
}
.logo-region:hover .site-name {
  color: #145ecb;
}
.nav-links a {
  margin-left: 24px;
  color: #333;
  font-weight: 500;
  text-decoration: none;
  position: relative;
  padding-bottom: 5px;
}
.nav-links a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #1677ff;
  transition: width 0.3s ease;
}
.nav-links a:hover::after {
  width: 100%;
}
.nav-links a:hover {
  color: #1677ff;
}
.nav-links a:hover { color: #1677ff; }

/* 用户信息样式 */
.user-info {
  position: relative;
  display: flex;
  align-items: center;
  margin-left: 24px;
  cursor: pointer;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}
.nickname {
  font-size: 14px;
  color: #333;
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
  z-index: 10;
}
.user-info:hover .dropdown {
  display: block;
}
.dropdown a {
  display: block;
  padding: 8px 16px;
  color: #333;
  text-decoration: none;
}
.dropdown a:hover {
  background: #f5f7fa;
  color: #1677ff;
}

/* 顶部小红点 */
.red-dot-sm {
  position: absolute;
  top: -2px;
  right: -8px;
  width: 6px;
  height: 6px;
  background-color: #f56c6c;
  border-radius: 50%;
}

/* 主体区域 */
.content-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.main-content {
  flex: 1;
  padding: 24px 32px;
  overflow-y: auto;
}

/* 页标题 */
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
}

/* 搜索 + 排序 卡片 */
.search-card {
  margin-bottom: 20px;
}
.search-card .el-form-item {
  margin-right: 12px;
}

/* 搜索结果 */
.hotel-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.hotel-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.hotel-thumb {
  width: 100%;
  height: 160px;
  object-fit: cover;
}
.hotel-info {
  padding: 12px;
}
.hotel-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}
.hotel-destination {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}
.hotel-rating {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #fbbf24;
  margin-bottom: 8px;
}
.hotel-rating .iconfont {
  margin-right: 4px;
}
.hotel-price {
  font-size: 16px;
  color: #e91e63;
  font-weight: 700;
}

/* 无结果提示 */
.no-results {
  text-align: center;
  color: #999;
  margin-top: 40px;
  font-size: 16px;
}

/* 响应式适配 */
@media (max-width: 900px) {
  .sidebar {
    display: none;
  }
  .main-content {
    padding-left: 10px;
    padding-right: 10px;
  }
}
/* 日期选择器样式 */
.date-picker {
  width: 180px;
}

.el-date-editor .el-input__inner {
  height: 32px;
  line-height: 32px;
}

.el-date-picker {
  z-index: 9999 !important;
}

.el-popper {
  position: absolute;
  background: #fff;
  min-width: 150px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  padding: 10px;
  z-index: 2000;
  color: #606266;
  line-height: 1.4;
  text-align: justify;
  font-size: 14px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  word-break: break-all;
}

.seat-info h4 {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.seat-info ul {
  list-style: none;
  padding: 0;
  margin: 4px 0 0 0;
}


.book-btn {
  background: #409eff;
  color: #fff;
  border: none;
  padding: 4px 8px;
  font-size: 13px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s ease;
}
.book-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}
.book-btn:not(:disabled):hover {
  background: #3a8ee6;
}
.seat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
</style>
