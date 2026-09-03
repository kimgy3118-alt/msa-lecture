import httpx
import logging
from typing import List
from app.config.settings import settings
from app.model.schemas import EventResponse, EventCategory

logger = logging.getLogger(__name__)


class EventServiceClient:
    """
    Event Service REST 클라이언트
    - 카테고리별 미예약 행사 목록 조회
    """

    def __init__(self):
        self.base_url = settings.event_service_url

    async def get_recommend_events(
        self,
        category: EventCategory,
        exclude_ids: List[int]
    ) -> List[EventResponse]:
        """
        GET /events/internal/recommend
        카테고리 기반 미예약 행사 목록 조회 (예약자 수 기준 정렬)
        """
        url = f"{self.base_url}/api/events/internal/recommend"
        params = {"category": category.value}
        if exclude_ids:
            params["excludeIds"] = ",".join(str(i) for i in exclude_ids)

        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url, params=params)
                response.raise_for_status()
                return [EventResponse(**c) for c in response.json()]
        except httpx.HTTPError as e:
            logger.error(f"[EventClient] 추천 행사 조회 실패 - category: {category}, error: {e}")
            return []

    async def get_all_events(self) -> List[EventResponse]:
        """
        GET /events - 전체 행사 목록 조회
        예약 이력이 없는 신규 사용자 추천용
        """
        url = f"{self.base_url}/api/events"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(url)
                response.raise_for_status()
                data = response.json()
                events = data.get("data", [])
                return [EventResponse(**c) for c in events]
        except httpx.HTTPError as e:
            logger.error(f"[EventClient] 전체 행사 조회 실패 - error: {e}")
            return []


event_client = EventServiceClient()
