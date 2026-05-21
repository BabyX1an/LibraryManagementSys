import numpy as np
from typing import List, Dict
from app.services.embedding import EmbeddingService


class SearchService:
    def __init__(self, embedding_service=None):
        self.embedding_service = embedding_service or EmbeddingService()

    def semantic_search(self, query: str, top_k: int = 10) -> List[Dict]:
        """
        语义搜索
        1. 将查询文本编码为向量
        2. 与所有图书向量矩阵乘法批量计算相似度
        3. 返回最相似的Top-K
        """
        embeddings = self.embedding_service.get_or_build_embeddings()

        # 编码查询
        query_vector = self.embedding_service.encode_single(query)

        # 构建矩阵，批量计算相似度
        matrix, book_ids, meta = self.embedding_service.build_matrix(embeddings)
        scores = self.embedding_service.batch_cosine_similarity(query_vector, matrix)

        # 取Top-K索引
        top_indices = np.argsort(scores)[::-1][:top_k]

        results = []
        for idx in top_indices:
            bid = book_ids[idx]
            data = meta[bid]
            results.append({
                "bookId": int(bid),
                "title": data.get("title"),
                "author": data.get("author"),
                "category": data.get("category"),
                "coverUrl": data.get("coverUrl"),
                "similarity": round(float(scores[idx]), 4)
            })

        return results
