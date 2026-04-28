package com.education.platform.controllers;

import com.education.platform.dto.social.ChallengeResponse;
import com.education.platform.dto.social.ChallengeLeaderboardEntryResponse;
import com.education.platform.dto.social.CreateChallengeRequest;
import com.education.platform.dto.social.CreateGoalRequest;
import com.education.platform.dto.social.CreateQuizRequest;
import com.education.platform.dto.social.CreateStudyGroupRequest;
import com.education.platform.dto.social.FeedEventResponse;
import com.education.platform.dto.social.GoalResponse;
import com.education.platform.dto.social.GroupQuizResponse;
import com.education.platform.dto.social.LaunchQuizRequest;
import com.education.platform.dto.social.MentorshipRequestCreateRequest;
import com.education.platform.dto.social.MentorshipResponse;
import com.education.platform.dto.social.QuizLeaderboardEntryResponse;
import com.education.platform.dto.social.SocialUserOptionResponse;
import com.education.platform.dto.social.StreakResponse;
import com.education.platform.dto.social.StudyGroupMemberResponse;
import com.education.platform.dto.social.StudyGroupResponse;
import com.education.platform.dto.social.AnswerQuizRequest;
import com.education.platform.dto.social.UpdateChallengeProgressRequest;
import com.education.platform.dto.social.UpdateGoalProgressRequest;
import com.education.platform.services.interfaces.CurrentUserService;
import com.education.platform.services.interfaces.SocialGraphService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/social")
public class SocialGraphController {

    private final CurrentUserService currentUserService;
    private final SocialGraphService socialGraphService;

    public SocialGraphController(CurrentUserService currentUserService, SocialGraphService socialGraphService) {
        this.currentUserService = currentUserService;
        this.socialGraphService = socialGraphService;
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public StudyGroupResponse createGroup(@Valid @RequestBody CreateStudyGroupRequest request) {
        return socialGraphService.createStudyGroup(currentUserService.getCurrentUser(), request);
    }

    @PostMapping("/groups/{groupId}/join")
    public StudyGroupResponse joinGroup(@PathVariable Long groupId) {
        return socialGraphService.joinStudyGroup(currentUserService.getCurrentUser(), groupId);
    }

    @PostMapping("/groups/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@PathVariable Long groupId) {
        socialGraphService.leaveStudyGroup(currentUserService.getCurrentUser(), groupId);
    }

    @GetMapping("/groups/me")
    public List<StudyGroupResponse> myGroups() {
        return socialGraphService.myStudyGroups(currentUserService.getCurrentUser());
    }

    @GetMapping("/groups")
    public List<StudyGroupResponse> allGroups() {
        return socialGraphService.allStudyGroups(currentUserService.getCurrentUser());
    }

    @GetMapping("/groups/{groupId}/members")
    public List<StudyGroupMemberResponse> groupMembers(@PathVariable Long groupId) {
        return socialGraphService.groupMembers(currentUserService.getCurrentUser(), groupId);
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return socialGraphService.createGoal(currentUserService.getCurrentUser(), request);
    }

    @PatchMapping("/goals/{goalId}/progress")
    public GoalResponse updateGoalProgress(@PathVariable Long goalId, @Valid @RequestBody UpdateGoalProgressRequest request) {
        return socialGraphService.updateGoalProgress(currentUserService.getCurrentUser(), goalId, request.getCurrentValue());
    }

    @GetMapping("/goals/me")
    public List<GoalResponse> myGoals() {
        return socialGraphService.myGoals(currentUserService.getCurrentUser());
    }

    @PostMapping("/challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(@Valid @RequestBody CreateChallengeRequest request) {
        return socialGraphService.createChallenge(currentUserService.getCurrentUser(), request);
    }

    @PatchMapping("/challenges/{challengeId}/progress")
    public ChallengeResponse updateChallengeProgress(
            @PathVariable Long challengeId,
            @Valid @RequestBody UpdateChallengeProgressRequest request
    ) {
        return socialGraphService.updateChallengeProgress(
                currentUserService.getCurrentUser(),
                challengeId,
                request.getProgressValue());
    }

    @GetMapping("/groups/{groupId}/challenges")
    public List<ChallengeResponse> groupChallenges(@PathVariable Long groupId) {
        return socialGraphService.groupChallenges(currentUserService.getCurrentUser(), groupId);
    }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupQuizResponse createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        return socialGraphService.createQuiz(currentUserService.getCurrentUser(), request);
    }

    @PostMapping("/quizzes/{quizId}/answer")
    public GroupQuizResponse answerQuiz(@PathVariable Long quizId, @Valid @RequestBody AnswerQuizRequest request) {
        return socialGraphService.answerQuiz(currentUserService.getCurrentUser(), quizId, request.getSelectedOption());
    }

    @GetMapping("/groups/{groupId}/quizzes")
    public List<GroupQuizResponse> groupQuizzes(@PathVariable Long groupId) {
        return socialGraphService.groupQuizzes(currentUserService.getCurrentUser(), groupId);
    }

    @PostMapping("/groups/{groupId}/quizzes/launch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<GroupQuizResponse> launchQuiz(
            @PathVariable Long groupId,
            @RequestBody(required = false) LaunchQuizRequest request
    ) {
        return socialGraphService.launchGeneratedQuiz(
                currentUserService.getCurrentUser(),
                groupId,
                request != null ? request.getTopic() : null
        );
    }

    @GetMapping("/groups/{groupId}/quizzes/leaderboard")
    public List<QuizLeaderboardEntryResponse> groupQuizLeaderboard(@PathVariable Long groupId) {
        return socialGraphService.groupQuizLeaderboard(currentUserService.getCurrentUser(), groupId);
    }

    @GetMapping("/challenges/{challengeId}/leaderboard")
    public List<ChallengeLeaderboardEntryResponse> challengeLeaderboard(@PathVariable Long challengeId) {
        return socialGraphService.challengeLeaderboard(currentUserService.getCurrentUser(), challengeId);
    }

    @PostMapping("/mentorship/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public MentorshipResponse requestMentorship(@Valid @RequestBody MentorshipRequestCreateRequest request) {
        return socialGraphService.requestMentorship(currentUserService.getCurrentUser(), request.getMentorId());
    }

    @PostMapping("/mentorship/requests/{requestId}/accept")
    public MentorshipResponse acceptMentorship(@PathVariable Long requestId) {
        return socialGraphService.acceptMentorship(currentUserService.getCurrentUser(), requestId);
    }

    @GetMapping("/mentorship/requests/incoming")
    public List<MentorshipResponse> incomingMentorshipRequests() {
        return socialGraphService.incomingMentorshipRequests(currentUserService.getCurrentUser());
    }

    @GetMapping("/users/search")
    public List<SocialUserOptionResponse> searchUsers(@RequestParam("query") String query) {
        return socialGraphService.searchUsers(currentUserService.getCurrentUser(), query);
    }

    @PostMapping("/streaks/activity")
    public StreakResponse markActivity() {
        return socialGraphService.markDailyActivity(currentUserService.getCurrentUser());
    }

    @GetMapping("/streaks/me")
    public StreakResponse myStreak() {
        return socialGraphService.myStreak(currentUserService.getCurrentUser());
    }

    @GetMapping("/feed")
    public List<FeedEventResponse> myFeed() {
        return socialGraphService.personalizedFeed(currentUserService.getCurrentUser());
    }
}
