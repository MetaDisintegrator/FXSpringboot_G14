<!-- src/pages/MealOrder.vue -->
<template>
  <div class="meal-page">

    <div class="header">
      <button class="back-btn" @click="goBack">返回</button>
      <h2 class="page-title">火车订餐</h2>
    </div>

    <!-- 列车信息展示 -->
    <div v-if="userStore.isLoggedIn && trainInfo" class="train-info">
      <h3>{{ trainInfo.trainNumber }}次列车</h3>
      <div class="route">
        <span class="station">{{ trainInfo.fromStation }}</span>
        <span class="arrow">→</span>
        <span class="station">{{ trainInfo.toStation }}</span>
      </div>
      <div class="time-info">
        <div class="time-item">
          <span class="label">发车时间:</span>
          <span class="value">{{ formatTime(trainInfo.departureTime) }}</span>
        </div>
        <div class="time-item">
          <span class="label">到达时间:</span>
          <span class="value">{{ formatTime(trainInfo.arrivalTime) }}</span>
        </div>
        <div class="time-item">
          <span class="label">历时:</span>
          <span class="value">{{ formatDuration(trainInfo.durationMinutes) }}</span>
        </div>
      </div>
    </div>

    <!-- 登录提示 -->
    <div v-if="!userStore.isLoggedIn" class="login-tip">
      <p>请先登录后再订餐</p>
      <router-link to="/login">前往登录</router-link>
    </div>

    <!-- 菜单展示 -->
    <div v-else-if="menu.length" class="menu-list">
      <div class="meal-card" v-for="item in menu" :key="item.mealId">
        <div class="info">
          <h4 class="meal-name">{{ item.name }}</h4>
          <p class="meal-desc">{{ item.description }}</p>
          <p class="price">￥{{ item.price.toFixed(2) }}</p>
          <button class="buy-btn" @click="submitOrder(item)">立即购买</button>
        </div>
      </div>
    </div>

    <div v-else class="tip">
      <span v-if="loading">加载中...</span>
      <span v-else>暂无可用菜单</span>
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {searchByTrain, startPayment} from '../api/trainMeal'
import {useUserStore} from '../store/user'
import {storeToRefs} from "pinia";
import {getTrainById} from "../api/train.js";

const router = useRouter()
const userStore = useUserStore()

const { id, username } = storeToRefs(userStore)

// 状态
const menu = ref([])
const loading = ref(false)

// 获取当前关联订单
const trainId = localStorage.getItem('currestTrainId')
const seatOrderId = localStorage.getItem('currentSeatOrderId')

// 新增：响应式数据
const trainInfo = ref(null)

// 新增：时间格式化方法（不使用dayjs）
const formatTime = (timeStr) => {
  try {
    const date = new Date(timeStr)
    // 格式化为：YYYY-MM-DD HH:mm
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')

    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return timeStr // 如果解析失败，返回原始字符串
  }
}

// 新增：时长格式化方法
const formatDuration = (minutes) => {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

// 新增返回方法
const goBack = () => {
  router.push({ name: 'HotelHome' })
}

// 获取菜单
const getMenu = async () => {
  if (!trainId) {
    alert('未找到当前座位订单信息')
    return
  }

  loading.value = true
  console.log(trainId)
  try {
    const res = await searchByTrain(trainId)
    trainInfo.value = (await getTrainById(trainId)).data
    console.log('列车详细信息:', trainInfo)
    console.log('[MealOrder] searchTrainMealOrderBySeatOrder 返回 →', res)

    let list = []
    if (Array.isArray(res.data)) {
      list = res.data
    } else if (Array.isArray(res.data?.data)) {
      list = res.data.data
    } else {
      console.warn('[MealOrder] 未识别的返回结构：', res.data)
    }

    menu.value = list
    if (!list.length) {
      alert('当前订单暂无可用餐食')
    }
  } catch (err) {
    console.error('[MealOrder] 获取菜单失败 →', err)
    alert(err.message || '获取菜单失败，请重试')
  } finally {
    loading.value = false
  }
}

// 提交订餐
const submitOrder = async (item) => {
  try {
    console.log(id)
    const payload = {
      userId: userStore.id,
      ticketReservationId: seatOrderId,
      trainMealId: item.id,
      quantity: 1
    }
    console.log('[MealOrder] startPayment 参数 →', payload)
    const { data } = await startPayment(payload)

    const orderId = data.id
    const orderNumber = data.number

    await router.push({name: 'MealPurchase',
      query: {
        mealId: item.id,
        orderId: orderId,
        orderNumber: orderNumber,
        trainNumber: trainInfo.value.trainNumber,
        name: item.name,
        description: item.description,
        price: item.price.toFixed(2)
    }})
  } catch (err) {
    console.error('[MealOrder] 订餐失败 →', err)
    alert('订餐失败,请先在列车上购票');
  }
}

// 页面加载时自动获取菜单
onMounted(() => {
  getMenu()
})

getMenu()
</script>

<style scoped>
/* 新增：列车信息样式 */
.train-info {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.train-info h3 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #333;
}

.route {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 18px;
}

.station {
  font-weight: bold;
  color: #1a73e8;
}

.arrow {
  margin: 0 12px;
  color: #666;
}

.time-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.time-item {
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 12px;
  color: #666;
}

.value {
  font-size: 14px;
  font-weight: 500;
}

.meal-page {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 24px;
  background: #fdfdfd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.page-title {
  text-align: center;
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 24px 0;
}
.login-tip {
  text-align: center;
  margin: 40px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}
.login-tip a {
  color: #409cff;
  text-decoration: none;
  font-weight: bold;
}
.menu-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}
.meal-card {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.meal-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.meal-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
}
.price {
  font-size: 16px;
  font-weight: bold;
  color: #ff6b00;
  margin: 8px 0;
}
.buy-btn {
  padding: 8px 16px;
  background: #409cff;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}
.buy-btn:hover {
  background: #3080e0;
}
.tip {
  color: #999;
  text-align: center;
  font-size: 16px;
  margin: 60px 0;
}

/* 新增头部和返回按钮样式 */
.header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  position: relative;
}

.back-btn {
  position: absolute;
  left: 0;
  padding: 8px 16px;
  background: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-btn:hover {
  background: #e0e0e0;
}

.page-title {
  text-align: center;
  flex-grow: 1;
  margin: 0;
}
</style>