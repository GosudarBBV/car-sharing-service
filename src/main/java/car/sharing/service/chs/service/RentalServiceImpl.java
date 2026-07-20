package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.mapper.RentalMapper;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.CarRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;
    private final UserService userService;

    private Long getCurrentUserId() {
        return userService.getAuthenticatedUserId();
    }

    private User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(()
                        -> new EntityNotFoundException("User not found with id: " + userId));
    }

    @Transactional
    @Override
    public RentalResponseDto createRental(CreateRentalRequestDto dto) {
        Car car = carRepository.findByIdForUpdate(dto.carId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: "
                        + dto.carId()));

        if (car.getInventory() <= 0) {
            throw new IllegalStateException("Car with id "
                    + car.getId()
                    + " is not available for rental");
        }

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        User currentUser = getCurrentUser();

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setUser(currentUser);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(dto.returnDate());

        Rental savedRental = rentalRepository.save(rental);
        notificationService.notifyNewRental(savedRental.getId());
        return rentalMapper.toDto(savedRental);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RentalResponseDto> getRentals(Boolean isActive, Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Rental> rentals;
        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(authority
                        -> authority.getAuthority().equals("ROLE_MANAGER"));

        if (isManager) {
            if (isActive == null) {
                rentals = rentalRepository.findAll(pageable);
            } else if (isActive) {
                rentals = rentalRepository.findAllByActualReturnDateIsNull(pageable);
            } else {
                rentals = rentalRepository.findAllByActualReturnDateIsNotNull(pageable);
            }
        } else {
            Long userId = currentUser.getId();
            if (isActive == null) {
                rentals = rentalRepository.findAllByUserId(userId, pageable);
            } else if (isActive) {
                rentals = rentalRepository
                        .findAllByUserIdAndActualReturnDateIsNull(userId,
                                pageable);
            } else {
                rentals = rentalRepository
                        .findAllByUserIdAndActualReturnDateIsNotNull(userId,
                                pageable);
            }
        }

        return rentals.map(rentalMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RentalResponseDto getRentalById(Long rentalId) {
        Long currentUserId = getCurrentUserId();
        User currentUser = getCurrentUser();

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Rental not found with id: "
                        + rentalId));

        boolean isManager = currentUser.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager && !rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied to rental with id " + rentalId);
        }

        return rentalMapper.toDto(rental);
    }

    @Transactional
    @Override
    public RentalResponseDto returnRental(Long rentalId) {
        Long currentUserId = getCurrentUserId();

        Rental rental = rentalRepository.findByIdForUpdate(rentalId)
                .orElseThrow(()
                        -> new EntityNotFoundException("Rental not found with id: "
                        + rentalId));

        if (!rental.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Cannot return rental with id " + rentalId
                    + " - it belongs to another user");
        }

        if (rental.getActualReturnDate() != null) {
            throw new IllegalStateException("Rental with id " + rentalId
                    + " has already been returned");
        }

        rental.setActualReturnDate(LocalDate.now());

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        Rental updatedRental = rentalRepository.save(rental);
        return rentalMapper.toDto(updatedRental);
    }
}
