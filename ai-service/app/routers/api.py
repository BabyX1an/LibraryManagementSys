import base64
from functools import lru_cache
from fastapi import APIRouter, Query, Header
from typing import Optional

from app.services.recommend import RecommendService
from app.services.search import SearchService
from app.services.embedding import EmbeddingService
from app.models.schemas import RecommendationResponse, SearchResponse

router = APIRouter()


@lru_cache()
def get_embedding_service() -> EmbeddingService:
    """单例：整个进程只加载一次BERT模型"""
    return EmbeddingService()


@lru_cache()
def get_search_service() -> SearchService:
    """单例：复用同一个EmbeddingService"""
    return SearchService(embedding_service=get_embedding_service())


@lru_cache()
def get_recommend_service() -> RecommendService:
    """单例：复用同一个EmbeddingService"""
    return RecommendService(embedding_service=get_embedding_service())


def get_user_id(authorization: Optional[str] = Header(None)) -> Optional[int]:
    """从Token中解析用户ID"""
    if not authorization or not authorization.startswith("Bearer "):
        return None
    try:
        token = authorization[7:]
        user_id_str = base64.b64decode(token).decode()
        return int(user_id_str)
    except:
        return None


@router.get("/recommend", response_model=RecommendationResponse)
async def get_recommendations(
    top_k: int = Query(10, ge=1, le=50),
    authorization: Optional[str] = Header(None)
):
    """
    获取个性化推荐图书
    需要登录，根据用户借阅历史推荐相似图书
    """
    user_id = get_user_id(authorization)

    try:
        service = get_recommend_service()
        results = service.recommend_for_user(user_id, top_k)
        return RecommendationResponse(code=200, message="success", data=results)
    except Exception as e:
        return RecommendationResponse(code=500, message=str(e), data=[])


@router.get("/search", response_model=SearchResponse)
async def semantic_search(
    q: str = Query(..., min_length=1, description="搜索关键词"),
    top_k: int = Query(10, ge=1, le=50)
):
    """
    语义搜索图书
    支持自然语言搜索，返回语义最相近的图书
    """
    try:
        service = get_search_service()
        results = service.semantic_search(q, top_k)
        return SearchResponse(code=200, message="success", data=results)
    except Exception as e:
        return SearchResponse(code=500, message=str(e), data=[])


@router.post("/embeddings/rebuild")
async def rebuild_embeddings():
    """
    重建向量缓存
    当图书数据有较大更新时调用
    """
    try:
        service = get_embedding_service()
        service.build_book_embeddings()
        return {"code": 200, "message": "向量缓存重建成功"}
    except Exception as e:
        return {"code": 500, "message": str(e)}
