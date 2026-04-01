/**
 * 将后端 MyBatis-Plus Page 响应转换为前端 PageResponse 格式
 * 后端返回: { current, size, total, records }
 * 前端期望: { list, total, page, pageSize }
 */
export function convertPageResponse<T>(backendPage: {
  current?: number;
  size?: number;
  total?: number;
  records?: T[];
}): { list: T[]; total: number; page: number; pageSize: number } {
  return {
    list: backendPage.records || [],
    total: backendPage.total || 0,
    page: backendPage.current || 1,
    pageSize: backendPage.size || 20,
  };
}
