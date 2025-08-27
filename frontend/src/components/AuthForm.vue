<template>
  <div class="form-container">
    <h2 class="title">{{ titleMap[type] }}</h2>

    <input
        v-if="showField('email')"
        v-model="email"
        class="input"
        placeholder="邮箱"
        @focus="clearError"
        @input="clearError"
    />
    <input
        v-if="showField('username')"
        v-model="username"
        class="input"
        placeholder="用户名"
        @focus="clearError"
        @input="clearError"
    />
    <input
        v-if="showField('password')"
        type="password"
        v-model="password"
        class="input"
        placeholder="密码"
        @focus="clearError"
        @input="clearError"
    />
    <input
        v-if="showField('confirmPassword')"
        type="password"
        v-model="confirmPassword"
        class="input"
        placeholder="确认密码"
        @focus="clearError"
        @input="clearError"
    />

    <div v-if="showField('gender')" class="gender-options">
      <label class="gender-option">
        <input type="radio" v-model="gender" value="MALE" />
        男
      </label>
      <label class="gender-option">
        <input type="radio" v-model="gender" value="FEMALE" />
        女
      </label>
    </div>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <button class="action-btn" :disabled="loading" @click="handleSubmit">
      {{ loading ? '处理中...' : buttonTextMap[type] }}
    </button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { login, register } from '../api/register'
import { useUserStore } from '../store/user'
import { useRouter } from 'vue-router'

const props = defineProps({ type: String })
const emit = defineEmits(['success'])

const userStore = useUserStore()
const router = useRouter()

const email = ref('')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const gender = ref('MALE')
const errorMessage = ref('')
const loading = ref(false)
let errorTimer = null

const titleMap = {
  login: '账号密码登录',
  register: '欢迎加入 👋'
}
const buttonTextMap = {
  login: '登 录',
  register: '注 册'
}

const showField = (field) => {
  const fieldsMap = {
    login: ['email', 'password'],
    register: ['email', 'username', 'password', 'confirmPassword', 'gender']
  }
  return fieldsMap[props.type].includes(field)
}

// 自动清空错误提示
const showError = (msg) => {
  errorMessage.value = msg
  clearTimeout(errorTimer)
  errorTimer = setTimeout(() => {
    errorMessage.value = ''
  }, 3000)
}

// 用户输入时手动清除错误提示
const clearError = () => {
  errorMessage.value = ''
}

// 切换 tab 自动清空内容
watch(() => props.type, () => {
  email.value = ''
  username.value = ''
  password.value = ''
  confirmPassword.value = ''
  errorMessage.value = ''
})

const handleSubmit = async () => {
  if (loading.value) return

  errorMessage.value = ''
  loading.value = true

  try {
    if (props.type === 'login') {
      // 直接调用userStore.login（不再单独调用API）
      const success = await userStore.login({
        email: email.value,
        password: password.value
      })
      if (success) {
        emit('success', '登录成功')
        await router.push({name : 'train'})
      }
    } else if (props.type === 'register') {
      if (!email.value || !username.value || !password.value || !confirmPassword.value || !gender.value) {
        showError('请完整填写所有信息')
        return
      }
      if (password.value !== confirmPassword.value) {
        showError('两次密码不一致')
        return
      }
      const success =  await userStore.register({
        email: email.value,
        username: username.value,
        password: password.value,
        gender: gender.value,
        role: 'REGULAR'
      })
      if (success) {
        emit('success', '注册成功')
        await router.push({name: 'train'})
      } else {
        emit('error', '注册失败')
      }
    }
  } catch (err) {
    showError(err?.response?.data?.error || '操作失败，请检查信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
.title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}
.input {
  width: 80%;
  max-width: 300px;
  padding: 12px 14px;
  margin-bottom: 12px;
  border-radius: 10px;
  border: 1px solid #ccc;
  font-size: 15px;
}
.input:focus {
  border-color: #1677ff;
  box-shadow: 0 0 5px rgba(22, 119, 255, 0.3);
  outline: none;
}
.error {
  color: #e53935;
  font-size: 13px;
  margin-bottom: 10px;
  text-align: center;
}
.action-btn {
  width: 80%;
  max-width: 300px;
  padding: 12px;
  font-size: 16px;
  background-color: #1677ff;
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.action-btn:hover {
  background-color: #125ccc;
}
.action-btn:disabled {
  background-color: #999;
  cursor: not-allowed;
}
.gender-options {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 12px;
  width: 80%;
  max-width: 300px;
}
.gender-option {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
</style>