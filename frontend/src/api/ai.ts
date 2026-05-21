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
