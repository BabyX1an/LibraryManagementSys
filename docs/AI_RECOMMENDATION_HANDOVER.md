# 智能推荐与语义搜索服务交接报告

## 一、可行性分析

### 1.1 技术可行性 ✅

| 方面 | 评估 | 说明 |
|------|------|------|
| BERT模型 | 可行 | 使用 `sentence-transformers` 库，可加载预训练的中文BERT模型（如 `paraphrase-multilingual-MiniLM-L12-v2`） |
| Python微服务 | 可行 | 使用 FastAPI 框架，轻量高效，与现有Java微服务通过HTTP通信 |
| 数据接入 | 可行 | Python可直接连接现有MySQL数据库读取图书和借阅数据 |
| 向量存储 | 可行 | 使用 Redis（已有）或 FAISS 进行向量索引和相似度搜索 |
| 性能 | 可接受 | BERT推理单次约50-100ms，批量处理+缓存可满足需求 |

### 1.2 数据基础

现有数据表结构完全支持智能推荐：

```
book 表：
- id, title, author, publisher, category, description
→ 可将 title + author + category + description 拼接为文本，生成向量

borrow_record 表：
- user_id, book_id, borrow_time, return_time
→ 可分析用户借阅历史，提取兴趣偏好
```

### 1.3 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                         library-gateway (8101)                   │
└─────────────────────────────────────────────────────────────────┘
                    │                    │                    │
                    ▼                    ▼                    ▼
         ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
         │   book-service   │  │   user-service   │  │  ai-service      │
         │   (Java/8105)    │  │   (Java/8106)    │  │  (Python/8108)   │
         └──────────────────┘  └──────────────────┘  └──────────────────┘
                    │                    │                    │
                    └────────────────────┼────────────────────┘
                                         ▼
                              ┌──────────────────┐
                              │     MySQL        │
                              │  library_system  │
                              └──────────────────┘
                                         │
                                         ▼
                              ┌──────────────────┐
                              │     Redis        │
                              │   向量缓存       │
                              └──────────────────┘
```

### 1.4 实现方案

| 功能 | 实现方式 |
|------|----------|
| 智能推荐 | 1. 获取用户借阅过的图书列表<br>2. 生成用户兴趣向量（借阅图书向量的加权平均）<br>3. 与所有图书向量计算余弦相似度<br>4. 返回Top-N相似图书（排除已借阅） |
| 语义搜索 | 1. 将搜索关键词通过BERT编码为向量<br>2. 与所有图书向量计算相似度<br>3. 返回Top-N相似图书 |

---

## 二、AI实现指令

以下是给AI助手的完整实现指令，直接复制使用即可。

### 2.1 项目初始化

```
在项目根目录下创建 ai-service 目录，使用 Python + FastAPI 实现智能推荐服务。

目录结构：
ai-service/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI入口
│   ├── config.py            # 配置文件
│   ├── database.py          # 数据库连接
│   ├── models/
│   │   ├── __init__.py
│   │   └── schemas.py       # Pydantic模型
│   ├── services/
│   │   ├── __init__.py
│   │   ├── embedding.py     # BERT向量化服务
│   │   ├── recommend.py     # 推荐服务
│   │   └── search.py        # 搜索服务
│   └── routers/
│       ├── __init__.py
│       └── api.py           # API路由
├── requirements.txt
└── Dockerfile
```

### 2.2 依赖文件 requirements.txt

```txt
fastapi==0.109.0
uvicorn==0.27.0
sentence-transformers==2.3.1
torch==2.1.2
pymysql==1.1.0
sqlalchemy==2.0.25
redis==5.0.1
numpy==1.26.3
pydantic==2.5.3
python-dotenv==1.0.0
```

### 2.3 配置文件 app/config.py

```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # 服务配置
    SERVICE_NAME: str = "ai-service"
    SERVICE_PORT: int = 8108

    # 数据库配置（与Java服务共用同一个数据库）
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "library_system"
    DB_USER: str = "root"
    DB_PASSWORD: str = "20040523666Ccc"

    # Redis配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 1  # 使用不同的DB避免冲突

    # BERT模型配置
    BERT_MODEL: str = "paraphrase-multilingual-MiniLM-L12-v2"
    EMBEDDING_CACHE_KEY: str = "book:embeddings"
    EMBEDDING_DIM: int = 384

    class Config:
        env_file = ".env"

settings = Settings()
```

### 2.4 数据库连接 app/database.py

```python
from sqlalchemy import create_engine, Column, BigInteger, String, Text, Integer
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.config import settings

# 数据库连接
DATABASE_URL = f"mysql+pymysql://{settings.DB_USER}:{settings.DB_PASSWORD}@{settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}"
engine = create_engine(DATABASE_URL, pool_pre_ping=True, pool_recycle=3600)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# 图书实体
class Book(Base):
    __tablename__ = "book"

    id = Column(BigInteger, primary_key=True, index=True)
    title = Column(String(255))
    author = Column(String(100))
    publisher = Column(String(100))
    category = Column(String(50))
    description = Column(Text)
    cover_url = Column(String(500))
    stock = Column(Integer)
    borrowed_count = Column(Integer)
    status = Column(Integer)
    deleted = Column(Integer, default=0)

# 借阅记录实体
class BorrowRecord(Base):
    __tablename__ = "borrow_record"

    id = Column(BigInteger, primary_key=True, index=True)
    user_id = Column(BigInteger, index=True)
    book_id = Column(BigInteger, index=True)
    status = Column(Integer)  # 1-借阅中, 2-已归还

# 获取数据库会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
```

### 2.5 BERT向量化服务 app/services/embedding.py

```python
import numpy as np
from typing import List, Optional
import redis
import json
from sentence_transformers import SentenceTransformer
from app.config import settings
from app.database import Book, SessionLocal

class EmbeddingService:
    def __init__(self):
        # 加载BERT模型（首次会下载，约400MB）
        self.model = SentenceTransformer(settings.BERT_MODEL)
        self.redis_client = redis.Redis(
            host=settings.REDIS_HOST,
            port=settings.REDIS_PORT,
            db=settings.REDIS_DB,
            decode_responses=True
        )

    def get_book_text(self, book: Book) -> str:
        """将图书信息拼接为文本"""
        parts = []
        if book.title:
            parts.append(f"书名: {book.title}")
        if book.author:
            parts.append(f"作者: {book.author}")
        if book.category:
            parts.append(f"分类: {book.category}")
        if book.description:
            parts.append(f"简介: {book.description[:200]}")  # 限制长度
        return " ".join(parts)

    def encode(self, texts: List[str]) -> np.ndarray:
        """将文本列表编码为向量"""
        return self.model.encode(texts, convert_to_numpy=True, normalize_embeddings=True)

    def encode_single(self, text: str) -> np.ndarray:
        """将单个文本编码为向量"""
        return self.model.encode([text], convert_to_numpy=True, normalize_embeddings=True)[0]

    def get_all_embeddings(self) -> Optional[dict]:
        """从Redis获取所有图书向量缓存"""
        try:
            data = self.redis_client.get(settings.EMBEDDING_CACHE_KEY)
            if data:
                return json.loads(data)
        except Exception as e:
            print(f"Redis读取失败: {e}")
        return None

    def save_all_embeddings(self, embeddings: dict):
        """保存所有图书向量到Redis"""
        try:
            self.redis_client.set(settings.EMBEDDING_CACHE_KEY, json.dumps(embeddings))
        except Exception as e:
            print(f"Redis写入失败: {e}")

    def build_book_embeddings(self) -> dict:
        """构建所有图书的向量（首次运行或更新时调用）"""
        db = SessionLocal()
        try:
            books = db.query(Book).filter(Book.deleted == 0).all()
            texts = [self.get_book_text(book) for book in books]
            embeddings = self.encode(texts)

            result = {}
            for book, emb in zip(books, embeddings):
                result[str(book.id)] = {
                    "embedding": emb.tolist(),
                    "title": book.title,
                    "author": book.author,
                    "category": book.category
                }

            # 缓存到Redis
            self.save_all_embeddings(result)
            return result
        finally:
            db.close()

    def get_or_build_embeddings(self) -> dict:
        """获取向量缓存，不存在则构建"""
        cached = self.get_all_embeddings()
        if cached:
            return cached
        return self.build_book_embeddings()

    def cosine_similarity(self, vec1: np.ndarray, vec2: np.ndarray) -> float:
        """计算余弦相似度"""
        return float(np.dot(vec1, vec2))
```

### 2.6 推荐服务 app/services/recommend.py

```python
import numpy as np
from typing import List, Dict, Tuple
from app.services.embedding import EmbeddingService
from app.database import Book, BorrowRecord, SessionLocal

class RecommendService:
    def __init__(self):
        self.embedding_service = EmbeddingService()

    def get_user_borrowed_book_ids(self, user_id: int, db) -> List[int]:
        """获取用户借阅过的所有图书ID"""
        records = db.query(BorrowRecord.book_id).filter(
            BorrowRecord.user_id == user_id
        ).distinct().all()
        return [r[0] for r in records]

    def get_user_interest_vector(self, user_id: int, embeddings: dict) -> Tuple[np.ndarray, List[int]]:
        """
        计算用户兴趣向量
        策略：用户借阅过的图书向量的加权平均（最近借阅权重更高）
        """
        db = SessionLocal()
        try:
            # 获取用户借阅记录，按时间排序
            records = db.query(BorrowRecord, Book).join(
                Book, BorrowRecord.book_id == Book.id
            ).filter(
                BorrowRecord.user_id == user_id
            ).order_by(BorrowRecord.borrow_time.desc()).all()

            if not records:
                return None, []

            vectors = []
            borrowed_ids = []

            # 加权：最近的借阅权重更高
            total_weight = 0
            for idx, (record, book) in enumerate(records):
                book_id = str(book.id)
                if book_id in embeddings:
                    weight = 1.0 / (1 + idx * 0.1)  # 越近权重越高
                    vec = np.array(embeddings[book_id]["embedding"])
                    vectors.append(vec * weight)
                    total_weight += weight
                    borrowed_ids.append(book.id)

            if not vectors:
                return None, []

            # 加权平均
            user_vector = np.sum(vectors, axis=0) / total_weight
            # 归一化
            user_vector = user_vector / np.linalg.norm(user_vector)

            return user_vector, borrowed_ids
        finally:
            db.close()

    def recommend_for_user(self, user_id: int, top_k: int = 10) -> List[Dict]:
        """
        为用户推荐图书
        1. 计算用户兴趣向量
        2. 与所有图书计算相似度
        3. 返回最相似的Top-K（排除已借阅）
        """
        embeddings = self.embedding_service.get_or_build_embeddings()

        user_vector, borrowed_ids = self.get_user_interest_vector(user_id, embeddings)

        if user_vector is None:
            return []

        # 计算与所有图书的相似度
        similarities = []
        for book_id, data in embeddings.items():
            if int(book_id) not in borrowed_ids:  # 排除已借阅
                book_vec = np.array(data["embedding"])
                sim = self.embedding_service.cosine_similarity(user_vector, book_vec)
                similarities.append((book_id, sim, data))

        # 按相似度排序
        similarities.sort(key=lambda x: x[1], reverse=True)

        # 返回Top-K
        results = []
        for book_id, sim, data in similarities[:top_k]:
            results.append({
                "bookId": int(book_id),
                "title": data.get("title"),
                "author": data.get("author"),
                "category": data.get("category"),
                "similarity": round(sim, 4)
            })

        return results
```

### 2.7 搜索服务 app/services/search.py

```python
import numpy as np
from typing import List, Dict
from app.services.embedding import EmbeddingService

class SearchService:
    def __init__(self):
        self.embedding_service = EmbeddingService()

    def semantic_search(self, query: str, top_k: int = 10) -> List[Dict]:
        """
        语义搜索
        1. 将查询文本编码为向量
        2. 与所有图书向量计算相似度
        3. 返回最相似的Top-K
        """
        embeddings = self.embedding_service.get_or_build_embeddings()

        # 编码查询
        query_vector = self.embedding_service.encode_single(query)

        # 计算相似度
        similarities = []
        for book_id, data in embeddings.items():
            book_vec = np.array(data["embedding"])
            sim = self.embedding_service.cosine_similarity(query_vector, book_vec)
            similarities.append((book_id, sim, data))

        # 按相似度排序
        similarities.sort(key=lambda x: x[1], reverse=True)

        # 返回Top-K
        results = []
        for book_id, sim, data in similarities[:top_k]:
            results.append({
                "bookId": int(book_id),
                "title": data.get("title"),
                "author": data.get("author"),
                "category": data.get("category"),
                "similarity": round(sim, 4)
            })

        return results
```

### 2.8 API路由 app/routers/api.py

```python
from fastapi import APIRouter, Depends, Query, Header
from sqlalchemy.orm import Session
from typing import List, Optional
import Base64

from app.database import get_db
from app.services.recommend import RecommendService
from app.services.search import SearchService
from app.models.schemas import RecommendationResponse, SearchResponse, BookRecommend

router = APIRouter()

def get_user_id(authorization: Optional[str] = Header(None)) -> Optional[int]:
    """从Token中解析用户ID"""
    if not authorization or not authorization.startswith("Bearer "):
        return None
    try:
        token = authorization[7:]
        user_id_str = Base64.b64decode(token).decode()
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
    if not user_id:
        return RecommendationResponse(code=401, message="请先登录", data=[])

    try:
        service = RecommendService()
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
        service = SearchService()
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
        from app.services.embedding import EmbeddingService
        service = EmbeddingService()
        service.build_book_embeddings()
        return {"code": 200, "message": "向量缓存重建成功"}
    except Exception as e:
        return {"code": 500, "message": str(e)}
```

### 2.9 数据模型 app/models/schemas.py

```python
from pydantic import BaseModel
from typing import List, Optional

class BookRecommend(BaseModel):
    bookId: int
    title: Optional[str] = None
    author: Optional[str] = None
    category: Optional[str] = None
    similarity: float

class RecommendationResponse(BaseModel):
    code: int
    message: str
    data: List[BookRecommend]

class SearchResponse(BaseModel):
    code: int
    message: str
    data: List[BookRecommend]
```

### 2.10 主入口 app/main.py

```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers import api

app = FastAPI(
    title="AI Recommendation Service",
    description="智能推荐与语义搜索服务",
    version="1.0.0"
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
```

### 2.11 网关路由配置

在 `library-gateway/src/main/java/com/library/gateway/config/GatewayConfig.java` 中添加：

```java
// AI Service 路由
.route("ai-service", r -> r
        .path("/api/v1/ai/**")
        .uri("http://localhost:8108")
)
```

### 2.12 前端API调用

在前端添加 `frontend/src/api/ai.ts`：

```typescript
import request from '@/utils/request'

export const aiApi = {
  // 获取个性化推荐
  getRecommendations(topK: number = 10) {
    return request.get('/v1/ai/recommend', { params: { top_k: topK } })
  },

  // 语义搜索
  semanticSearch(query: string, topK: number = 10) {
    return request.get('/v1/ai/search', { params: { q: query, top_k: topK } })
  }
}
```

---

## 三、部署说明

### 3.1 启动步骤

```bash
# 1. 进入ai-service目录
cd ai-service

# 2. 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 或 venv\Scripts\activate  # Windows

# 3. 安装依赖
pip install -r requirements.txt

# 4. 启动服务
python -m app.main
# 或
uvicorn app.main:app --host 0.0.0.0 --port 8108
```

### 3.2 首次启动注意事项

1. **模型下载**：首次启动会自动下载BERT模型（约400MB），需要等待
2. **向量构建**：首次调用接口时会构建所有图书的向量并缓存到Redis
3. **内存需求**：建议至少4GB可用内存

### 3.3 API测试

```bash
# 健康检查
curl http://localhost:8108/health

# 语义搜索
curl "http://localhost:8108/api/v1/ai/search?q=编程入门&top_k=5"

# 个性化推荐（需要Token）
curl -H "Authorization: Bearer <token>" "http://localhost:8108/api/v1/ai/recommend?top_k=5"
```

---

## 四、优化建议

1. **向量数据库**：图书数量超过1万时，建议使用 Milvus 或 Pinecone 替代 Redis
2. **异步处理**：向量构建可改为后台异步任务
3. **定期更新**：添加定时任务，每日凌晨更新向量缓存
4. **GPU加速**：如有GPU，修改 embedding.py 使用 CUDA 加速推理

---

**文档版本**：v1.0
**更新日期**：2026-04-07
