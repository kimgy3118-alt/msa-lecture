import httpx
import logging
from app.config.settings import settings
from app.model.schemas import ReservationHistoryResponse

logger = logging.getLogger(__name__)


class ReservationServiceClient:
    """
    Reservation Service REST 클라이언트
    - 예약 이력 조회 (ACTIVE 행사 ID 목록)
    """

    def __init__(self):
        self.base_url = settings.reservation_service_url

    async def get_reservation_history(self, user_id: int) -> ReservationHistoryResponse:
        """
        GET /reservations/internal/history/{userId}
        사용자의 참여 중인 행사 ID 목록 조회
        """
        url = f"{self.base_url}/api/reservations/internal/history/{user_id}"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                data = response.json()
                return ReservationHistoryResponse(**data)
        except httpx.HTTPError as e:
            logger.error(f"[ReservationClient] 예약 이력 조회 실패 - userId: {user_id}, error: {e}")
            # 실패 시 빈 이력 반환 (추천 서비스는 비핵심 기능)
            return ReservationHistoryResponse(userId=user_id, activeEventIds=[])


reservation_client = ReservationServiceClient()
