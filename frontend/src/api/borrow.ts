import request from '@/utils/request'
import type { BorrowRecord, PageResponse } from '@/types'

export const borrowApi = {
  // 借阅图书
  borrowBook(bookId: number) {
    return request.post<BorrowRecord>(`/v1/borrow/${bookId}`)
  },

  // 归还图书
  returnBook(recordId: number) {
    return request.put<boolean>(`/v1/borrow/${recordId}/return`)
  },

  // 获取当前借阅（借阅中）
  getCurrentBorrows() {
    return request.get<BorrowRecord[]>('/v1/borrow/current')
  },

  // 获取借阅历史（已归还，分页）
  getBorrowHistory(current: number = 1, size: number = 10) {
    return request.get<PageResponse<BorrowRecord>>('/v1/borrow/history', {
      params: { current, size }
    })
  }
}
