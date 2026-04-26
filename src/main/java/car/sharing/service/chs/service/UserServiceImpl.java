package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;
import car.sharing.service.chs.dto.UserUpdateDto;
import car.sharing.service.chs.exception.PasswordMismatchException;
import car.sharing.service.chs.exception.RoleNotFoundException;
import car.sharing.service.chs.exception.UserAlreadyExistsException;
import car.sharing.service.chs.exception.UserNotDeletedException;
import car.sharing.service.chs.mapper.UserMapper;
import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.RoleRepository;
import car.sharing.service.chs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegisterRequestDto requestDto) {
        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new UserAlreadyExistsException("User already exists with email: "
                    + requestDto.email());
        }

        Role userRole = roleRepository.findByName(requestDto.role())
                .orElseThrow(() -> new RoleNotFoundException("Role not found: "
                        + requestDto.role()));

        User user = userMapper.toModel(requestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(userRole));

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public void deleteById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

    @Override
    public UserResponseDto getCurrentUser() {
        User user = getAuthenticatedUser();
        return userMapper.toResponseDto(user);
    }

    public Long getUserIdByEmail(String email) {
        return userRepository.findIdByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "
                        + email));
    }

    @Override
    public UserResponseDto updateProfile(UserUpdateDto dto) {
        User user = getAuthenticatedUser();

        if (dto.firstName() != null && !dto.firstName().isBlank()) {
            user.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null && !dto.lastName().isBlank()) {
            user.setLastName(dto.lastName());
        }
        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public void updateRole(Long id, RoleName roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));

        user.getRoles().clear();
        user.getRoles().add(role);

        userRepository.save(user);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findWithRolesByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));
    }

    @Override
    public void restoreById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.isDeleted()) {
            throw new UserNotDeletedException("User is not deleted");
        }

        user.setDeleted(false);
        userRepository.save(user);
    }
}
