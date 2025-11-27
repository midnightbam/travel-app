<template>
  <div class="notification-container">
    <transition-group name="notification" tag="div">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        :class="['notification', `notification-${notification.type}`]"
        @click="removeNotification(notification.id)"
      >
        <div class="notification-icon">
          <svg v-if="notification.type === 'success'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
          </svg>
          <svg v-else-if="notification.type === 'error'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
          </svg>
          <svg v-else-if="notification.type === 'warning'" width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
          </svg>
          <svg v-else width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
        </div>
        <div class="notification-content">
          <p class="notification-message">{{ notification.message }}</p>
        </div>
        <button 
          class="notification-close"
          @click.stop="removeNotification(notification.id)"
          type="button"
        >
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 7l3-3 1 1-3 3 3 3-1 1-3-3-3 3-1-1 3-3-3-3 1-1 3 3z"/>
          </svg>
        </button>
      </div>
    </transition-group>
  </div>
</template>

<script>
import { useNotification } from '../composables/useNotification.js'

export default {
  name: 'NotificationContainer',
  setup() {
    const { notifications, removeNotification } = useNotification()
    
    return {
      notifications,
      removeNotification
    }
  }
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  pointer-events: none;
}

.notification {
  display: flex;
  align-items: center;
  gap: 12px;
  max-width: 400px;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  backdrop-filter: blur(8px);
  pointer-events: auto;
  cursor: pointer;
  transition: all 0.3s ease;
}

.notification:hover {
  transform: translateY(-2px);
  box-shadow: 0 15px 35px -5px rgba(0, 0, 0, 0.15), 0 8px 12px -2px rgba(0, 0, 0, 0.08);
}

.notification-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-info {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-icon {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-message {
  margin: 0;
  font-weight: 500;
  font-size: 14px;
  line-height: 1.4;
}

.notification-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
  opacity: 0.8;
}

.notification-close:hover {
  background: rgba(255, 255, 255, 0.2);
  opacity: 1;
}

/* Transition animations */
.notification-enter-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.notification-leave-active {
  transition: all 0.3s ease-in;
}

.notification-enter-from {
  transform: translateX(100%) scale(0.8);
  opacity: 0;
}

.notification-leave-to {
  transform: translateX(100%) scale(0.8);
  opacity: 0;
}

.notification-move {
  transition: transform 0.3s ease;
}

@media (max-width: 640px) {
  .notification-container {
    top: 10px;
    right: 10px;
    left: 10px;
  }
  
  .notification {
    max-width: none;
    margin-bottom: 8px;
    padding: 12px;
  }
  
  .notification-message {
    font-size: 13px;
  }
}
</style>