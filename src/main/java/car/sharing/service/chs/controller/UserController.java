package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.dto.user.UserUpdateDto;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users",
        description = "Endpoints for managing user profiles and roles")
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update user role",
            description = "Allows a manager to update the role of a specific user")
    public void updateRole(@PathVariable Long id, @RequestParam RoleName role) {
        userService.updateRole(id, role);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "Get current user profile",
            description = "Returns the profile information of the authenticated user")
    public UserResponseDto getProfile() {
        return userService.getCurrentUser();
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "Update current user profile",
            description = "Allows the authenticated user to update their profile information")
    public UserResponseDto updateProfile(@RequestBody @Valid UserUpdateDto dto) {
        return userService.updateProfile(dto);
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "Delete own account",
            description = "Allows the authenticated user to delete their own account")
    public void deleteMyAccount() {
        Long userId = userService.getAuthenticatedUserId();
        userService.deleteById(userId);
    }
}
