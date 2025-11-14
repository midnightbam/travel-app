<template>
  <div class="search-bar">
    <div class="search-container">
      <div class="search-input-wrapper">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search by name or province"
          class="search-input"
          @input="onInput"
          @keyup.enter="onSearch"
        />
        <button class="search-btn" @click="onSearch">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M21 21L16.514 16.506L21 21ZM19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
      <button 
        v-if="searchQuery" 
        class="clear-btn" 
        @click="clearSearch"
        title="Clear search"
      >
        <svg class="clear-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  data() {
    return {
      searchQuery: '',
      debounceTimer: null
    };
  },
  methods: {
    onInput() {
      // Debounce search for better performance
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer);
      }
      
      this.debounceTimer = setTimeout(() => {
        this.$emit('search', this.searchQuery.trim());
      }, 300);
    },
    onSearch() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer);
      }
      this.$emit('search', this.searchQuery.trim());
    },
    clearSearch() {
      this.searchQuery = '';
      this.$emit('search', '');
    }
  }
};
</script>

<style scoped>
.search-bar {
  margin-bottom: 2rem;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  max-width: 600px;
  margin: 0 auto;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.search-input-wrapper:focus-within {
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.search-input {
  flex: 1;
  padding: 1rem 1.25rem;
  border: none;
  font-size: 1rem;
  outline: none;
  background: transparent;
}

.search-input::placeholder {
  color: #a0aec0;
}

.search-btn {
  padding: 1rem 1.25rem;
  background: #3182ce;
  color: white;
  border: none;
  cursor: pointer;
  transition: background-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background: #2c5282;
}

.search-icon {
  width: 20px;
  height: 20px;
}

.clear-btn {
  width: 40px;
  height: 40px;
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  background: #e2e8f0;
  border-color: #cbd5e0;
}

.clear-icon {
  width: 16px;
  height: 16px;
  color: #4a5568;
}

/* Responsive Design */
@media (max-width: 768px) {
  .search-container {
    max-width: 100%;
  }
  
  .search-input {
    padding: 0.875rem 1rem;
    font-size: 0.9rem;
  }
  
  .search-btn {
    padding: 0.875rem 1rem;
  }
  
  .clear-btn {
    width: 36px;
    height: 36px;
  }
}
</style>