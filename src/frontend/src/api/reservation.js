import api from './index.js'

export const reservationApi = {
  getMyReservations() {
    return api.get('/reservation-service/api/reservations/my')
  },
  reserve(eventId) {
    return api.post('/reservation-service/api/reservations', { eventId })
  },
  cancel(reservationId) {
    return api.delete(`/reservation-service/api/reservations/${reservationId}`)
  },
  getRecommendations(userId) {
    return api.get(`/api/recommend/${userId}`)
  }
}
