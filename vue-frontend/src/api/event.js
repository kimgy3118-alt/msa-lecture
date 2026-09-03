import api from './index.js'

export const eventApi = {
  getEvents(params) {
    return api.get('/event-service/api/events', { params })
  },

  getAll(params) {
    return api.get('/event-service/api/events', { params })
  },

  getById(id) {
    return api.get(`/event-service/api/events/${id}`)
  },

  create(data) {
    return api.post('/event-service/api/events', data)
  },

  update(id, data) {
    return api.put(`/event-service/api/events/${id}`, data)
  }
}