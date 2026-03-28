package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.UserRegisterRequestDto;
import car.sharing.service.chs.dto.UserResponseDto;
import car.sharing.service.chs.exception.PasswordMismatchException;
import car.sharing.service.chs.exception.RoleNotFoundException;
import car.sharing.service.chs.exception.UserAlreadyExistsException;
import car.sharing.service.chs.mapper.UserMapper;
import car.sharing.service.chs.model.Role;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.RoleRepository;
import car.sharing.service.chs.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

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

        if (userRepository.existsByEmail(requestDto.email())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        System.out.println("ROLE FROM DTO: " + requestDto.role());

        User user = userMapper.toModel(requestDto);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(requestDto.role())
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        user.setRoles(Set.of(userRole));

        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
