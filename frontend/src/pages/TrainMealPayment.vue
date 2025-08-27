<!-- src/pages/TrainMealPayment.vue -->
<template>
  <div class="payment-page">
    <div class="payment-card">
      <h2>餐食订单支付</h2>

      <!-- 订单信息展示 -->
      <div class="info">
        <p><strong>订单编号：</strong>{{ orderNumber }}</p>
        <p><strong>车次：</strong>{{ trainInfo.trainNumber }}</p>
        <p><strong>餐品名称：</strong>{{ mealInfo.name }}</p>
        <p><strong>餐品描述：</strong>{{ mealInfo.description }}</p>
        <p><strong>单价：</strong>{{ mealInfo.price }} 元</p>
        <p><strong>数量：</strong>1</p>
        <p v-if="remainingTime > 0" class="time-remaining">
          <strong>剩余支付时间：</strong>
          <span :class="{ 'text-danger': remainingTime < 30 }">
            {{ formatTime(remainingTime) }}
          </span>
        </p>
      </div>

      <hr />

      <div class="payment-actions">
        <p class="total">应付金额：<span>{{ mealInfo.price }} 元</span></p>
        <div class="buttons">
          <button
              class="pay-btn"
              :disabled="payLoading"
              @click="handlePayment"
          >
            {{ payLoading ? '支付中...' : '立即支付' }}
          </button>
          <button
              class="cancel-btn"
              :disabled="cancelLoading"
              @click="handleCancel"
          >
            {{ cancelLoading ? '取消中...' : '放弃支付' }}
          </button>
        </div>
        <div v-if="pollingMessage" class="status-message">
          <span v-if="isPolling" class="loading-icon">⏳</span>
          {{ pollingMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {doAsync} from "../api/trainMeal.js";
import {complete, fail} from "../api/pay.js";
import {ElMessage} from "element-plus";

const route = useRoute()
const router = useRouter()

// 订单数据
const trainInfo = ref({
  trainNumber: route.query.trainNumber || ''
})
const mealInfo = ref({
  mealId: route.query.mealId || '',
  name: route.query.name || '暂无名称',
  description: route.query.description || '暂无描述',
  price: route.query.price || 0
})

const id = ref(route.query.orderId || 0)
const orderNumber = ref(route.query.orderNumber || '')

// 状态控制
const payLoading = ref(false)
const cancelLoading = ref(false)
const pollingInterval = ref(null)
const remainingTime = ref(400) // 5分钟支付时限
const pollingMessage = ref('')
const isPolling = ref(false)

// 生命周期
onMounted(() => {
  startPolling()
})

startPolling()

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
    await fail({ orderNumber: orderNumber.value })
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '取消失败')
  } finally {
    cancelLoading.value = false
  }
}

// 支付成功处理
function handlePaymentSuccess() {
  stopPolling()
  pollingMessage.value = '支付成功，正在跳转...'
  setTimeout(() => {
    router.push({ name: 'HotelHome' })
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
function formatTime(seconds) {
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}分${secs}秒`
}
</script>

<style scoped>
.payment-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 90vh;
  background: #f5f7fa;
  padding: 20px;
}

.payment-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
}

.info p {
  margin: 10px 0;
  font-size: 15px;
  line-height: 1.5;
}

.time-remaining {
  margin-top: 15px;
  color: #666;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.payment-actions {
  margin-top: 25px;
}

.total {
  font-size: 18px;
  margin-bottom: 20px;
  text-align: right;
}

.total span {
  color: #409eff;
  font-weight: bold;
  font-size: 20px;
}

.buttons {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.pay-btn, .cancel-btn {
  flex: 1;
  padding: 12px;
  border-radius: 6px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.pay-btn {
  background: #409eff;
  color: white;
  border: none;
}

.pay-btn:hover:not(:disabled) {
  background: #3080e0;
}

.pay-btn:disabled {
  background: #a0c9ff;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
}

.cancel-btn:hover:not(:disabled) {
  background: #e5e5e5;
}

.cancel-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.status-message {
  margin-top: 15px;
  color: #666;
  font-size: 14px;
  text-align: center;
  min-height: 20px;
}

.loading-icon {
  display: inline-block;
  margin-right: 5px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

hr {
  border: none;
  border-top: 1px solid #eee;
  margin: 20px 0;
}
</style>