import request from '@/utils/request'
import type { Book, PageResponse } from '@/types'

export const bookApi = {
  // 分页查询图书
  pageBooks(current: number = 1, size: number = 10, title?: string, author?: string, category?: string) {
    return request.get<PageResponse<Book>>('/v1/books', {
      params: { current, size, title, author, category }
    })
  },

  // 根据ID查询图书
  getBookById(id: number) {
    return request.get<Book>(`/v1/books/${id}`)
  },

  // 添加图书
  addBook(book: Partial<Book>) {
    return request.post<Book>('/v1/books', book)
  },

  // 更新图书
  updateBook(book: Partial<Book>) {
    const id = book.id
    if (!id) {
      throw new Error('更新图书时必须提供图书ID')
    }
    return request.put<Book>(`/v1/books/${id}`, book)
  },

  // 删除图书
  deleteBook(id: number) {
    return request.delete(`/v1/books/${id}`)
  },

  // 获取热门图书
  getPopularBooks(limit: number = 10) {
    return request.get<Book[]>('/v1/books/popular', {
      params: { limit }
    })
  }
}