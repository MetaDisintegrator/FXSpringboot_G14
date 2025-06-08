<template>
  <div class="hotel-app">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <ul>
        <li class="active">
          <i class="iconfont">&#xe6b6;</i>酒店
        </li>
        <li :class="{ active: currentMenu === 'train' }" @click="goTrain">
          <i class="iconfont">&#xe608;</i>火车票
        </li>
      </ul>
    </aside>

    <!-- 右侧内容区，含导航和主内容 -->
    <div class="content-wrapper">
      <!-- 顶部导航栏 -->
      <header class="top-nav">
        <div class="logo-section" >
          <img class="logo" src="/images/ICON.png" alt="飞行旅行网">
          <span class="site-name">风行旅行</span>
        </div>
        <nav class="nav-links">
          <LoginNotice />
          <a href="#" @click.prevent="go('/orders')">我的订单</a>
          <a href="#" @click.prevent="go('/messages')" class="message-link">消息中心</a>
          <a href="#" @click.prevent="go('/aboutUs')">关于我们</a>
        </nav>
      </header>

      <!-- 主体内容与右侧榜单横向排列 -->
      <div class="content-body">
        <!-- 主内容区 -->
        <main class="main-content">
          <!-- 预订酒店模块 -->
          <section class="booking-section">
            <div class="booking-bg"></div>
            <h2>预订酒店</h2>
            <form class="booking-form" @submit.prevent="handleSearch">
              <div class="form-row">
                <label>
                  目的地/酒店名称
                  <input v-model="searchCity" placeholder="请输入城市或酒店" />
                </label>
                <label>
                  入住
                  <input type="date" v-model="checkIn" />
                </label>
                <span class="nights">- {{ nights }}晚 -</span>
                <label>
                  退房
                  <input type="date" v-model="checkOut" />
                </label>
                <button class="search-btn" type="submit">搜索</button>
              </div>
            </form>
          </section>

          <!-- 酒店推荐，使用背景图 -->
            <section class="recommend-section">
              <div class="recommend-title">
                <span>酒店</span><span class="highlight">推荐</span>
              </div>
              <div class="city-tabs">
                <button
                  v-for="city in tabCities"
                  :key="city"
                  :class="{ active: city === currentCity }"
                  @click="setCity(city)">
                  {{ city }}
                </button>
              </div>
              <div class="hotel-list">
                <div
                  v-for="hotel in filteredHotels"
                  :key="hotel.id"
                  class="hotel-card">
                  <img :src="hotel.img" class="hotel-img" />
                  <div class="hotel-info">
                    <div class="hotel-name">{{ hotel.name }}</div>
                    <div class="stars">
                      <i v-for="i in hotel.stars" :key="i" class="iconfont">&#xe60a;</i>
                    </div>
                    <div class="hotel-price">￥{{ hotel.price }} 起</div>
                  </div>
                </div>
              </div>
            </section>
          </main>

        <!-- 右侧榜单推荐 -->
        <aside class="rightbar">
          <div class="rank-title">北京酒店口碑榜</div>
          <div class="rank-list">
            <div class="rank-item" v-for="item in rankHotels" :key="item.id">
              <img :src="item.img" class="rank-img" />
              <div class="rank-info">
                <div class="rank-name">{{ item.name }}</div>
                <div class="rank-price">￥{{ item.price }}起</div>
              </div>
            </div>
          </div>

        </aside>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import LoginNotice from "../components/LoginNotice.vue";

const router = useRouter()
const route = useRoute()

const currentMenu = ref(route.path.startsWith('/train') ? 'train' : 'hotel')

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

onMounted(() => {
  const name = localStorage.getItem('username')
  const avatar = localStorage.getItem('avatar')
  if (name) {
    username.value = name
    isLoggedIn.value = true
    avatarUrl.value = avatar || '/images/default-avatar.png'
  }
})


// 计算住房天数
const nights = computed(() => {
  if (!checkIn.value || !checkOut.value) return 1 // 如果日期未选择，默认显示1晚

  const startDate = new Date(checkIn.value)
  const endDate = new Date(checkOut.value)

  // 计算天数差
  const timeDifference = endDate - startDate
  const dayDifference = Math.floor(timeDifference / (1000 * 60 * 60 * 24))

  return dayDifference > 0 ? dayDifference : 1 // 至少显示1晚
})

function go(path) {
  router.push(path); // 使用 Vue Router 进行页面导航
}

function goTrain() {
  currentMenu.value = 'train'
  if (route.path !== '/train') router.push('/train')
}
//搜索表单绑定
const searchCity = ref('北京')
const checkIn = ref(new Date().toISOString().split('T')[0])
const checkOut = ref(new Date(Date.now() + 86400000).toISOString().split('T')[0])
//城市标签
const tabCities = ['上海', '北京', '广州', '三亚']
const currentCity = ref('上海')
const setCity = city => (currentCity.value = city)

const hotels = ref([
  {
    id: 1,
    city: '上海',
    name: '上海吉臣维景酒店',
    stars: 4,
    price: 798,
    img: '../public/images/weijing.png'
  },
  {
    id: 2,
    city: '上海',
    name: '上海陆家嘴酒店',
    stars: 5,
    price: 1588,
    img: '../public/images/lujiazuiHotel.png'
  },
  {
    id: 3,
    city: '上海',
    name: '上海徐家汇禧玥酒店',
    stars: 3,
    price: 2999,
    img: '../public/images/xujiahuiHotel.webp'
  },
  {
    id: 4,
    city: '北京',
    name: '北京王府井希尔顿酒店',
    stars: 5,
    price: 999,
    img: '../public/images/huaerdaofu.png'
  },
  {
    id: 5,
    city: '广州',
    name: '广州天河希尔顿酒店',
    stars: 5,
    price: 799,
    img: '../public/images/bandao.webp'
  },
  {
    id: 6,
    city: '三亚',
    name: '三亚中心皇冠假日酒店',
    stars: 5,
    price: 1190,
    img: '../public/images/crown.png'
  }
])
const filteredHotels = computed(() => hotels.value.filter(h => h.city === currentCity.value))

const rankHotels = [
  {
    id: 1,
    name: '君悦大酒店',
    price: 2484,
    img: '../public/images/wangfujing.webp'
  },
  {
    id: 2,
    name: '华尔道夫酒店',
    price: 2899,
    img: '../public/images/huaerdaofu.png'
  },
  {
    id: 3,
    name: '皇冠假日酒店',
    price: 1498,
    img: '../public/images/crown.png'
  }
]
// 点击搜索：跳转到 HotelSearch 并传递查询参数
function handleSearch() {
  router.push({
    name: 'HotelSearch',
    query: {
      destination: searchCity.value,
      checkInDate: checkIn.value,
      checkOutDate: checkOut.value
    }
  })
}
</script>


<style scoped>
@import url('//at.alicdn.com/t/font_2738890_1x2k9b0b1c6.css'); /* 示例iconfont库 */
@import url('https://fonts.googleapis.com/css2?family=Pacifico&display=swap');

.nav-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* logo 与站点名的容器 */
.logo-container {
  display: flex;
  align-items: center;
}

/* 原 logo 样式，去掉行内高度，统一交给 CSS 控制 */
.logo {
  height: 40px;
  width: auto;
}

/* “风行旅行”艺术字体样式 */
.site-name {
  margin-left: 10px;
  font-family: 'Pacifico', cursive; /* 艺术字体 */
  font-size: 26px;                  /* 根据设计稿可微调 */
  color: #1677ff;                   /* 蓝色 */
  line-height: 1;
}
.hotel-app {
  display: flex;
  background: #f5f7fa;
  min-height: 100vh;
  font-family: "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}
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
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
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
.sidebar li.active, .sidebar li:hover {
  background: #e6f6ff;
  color: #1677ff;
}
.sidebar .iconfont {
  font-size: 20px;
  margin-right: 12px;
}

.main-content {
  flex: 1;
  min-width: 0;
  padding: 24px 28px 24px 32px;
}

.booking-section {
  position: relative;
  background: linear-gradient(rgba(255,255,255,0.6), rgba(255,255,255,0.6)),
              url('/images/hotel_1.jpg'); /* 本地图片路径 */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 40px 24px;
  margin-bottom: 20px;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(22, 119, 255, 0.1);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .booking-section {
    background-image: url('/images/hotel_2.jpg'); /* 移动端专用图片 */
  }
}

/* 可选：如果使用独立背景层 */
.booking-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url(https://pic.c-ctrip.com/platform/h5/hotel/bg-hotel-search.png) center/cover;
  z-index: 1;
  opacity: 0.1; /* 调整透明度 */
}

/* 调整表单内容层级 */
.booking-section h2,
.booking-form {
  position: relative; /* 提升层级 */
  z-index: 2; /* 确保内容在背景之上 */
}

.booking-form .form-row {
  background: rgba(255,255,255,0.85); /* 表单半透明白底 */
  padding: 20px;
  border-radius: 12px;
  backdrop-filter: blur(4px); /* 毛玻璃效果 */
}

.booking-form label {
  display: flex;
  flex-direction: column;
  font-size: 15px;
  color: #666;
}
.booking-form input[type="date"], .booking-form input[type="text"], .booking-form input {
  border: 1px solid #e6e6e6;
  border-radius: 5px;
  padding: 6px 10px;
  margin-top: 6px;
  font-size: 15px;
  background: #f9f9f9;
  width: 130px;
}
.booking-form .nights {
  color: #999;
  font-size: 14px;
}
.search-btn {
  background: #1677ff;
  border: none;
  color: #fff;
  padding: 10px 28px;
  font-size: 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-left: 10px;
  transition: background .2s;
}
.search-btn:hover {
  background: #145ecb;
}
.banner-section {
  margin: 20px 0;
}
.banner-img {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px #e6f6ff;
}

.recommend-section {
  margin-top: 12px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px #e6f6ff;
  padding: 18px 18px 18px 18px;
}
.recommend-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}
.recommend-title .highlight {
  color: #ff8800;
  font-weight: 700;
}
.city-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 18px;
}
.city-tabs button {
  background: #f5f7fa;
  border: none;
  border-radius: 16px;
  color: #222;
  font-size: 15px;
  padding: 6px 20px;
  cursor: pointer;
  transition: background .2s;
}
.city-tabs button.active, .city-tabs button:hover {
  background: #1677ff;
  color: #fff;
}
.hotel-list {
  display: flex;
  gap: 22px;
  overflow-x: auto;
  padding-bottom: 10px;
}
.hotel-card {
  width: 200px;
  background: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px #e6f6ff;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform: perspective(1px) translateZ(0);
}
.hotel-card:hover {
  transform: perspective(1px) scale(1.03);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.2);
  z-index: 1;
}
.hotel-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}
.hotel-info {
  padding: 10px 12px 14px 12px;
}
.hotel-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 5px;
}
.stars {
  color: #fbbf24;
  margin-bottom: 5px;
}
.hotel-price {
  color: #1677ff;
  font-weight: 700;
  font-size: 15px;
}
.rightbar {
  width: 270px;
  padding: 34px 14px 0 14px;
}
.rank-title {
  background: #1677ff;
  color: #fff;
  padding: 10px 18px;
  border-radius: 8px 8px 0 0;
  font-size: 16px;
  font-weight: 700;
}
.rank-list {
  background: #fff;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 2px 8px #e6f6ff;
  padding: 14px 0 14px 0;
  margin-bottom: 18px;
}
.rank-item {
  display: flex;
  align-items: center;
  margin-bottom: 13px;
}
.rank-img {
  width: 46px;
  height: 46px;
  border-radius: 7px;
  margin-right: 10px;
  object-fit: cover;
}
.rank-info {
  flex: 1;
}
.rank-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 3px;
}
.rank-price {
  color: #1677ff;
  font-size: 13px;
}
.right-pic {
  width: 100%;
  border-radius: 13px;
  margin-top: 8px;
  object-fit: cover;
}
@media (max-width: 1100px) {
  .rightbar { display: none; }
}
@media (max-width: 900px) {
  .sidebar { display: none; }
  .main-content { padding-left: 10px; padding-right: 10px; }
}
/* 内容区包含头部和主体 */
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10;
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

/* 主体与侧边并排 */
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

/* 右侧榜单容器 */
.rightbar {
  width: 270px;
  padding: 34px 14px 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(22,119,255,0.1);

  /* 新增背景图 */
  background: url('/images/hotel_2.jpg') no-repeat center/cover;
  position: relative;
  color: #fff;             /* 文本改为白色，便于在深色背景上可读 */
  overflow: hidden;
}

/* 遮罩层：深色半透明，提升对比度 */
.rightbar::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 10px;
  z-index: 0;
}

/* 保证子元素在遮罩之上 */
.rightbar > * {
  position: relative;
  z-index: 1;
}

/* 调整榜单标题和列表背景为半透明 */
.rank-title {
  background: rgba(22,119,255,0.8);
  color: #fff;
}
.rank-list {
  background: rgba(255,255,255,0.15);
  border-radius: 0 0 10px 10px;
  box-shadow: none;
  padding: 14px 0;
  margin-bottom: 18px;
}
.rank-item {
  margin-bottom: 12px;
}
.rank-info .rank-price {
  color: #ffd700; /* 金色高亮价格 */
}

/* 酒店推荐背景 */
.recommend-section {
  margin-top: 12px;
  background: url('/images/hotel_IMG.jpeg') center/cover no-repeat;
  border-radius: 10px;
  box-shadow: 0 2px 8px #e6f6ff;
  padding: 18px;
  color: #fff;
}
.recommend-title, .city-tabs button, .hotel-card {
  background: rgba(255,255,255,0.2);
}
.city-tabs button.active, .city-tabs button:hover {
  background: #1677ff;
  color: #fff;
}
.hotel-list {
  display: flex;
  gap: 22px;
  overflow-x: auto;
  padding-bottom: 10px;
}
.hotel-card {
  width: 200px;
  background: rgba(0,0,0,0.4);
}
.hotel-name, .hotel-price { color: #fff; }
.stars { color: #fbbf24; }

/* 给“消息中心”链接多一点左侧 padding，以对齐图标与文字 */
.message-link {
  padding-left: 12px;
}
</style>