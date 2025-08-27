<template>
  <div class="hotel-app">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <ul>
        <li class="sidebar-item" :class="{ active: currentMenu === 'hotel' }" @click="goHome">
          <i class="iconfont">&#xe6b6;</i>酒店
        </li>
        <li class="sidebar-item" :class="{ active: currentMenu === 'train' }" @click="goTrain">
          <i class="iconfont">&#xe608;</i>火车票
        </li>
        <li class="sidebar-item" :class="{ active: currentMenu === 'messages' }" @click="goMessages">
          <i class="iconfont">&#xe600;</i>消息
          <!-- 顶部红点（Badge） -->
          <span v-if="unreadCount > 0" class="red-dot"></span>
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
          <a class="nav-link" @click.prevent="go('/login')">登录</a>
          <a class="nav-link" @click.prevent="go('/register')">注册</a>
          <a class="nav-link" @click.prevent="go('/orders')">我的订单</a>
          <a class="nav-link active" @click.prevent="go('/messages')">消息中心</a>
          <a class="nav-link" @click.prevent="go('/aboutUs')">关于我们</a>
        </nav>
      </header>

      <!-- 主体内容 -->
      <div class="content-body">
        <main class="main-content">
          <h2 class="page-title">消息中心</h2>

          <!-- 分类 Tab -->
          <el-tabs v-model="activeTab" class="message-tabs" @tab-click="onTabClick">
            <el-tab-pane label="全部消息" name="all" />
            <el-tab-pane label="订单消息" name="order" />
            <el-tab-pane label="系统通知" name="system" />
          </el-tabs>

          <!-- 如果列表为空时展示提示 -->
          <div v-if="messages.length === 0" class="empty-tip">
            暂无消息
          </div>

          <!-- 消息列表 -->
          <div v-else class="message-list">
            <el-card
              v-for="msg in messages"
              :key="msg.mealId"
              class="message-card fade-in-card"
              :body-style="{ padding: '12px 16px' }"
              @click="markAsRead(msg)"
            >
              <div class="message-card-content">
                <i :class="getIconClass(msg.eventType)" class="message-icon"></i>
                <div class="message-info">
                  <div class="message-header">
                    <span class="message-title">
                      {{ msg.content }}
                      <span v-if="!msg.read" class="unread-dot"></span>
                    </span>
                    <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 分页 -->
          <el-pagination
            v-if="total > pageSize"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            v-model:current-page="currentPage"
            @current-change="fetchMessages"
            class="message-pagination"
          />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  fetchMessageHistory,
  // markNotificationRead, // 如果后端提供“标记已读”接口，可取消注释并调用
} from '../api/notification'

// router / route
const router = useRouter()
const route = useRoute()

// 当前菜单，用于高亮侧边栏
const currentMenu = ref('messages')

// 路由跳转函数
const go = path => router.push(path)
const goHome = () => router.push({ name: 'HotelHome' })
const goTrain = () => router.push({ name: 'train' })
const goMessages = () => router.push({ name: 'MessageCenter' })

// 消息分页、筛选的相关状态
const activeTab = ref('all')    // all / order / system
const messages = ref([])
const total = ref(0)
const pageSize = ref(10)
const currentPage = ref(1)
const unreadCount = ref(0)

// 根据 activeTab 切换，重新拉取
const onTabClick = pane => {
  currentPage.value = 1
  fetchMessages(1)
}

// 从后端拉取消息（分页+分类），并统计未读数
async function fetchMessages(page = 1) {
  currentPage.value = page

  // 构造查询参数
  const params = {
    userId: getCurrentUserId(), // 请根据实际获取当前用户 ID
    page,
    size: pageSize.value,
    type: activeTab.value === 'all' ? '' : activeTab.value.toUpperCase(), // order 或 system
  }
  try {
    const { data } = await fetchMessageHistory(params.userId)
    // 假设后端直接返回一个数组，或者 data.records + data.total
    // 具体字段请根据后端实际返回做调整
    messages.value = data.records || data || []
    total.value = data.total || messages.value.length

    // 计算未读数量
    unreadCount.value = messages.value.filter(m => !m.read).length
  } catch (err) {
    console.error('拉取消息失败：', err)
  }
}

// 格式化时间戳
function formatTime(ts) {
  const d = new Date(ts)
  const MM = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${d.getFullYear()}-${MM}-${dd} ${hh}:${mm}`
}

// 点击某条消息时，将其标为“已读”（前端本地置灰），如果后端提供接口可调用
function markAsRead(msg) {
  if (!msg.read) {
    msg.read = true
    unreadCount.value--
    // 如果后端提供“标记已读”接口，取消注释并调用：
    // markNotificationRead(msg.id).catch(() => { /* 即使失败，也先在前端置灰 */ })
  }
  // 若需要点击后跳转到某页面，可在这里加逻辑，例如：
  // if (msg.eventType === 'ORDER_CANCELLED') router.push({ name: 'OrderDetail', params: { id: msg.orderId } })
}

onMounted(() => {
  fetchMessages(1)
})

// 示例：获取当前用户 ID，实际项目请替换为你自己的用户状态管理
function getCurrentUserId() {
  // 假设 localStorage 中存放了 user 对象
  const u = localStorage.getItem('currentUser')
  return u ? JSON.parse(u).mealId : 0
}

// 根据不同 eventType 返回不同图标类名
function getIconClass(eventType) {
  switch (eventType) {
    case 'TICKET_PURCHASED':
      return 'iconfont &#xe602;'   // 自行替换为合适的 iconfont
    case 'ORDER_CANCELLED':
      return 'iconfont &#xe603;'
    case 'PAYMENT_COMPLETED':
      return 'iconfont &#xe604;'
    case 'MEAL_ORDERED':
      return 'iconfont &#xe605;'
    case 'HOTEL_BOOKED':
      return 'iconfont &#xe606;'
    default:
      return 'iconfont &#xe600;'
  }
}
</script>

<style scoped>
@import url('//at.alicdn.com/t/font_2738890_1x2k9b0b1c6.css');
@import url('https://fonts.googleapis.com/css2?family=Pacifico&family=Montserrat:wght@400;600&display=swap');

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
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.05);
}
.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sidebar-item {
  display: flex;
  align-items: center;
  padding: 14px 26px;
  margin-bottom: 8px;
  font-size: 16px;
  color: #222;
  cursor: pointer;
  border-radius: 5px 0 0 5px;
  position: relative;
  transition: background 0.2s, transform 0.2s;
}
.sidebar-item.active,
.sidebar-item:hover {
  background: #e6f6ff;
  color: #1677ff;
  transform: translateX(8px);
}
.sidebar-item .iconfont {
  font-size: 20px;
  margin-right: 12px;
}
/* 小红点 */
.red-dot {
  position: absolute;
  top: 12px;
  right: 16px;
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  z-index: 2;
}

/* 顶部导航 */
.content-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}
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
.logo-section:hover .site-name {
  color: #145ecb;
}
.nav-links {
  display: flex;
  align-items: center;
}
.nav-link {
  margin-left: 24px;
  color: #333;
  font-weight: 500;
  transition: color 0.2s;
  text-decoration: none;
}
.nav-link:hover {
  color: #1677ff;
}
.nav-link.active {
  color: #1677ff;
  text-decoration: underline;
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

/* 标题 */
.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
}

/* Tab 样式 */
.message-tabs {
  margin-bottom: 16px;
}

/* 空列表提示 */
.empty-tip {
  text-align: center;
  color: #999;
  margin-top: 40px;
  font-size: 16px;
}

/* 消息卡片 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.message-card {
  cursor: pointer;
  transition: box-shadow 0.2s;
  border-radius: 8px;
}
.message-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
/* 卡片渐显 */
.fade-in-card {
  opacity: 0;
  transform: translateY(20px);
  animation: fade-in-up 0.5s forwards;
}
.fade-in-card:nth-child(1) {
  animation-delay: 0.2s;
}
.fade-in-card:nth-child(2) {
  animation-delay: 0.3s;
}
.fade-in-card:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes fade-in-up {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 卡片内容布局 */
.message-card-content {
  display: flex;
  align-items: flex-start;
}
.message-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #409eff;
}
.message-info {
  flex: 1;
}
.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.message-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  position: relative;
}
.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  background-color: #f56c6c;
  border-radius: 50%;
  margin-left: 6px;
  vertical-align: middle;
}
.message-time {
  font-size: 12px;
  color: #999;
}
.message-body {
  margin-top: 6px;
  font-size: 14px;
  color: #666;
}

/* 分页 */
.message-pagination {
  text-align: center;
  margin-top: 20px;
}
</style>