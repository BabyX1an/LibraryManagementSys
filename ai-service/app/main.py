from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers import api


@asynccontextmanager
async def lifespan(app):
    # 启动时预热：加载BERT模型 + 构建向量缓存
    print("正在预热AI模型...")
    embedding_service = api.get_embedding_service()
    embedding_service.get_or_build_embeddings()
    api.get_recommend_service()
    api.get_search_service()
    print("AI模型预热完成")
    yield


app = FastAPI(
    title="AI Recommendation Service",
    description="智能推荐与语义搜索服务",
    version="1.0.0",
    lifespan=lifespan
)

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(api.router, prefix="/api/v1/ai")


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": settings.SERVICE_NAME}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.SERVICE_PORT)
