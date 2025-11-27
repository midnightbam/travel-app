import { ref, reactive } from 'vue'

const notifications = ref([])
let notificationId = 0

export function useNotification() {
  const showNotification = (message, type = 'success', duration = 4000) => {
    const id = ++notificationId
    const notification = {
      id,
      message,
      type, // 'success', 'error', 'info', 'warning'
      duration,
      visible: true
    }
    
    notifications.value.push(notification)
    
    // Auto remove notification after duration
    setTimeout(() => {
      removeNotification(id)
    }, duration)
    
    return id
  }
  
  const removeNotification = (id) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }
  
  const success = (message, duration) => showNotification(message, 'success', duration)
  const error = (message, duration) => showNotification(message, 'error', duration)
  const info = (message, duration) => showNotification(message, 'info', duration)
  const warning = (message, duration) => showNotification(message, 'warning', duration)
  
  return {
    notifications,
    showNotification,
    removeNotification,
    success,
    error,
    info,
    warning
  }
}