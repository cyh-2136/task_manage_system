import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  withCredentials: true,
  timeout: 15000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) return res
    console.error('API error:', res.message)
    return Promise.reject(new Error(res.message))
  },
  error => {
    console.error('Request error:', error.message)
    return Promise.reject(error)
  }
)

export default request
