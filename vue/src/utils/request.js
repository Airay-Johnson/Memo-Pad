import axios from "axios"
import { ElMessage } from "element-plus"

const request = axios.create({
    baseURL: 'http://localhost:9999',
    timeout: 30000
})

request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8'
    const user = localStorage.getItem('user')
    if (user) {
        try {
            const userObj = JSON.parse(user)
            if (userObj.id) config.headers['userId'] = userObj.id
        } catch(e) {}
    }
    return config
}, error => {
    return Promise.reject(error)
})

request.interceptors.response.use(
    response => {
        let res = response.data
        if (typeof res === 'string') res = JSON.parse(res)
        // 后端统一返回 { code, data, msg }
        if (res.code && res.code !== '200') {
            ElMessage.error(res.msg || '请求失败')
        }
        return res
    },
    error => {
        if (error.response) {
            if (error.response.status === 404) ElMessage.error('未找到请求接口')
            else if (error.response.status === 500) ElMessage.error('系统异常，请查看后端控制台报错')
            else if (error.response.status === 401) {
                ElMessage.error('登录已过期，请重新登录')
                localStorage.removeItem('user')
                window.location.href = '/login'
            }
        } else {
            console.error('网络错误，后端可能未启动')
        }
        return Promise.reject(error)
    }
)

export default request
