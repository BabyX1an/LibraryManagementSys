import numpy as np
from typing import List, Optional, Tuple
import redis
import json
from sentence_transformers import SentenceTransformer
from app.config import settings
from app.database import Book, SessionLocal


class EmbeddingService:
    def __init__(self):
        # 加载BERT模型，优先使用本地缓存，避免联网超时
        self.model = SentenceTransformer(settings.BERT_MODEL, local_files_only=True)
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
                    "category": book.category,
                    "coverUrl": book.cover_url
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

    def build_matrix(self, embeddings: dict) -> Tuple[np.ndarray, List[str], dict]:
        """
        将embeddings字典转换为numpy矩阵，用于批量计算
        返回: (矩阵 N×384, book_id有序列表, book_id→元数据映射)
        """
        book_ids = list(embeddings.keys())
        matrix = np.array([embeddings[bid]["embedding"] for bid in book_ids])
        meta = {bid: embeddings[bid] for bid in book_ids}
        return matrix, book_ids, meta

    def batch_cosine_similarity(self, query_vec: np.ndarray, matrix: np.ndarray) -> np.ndarray:
        """
        批量计算余弦相似度
        query_vec: (dim,)  matrix: (N, dim)
        返回: (N,) 相似度数组
        """
        return matrix @ query_vec
