<!-- src/pages/TrainBooking.vue -->
<template>
  <div class="booking-page">
    <div class="booking-card">
      <h2>订单支付</h2>

      <!-- 订单信息展示 -->
      <div class="info">
        <p><strong>订单编号：</strong>{{ orderNumber }}</p>
        <p><strong>车次：</strong>{{ trainNumber }}</p>
        <p><strong>出发城市：</strong>{{ from }}</p>
        <p><strong>到达城市：</strong>{{ to }}</p>
        <p><strong>出发时间：</strong>{{ formatDateTime(departTime) }}</p>
        <p><strong>到达时间：</strong>{{ formatDateTime(arriveTime) }}</p>
        <p><strong>座席：</strong>{{ seat }}</p>
        <p><strong>票价：</strong>{{ price }} 元</p>
        <p v-if="remainingTime > 0" class="time-remaining">
          <strong>剩余支付时间：</strong>
          <span :class="{ 'text-danger': remainingTime < 30 }">
            {{ formatTime(remainingTime) }}
          </span>
        </p>
      </div>

      <hr />

      <div class="payment-actions">
        <p class="total">应付金额：<span>{{ price }} 元</span></p>
        <div class="buttons">
          <el-button
              type="primary"
              :loading="payLoading"
              @click="handlePayment"
          >
            立即支付
          </el-button>
          <el-button
              :loading="cancelLoading"
              @click="handleCancel"
          >
            放弃支付
          </el-button>
        </div>
        <div v-if="pollingMessage" class="status-message">
          <i class="el-icon-loading" v-if="isPolling"></i>
          {{ pollingMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { complete, fail } from '../api/pay'
import { doAsync } from '../api/train'

const route = useRoute()
const router = useRouter()

// 订单数据
const orderNumber = ref(route.query.number || '')
const id = ref(route.query.id || '')
const trainId = ref(route.query.trainId || '')
const trainNumber = ref(route.query.trainNumber || '')
const from = ref(route.query.from || '')
const to = ref(route.query.to || '')
const departTime = ref(route.query.departTime || '')
const arriveTime = ref(route.query.arriveTime || '')
const seat = ref(route.query.seat || '')
const price = ref(Number(route.query.price) || 0)

// 状态控制
const payLoading = ref(false)
const cancelLoading = ref(false)
const pollingInterval = ref(null)
const pollingMessage = ref('')
const remainingTime = ref(400) // 默认5分钟支付时限
const isPolling = ref(false)

// 方案1：确保 onMounted 触发
onMounted(() => {
  console.log('组件挂载，开始轮询')
  startPolling()
})

startPolling()

// 方案2：监听路由参数变化（可选）
watch(
    () => route.query.id,
    (newId) => {
      if (newId && newId !== id.value) {
        id.value = newId
        stopPolling()
        startPolling()
      }
    }
)

// 轮询控制
function startPolling() {
  stopPolling()
  isPolling.value = true
  pollingMessage.value = '正在获取订单状态...'

  // 立即检查一次
  checkOrderStatus()

  pollingInterval.value = setInterval(() => {
    checkOrderStatus()
  }, 1000)
}

function stopPolling() {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
  isPolling.value = false
}

// 检查订单状态
async function checkOrderStatus() {
  try {
    const { data } = await doAsync(id.value)

    if (data.remainingTimeSeconds) {
      remainingTime.value = data.remainingTimeSeconds
    }

    if (data.currentStatus === 'COMPLETED') {
      handlePaymentSuccess()
    } else if (data.currentStatus === 'FAILED') {
      await handlePaymentFailure('超时或主动放弃')
    }
  } catch (error) {
    console.error('轮询请求失败:', error)
  }
}

// 立即支付
async function handlePayment() {
  payLoading.value = true
  try {
    const payRes = await complete({ orderNumber: orderNumber.value })
    console.log(payRes)
    if (payRes.value) {
      pollingMessage.value = '支付请求已提交，请稍候...'
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '支付请求失败')
  } finally {
    payLoading.value = false
  }
}

// 放弃支付
async function handleCancel() {
  cancelLoading.value = true
  try {
    await fail({ orderNumber: orderNumber.value , data: seat.value })
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '取消失败')
  } finally {
    cancelLoading.value = false
  }
}

// 支付成功处理
function handlePaymentSuccess() {
  stopPolling()
  localStorage.setItem('currestTrainId', trainId.value)
  localStorage.setItem('currentSeatOrderId', id.value)
  pollingMessage.value = '支付成功，正在跳转...'
  setTimeout(() => {
    router.push({
      name: 'BookingSuccess',
      query: { orderNumber: orderNumber.value }
    })
  }, 1500)
}

// 支付失败处理
async function handlePaymentFailure(message) {
  stopPolling()
  pollingMessage.value = message
  setTimeout(() => {
    router.push({ name: 'HotelHome' })
  }, 1500)
}

// 格式化时间
function formatDateTime(isoString) {
  if (!isoString) return '--:--'
  const d = new Date(isoString)
  return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2,'0')}-${d.getDate().toString().padStart(2,'0')} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}分${secs}秒`
}
</script>

<style scoped>
.booking-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  background: linear-gradient(to right, #f5f7fa, #e4e8eb);
}

.booking-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 400px;
}

.info p {
  margin: 8px 0;
  font-size: 15px;
}

.time-remaining {
  margin-top: 12px;
  color: #666;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.payment-actions {
  margin-top: 20px;
}

.total {
  font-size: 18px;
  margin-bottom: 20px;
}

.total span {
  color: #409eff;
  font-weight: bold;
}

.buttons {
  display: flex;
  gap: 12px;
}

.buttons .el-button {
  flex: 1;
}

.status-message {
  margin-top: 12px;
  color: #666;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>