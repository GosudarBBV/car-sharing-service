package car.sharing.service.chs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.service.chs.dto.user.UserRegisterRequestDto;
import car.sharing.service.chs.dto.user.UserResponseDto;
import car.sharing.service.chs.dto.user.UserUpdateDto;
import car.sharing.service.chs.exception.RoleNotFoundException;
import car.sharing.service.chs.exception.UserAlreadyExistsException;
import car.sharing.service.chs.exception.UserNotDeletedException;
import car.sharing.service.chs.mapper.UserMapper;
import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.RoleRepository;
import car.sharing.service.chs.repository.UserRepository;
import car.sharing.service.chs.util.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = TestEntityFactory.createUser();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(TEST_PASSWORD);

        testRole = TestEntityFactory.createRole(RoleName.CUSTOMER);
    }

    private void mockAuthentication(String email, Long userId) {
        User authenticatedUser = TestEntityFactory.createUser();
        authenticatedUser.setId(userId);
        authenticatedUser.setEmail(email);

        when(userRepository.findWithRolesByEmail(email))
                .thenReturn(Optional.of(authenticatedUser));

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken(email, null));

        SecurityContextHolder.setContext(securityContext);
    }

    // ================= REGISTER TESTS =================

    @Test
    @DisplayName("Register - success")
    void register_Success() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto(
                TEST_EMAIL, "John", "Doe", TEST_PASSWORD, TEST_PASSWORD, RoleName.CUSTOMER);

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userMapper.toModel(dto)).thenReturn(testUser);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDto(testUser)).thenReturn(mock(UserResponseDto.class));

        userService.register(dto);

        verify(userRepository).save(testUser);
        verify(passwordEncoder).encode(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Register - throws exception when email already exists")
    void register_EmailExists_ThrowsException() {
        UserRegisterRequestDto dto = new UserRegisterRequestDto(
                TEST_EMAIL, "John", "Doe", TEST_PASSWORD, TEST_PASSWORD, RoleName.CUSTOMER);

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(dto));

        verify(userRepository).existsByEmail(dto.email());
    }

    // ================= DELETE TESTS =================

    @Test
    @DisplayName("Delete - success")
    void delete_Success() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        userService.deleteById(TEST_USER_ID);

        assertTrue(testUser.isDeleted());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Delete - throws exception when user not found")
    void delete_UserNotFound_ThrowsException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.deleteById(TEST_USER_ID));
    }

    // ================= AUTH TESTS =================

    @Test
    @DisplayName("Get authenticated user id - success")
    void getAuthenticatedUserId_Success() {
        mockAuthentication(TEST_EMAIL, TEST_USER_ID);

        Long userId = userService.getAuthenticatedUserId();

        assertEquals(TEST_USER_ID, userId);
    }

    // ================= UPDATE ROLE TESTS =================

    @Test
    @DisplayName("Update role - success")
    void updateRole_Success() {
        Role managerRole = TestEntityFactory.createRole(RoleName.MANAGER);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(RoleName.MANAGER)).thenReturn(Optional.of(managerRole));

        userService.updateRole(TEST_USER_ID, RoleName.MANAGER);

        assertEquals(Set.of(managerRole), testUser.getRoles());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Update role - throws exception when user not found")
    void updateRole_UserNotFound_ThrowsException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.updateRole(TEST_USER_ID, RoleName.MANAGER));
    }

    @Test
    @DisplayName("Update role - throws exception when role not found")
    void updateRole_RoleNotFound_ThrowsException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(RoleName.MANAGER)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class,
                () -> userService.updateRole(TEST_USER_ID, RoleName.MANAGER));
    }

    // ================= RESTORE TESTS =================

    @Test
    @DisplayName("Restore - success")
    void restore_Success() {
        testUser.setDeleted(true);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        userService.restoreById(TEST_USER_ID);

        assertFalse(testUser.isDeleted());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Restore - throws exception when user not deleted")
    void restore_UserNotDeleted_ThrowsException() {
        testUser.setDeleted(false);

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        assertThrows(UserNotDeletedException.class,
                () -> userService.restoreById(TEST_USER_ID));
    }

    @Test
    @DisplayName("Restore - throws exception when user not found")
    void restore_UserNotFound_ThrowsException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.restoreById(TEST_USER_ID));
    }

    // ================= UPDATE PROFILE TESTS =================

    @Test
    @DisplayName("Update profile - success")
    void updateProfile_Success() {
        mockAuthentication(TEST_EMAIL, TEST_USER_ID);

        UserUpdateDto updateDto = new UserUpdateDto("UpdatedName", "UpdatedLastName", "newPassword");

        when(passwordEncoder.encode("newPassword")).thenReturn(ENCODED_PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenReturn(testUser);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(mock(UserResponseDto.class));

        userService.updateProfile(updateDto);

        User savedUser = userCaptor.getValue();
        assertEquals("UpdatedName", savedUser.getFirstName());
        assertEquals("UpdatedLastName", savedUser.getLastName());

        verify(userRepository).save(any(User.class));
    }

    // ================= GET USER BY EMAIL TESTS =================

    @Test
    @DisplayName("Get user id by email - success")
    void getUserIdByEmail_Success() {
        when(userRepository.findIdByEmail(TEST_EMAIL)).thenReturn(Optional.of(TEST_USER_ID));

        Long userId = userService.getUserIdByEmail(TEST_EMAIL);

        assertEquals(TEST_USER_ID, userId);
    }
}
