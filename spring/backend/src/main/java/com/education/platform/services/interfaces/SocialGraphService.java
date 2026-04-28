package com.education.platform.services.interfaces;

import com.education.platform.dto.social.ChallengeResponse;
import com.education.platform.dto.social.ChallengeLeaderboardEntryResponse;
import com.education.platform.dto.social.CreateChallengeRequest;
import com.education.platform.dto.social.CreateGoalRequest;
import com.education.platform.dto.social.CreateQuizRequest;
import com.education.platform.dto.social.CreateStudyGroupRequest;
import com.education.platform.dto.social.FeedEventResponse;
import com.education.platform.dto.social.GoalResponse;
import com.education.platform.dto.social.GroupQuizResponse;
import com.education.platform.dto.social.QuizLeaderboardEntryResponse;
import com.education.platform.dto.social.MentorshipResponse;
import com.education.platform.dto.social.SocialUserOptionResponse;
import com.education.platform.dto.social.StreakResponse;
import com.education.platform.dto.social.StudyGroupResponse;
import com.education.platform.dto.social.StudyGroupMemberResponse;
import com.education.platform.entities.User;

import java.util.List;

public interface SocialGraphService {

    StudyGroupResponse createStudyGroup(User current, CreateStudyGroupRequest request);

    StudyGroupResponse joinStudyGroup(User current, Long groupId);

    void leaveStudyGroup(User current, Long groupId);

    List<StudyGroupResponse> myStudyGroups(User current);

    List<StudyGroupResponse> allStudyGroups(User current);

    List<StudyGroupMemberResponse> groupMembers(User current, Long groupId);

    GoalResponse createGoal(User current, CreateGoalRequest request);

    GoalResponse updateGoalProgress(User current, Long goalId, int currentValue);

    List<GoalResponse> myGoals(User current);

    ChallengeResponse createChallenge(User current, CreateChallengeRequest request);

    ChallengeResponse updateChallengeProgress(User current, Long challengeId, int progressValue);

    List<ChallengeResponse> groupChallenges(User current, Long groupId);

    GroupQuizResponse createQuiz(User current, CreateQuizRequest request);

    GroupQuizResponse answerQuiz(User current, Long quizId, String selectedOption);

    List<GroupQuizResponse> groupQuizzes(User current, Long groupId);

    List<GroupQuizResponse> launchGeneratedQuiz(User current, Long groupId, String topicOverride);

    List<QuizLeaderboardEntryResponse> groupQuizLeaderboard(User current, Long groupId);

    List<ChallengeLeaderboardEntryResponse> challengeLeaderboard(User current, Long challengeId);

    MentorshipResponse requestMentorship(User current, Long mentorId);

    MentorshipResponse acceptMentorship(User current, Long requestId);

    List<MentorshipResponse> incomingMentorshipRequests(User current);

    List<SocialUserOptionResponse> searchUsers(User current, String query);

    StreakResponse markDailyActivity(User current);

    StreakResponse myStreak(User current);

    List<FeedEventResponse> personalizedFeed(User current);
}
