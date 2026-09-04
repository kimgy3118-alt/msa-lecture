import logging
from fastapi import APIRouter, Depends
from app.config.security import verify_token
from app.model.schemas import RecommendResponse
from app.service.recommend_service import recommend_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/recommend", tags=["recommend"])


@router.get("/{user_id}", response_model=RecommendResponse)
async def get_recommendations(
    user_id: int,
    token_payload: dict = Depends(verify_token)
):
    """
    GET /recommend/{userId} - 사용자 기반 행사 추천

    추천 규칙:
    - 예약 이력 있음: 최빈 카테고리 기반 미예약 행사 추천 (예약자 수 기준 정렬)
    - 예약 이력 없음: 전체 인기 행사 추천
    """
    logger.info(f"[Router] 추천 요청 - userId: {user_id}")
    return await recommend_service.get_recommendations(user_id)


@router.get("/health", include_in_schema=False)
async def health_check():
    return {"status": "UP", "service": "recommend-service"}
