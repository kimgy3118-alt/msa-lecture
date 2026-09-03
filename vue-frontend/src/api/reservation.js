import api from './index.js'

export const reservationApi = {
  getMyReservations() {
    return api.get('/api/reservations/my')
  },
  reserve(eventId) {
    return api.post('/api/reservations', { eventId })
  },
  cancel(reservationId) {
    return api.delete(`/api/reservations/${reservationId}`)
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  }
}
