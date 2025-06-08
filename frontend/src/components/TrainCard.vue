<!-- src/components/TrainCard.vue -->
<template>
  <div class="train-card">
    <div class="train-header">
      <span class="train-id">车次：{{ train.trainNumber }}</span>
      <span class="train-type">类型：{{ train.trainType }}</span>
    </div>

    <div class="train-stations">
      <span>{{ train.fromStation }} → {{ train.toStation }}</span>
    </div>

    <div class="train-times">
      <span>出发：{{ formatDateTime(train.departureTime) }}</span>
      <span>到达：{{ formatDateTime(train.arrivalTime) }}</span>
      <span>历时：{{ train.durationMinutes }} 分钟</span>
    </div>

    <div v-if="seats && seats.length" class="seat-info">
      <h4>座位 & 票价：</h4>
      <ul>
        <li v-for="seatItem in seats" :key="seatItem.id" class="seat-item">
          <span>
            {{ seatItem.seatType }} – ¥{{ seatItem.price }} – 剩余：{{ seatItem.remain }}
          </span>
          <button
              class="book-btn"
              @click="bookSeat(seatItem)"
              :disabled="seatItem.remain === 0"
          >
            {{ seatItem.remain > 0 ? '预订' : '售罄' }}
          </button>
        </li>
      </ul>
    </div>

    <div v-else class="no-seat">
      暂无座位信息
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { startPayment } from '../api/train'
import { useUserStore } from '../store/user'

const props = defineProps({
  train: {
    type: Object,
    required: true
  },
  seats: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()

const userStore = useUserStore()
const userId = userStore.userInfo?.id  // 👈 获取当前用户 id

async function bookSeat(seatItem) {
  const payload = {
    trainId: props.train.id,
    trainNumber: props.train.trainNumber,
    from: props.train.fromStation,
    to: props.train.toStation,
    departTime: props.train.departureTime,
    arriveTime: props.train.arrivalTime,
    seatType: seatItem.seatType,
    seatId: seatItem.id,
    price: seatItem.price,
    userId: userId
  }

  try {
    const { data } = await startPayment(payload)

    if (data.message) {
      ElMessage.success(data.message)
    }

    // 确保获取到 id 和 number
    const orderId = data.id
    const orderNumber = data.number
    const seat = data.seat

    if (!orderId || !orderNumber) {
      ElMessage.error('下单失败：未获取订单信息')
      return
    }

    await router.push({
      name: 'TrainBooking',
      query: {
        ...payload,
        id: orderId,
        number: orderNumber,
        seat: seat
      }
    })
  } catch (err) {
    console.error('startPayment 异常：', err)
    const msg = err.response?.data?.message || '下单失败，请稍后重试'
    ElMessage.error(msg)
  }
}

function formatDateTime(isoString) {
  if (!isoString) return ''
  const d = new Date(isoString)
  const Y = d.getFullYear()
  const M = String(d.getMonth() + 1).padStart(2, '0')
  const D = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}`
}
</script>

<style scoped>
.train-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.train-header {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
}

.train-stations {
  color: #555;
  font-size: 14px;
}

.train-times {
  display: flex;
  gap: 16px;
  color: #777;
  font-size: 14px;
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

.seat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
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

.no-seat {
  font-size: 13px;
  color: #999;
}
</style>
