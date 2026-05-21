from pydantic import BaseModel
from typing import List, Optional


class BookRecommend(BaseModel):
    bookId: int
    title: Optional[str] = None
    author: Optional[str] = None
    category: Optional[str] = None
    coverUrl: Optional[str] = None
    similarity: float


class RecommendationResponse(BaseModel):
    code: int
    message: str
    data: List[BookRecommend]


class SearchResponse(BaseModel):
    code: int
    message: str
    data: List[BookRecommend]
