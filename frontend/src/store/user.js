// src/store/user.js
import {defineStore} from 'pinia'
import {getCurrentUser as apiGetCurrentUser,
    login as apiLogin,
    logout as apiLogout,
    register as apiRegister,
} from '../api/register'

export const useUserStore = defineStore('user', {
    state: () => ({
        // 登录状态
        isLoggedIn: false,
        // 当前用户信息（从后端 /user/userdata 拉取）
        userInfo: {
            id: null,
            email: '',
            username: '',
            verified: false,
            gender: '',
            role: ''
        }
    }),

    actions: {
        /**
         * 1. 登录：调用后端 /auth/login 接口，登录成功后拉取当前用户信息
         * @param {Object} payload { email: string, password: string }
         */
        async login(payload) {
            try {
                // 调用后端 /auth/login（HttpSession + Cookie）
                await apiLogin({
                    email: payload.email,
                    password: payload.password,
                })

                // 登录成功后，从后端 /user/userdata 获取用户信息
                await this.fetchCurrentUser()
                console.log("login success")
                return true
            } catch (err) {
                // 捕获后端返回的错误信息
                this.resetState()
                return false
            }
        },

        /**
         * 1.5. 注册
         */
        async register(payload) {
            try {
                // 调用后端 /auth/login（HttpSession + Cookie）
                await apiRegister({
                    email: payload.email,
                    username: payload.username,
                    password: payload.password,
                    gender: payload.gender,
                    role: payload.role,
                })

                // 注册成功后，从后端 /user/userdata 获取用户信息
                await this.fetchCurrentUser()
                console.log("register success")
                return true
            } catch (err) {
                // 捕获后端返回的错误信息
                this.resetState()
                return false
            }
        },

        /**
         * 2. 退出登录：调用后端 /auth/logout，然后清空本地状态
         */
        async logout() {
            try {
                await apiLogout()
                console.log("logout success")
            } catch (err) {
                // 即使后端出错，也继续清除本地状态
                console.warn('后端登出失败，但本地状态仍会清空：', err)
            }
            this.resetState()
        },

        /**
         * 3. 刷新/初始化时调用：尝试拉取当前用户信息，若成功则标记已登录，否则重置状态
         */
        async fetchCurrentUser() {
            try {
                this.userInfo = (await apiGetCurrentUser()).data
                this.isLoggedIn = true
                console.log("fetchCurrentUser")
                console.log(this.userInfo)
                localStorage.setItem("userStore", JSON.stringify(
                    {
                        isLoggedIn: this.isLoggedIn,
                        userInfo: this.userInfo,
                    }))
                console.log("user saved")
            } catch (err) {
                // 取不到用户信息，就认为未登录
                this.resetState()
            }
        },

        // 新增：重置状态的辅助方法
        resetState() {
            this.isLoggedIn = false
            this.userInfo = {
                id: null,
                email: '',
                username: '',
                verified: false,
                gender: '',
                role: ''
            }
            localStorage.removeItem('userStore')
        },

        // 新增：从 localStorage 初始化状态
        async initializeFromStorage() {
            const savedState = localStorage.getItem('userStore')
            console.log("get savedState")
            console.log(savedState)
            if (savedState && !this.isLoggedIn) {
                const {isLoggedIn, userInfo} = JSON.parse(savedState)
                await this.login({
                    email: userInfo.email,
                    password: userInfo.password,
                })
            }
        },
    },

    getters: {
        // 获取当前登录状态
        loggedIn: (state) => state.isLoggedIn,

        // 获取当前用户信息
        currentUser: (state) => state.userInfo,

        // 获取用户名（假设userInfo中有username字段）
        username: (state) => state.userInfo?.username || '',

        // 获取用户邮箱（假设userInfo中有email字段）
        email: (state) => state.userInfo?.email || '',

        id: state => state.userInfo?.id,
    },
})
