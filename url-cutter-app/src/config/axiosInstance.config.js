import axios from 'axios';
import { useAuthStore } from '../store/useAuthStore.js'

const api = axios.create({
  baseURL: 'http://localhost:8081/',
  timeout: 30000,
  headers: {'X-Custom-Header': 'foobar'}
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = Bearer ${token}
  }
  return config
})


export default api;
