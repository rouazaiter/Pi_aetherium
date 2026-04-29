package com.education.platform.controllers.portfolio;

import com.education.platform.dto.portfolio.UploadedProjectMediaResponse;
import com.education.platform.services.implementations.portfolio.PortfolioMediaStorage;
import com.education.platform.services.interfaces.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/portfolio/me/projects/media")
public class PortfolioMediaUploadController {

    private final CurrentUserService currentUserService;
    private final PortfolioMediaStorage portfolioMediaStorage;

    public PortfolioMediaUploadController(CurrentUserService currentUserService, PortfolioMediaStorage portfolioMediaStorage) {
        this.currentUserService = currentUserService;
        this.portfolioMediaStorage = portfolioMediaStorage;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadedProjectMediaResponse upload(@RequestPart("file") MultipartFile file) throws IOException {
        return portfolioMediaStorage.store(currentUserService.getCurrentUser().getId(), file);
    }
}
