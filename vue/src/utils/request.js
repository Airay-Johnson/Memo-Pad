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
            if (userObj.id) config.headers['X-User-Id'] = String(userObj.id)
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

request.streamPost = async function(url, data, callbacks) {
    const { onToken, onTool, onDone, onError } = callbacks
    try {
        const headers = { 'Content-Type': 'application/json' }
        const user = localStorage.getItem('user')
        if (user) {
            try {
                const u = JSON.parse(user)
                if (u.id) headers['X-User-Id'] = String(u.id)
            } catch(e) {}
        }
        const response = await fetch('http://localhost:9999' + url, {
            method: 'POST',
            headers,
            body: JSON.stringify(data)
        })
        if (!response.ok) {
            onError && onError(new Error('HTTP ' + response.status))
            return
        }
        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''
        let currentEvent = ''
        while (true) {
            const { done, value } = await reader.read()
            if (done) break
            buffer += decoder.decode(value, { stream: true })
            const lines = buffer.split('\n')
            buffer = lines.pop() || ''
            for (const line of lines) {
                if (line.startsWith('event:')) {
                    currentEvent = line.slice(6).trim()
                } else if (line.startsWith('data:')) {
                    const text = line.slice(5).trim()
                    if (currentEvent === 'tool') {
                        onTool && onTool(text)
                    } else {
                        onToken && onToken(text)
                    }
                    currentEvent = ''
                }
            }
        }
        if (buffer.startsWith('data:')) {
            onToken && onToken(buffer.slice(5).trim())
        }
        onDone && onDone()
    } catch (e) {
        onError && onError(e)
    }
}

export default request
