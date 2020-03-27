package com.thanhtam.backend.controller;

import com.thanhtam.backend.entity.Profile;
import com.thanhtam.backend.entity.ServiceResult;
import com.thanhtam.backend.service.ProfileService;
import com.thanhtam.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class ProfileController {
    private ProfileService profileService;
    private UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllProfiles() {

        List<Profile> profiles = profileService.getAllProfiles();
        return ResponseEntity.ok(new ServiceResult(HttpStatus.OK.value(), "Get list of profile successfully!", profiles));

    }

}
