from pydantic import BaseModel
from typing import List, Optional
from enum import Enum
from decimal import Decimal
from datetime import datetime


class EventCategory(str, Enum):
    FESTIVAL = "FESTIVAL"
    EXHIBITION = "EXHIBITION"
    PERFORMANCE = "PERFORMANCE"
    CULTURE_EXPERIENCE = "CULTURE_EXPERIENCE"
    SPORTS = "SPORTS"
    EDUCATION = "EDUCATION"
    OTHER = "OTHER"

class EventResponse(BaseModel):
    id: int
    title: str
    description: Optional[str] = None
    category: EventCategory
    eventType: Optional[str] = None
    venue: Optional[str] = None
    organizerName: Optional[str] = None
    imageUrl: Optional[str] = None
    price: Decimal
    organizerId: int
    reservationCount: int
    status: str
    createdAt: Optional[datetime] = None
    eventStartAt: Optional[datetime] = None
    eventEndAt: Optional[datetime] = None
    registrationStartAt: Optional[datetime] = None
    registrationEndAt: Optional[datetime] = None


class ReservationHistoryResponse(BaseModel):
    userId: int
    activeEventIds: List[int]


class RecommendResponse(BaseModel):
    userId: int
    recommendedEvents: List[EventResponse]
    basedOnCategory: Optional[EventCategory] = None
    message: str


class ApiResponse(BaseModel):
    success: bool
    message: str
    data: Optional[dict] = None
