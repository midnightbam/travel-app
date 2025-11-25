<template>
  <div class="trip-card" @click="viewDetail">
    <div class="trip-image">
      <img 
        :src="trip.coverImage || '/placeholder-image.jpg'" 
        :alt="trip.title"
        @error="handleImageError"
      />
    </div>
    <div class="trip-content">
      <div class="trip-header">
        <h3 class="trip-title">{{ trip.title }}</h3>
        <span class="trip-province" v-if="trip.province">{{ trip.province }}</span>
      </div>
      <p class="trip-description">{{ trip.description }}</p>
      <div class="trip-footer">
        <div class="trip-tags" v-if="trip.tags && trip.tags.length > 0">
          <span 
            class="tag" 
            v-for="tag in displayTags" 
            :key="tag"
          >
            {{ tag }}
          </span>
        </div>
        <div class="action-buttons">
          <button class="view-detail-btn">View Detail</button>
          <button @click.stop="copyLink" class="copy-link-icon-btn" :class="{ copied: linkCopied }" :title="linkCopied ? 'Copied!' : 'Copy Link'">
            <svg v-if="!linkCopied" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TripCard',
  props: {
    trip: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      linkCopied: false
    }
  },
  computed: {
    displayTags() {
      // Show first 3 tags
      return this.trip.tags ? this.trip.tags.slice(0, 3) : [];
    }
  },
  methods: {
    viewDetail() {
      this.$router.push(`/trip/${this.trip.id}`);
    },
    handleImageError(event) {
      // Fallback to placeholder if image fails to load
      event.target.src = '/placeholder-image.jpg';
    },
    async copyLink() {
      const url = `${window.location.origin}/trip/${this.trip.id}`
      try {
        await navigator.clipboard.writeText(url)
        this.linkCopied = true
        setTimeout(() => {
          this.linkCopied = false
        }, 2000)
      } catch (err) {
        // Fallback for older browsers
        const textArea = document.createElement('textarea')
        textArea.value = url
        document.body.appendChild(textArea)
        textArea.select()
        document.execCommand('copy')
        document.body.removeChild(textArea)
        this.linkCopied = true
        setTimeout(() => {
          this.linkCopied = false
        }, 2000)
      }
    }
  }
}
</script>

<style scoped>
.trip-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.trip-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.trip-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.trip-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.trip-card:hover .trip-image img {
  transform: scale(1.05);
}

.trip-content {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.trip-header {
  margin-bottom: 1rem;
}

.trip-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.trip-province {
  background: #e53e3e;
  color: white;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 12px;
  font-weight: 500;
}

.trip-description {
  color: #4a5568;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.trip-footer {
  margin-top: auto;
}

.trip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  background: #f7fafc;
  color: #4a5568;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.view-detail-btn {
  flex: 1;
  background: #3182ce;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.view-detail-btn:hover {
  background: #2c5282;
}

.copy-link-icon-btn {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.copy-link-icon-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.copy-link-icon-btn.copied {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.copy-link-icon-btn svg {
  width: 20px;
  height: 20px;
  color: white;
}

/* Responsive Design */
@media (max-width: 768px) {
  .trip-content {
    padding: 1rem;
  }
  
  .trip-title {
    font-size: 1rem;
  }
  
  .trip-description {
    font-size: 0.85rem;
  }
}
</style>