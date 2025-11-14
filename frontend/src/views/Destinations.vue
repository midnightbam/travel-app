<template>
  <div class="destinations">
    <div class="destinations-header">
      <h1>Travel Destinations</h1>
      <button @click="showAddForm = true" class="btn btn-success">
        Add New Destination
      </button>
    </div>
    
    <div v-if="error" class="alert alert-error">
      {{ error }}
    </div>
    
    <div v-if="showAddForm" class="add-form card">
      <h3>Add New Destination</h3>
      <form @submit.prevent="addDestination">
        <div class="form-group">
          <label>Name:</label>
          <input v-model="newDestination.name" type="text" required />
        </div>
        <div class="form-group">
          <label>Country:</label>
          <input v-model="newDestination.country" type="text" required />
        </div>
        <div class="form-group">
          <label>Description:</label>
          <input v-model="newDestination.description" type="text" required />
        </div>
        <div class="form-group">
          <label>Price ($):</label>
          <input v-model.number="newDestination.price" type="number" required />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">Add Destination</button>
          <button type="button" @click="cancelAdd" class="btn">Cancel</button>
        </div>
      </form>
    </div>
    
    <div v-if="loading" class="loading">Loading destinations...</div>
    
    <div v-else class="destinations-grid">
      <div v-for="destination in destinations" :key="destination.id" class="destination-card">
        <h3>{{ destination.name }}</h3>
        <p class="country">{{ destination.country }}</p>
        <p class="description">{{ destination.description }}</p>
        <div class="price">${{ destination.price }}</div>
        <button class="btn btn-primary">Book Now</button>
      </div>
    </div>
  </div>
</template>

<script>
import { travelService } from '../services/travel.js'

export default {
  name: 'Destinations',
  data() {
    return {
      destinations: [],
      loading: true,
      error: null,
      showAddForm: false,
      newDestination: {
        name: '',
        country: '',
        description: '',
        price: 0
      }
    }
  },
  async mounted() {
    await this.loadDestinations()
  },
  methods: {
    async loadDestinations() {
      this.loading = true
      this.error = null
      
      try {
        this.destinations = await travelService.getDestinations()
      } catch (error) {
        this.error = 'Failed to load destinations'
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    async addDestination() {
      try {
        await travelService.addDestination(this.newDestination)
        this.showAddForm = false
        this.resetForm()
        await this.loadDestinations()
      } catch (error) {
        this.error = 'Failed to add destination'
        console.error(error)
      }
    },
    cancelAdd() {
      this.showAddForm = false
      this.resetForm()
    },
    resetForm() {
      this.newDestination = {
        name: '',
        country: '',
        description: '',
        price: 0
      }
    }
  }
}
</script>

<style scoped>
.destinations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.destinations-header h1 {
  color: #2c3e50;
}

.add-form {
  margin-bottom: 2rem;
}

.add-form h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #7f8c8d;
}

.destinations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.destination-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.destination-card:hover {
  transform: translateY(-2px);
}

.destination-card h3 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
}

.country {
  color: #7f8c8d;
  font-weight: 500;
  margin-bottom: 1rem;
}

.description {
  color: #555;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #27ae60;
  margin-bottom: 1rem;
}

.destination-card .btn {
  width: 100%;
}
</style>