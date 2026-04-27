import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  ChallengeResponse,
  ChallengeLeaderboardEntryResponse,
  CreateGroupQuizRequest,
  LaunchGroupQuizRequest,
  CreateChallengeRequest,
  CreateGoalRequest,
  CreateStudyGroupRequest,
  FeedEventResponse,
  GoalResponse,
  GroupQuizResponse,
  MentorshipResponse,
  QuizLeaderboardEntryResponse,
  SocialUserOptionResponse,
  StreakResponse,
  StudyGroupMemberResponse,
  StudyGroupResponse,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SocialGraphService {
  private readonly http = inject(HttpClient);

  createGroup(body: CreateStudyGroupRequest): Observable<StudyGroupResponse> {
    return this.http.post<StudyGroupResponse>(`${environment.apiUrl}/api/social/groups`, body);
  }

  joinGroup(groupId: number): Observable<StudyGroupResponse> {
    return this.http.post<StudyGroupResponse>(`${environment.apiUrl}/api/social/groups/${groupId}/join`, {});
  }

  leaveGroup(groupId: number): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/social/groups/${groupId}/leave`, {});
  }

  myGroups(): Observable<StudyGroupResponse[]> {
    return this.http.get<StudyGroupResponse[]>(`${environment.apiUrl}/api/social/groups/me`);
  }

  allGroups(): Observable<StudyGroupResponse[]> {
    return this.http.get<StudyGroupResponse[]>(`${environment.apiUrl}/api/social/groups`);
  }

  groupMembers(groupId: number): Observable<StudyGroupMemberResponse[]> {
    return this.http.get<StudyGroupMemberResponse[]>(`${environment.apiUrl}/api/social/groups/${groupId}/members`);
  }

  createGoal(body: CreateGoalRequest): Observable<GoalResponse> {
    return this.http.post<GoalResponse>(`${environment.apiUrl}/api/social/goals`, body);
  }

  updateGoalProgress(goalId: number, currentValue: number): Observable<GoalResponse> {
    return this.http.patch<GoalResponse>(`${environment.apiUrl}/api/social/goals/${goalId}/progress`, { currentValue });
  }

  myGoals(): Observable<GoalResponse[]> {
    return this.http.get<GoalResponse[]>(`${environment.apiUrl}/api/social/goals/me`);
  }

  createChallenge(body: CreateChallengeRequest): Observable<ChallengeResponse> {
    return this.http.post<ChallengeResponse>(`${environment.apiUrl}/api/social/challenges`, body);
  }

  updateChallengeProgress(challengeId: number, progressValue: number): Observable<ChallengeResponse> {
    return this.http.patch<ChallengeResponse>(`${environment.apiUrl}/api/social/challenges/${challengeId}/progress`, {
      progressValue,
    });
  }

  groupChallenges(groupId: number): Observable<ChallengeResponse[]> {
    return this.http.get<ChallengeResponse[]>(`${environment.apiUrl}/api/social/groups/${groupId}/challenges`);
  }

  createQuiz(body: CreateGroupQuizRequest): Observable<GroupQuizResponse> {
    return this.http.post<GroupQuizResponse>(`${environment.apiUrl}/api/social/quizzes`, body);
  }

  answerQuiz(quizId: number, selectedOption: 'A' | 'B' | 'C' | 'D'): Observable<GroupQuizResponse> {
    return this.http.post<GroupQuizResponse>(`${environment.apiUrl}/api/social/quizzes/${quizId}/answer`, { selectedOption });
  }

  groupQuizzes(groupId: number): Observable<GroupQuizResponse[]> {
    return this.http.get<GroupQuizResponse[]>(`${environment.apiUrl}/api/social/groups/${groupId}/quizzes`);
  }

  launchQuiz(groupId: number, body?: LaunchGroupQuizRequest): Observable<GroupQuizResponse[]> {
    return this.http.post<GroupQuizResponse[]>(`${environment.apiUrl}/api/social/groups/${groupId}/quizzes/launch`, body ?? {});
  }

  groupQuizLeaderboard(groupId: number): Observable<QuizLeaderboardEntryResponse[]> {
    return this.http.get<QuizLeaderboardEntryResponse[]>(`${environment.apiUrl}/api/social/groups/${groupId}/quizzes/leaderboard`);
  }

  challengeLeaderboard(challengeId: number): Observable<ChallengeLeaderboardEntryResponse[]> {
    return this.http.get<ChallengeLeaderboardEntryResponse[]>(
      `${environment.apiUrl}/api/social/challenges/${challengeId}/leaderboard`,
    );
  }

  requestMentorship(mentorId: number): Observable<MentorshipResponse> {
    return this.http.post<MentorshipResponse>(`${environment.apiUrl}/api/social/mentorship/requests`, { mentorId });
  }

  acceptMentorship(requestId: number): Observable<MentorshipResponse> {
    return this.http.post<MentorshipResponse>(`${environment.apiUrl}/api/social/mentorship/requests/${requestId}/accept`, {});
  }

  incomingMentorshipRequests(): Observable<MentorshipResponse[]> {
    return this.http.get<MentorshipResponse[]>(`${environment.apiUrl}/api/social/mentorship/requests/incoming`);
  }

  searchUsers(query: string): Observable<SocialUserOptionResponse[]> {
    return this.http.get<SocialUserOptionResponse[]>(`${environment.apiUrl}/api/social/users/search`, {
      params: { query },
    });
  }

  markActivity(): Observable<StreakResponse> {
    return this.http.post<StreakResponse>(`${environment.apiUrl}/api/social/streaks/activity`, {});
  }

  myStreak(): Observable<StreakResponse> {
    return this.http.get<StreakResponse>(`${environment.apiUrl}/api/social/streaks/me`);
  }

  feed(): Observable<FeedEventResponse[]> {
    return this.http.get<FeedEventResponse[]>(`${environment.apiUrl}/api/social/feed`);
  }
}
