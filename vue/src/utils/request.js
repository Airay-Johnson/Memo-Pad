import axios from "axios";
import {ElMessage} from "element-plus";

const request = axios.create({
    baseURL: 'http://localhost:9999',
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    // 从 localStorage 读取用户信息，附加到请求中（如果需要身份标识）
    const user = localStorage.getItem('user')
    if (user) {
      try {
        const userObj = JSON.parse(user)
        if (userObj.id) {
          config.headers['userId'] = userObj.id
        }
      } catch(e) {}
    }

    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        if (error.response) {
            if (error.response.status === 404) {
                ElMessage.error('未找到请求接口')
            } else if (error.response.status === 500) {
                ElMessage.error('系统异常，请查看后端控制台报错')
            } else if (error.response.status === 401) {
                ElMessage.error('登录已过期，请重新登录')
                localStorage.removeItem('user')
                window.location.href = '/login'
            } else {
                console.error(error.message)
            }
        } else {
            console.error('网络错误，后端可能未启动')
        }
        return Promise.reject(error)
    }
)

export default request
