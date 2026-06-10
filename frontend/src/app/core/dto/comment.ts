export interface Comment {
  id: number;
  taskId: number;
  authorName: string;
  content: string;
  createdAt: string;
}

export interface CreateCommentRequest {
  content: string;
}
