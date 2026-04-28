import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Post, Comment, Discussion, DiscussionMessage,
  Reaction, ReactionType, User, LikeCount
} from '../models/blog.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Users
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users`);
  }
  createUser(user: Partial<User>): Observable<User> {
    return this.http.post<User>(`${this.base}/users`, user);
  }

  // Posts
  getPosts(viewerId?: number): Observable<Post[]> {
    const params = viewerId ? `?viewerId=${viewerId}` : '';
    return this.http.get<Post[]>(`${this.base}/posts${params}`);
  }
  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.base}/posts/${id}`);
  }
  createPost(post: Partial<Post>, authorId: number): Observable<Post> {
    return this.http.post<Post>(`${this.base}/posts?authorId=${authorId}`, post);
  }
  updatePost(id: number, post: Partial<Post>, requesterId: number): Observable<Post> {
    return this.http.put<Post>(`${this.base}/posts/${id}?requesterId=${requesterId}`, post);
  }
  deletePost(id: number, requesterId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/posts/${id}?requesterId=${requesterId}`);
  }

  // Likes
  likePost(postId: number, userId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/posts/${postId}/likes?userId=${userId}`, {});
  }
  unlikePost(postId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/posts/${postId}/likes?userId=${userId}`);
  }
  getPostLikeCount(postId: number): Observable<LikeCount> {
    return this.http.get<LikeCount>(`${this.base}/posts/${postId}/likes/count`);
  }
  likeComment(commentId: number, userId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/comments/${commentId}/likes?userId=${userId}`, {});
  }
  unlikeComment(commentId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/comments/${commentId}/likes?userId=${userId}`);
  }

  // Comments
  getComments(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.base}/posts/${postId}/comments`);
  }
  addComment(postId: number, authorId: number, content: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.base}/posts/${postId}/comments?authorId=${authorId}`, { content });
  }
  updateComment(commentId: number, content: string, requesterId: number): Observable<Comment> {
    return this.http.put<Comment>(`${this.base}/comments/${commentId}?requesterId=${requesterId}`, { content });
  }
  deleteComment(commentId: number, requesterId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/comments/${commentId}?requesterId=${requesterId}`);
  }

  // Discussions
  getDiscussions(userId: number): Observable<Discussion[]> {
    return this.http.get<Discussion[]>(`${this.base}/discussions/user/${userId}`);
  }
  createDiscussion(theme: string, creatorId: number): Observable<Discussion> {
    return this.http.post<Discussion>(`${this.base}/discussions?creatorId=${creatorId}`, { theme });
  }
  updateDiscussion(id: number, theme: string, requesterId: number): Observable<Discussion> {
    return this.http.put<Discussion>(`${this.base}/discussions/${id}?requesterId=${requesterId}`, { theme });
  }
  deleteDiscussion(id: number, requesterId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/discussions/${id}?requesterId=${requesterId}`);
  }

  // Messages
  getMessages(discId: number): Observable<DiscussionMessage[]> {
    return this.http.get<DiscussionMessage[]>(`${this.base}/discussions/${discId}/messages`);
  }
  getReplies(discId: number, messageId: number): Observable<DiscussionMessage[]> {
    return this.http.get<DiscussionMessage[]>(`${this.base}/discussions/${discId}/messages/${messageId}/replies`);
  }
  sendMessage(discId: number, senderId: number, content: string, parentId?: number): Observable<DiscussionMessage> {
    const parent = parentId ? `&parentId=${parentId}` : '';
    return this.http.post<DiscussionMessage>(
      `${this.base}/discussions/${discId}/messages?senderId=${senderId}${parent}`, { content }
    );
  }
  updateMessage(discId: number, messageId: number, content: string, requesterId: number): Observable<DiscussionMessage> {
    return this.http.put<DiscussionMessage>(`${this.base}/discussions/${discId}/messages/${messageId}?requesterId=${requesterId}`, { content });
  }
  deleteMessage(discId: number, messageId: number, requesterId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/discussions/${discId}/messages/${messageId}?requesterId=${requesterId}`);
  }

  // Reactions
  getReactions(messageId: number): Observable<Reaction[]> {
    return this.http.get<Reaction[]>(`${this.base}/messages/${messageId}/reactions`);
  }
  reactToMessage(messageId: number, userId: number, type: ReactionType): Observable<Reaction> {
    return this.http.post<Reaction>(`${this.base}/messages/${messageId}/reactions?userId=${userId}&type=${type}`, {});
  }
  removeReaction(messageId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/messages/${messageId}/reactions?userId=${userId}`);
  }

  // Knowledge Base
  markDiscussionSolved(discussionId: number, acceptedMessageId: number, requesterId: number): Observable<any> {
    return this.http.post(`${this.base}/knowledge-base/solve?discussionId=${discussionId}&acceptedMessageId=${acceptedMessageId}&requesterId=${requesterId}`, {});
  }
  getKnowledgeBase(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/knowledge-base`);
  }
  searchKnowledgeBase(q: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/knowledge-base/search?q=${encodeURIComponent(q)}`);
  }
  getPopularKB(): Observable<any[]> {
    return this.http.get<any[]>(`${this.base}/knowledge-base/popular`);
  }
  getKBArticle(id: number): Observable<any> {
    return this.http.get<any>(`${this.base}/knowledge-base/${id}`);
  }

  // Reports
  submitReport(reporterId: number, targetType: 'POST' | 'COMMENT', targetId: number,
               reason: string, details: string): Observable<any> {
    return this.http.post(
      `${this.base}/reports?reporterId=${reporterId}&targetType=${targetType}&targetId=${targetId}&reason=${reason}`,
      { details }
    );
  }
  getAllReports(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/reports`); }
  getPendingReports(): Observable<any[]> { return this.http.get<any[]>(`${this.base}/reports/pending`); }
  updateReportStatus(id: number, status: string): Observable<any> {
    return this.http.patch(`${this.base}/reports/${id}/status?status=${status}`, {});
  }
}