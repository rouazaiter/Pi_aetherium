package com.education.platform.services.implementations.cv;

import com.education.platform.common.ApiException;
import com.education.platform.dto.cv.CvAiImproveMaxLength;
import com.education.platform.dto.cv.CvAiImproveRequest;
import com.education.platform.dto.cv.CvAiImproveSectionType;
import com.education.platform.dto.cv.CvAiImproveTargetTone;
import com.education.platform.dto.cv.CvAiImproveTopic;
import com.education.platform.entities.User;
import com.education.platform.services.interfaces.cv.OllamaClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CVAiImprovementServiceImplTest {

    @Mock
    private OllamaClient ollamaClient;

    @Test
    void improveForUserBuildsStrictPromptAndCleansResponse() {
        CVAiImprovementServiceImpl cvAiImprovementService = new CVAiImprovementServiceImpl(ollamaClient, new ObjectMapper());
        User user = User.builder().id(1L).build();
        CvAiImproveRequest request = new CvAiImproveRequest();
        request.setTopic(CvAiImproveTopic.PROJECT_DESCRIPTION);
        request.setSectionType(CvAiImproveSectionType.PROJECTS);
        request.setField("description");
        request.setText("built api for school platform");
        request.setTargetTone(CvAiImproveTargetTone.TECHNICAL);
        request.setMaxLength(CvAiImproveMaxLength.SHORT);

        CvAiImproveRequest.Context context = new CvAiImproveRequest.Context();
        CvAiImproveRequest.ProjectContext project = new CvAiImproveRequest.ProjectContext();
        project.setTitle("Education API");
        project.setDescription("REST backend");
        project.getSkills().add("Java");
        context.setProject(project);
        request.setContext(context);

        when(ollamaClient.generate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("<think>hidden</think> Professional Project Description: \"Designed and implemented a Java REST API for an education platform.\"");

        String suggestion = cvAiImprovementService.improveForUser(user, request).getSuggestion();

        assertEquals("Designed and implemented a Java REST API for an education platform.", suggestion);

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(ollamaClient).generate(promptCaptor.capture());
        String prompt = promptCaptor.getValue();
        assertEquals(true, prompt.contains("TOPIC: PROJECT_DESCRIPTION"));
        assertEquals(true, prompt.contains("SECTION_TYPE: PROJECTS"));
        assertEquals(true, prompt.contains("\"title\":\"Education API\""));
        assertEquals(true, prompt.contains("Do NOT invent any facts."));
    }

    @Test
    void improveForUserRejectsBlankText() {
        CVAiImprovementServiceImpl cvAiImprovementService = new CVAiImprovementServiceImpl(ollamaClient, new ObjectMapper());
        CvAiImproveRequest request = new CvAiImproveRequest();
        request.setTopic(CvAiImproveTopic.PROFILE_SUMMARY);
        request.setSectionType(CvAiImproveSectionType.PROFILE);
        request.setText(" ");
        request.setTargetTone(CvAiImproveTargetTone.ATS_PROFESSIONAL);
        request.setMaxLength(CvAiImproveMaxLength.SHORT);

        ApiException exception = assertThrows(ApiException.class,
                () -> cvAiImprovementService.improveForUser(User.builder().id(2L).build(), request));

        assertEquals("Text is required", exception.getMessage());
        assertEquals(400, exception.getStatus().value());
    }
}
