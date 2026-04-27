package com.education.platform.services.implementations;

import com.education.platform.common.ApiException;
import com.education.platform.dto.social.ChallengeResponse;
import com.education.platform.dto.social.ChallengeLeaderboardEntryResponse;
import com.education.platform.dto.social.CreateChallengeRequest;
import com.education.platform.dto.social.CreateGoalRequest;
import com.education.platform.dto.social.CreateQuizRequest;
import com.education.platform.dto.social.CreateStudyGroupRequest;
import com.education.platform.dto.social.FeedEventResponse;
import com.education.platform.dto.social.GoalResponse;
import com.education.platform.dto.social.GroupQuizResponse;
import com.education.platform.dto.social.MentorshipResponse;
import com.education.platform.dto.social.QuizLeaderboardEntryResponse;
import com.education.platform.dto.social.SocialUserOptionResponse;
import com.education.platform.dto.social.StreakResponse;
import com.education.platform.dto.social.StudyGroupMemberResponse;
import com.education.platform.dto.social.StudyGroupResponse;
import com.education.platform.entities.ChallengeProgress;
import com.education.platform.entities.FeedEvent;
import com.education.platform.entities.FeedEventType;
import com.education.platform.entities.FeedProgressStage;
import com.education.platform.entities.GoalStatus;
import com.education.platform.entities.GoalVisibility;
import com.education.platform.entities.GroupQuiz;
import com.education.platform.entities.GroupMemberRole;
import com.education.platform.entities.Mentorship;
import com.education.platform.entities.MentorshipStatus;
import com.education.platform.entities.QuizAnswer;
import com.education.platform.entities.SharedGoal;
import com.education.platform.entities.SocialChallenge;
import com.education.platform.entities.StudyGroup;
import com.education.platform.entities.StudyGroupMember;
import com.education.platform.entities.User;
import com.education.platform.entities.UserStreak;
import com.education.platform.repositories.ChallengeProgressRepository;
import com.education.platform.repositories.FeedEventRepository;
import com.education.platform.repositories.GroupQuizRepository;
import com.education.platform.repositories.MentorshipRepository;
import com.education.platform.repositories.QuizAnswerRepository;
import com.education.platform.repositories.SharedGoalRepository;
import com.education.platform.repositories.SocialChallengeRepository;
import com.education.platform.repositories.StudyGroupMemberRepository;
import com.education.platform.repositories.StudyGroupRepository;
import com.education.platform.repositories.UserRepository;
import com.education.platform.repositories.UserStreakRepository;
import com.education.platform.services.interfaces.SocialGraphService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class SocialGraphServiceImpl implements SocialGraphService {
    private static final int GENERATED_QUIZ_SIZE = 10;

    private final UserRepository userRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMemberRepository groupMemberRepository;
    private final SharedGoalRepository goalRepository;
    private final SocialChallengeRepository challengeRepository;
    private final ChallengeProgressRepository challengeProgressRepository;
    private final MentorshipRepository mentorshipRepository;
    private final UserStreakRepository streakRepository;
    private final FeedEventRepository feedEventRepository;
    private final GroupQuizRepository groupQuizRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizGenerationService quizGenerationService;

    public SocialGraphServiceImpl(
            UserRepository userRepository,
            StudyGroupRepository studyGroupRepository,
            StudyGroupMemberRepository groupMemberRepository,
            SharedGoalRepository goalRepository,
            SocialChallengeRepository challengeRepository,
            ChallengeProgressRepository challengeProgressRepository,
            MentorshipRepository mentorshipRepository,
            UserStreakRepository streakRepository,
            FeedEventRepository feedEventRepository,
            GroupQuizRepository groupQuizRepository,
            QuizAnswerRepository quizAnswerRepository,
            QuizGenerationService quizGenerationService
    ) {
        this.userRepository = userRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.goalRepository = goalRepository;
        this.challengeRepository = challengeRepository;
        this.challengeProgressRepository = challengeProgressRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.streakRepository = streakRepository;
        this.feedEventRepository = feedEventRepository;
        this.groupQuizRepository = groupQuizRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.quizGenerationService = quizGenerationService;
    }

    @Override
    @Transactional
    public StudyGroupResponse createStudyGroup(User current, CreateStudyGroupRequest request) {
        User me = getManagedUser(current.getId());
        StudyGroup group = StudyGroup.builder()
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .topic(trimToNull(request.getTopic()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .createdAt(Instant.now())
                .owner(me)
                .build();
        studyGroupRepository.save(group);

        StudyGroupMember ownerMember = StudyGroupMember.builder()
                .group(group)
                .user(me)
                .role(GroupMemberRole.OWNER)
                .joinedAt(Instant.now())
                .build();
        groupMemberRepository.save(ownerMember);

        createFeedEvent(me, group, null, null, FeedEventType.GROUP_JOINED, group.getTopic(),
                FeedProgressStage.BEGINNER, me.getUsername() + " created study group \"" + group.getName() + "\"", 3);
        touchStreak(me);
        return toStudyGroupResponse(group, ownerMember.getRole(), 1);
    }

    @Override
    @Transactional
    public StudyGroupResponse joinStudyGroup(User current, Long groupId) {
        User me = getManagedUser(current.getId());
        StudyGroup group = getStudyGroup(groupId);
        groupMemberRepository.findByGroup_IdAndUser_Id(groupId, me.getId()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Vous êtes déjà membre de ce groupe");
        });
        StudyGroupMember member = StudyGroupMember.builder()
                .group(group)
                .user(me)
                .role(GroupMemberRole.MEMBER)
                .joinedAt(Instant.now())
                .build();
        groupMemberRepository.save(member);
        int count = groupMemberRepository.findByGroup_Id(groupId).size();
        createFeedEvent(me, group, null, null, FeedEventType.GROUP_JOINED, group.getTopic(),
                FeedProgressStage.BEGINNER, me.getUsername() + " joined study group \"" + group.getName() + "\"", 2);
        touchStreak(me);
        return toStudyGroupResponse(group, member.getRole(), count);
    }

    @Override
    @Transactional
    public void leaveStudyGroup(User current, Long groupId) {
        User me = getManagedUser(current.getId());
        StudyGroupMember member = groupMemberRepository.findByGroup_IdAndUser_Id(groupId, me.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vous n'êtes pas membre de ce groupe"));
        if (member.getRole() == GroupMemberRole.OWNER) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le propriétaire ne peut pas quitter son groupe");
        }
        groupMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> myStudyGroups(User current) {
        return groupMemberRepository.findByUser_Id(current.getId()).stream()
                .map(member -> toStudyGroupResponse(
                        member.getGroup(),
                        member.getRole(),
                        groupMemberRepository.findByGroup_Id(member.getGroup().getId()).size()))
                .sorted(Comparator.comparing(StudyGroupResponse::getCreatedAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupResponse> allStudyGroups(User current) {
        return studyGroupRepository.findAll().stream()
                .map(group -> {
                    GroupMemberRole role = groupMemberRepository.findByGroup_IdAndUser_Id(group.getId(), current.getId())
                            .map(StudyGroupMember::getRole)
                            .orElse(null);
                    int memberCount = groupMemberRepository.findByGroup_Id(group.getId()).size();
                    return toStudyGroupResponse(group, role, memberCount);
                })
                .sorted(Comparator.comparing(StudyGroupResponse::getCreatedAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyGroupMemberResponse> groupMembers(User current, Long groupId) {
        ensureGroupMember(current.getId(), groupId);
        Map<Long, Integer> quizScoresByUser = buildQuizScoresByUser(groupId);
        return groupMemberRepository.findByGroup_Id(groupId).stream()
                .sorted(Comparator
                        .comparing((StudyGroupMember m) -> m.getRole() == GroupMemberRole.OWNER ? 0 : 1)
                        .thenComparing(m -> m.getUser().getUsername(), String.CASE_INSENSITIVE_ORDER))
                .map(member -> {
                    User user = member.getUser();
                    String firstName = user.getProfile() != null ? user.getProfile().getFirstName() : null;
                    String lastName = user.getProfile() != null ? user.getProfile().getLastName() : null;
                    String profilePicture = user.getProfile() != null ? user.getProfile().getProfilePicture() : null;
                    return StudyGroupMemberResponse.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .firstName(firstName)
                            .lastName(lastName)
                            .profilePicture(profilePicture)
                            .role(member.getRole().name())
                            .score(quizScoresByUser.getOrDefault(user.getId(), 0))
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public GoalResponse createGoal(User current, CreateGoalRequest request) {
        User me = getManagedUser(current.getId());
        StudyGroup group = null;
        if (request.getGroupId() != null) {
            group = getStudyGroup(request.getGroupId());
            ensureGroupMember(me.getId(), group.getId());
        }

        SharedGoal goal = SharedGoal.builder()
                .user(me)
                .group(group)
                .title(request.getTitle().trim())
                .topic(trimToNull(request.getTopic()))
                .targetValue(request.getTargetValue())
                .currentValue(0)
                .deadline(request.getDeadline())
                .visibility(request.getVisibility())
                .status(GoalStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        goalRepository.save(goal);
        createFeedEvent(me, group, goal, null, FeedEventType.GOAL_CREATED, goal.getTopic(),
                FeedProgressStage.BEGINNER, me.getUsername() + " created goal \"" + goal.getTitle() + "\"", 4);
        touchStreak(me);
        return toGoalResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoalProgress(User current, Long goalId, int currentValue) {
        User me = getManagedUser(current.getId());
        SharedGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Objectif introuvable"));
        if (!goal.getUser().getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous ne pouvez pas modifier cet objectif");
        }
        if (currentValue < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La progression ne peut pas être négative");
        }
        goal.setCurrentValue(Math.min(currentValue, goal.getTargetValue()));
        goal.setStatus(goal.getCurrentValue() >= goal.getTargetValue() ? GoalStatus.COMPLETED : GoalStatus.ACTIVE);
        goal.setUpdatedAt(Instant.now());

        FeedEventType eventType = goal.getStatus() == GoalStatus.COMPLETED ? FeedEventType.GOAL_COMPLETED : FeedEventType.GOAL_PROGRESS;
        createFeedEvent(me, goal.getGroup(), goal, null, eventType, goal.getTopic(), inferStage(goal),
                me.getUsername() + " updated progress for goal \"" + goal.getTitle() + "\"", 6);
        touchStreak(me);
        return toGoalResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> myGoals(User current) {
        return goalRepository.findByUser_IdOrderByUpdatedAtDesc(current.getId()).stream()
                .map(this::toGoalResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChallengeResponse createChallenge(User current, CreateChallengeRequest request) {
        User me = getManagedUser(current.getId());
        StudyGroup group = getStudyGroup(request.getGroupId());
        ensureGroupMember(me.getId(), group.getId());
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La date de fin doit être après la date de début");
        }

        SocialChallenge challenge = SocialChallenge.builder()
                .group(group)
                .createdBy(me)
                .title(request.getTitle().trim())
                .description(trimToNull(request.getDescription()))
                .topic(trimToNull(request.getTopic()))
                .targetValue(request.getTargetValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(Instant.now())
                .build();
        challengeRepository.save(challenge);
        createFeedEvent(me, group, null, challenge, FeedEventType.CHALLENGE_CREATED, challenge.getTopic(),
                FeedProgressStage.BEGINNER, me.getUsername() + " created challenge \"" + challenge.getTitle() + "\"", 5);
        touchStreak(me);
        return toChallengeResponse(challenge, null);
    }

    @Override
    @Transactional
    public ChallengeResponse updateChallengeProgress(User current, Long challengeId, int progressValue) {
        User me = getManagedUser(current.getId());
        SocialChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Challenge introuvable"));
        ensureGroupMember(me.getId(), challenge.getGroup().getId());

        if (progressValue < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La progression doit être positive");
        }

        ChallengeProgress progress = challengeProgressRepository.findByChallenge_IdAndUser_Id(challengeId, me.getId())
                .orElse(ChallengeProgress.builder()
                        .challenge(challenge)
                        .user(me)
                        .progressValue(0)
                        .points(0)
                        .completed(false)
                        .updatedAt(Instant.now())
                        .build());
        progress.setProgressValue(Math.min(progressValue, challenge.getTargetValue()));
        boolean done = progress.getProgressValue() >= challenge.getTargetValue();
        progress.setCompleted(done);
        progress.setPoints(Math.max(progress.getPoints(), (int) Math.round((progress.getProgressValue() * 100.0) / challenge.getTargetValue())));
        progress.setUpdatedAt(Instant.now());
        if (done && progress.getCompletedAt() == null) {
            progress.setCompletedAt(Instant.now());
            createFeedEvent(me, challenge.getGroup(), null, challenge, FeedEventType.CHALLENGE_COMPLETED, challenge.getTopic(),
                    FeedProgressStage.ADVANCED, me.getUsername() + " completed challenge \"" + challenge.getTitle() + "\"", 8);
        }
        challengeProgressRepository.save(progress);
        touchStreak(me);
        return toChallengeResponse(challenge, progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeResponse> groupChallenges(User current, Long groupId) {
        ensureGroupMember(current.getId(), groupId);
        return challengeRepository.findByGroup_IdOrderByStartDateDesc(groupId).stream()
                .map(challenge -> toChallengeResponse(
                        challenge,
                        challengeProgressRepository.findByChallenge_IdAndUser_Id(challenge.getId(), current.getId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional
    public GroupQuizResponse createQuiz(User current, CreateQuizRequest request) {
        User me = getManagedUser(current.getId());
        StudyGroup group = getStudyGroup(request.getGroupId());
        ensureGroupMember(me.getId(), group.getId());

        GroupQuiz quiz = createQuizEntity(
                group,
                me,
                request.getQuestion(),
                request.getOptionA(),
                request.getOptionB(),
                request.getOptionC(),
                request.getOptionD(),
                request.getCorrectOption()
        );
        groupQuizRepository.save(quiz);

        createFeedEvent(me, group, null, null, FeedEventType.CHALLENGE_CREATED, group.getTopic(),
                FeedProgressStage.INTERMEDIATE, me.getUsername() + " posted a quiz in \"" + group.getName() + "\"", 5);
        touchStreak(me);
        return toQuizResponse(quiz, me.getId());
    }

    @Override
    @Transactional
    public GroupQuizResponse answerQuiz(User current, Long quizId, String selectedOption) {
        User me = getManagedUser(current.getId());
        GroupQuiz quiz = groupQuizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Quiz introuvable"));
        ensureGroupMember(me.getId(), quiz.getGroup().getId());

        String normalized = normalizeOption(selectedOption);
        boolean correct = normalized.equals(quiz.getCorrectOption());

        QuizAnswer answer = quizAnswerRepository.findByQuiz_IdAndUser_Id(quizId, me.getId())
                .orElse(QuizAnswer.builder()
                        .quiz(quiz)
                        .user(me)
                        .answeredAt(Instant.now())
                        .build());
        answer.setSelectedOption(normalized);
        answer.setCorrect(correct);
        answer.setAnsweredAt(Instant.now());
        quizAnswerRepository.save(answer);

        createFeedEvent(me, quiz.getGroup(), null, null, FeedEventType.GOAL_PROGRESS, quiz.getGroup().getTopic(),
                correct ? FeedProgressStage.ADVANCED : FeedProgressStage.INTERMEDIATE,
                me.getUsername() + " answered a quiz in \"" + quiz.getGroup().getName() + "\"", 4);
        touchStreak(me);
        return toQuizResponse(quiz, me.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupQuizResponse> groupQuizzes(User current, Long groupId) {
        ensureGroupMember(current.getId(), groupId);
        return groupQuizRepository.findByGroup_IdOrderByCreatedAtDesc(groupId).stream()
                .map(quiz -> toQuizResponse(quiz, current.getId()))
                .toList();
    }

    @Override
    @Transactional
    public List<GroupQuizResponse> launchGeneratedQuiz(User current, Long groupId, String topicOverride) {
        User me = getManagedUser(current.getId());
        StudyGroup group = getStudyGroup(groupId);
        ensureGroupMember(me.getId(), groupId);

        String effectiveTopic = trimToNull(topicOverride);
        if (effectiveTopic == null) {
            effectiveTopic = group.getTopic();
        }
        if (effectiveTopic == null) {
            effectiveTopic = "general learning";
        }

        List<GroupQuiz> created = quizGenerationService.generateQuestions(effectiveTopic, GENERATED_QUIZ_SIZE).stream()
                .map(generated -> createQuizEntity(
                        group,
                        me,
                        generated.question(),
                        generated.optionA(),
                        generated.optionB(),
                        generated.optionC(),
                        generated.optionD(),
                        generated.correctOption()))
                .toList();
        groupQuizRepository.saveAll(created);

        createFeedEvent(me, group, null, null, FeedEventType.CHALLENGE_CREATED, effectiveTopic,
                FeedProgressStage.INTERMEDIATE,
                me.getUsername() + " launched an AI quiz set (" + GENERATED_QUIZ_SIZE + " questions) in \"" + group.getName() + "\"",
                6);
        touchStreak(me);
        return created.stream().map(quiz -> toQuizResponse(quiz, me.getId())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizLeaderboardEntryResponse> groupQuizLeaderboard(User current, Long groupId) {
        ensureGroupMember(current.getId(), groupId);
        List<QuizAnswer> answers = quizAnswerRepository.findByQuiz_Group_Id(groupId);
        Map<Long, List<QuizAnswer>> byUser = answers.stream()
                .collect(java.util.stream.Collectors.groupingBy(a -> a.getUser().getId()));

        return byUser.entrySet().stream()
                .map(entry -> {
                    List<QuizAnswer> userAnswers = entry.getValue();
                    int quizzesAnswered = userAnswers.size();
                    int correctAnswers = (int) userAnswers.stream().filter(QuizAnswer::isCorrect).count();
                    int totalScore = (correctAnswers * 100) + (quizzesAnswered * 10);
                    int accuracy = quizzesAnswered == 0 ? 0 : (int) Math.round((correctAnswers * 100.0) / quizzesAnswered);
                    User u = userAnswers.get(0).getUser();
                    return QuizLeaderboardEntryResponse.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .quizzesAnswered(quizzesAnswered)
                            .correctAnswers(correctAnswers)
                            .totalScore(totalScore)
                            .accuracyPercent(accuracy)
                            .build();
                })
                .sorted(Comparator.comparing(QuizLeaderboardEntryResponse::getTotalScore).reversed()
                        .thenComparing(QuizLeaderboardEntryResponse::getCorrectAnswers).reversed()
                        .thenComparing(QuizLeaderboardEntryResponse::getQuizzesAnswered).reversed()
                        .thenComparing(QuizLeaderboardEntryResponse::getUsername))
                .collect(java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.toList(), rows ->
                        java.util.stream.IntStream.range(0, rows.size())
                                .mapToObj(i -> QuizLeaderboardEntryResponse.builder()
                                        .rank(i + 1)
                                        .userId(rows.get(i).getUserId())
                                        .username(rows.get(i).getUsername())
                                        .quizzesAnswered(rows.get(i).getQuizzesAnswered())
                                        .correctAnswers(rows.get(i).getCorrectAnswers())
                                        .totalScore(rows.get(i).getTotalScore())
                                        .accuracyPercent(rows.get(i).getAccuracyPercent())
                                        .build())
                                .toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeLeaderboardEntryResponse> challengeLeaderboard(User current, Long challengeId) {
        SocialChallenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Challenge introuvable"));
        ensureGroupMember(current.getId(), challenge.getGroup().getId());
        List<ChallengeProgress> progress = challengeProgressRepository.findByChallenge_IdOrderByPointsDesc(challengeId);
        return java.util.stream.IntStream.range(0, progress.size())
                .mapToObj(index -> {
                    ChallengeProgress entry = progress.get(index);
                    return ChallengeLeaderboardEntryResponse.builder()
                            .rank(index + 1)
                            .userId(entry.getUser().getId())
                            .username(entry.getUser().getUsername())
                            .progressValue(entry.getProgressValue())
                            .points(entry.getPoints())
                            .completed(entry.isCompleted())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public MentorshipResponse requestMentorship(User current, Long mentorId) {
        User me = getManagedUser(current.getId());
        User mentor = getManagedUser(mentorId);
        if (me.getId().equals(mentor.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas vous mentorer vous-même");
        }
        mentorshipRepository.findByMentor_IdAndMentee_IdAndStatus(mentor.getId(), me.getId(), MentorshipStatus.PENDING)
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Demande de mentorat déjà envoyée");
                });

        Mentorship mentorship = Mentorship.builder()
                .mentor(mentor)
                .mentee(me)
                .status(MentorshipStatus.PENDING)
                .requestedAt(Instant.now())
                .build();
        mentorshipRepository.save(mentorship);
        return toMentorshipResponse(mentorship);
    }

    @Override
    @Transactional
    public MentorshipResponse acceptMentorship(User current, Long requestId) {
        User me = getManagedUser(current.getId());
        Mentorship mentorship = mentorshipRepository.findByIdAndMentor_Id(requestId, me.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande de mentorat introuvable"));
        if (mentorship.getStatus() != MentorshipStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette demande n'est plus en attente");
        }
        mentorship.setStatus(MentorshipStatus.ACTIVE);
        mentorship.setAcceptedAt(Instant.now());
        createFeedEvent(me, null, null, null, FeedEventType.MENTORSHIP_ACCEPTED, null,
                FeedProgressStage.INTERMEDIATE,
                me.getUsername() + " accepted mentorship with " + mentorship.getMentee().getUsername(), 7);
        touchStreak(me);
        return toMentorshipResponse(mentorship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MentorshipResponse> incomingMentorshipRequests(User current) {
        return mentorshipRepository.findByMentor_IdAndStatus(current.getId(), MentorshipStatus.PENDING).stream()
                .map(this::toMentorshipResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SocialUserOptionResponse> searchUsers(User current, String query) {
        String trimmed = query != null ? query.trim() : "";
        if (trimmed.length() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tapez au moins 2 caracteres pour rechercher");
        }
        return userRepository.findByUsernameContainingIgnoreCaseOrderByUsernameAsc(trimmed, PageRequest.of(0, 20)).stream()
                .filter(user -> !user.getId().equals(current.getId()))
                .map(user -> SocialUserOptionResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getProfile() != null ? user.getProfile().getFirstName() : null)
                        .lastName(user.getProfile() != null ? user.getProfile().getLastName() : null)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public StreakResponse markDailyActivity(User current) {
        User me = getManagedUser(current.getId());
        UserStreak streak = touchStreak(me);
        return toStreakResponse(streak);
    }

    @Override
    @Transactional(readOnly = true)
    public StreakResponse myStreak(User current) {
        UserStreak streak = streakRepository.findByUser_Id(current.getId())
                .orElse(UserStreak.builder()
                        .user(current)
                        .currentStreak(0)
                        .bestStreak(0)
                        .lastActivityDate(null)
                        .updatedAt(Instant.now())
                        .build());
        return toStreakResponse(streak);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedEventResponse> personalizedFeed(User current) {
        User me = getManagedUser(current.getId());
        Set<Long> friendIds = me.getFriends().stream().map(User::getId).collect(java.util.stream.Collectors.toSet());
        Set<Long> myGroupIds = groupMemberRepository.findByUser_Id(me.getId()).stream()
                .map(m -> m.getGroup().getId())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> interests = new HashSet<>();
        if (me.getProfile() != null && me.getProfile().getInterests() != null) {
            me.getProfile().getInterests().forEach(i -> interests.add(i.toLowerCase(Locale.ROOT)));
        }
        FeedProgressStage myStage = inferMyStage(me.getId());

        Instant now = Instant.now();
        return feedEventRepository.findTop200ByOrderByCreatedAtDesc().stream()
                .filter(event -> isVisibleToUser(event, me.getId(), friendIds, myGroupIds))
                .map(event -> toFeedResponse(event, computeRanking(event, me.getId(), friendIds, myGroupIds, interests, myStage, now)))
                .sorted(Comparator.comparing(FeedEventResponse::getRankingScore).reversed()
                        .thenComparing(FeedEventResponse::getCreatedAt).reversed())
                .limit(50)
                .toList();
    }

    private boolean isVisibleToUser(FeedEvent event, Long myId, Set<Long> friendIds, Set<Long> myGroupIds) {
        if (event.getActor().getId().equals(myId)) {
            return true;
        }
        if (event.getGoal() != null) {
            GoalVisibility visibility = event.getGoal().getVisibility();
            if (visibility == GoalVisibility.PRIVATE) {
                return false;
            }
            if (visibility == GoalVisibility.FRIENDS) {
                return friendIds.contains(event.getActor().getId());
            }
            if (visibility == GoalVisibility.GROUP) {
                return event.getGroup() != null && myGroupIds.contains(event.getGroup().getId());
            }
            return true;
        }
        if (event.getGroup() != null) {
            return myGroupIds.contains(event.getGroup().getId()) || friendIds.contains(event.getActor().getId());
        }
        return true;
    }

    private double computeRanking(
            FeedEvent event,
            Long myId,
            Set<Long> friendIds,
            Set<Long> myGroupIds,
            Set<String> interests,
            FeedProgressStage myStage,
            Instant now
    ) {
        double affinity = 0.2;
        if (event.getActor().getId().equals(myId)) {
            affinity = 1.0;
        } else if (friendIds.contains(event.getActor().getId())) {
            affinity = 0.9;
        } else if (event.getGroup() != null && myGroupIds.contains(event.getGroup().getId())) {
            affinity = 0.7;
        }

        double topicMatch = 0.3;
        if (event.getTopic() != null && interests.contains(event.getTopic().toLowerCase(Locale.ROOT))) {
            topicMatch = 1.0;
        }

        double stageMatch = 0.5;
        if (event.getProgressStage() != null && myStage != null) {
            stageMatch = event.getProgressStage() == myStage ? 1.0 : 0.6;
        }

        long ageHours = Math.max(0L, Duration.between(event.getCreatedAt(), now).toHours());
        double freshness = Math.max(0.05, 1.0 - (ageHours / 72.0));
        double engagement = Math.min(1.0, event.getEngagementScore() / 10.0);

        return (0.35 * affinity) + (0.25 * topicMatch) + (0.2 * stageMatch) + (0.15 * freshness) + (0.05 * engagement);
    }

    private FeedEventResponse toFeedResponse(FeedEvent event, double score) {
        return FeedEventResponse.builder()
                .id(event.getId())
                .type(event.getType())
                .actorUsername(event.getActor().getUsername())
                .message(event.getMessage())
                .topic(event.getTopic())
                .groupId(event.getGroup() != null ? event.getGroup().getId() : null)
                .goalId(event.getGoal() != null ? event.getGoal().getId() : null)
                .challengeId(event.getChallenge() != null ? event.getChallenge().getId() : null)
                .createdAt(event.getCreatedAt())
                .rankingScore(score)
                .build();
    }

    private StudyGroup getStudyGroup(Long id) {
        return studyGroupRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Groupe introuvable"));
    }

    private User getManagedUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private void ensureGroupMember(Long userId, Long groupId) {
        groupMemberRepository.findByGroup_IdAndUser_Id(groupId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Accès refusé : vous n'êtes pas membre de ce groupe"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private UserStreak touchStreak(User user) {
        LocalDate today = LocalDate.now();
        UserStreak streak = streakRepository.findByUser_Id(user.getId())
                .orElse(UserStreak.builder()
                        .user(user)
                        .currentStreak(0)
                        .bestStreak(0)
                        .updatedAt(Instant.now())
                        .build());
        if (streak.getLastActivityDate() == null) {
            streak.setCurrentStreak(1);
        } else {
            if (streak.getLastActivityDate().isEqual(today.minusDays(1))) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else if (!streak.getLastActivityDate().isEqual(today)) {
                streak.setCurrentStreak(1);
            }
        }
        if (streak.getCurrentStreak() > streak.getBestStreak()) {
            streak.setBestStreak(streak.getCurrentStreak());
        }
        streak.setLastActivityDate(today);
        streak.setUpdatedAt(Instant.now());
        streakRepository.save(streak);
        createFeedEvent(user, null, null, null, FeedEventType.STREAK_UPDATED, null, FeedProgressStage.BEGINNER,
                user.getUsername() + " updated their learning streak to " + streak.getCurrentStreak(), 3);
        return streak;
    }

    private void createFeedEvent(
            User actor,
            StudyGroup group,
            SharedGoal goal,
            SocialChallenge challenge,
            FeedEventType type,
            String topic,
            FeedProgressStage stage,
            String message,
            int engagementScore
    ) {
        FeedEvent event = FeedEvent.builder()
                .actor(actor)
                .group(group)
                .goal(goal)
                .challenge(challenge)
                .type(type)
                .topic(topic)
                .progressStage(stage)
                .message(message)
                .engagementScore(engagementScore)
                .createdAt(Instant.now())
                .build();
        feedEventRepository.save(event);
    }

    private FeedProgressStage inferMyStage(Long userId) {
        List<SharedGoal> goals = goalRepository.findByUser_IdOrderByUpdatedAtDesc(userId);
        if (goals.isEmpty()) {
            return FeedProgressStage.BEGINNER;
        }
        double avg = goals.stream()
                .mapToDouble(g -> g.getTargetValue() <= 0 ? 0.0 : ((double) g.getCurrentValue() / g.getTargetValue()))
                .average()
                .orElse(0.0);
        if (avg >= 0.8) {
            return FeedProgressStage.ADVANCED;
        }
        if (avg >= 0.4) {
            return FeedProgressStage.INTERMEDIATE;
        }
        return FeedProgressStage.BEGINNER;
    }

    private FeedProgressStage inferStage(SharedGoal goal) {
        if (goal.getTargetValue() <= 0) {
            return FeedProgressStage.BEGINNER;
        }
        double ratio = (double) goal.getCurrentValue() / goal.getTargetValue();
        if (ratio >= 0.8) {
            return FeedProgressStage.ADVANCED;
        }
        if (ratio >= 0.4) {
            return FeedProgressStage.INTERMEDIATE;
        }
        return FeedProgressStage.BEGINNER;
    }

    private String normalizeOption(String value) {
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Option de réponse invalide");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("A") && !normalized.equals("B") && !normalized.equals("C") && !normalized.equals("D")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Option de réponse invalide");
        }
        return normalized;
    }

    private GroupQuiz createQuizEntity(
            StudyGroup group,
            User createdBy,
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption
    ) {
        String q = question != null ? question.trim() : "";
        String a = optionA != null ? optionA.trim() : "";
        String b = optionB != null ? optionB.trim() : "";
        String c = optionC != null ? optionC.trim() : "";
        String d = optionD != null ? optionD.trim() : "";
        if (q.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Question et options du quiz sont obligatoires");
        }
        return GroupQuiz.builder()
                .group(group)
                .createdBy(createdBy)
                .question(q)
                .optionA(a)
                .optionB(b)
                .optionC(c)
                .optionD(d)
                .correctOption(normalizeOption(correctOption))
                .createdAt(Instant.now())
                .build();
    }

    private StudyGroupResponse toStudyGroupResponse(StudyGroup group, GroupMemberRole myRole, int memberCount) {
        return StudyGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .topic(group.getTopic())
                .imageUrl(group.getImageUrl())
                .createdAt(group.getCreatedAt())
                .ownerUsername(group.getOwner().getUsername())
                .memberCount(memberCount)
                .myRole(myRole != null ? myRole.name() : null)
                .build();
    }

    private GoalResponse toGoalResponse(SharedGoal goal) {
        int percent = goal.getTargetValue() <= 0 ? 0 : (int) Math.round((goal.getCurrentValue() * 100.0) / goal.getTargetValue());
        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .topic(goal.getTopic())
                .targetValue(goal.getTargetValue())
                .currentValue(goal.getCurrentValue())
                .completionPercent(Math.min(percent, 100))
                .deadline(goal.getDeadline())
                .visibility(goal.getVisibility())
                .status(goal.getStatus())
                .ownerUsername(goal.getUser().getUsername())
                .groupId(goal.getGroup() != null ? goal.getGroup().getId() : null)
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    private ChallengeResponse toChallengeResponse(SocialChallenge challenge, ChallengeProgress myProgress) {
        return ChallengeResponse.builder()
                .id(challenge.getId())
                .groupId(challenge.getGroup().getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .topic(challenge.getTopic())
                .targetValue(challenge.getTargetValue())
                .startDate(challenge.getStartDate())
                .endDate(challenge.getEndDate())
                .createdBy(challenge.getCreatedBy().getUsername())
                .createdAt(challenge.getCreatedAt())
                .myProgress(myProgress != null ? myProgress.getProgressValue() : 0)
                .myPoints(myProgress != null ? myProgress.getPoints() : 0)
                .myCompleted(myProgress != null && myProgress.isCompleted())
                .build();
    }

    private GroupQuizResponse toQuizResponse(GroupQuiz quiz, Long userId) {
        QuizAnswer myAnswer = quizAnswerRepository.findByQuiz_IdAndUser_Id(quiz.getId(), userId).orElse(null);
        return GroupQuizResponse.builder()
                .id(quiz.getId())
                .groupId(quiz.getGroup().getId())
                .question(quiz.getQuestion())
                .optionA(quiz.getOptionA())
                .optionB(quiz.getOptionB())
                .optionC(quiz.getOptionC())
                .optionD(quiz.getOptionD())
                .createdBy(quiz.getCreatedBy().getUsername())
                .createdAt(quiz.getCreatedAt())
                .answerCount(quizAnswerRepository.countByQuiz_Id(quiz.getId()))
                .optionACount(quizAnswerRepository.countByQuiz_IdAndSelectedOption(quiz.getId(), "A"))
                .optionBCount(quizAnswerRepository.countByQuiz_IdAndSelectedOption(quiz.getId(), "B"))
                .optionCCount(quizAnswerRepository.countByQuiz_IdAndSelectedOption(quiz.getId(), "C"))
                .optionDCount(quizAnswerRepository.countByQuiz_IdAndSelectedOption(quiz.getId(), "D"))
                .mySelectedOption(myAnswer != null ? myAnswer.getSelectedOption() : null)
                .myCorrect(myAnswer != null && myAnswer.isCorrect())
                .build();
    }

    private Map<Long, Integer> buildQuizScoresByUser(Long groupId) {
        return quizAnswerRepository.findByQuiz_Group_Id(groupId).stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getUser().getId(),
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors.toList(),
                                answers -> {
                                    int quizzesAnswered = answers.size();
                                    int correctAnswers = (int) answers.stream().filter(QuizAnswer::isCorrect).count();
                                    return (correctAnswers * 100) + (quizzesAnswered * 10);
                                })));
    }

    private MentorshipResponse toMentorshipResponse(Mentorship mentorship) {
        return MentorshipResponse.builder()
                .id(mentorship.getId())
                .mentorId(mentorship.getMentor().getId())
                .mentorUsername(mentorship.getMentor().getUsername())
                .menteeId(mentorship.getMentee().getId())
                .menteeUsername(mentorship.getMentee().getUsername())
                .status(mentorship.getStatus())
                .requestedAt(mentorship.getRequestedAt())
                .acceptedAt(mentorship.getAcceptedAt())
                .build();
    }

    private StreakResponse toStreakResponse(UserStreak streak) {
        return StreakResponse.builder()
                .currentStreak(streak.getCurrentStreak())
                .bestStreak(streak.getBestStreak())
                .lastActivityDate(streak.getLastActivityDate())
                .build();
    }
}
