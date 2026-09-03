import api from './index.js'

export const eventApi = {
  getEvents(params) {
    return api.get('/api/events', { params })
  },

  getAll(params) {
    return api.get('/api/events', { params })
  },

  getById(id) {
    return api.get(`/api/events/${id}`)
  },

  create(data) {
    return api.post('/api/events', data)
  },

  update(id, data) {
    return api.put(`/api/events/${id}`, data)
  }
}