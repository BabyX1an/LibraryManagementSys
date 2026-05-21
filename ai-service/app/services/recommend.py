import numpy as np
from typing import List, Dict, Tuple, Optional
from app.services.embedding import EmbeddingService
from app.database import Book, BorrowRecord, SessionLocal


class RecommendService:
    def __init__(self, embedding_service=None):
        self.embedding_service = embedding_service or EmbeddingService()

    def get_user_borrowed_book_ids(self, user_id: int, db) -> List[int]:
        """获取用户借阅过的所有图书ID"""
        records = db.query(BorrowRecord.book_id).filter(
            BorrowRecord.user_id == user_id
        ).distinct().all()
        return [r[0] for r in records]

    def get_popular_books(self, top_k: int = 10, exclude_ids: List[int] = None) -> List[Dict]:
        """按借阅量降序返回热门图书（新用户兜底），排除已借阅"""
        db = SessionLocal()
        try:
            query = db.query(Book).filter(
                Book.deleted == 0,
                Book.status == 1
            )
            if exclude_ids:
                query = query.filter(Book.id.notin_(exclude_ids))
            books = query.order_by(Book.borrowed_count.desc()).limit(top_k).all()

            return [{
                "bookId": book.id,
                "title": book.title,
                "author": book.author,
                "category": book.category,
                "coverUrl": book.cover_url,
                "similarity": 0.0
            } for book in books]
        finally:
            db.close()

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

    def recommend_for_user(self, user_id: int = None, top_k: int = 10) -> List[Dict]:
        """
        为用户推荐图书
        1. 计算用户兴趣向量
        2. 与所有图书计算相似度
        3. 返回最相似的Top-K（排除已借阅）
        未登录或无借阅记录时返回热门图书（排除已借阅）
        """
        # 获取用户已借阅的图书ID（用于排除）
        borrowed_ids = []
        if user_id:
            db = SessionLocal()
            try:
                borrowed_ids = self.get_user_borrowed_book_ids(user_id, db)
            finally:
                db.close()

        embeddings = self.embedding_service.get_or_build_embeddings()

        # 未登录用户，返回热门图书
        if not user_id:
            return self.get_popular_books(top_k)

        user_vector, interest_borrowed_ids = self.get_user_interest_vector(user_id, embeddings)

        if user_vector is None:
            # 有用户但无借阅记录，返回热门图书（排除已借阅）
            return self.get_popular_books(top_k, exclude_ids=borrowed_ids)

        # 构建矩阵，批量计算相似度
        matrix, book_ids, meta = self.embedding_service.build_matrix(embeddings)
        scores = self.embedding_service.batch_cosine_similarity(user_vector, matrix)

        # 排除已借阅，取Top-K
        borrowed_set = set(borrowed_ids)
        valid_indices = [i for i, bid in enumerate(book_ids) if int(bid) not in borrowed_set]
        valid_scores = scores[valid_indices]
        top_local = np.argsort(valid_scores)[::-1][:top_k]

        results = []
        for local_idx in top_local:
            real_idx = valid_indices[local_idx]
            bid = book_ids[real_idx]
            data = meta[bid]
            results.append({
                "bookId": int(bid),
                "title": data.get("title"),
                "author": data.get("author"),
                "category": data.get("category"),
                "coverUrl": data.get("coverUrl"),
                "similarity": round(float(valid_scores[local_idx]), 4)
            })

        return results
