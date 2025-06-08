<!-- src/pages/OrderHistory.vue -->
<template>
  <div class="order-history">
    <h2>🧾 我的历史订单</h2>

    <!-- 🔍 筛选工具条 -->
    <div class="filters">
      <el-input
          v-model="keyword"
          placeholder="搜索车次/站点/餐品"
          clearable
          class="filter-item"
      />
      <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="filter-item"
      />
      <el-select
          v-model="selectedStatus"
          placeholder="订单状态"
          clearable
          class="filter-item"
      >
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="处理中" value="PENDING" />
      </el-select>
      <el-button type="primary" @click="fetchOrders" class="filter-item">
        查询
      </el-button>
    </div>

    <div v-if="loading" class="loading">订单加载中...</div>
    <div
        v-else-if="!filteredTickets.length && !filteredMeals.length"
        class="empty"
    >
      暂无订单记录
    </div>

    <!-- 🎫 车票订单 -->
    <div v-if="filteredTickets.length" class="section">
      <h3>车票订单</h3>
      <el-row :gutter="20">
        <el-col
            :xs="24"
            :sm="12"
            :md="8"
            v-for="order in filteredTickets"
            :key="order.id"
        >
          <el-card class="order-card">
            <div class="info">
              <p><strong>订单号：</strong>{{ order.id }}</p>
              <p><strong>车次：</strong>{{ order.train.trainNumber }}</p>
              <p><strong>出发：</strong>{{ order.train.fromStation }}</p>
              <p><strong>到达：</strong>{{ order.train.toStation }}</p>
              <p>
                <strong>出发时间：</strong
                >{{ formatTime(order.train.departureTime) }}
              </p>
              <p><strong>车型：</strong>{{ order.train.trainType }}</p>
              <p><strong>座位类型：</strong>{{ order.trainSeat.seatType }}</p>
              <p><strong>状态：</strong>{{ formatStatus(order.status) }}</p>
            </div>
            <div class="actions">
              <el-button size="small" @click="copyTicket(order)"
              >复制订单</el-button
              >
              <el-button
                  size="small"
                  type="warning"
                  @click="goToMeal(order.train.trainNumber)"
              >订餐</el-button
              >
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 🍱 订餐订单 -->
    <div v-if="filteredMeals.length" class="section">
      <h3>订餐订单</h3>
      <el-row :gutter="20">
        <el-col
            :xs="24"
            :sm="12"
            :md="8"
            v-for="meal in filteredMeals"
            :key="meal.id"
        >
          <el-card class="order-card">
            <div class="info">
              <p><strong>车次订单号：</strong>{{ meal.reservationSeatOrderNumber }}</p>
              <p><strong>餐品：</strong>{{ meal.trainMealName }}</p>
              <p><strong>金额：</strong>￥{{ meal.totalAmount }}</p>
              <p><strong>状态：</strong>{{ formatStatus(meal.status) }}</p>
              <p><strong>订单生成时间：</strong>{{ formatTime(meal.createTime) }}</p>
            </div>
            <div class="actions">
              <el-button size="small" @click="copyMeal(meal)"
              >复制订单</el-button
              >
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { searchTrainSeatOrder } from '../api/train' 
import { searchTrainMealOrder } from '../api/trainMeal'


const userStore = useUserStore()
const router = useRouter()

const userId = userStore.userInfo?.id

const ticketOrders = ref([])
const mealOrders = ref([])
const loading = ref(false)

const keyword = ref('')
const selectedStatus = ref('')
const dateRange = ref([])

// 格式化函数
const formatTime = dt => new Date(dt).toLocaleString()
const formatStatus = s =>
    ({
      COMPLETED: '已完成',
      CANCELLED: '已取消',
      PENDING: '处理中',
      IDLE: '未开始'
    }[s] || s)

// 拉取订单数据
async function fetchOrders() {
  if (!userId) {
    ElMessage.warning('请先登录后查看订单')
    return router.push({ name: 'Login' })
  }

  loading.value = true
  try {
    // ✅ 使用封装好的车票订单接口
    const ticketRes = await searchTrainSeatOrder(userId)
    ticketOrders.value = ticketRes?.data || []

    // ✅ 保留原来的订餐订单（你可以封装成接口再替换）
    const mealRes = await searchTrainMealOrder(userId)
    mealOrders.value = mealRes?.data.data || []
  } catch (e) {
    console.error(e)
    ElMessage.error('订单加载失败')
  } finally {
    loading.value = false
  }
}

// 复制订单信息
const copyTicket = order => {
  const text = `车票订单：订单号 ${order.id}，车次 ${order.train.trainNumber}，${order.train.fromStation}→${order.train.toStation}，时间 ${formatTime(order.train.departureTime)}，状态 ${formatStatus(order.status)}`
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('车票订单信息已复制')
  })
}
const copyMeal = meal => {
  const text = `订餐订单：订单号 ${meal.id}，车次 ${meal.trainNumber}，餐品 ${meal.items?.join('、')}，金额 ￥${meal.total}，状态 ${formatStatus(meal.status)}`
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('订餐订单信息已复制')
  })
}

// 跳转订餐
function goToMeal(trainNumber) {
  router.push({
    name: 'TrainMeal',
    query: { trainId: trainNumber }
  })
}

onMounted(fetchOrders)

const filteredTickets = computed(() =>
    ticketOrders.value.filter(o => {
      const matchKeyword =
          !keyword.value ||
          `${o.train.trainNumber} ${o.train.fromStation} ${o.train.toStation}`.includes(keyword.value)
      const matchStatus =
          !selectedStatus.value || o.status === selectedStatus.value
      const matchDate =
          !dateRange.value.length ||
          (new Date(o.train.departureTime) >= new Date(dateRange.value[0]) &&
              new Date(o.train.departureTime) <= new Date(dateRange.value[1]))
      return matchKeyword && matchStatus && matchDate
    })
)

const filteredMeals = computed(() =>
    mealOrders.value.filter(m => {
      const matchKeyword =
          !keyword.value ||
          `${m.trainNumber} ${m.items?.join(',')}`.includes(keyword.value)
      const matchStatus =
          !selectedStatus.value || m.status === selectedStatus.value
      return matchKeyword && matchStatus
    })
)
</script>


<style scoped>
.order-history {
  padding: 24px;
  background: #f5f7fa;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}
.filter-item {
  flex: 1;
  min-width: 200px;
}
.section {
  margin-top: 24px;
}
.order-card {
  border-radius: 8px;
}
.order-card .info p {
  margin: 4px 0;
}
.actions {
  text-align: right;
  margin-top: 8px;
}
.loading,
.empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
}
</style>
