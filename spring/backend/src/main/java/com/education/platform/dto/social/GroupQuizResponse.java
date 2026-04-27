package com.education.platform.dto.social;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupQuizResponse {

    private Long id;
    private Long groupId;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String createdBy;
    private Instant createdAt;
    private long answerCount;
    private long optionACount;
    private long optionBCount;
    private long optionCCount;
    private long optionDCount;
    private String mySelectedOption;
    private boolean myCorrect;
}
