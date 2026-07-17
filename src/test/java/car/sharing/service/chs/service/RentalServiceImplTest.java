package car.sharing.service.chs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.mapper.RentalMapper;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.Rental;
import car.sharing.service.chs.model.RoleName;
import car.sharing.service.chs.model.User;
import car.sharing.service.chs.repository.CarRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.repository.UserRepository;
import car.sharing.service.chs.util.TestEntityFactory;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {
    private static final long TEST_USER_ID = 1L;
    private static final long TEST_CAR_ID = 1L;
    private static final long TEST_RENTAL_ID = 1L;
    private static final long NON_EXISTENT_ID = 999L;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int INVENTORY_COUNT = 5;
    private static final int INVENTORY_AFTER_RENTAL = 4;
    private static final int INVENTORY_AFTER_RETURN = 2;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalMapper rentalMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    // ================= CREATE =================

    @Test
    @DisplayName("Create rental - success")
    void createRental_Success() {
        User user = TestEntityFactory.createUser();
        user.setId(TEST_USER_ID);

        Car car = TestEntityFactory.createCar();
        car.setId(TEST_CAR_ID);
        car.setInventory(INVENTORY_COUNT);

        LocalDate returnDate = LocalDate.now().plusDays(7);
        CreateRentalRequestDto dto = new CreateRentalRequestDto(TEST_CAR_ID, returnDate);

        Rental savedRental = TestEntityFactory.createRental(user, car);
        savedRental.setId(TEST_RENTAL_ID);
        savedRental.setReturnDate(returnDate);

        RentalResponseDto expectedResponse = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                savedRental.getRentalDate(), returnDate, null);

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(user));
        when(carRepository.findByIdForUpdate(TEST_CAR_ID)).thenReturn(Optional.of(car));
        when(rentalRepository.save(any(Rental.class))).thenReturn(savedRental);
        when(rentalMapper.toDto(savedRental)).thenReturn(expectedResponse);
        doNothing().when(notificationService).notifyNewRental(TEST_RENTAL_ID);

        RentalResponseDto result = rentalService.createRental(dto);

        assertEquals(expectedResponse, result);
        assertEquals(INVENTORY_AFTER_RENTAL, car.getInventory());

        verify(userService).getAuthenticatedUserId();
        verify(userRepository).findById(TEST_USER_ID);
        verify(carRepository).findByIdForUpdate(TEST_CAR_ID);
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationService).notifyNewRental(TEST_RENTAL_ID);
    }

    @Test
    @DisplayName("Create rental - throws exception when user not found")
    void createRental_UserNotFound_ThrowsException() {
        Car car = TestEntityFactory.createCar();
        car.setId(TEST_CAR_ID);
        car.setInventory(5);

        when(userService.getAuthenticatedUserId()).thenReturn(NON_EXISTENT_ID);
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());
        when(carRepository.findByIdForUpdate(TEST_CAR_ID)).thenReturn(Optional.of(car));

        CreateRentalRequestDto dto = new CreateRentalRequestDto(TEST_CAR_ID, LocalDate.now());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.createRental(dto));

        verify(carRepository).findByIdForUpdate(TEST_CAR_ID);
        verify(userService).getAuthenticatedUserId();
        verify(userRepository).findById(NON_EXISTENT_ID);
        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create rental - throws exception when car not found")
    void createRental_CarNotFound_ThrowsException() {
        when(carRepository.findByIdForUpdate(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        CreateRentalRequestDto dto = new CreateRentalRequestDto(NON_EXISTENT_ID, LocalDate.now());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.createRental(dto));

        verify(carRepository).findByIdForUpdate(NON_EXISTENT_ID);
        verify(rentalRepository, never()).save(any());
        verify(userService, never()).getAuthenticatedUserId();
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Create rental - throws exception when no inventory")
    void createRental_NoInventory_ThrowsException() {
        Car car = TestEntityFactory.createCar();
        car.setId(TEST_CAR_ID);
        car.setInventory(0);

        when(carRepository.findByIdForUpdate(TEST_CAR_ID)).thenReturn(Optional.of(car));

        CreateRentalRequestDto dto = new CreateRentalRequestDto(TEST_CAR_ID, LocalDate.now());

        assertThrows(IllegalStateException.class,
                () -> rentalService.createRental(dto));

        verify(carRepository).findByIdForUpdate(TEST_CAR_ID);
        verify(rentalRepository, never()).save(any());
        verify(userService, never()).getAuthenticatedUserId();
        verify(userRepository, never()).findById(any());
    }

    // ================= GET RENTALS =================

    @Test
    @DisplayName("Get rentals as manager - returns all rentals")
    void getRentals_AsManager_ReturnsAllRentals() {
        User manager = TestEntityFactory.createUser();
        manager.setId(TEST_USER_ID);
        manager.setRoles(Set.of(TestEntityFactory.createRole(RoleName.MANAGER)));

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Rental rental = TestEntityFactory.createRental(manager, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                rental.getRentalDate(), rental.getReturnDate(), null);

        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(manager));
        when(rentalRepository.findAll(pageable)).thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        Page<RentalResponseDto> result = rentalService.getRentals(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(expectedDto, result.getContent().get(0));
    }

    @Test
    @DisplayName("Get rentals as manager with active filter - returns active only")
    void getRentals_AsManagerWithActiveFilter_ReturnsActiveOnly() {
        User manager = TestEntityFactory.createUser();
        manager.setId(TEST_USER_ID);
        manager.setRoles(Set.of(TestEntityFactory.createRole(RoleName.MANAGER)));

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Rental rental = TestEntityFactory.createRental(manager, TestEntityFactory.createCar());

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                rental.getRentalDate(), rental.getReturnDate(), null);

        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(manager));
        when(rentalRepository.findAllByActualReturnDateIsNull(pageable)).thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        Page<RentalResponseDto> result = rentalService.getRentals(true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Get rentals as customer - returns own rentals only")
    void getRentals_AsCustomer_ReturnsOwnRentals() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);
        customer.setRoles(Set.of(TestEntityFactory.createRole(RoleName.CUSTOMER)));

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Rental rental = TestEntityFactory.createRental(customer, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                rental.getRentalDate(), rental.getReturnDate(), null);

        Page<Rental> rentalPage = new PageImpl<>(List.of(rental));

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(customer));
        when(rentalRepository.findAllByUserId(TEST_USER_ID, pageable)).thenReturn(rentalPage);
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        Page<RentalResponseDto> result = rentalService.getRentals(null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(expectedDto, result.getContent().get(0));
    }

    @Test
    @DisplayName("Get rentals - throws exception when user not found")
    void getRentals_UserNotFound_ThrowsException() {
        when(userService.getAuthenticatedUserId()).thenReturn(NON_EXISTENT_ID);
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.getRentals(null, pageable));
    }

    // ================= GET BY ID =================

    @Test
    @DisplayName("Get rental by id as customer owner - returns rental")
    void getRentalById_AsCustomerOwner_ReturnsRental() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);
        customer.setRoles(Set.of(TestEntityFactory.createRole(RoleName.CUSTOMER)));

        Rental rental = TestEntityFactory.createRental(customer, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                rental.getRentalDate(), rental.getReturnDate(), null);

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(customer));
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.getRentalById(TEST_RENTAL_ID);

        assertEquals(expectedDto, result);
        verify(rentalRepository).findById(TEST_RENTAL_ID);
    }

    @Test
    @DisplayName("Get rental by id as manager - returns any rental")
    void getRentalById_AsManager_ReturnsAnyRental() {
        User manager = TestEntityFactory.createUser();
        manager.setId(TEST_USER_ID);
        manager.setRoles(Set.of(TestEntityFactory.createRole(RoleName.MANAGER)));

        User owner = TestEntityFactory.createUser();
        owner.setId(2L);

        Rental rental = TestEntityFactory.createRental(owner, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, 2L,
                rental.getRentalDate(), rental.getReturnDate(), null);

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(manager));
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.getRentalById(TEST_RENTAL_ID);

        assertEquals(expectedDto, result);
    }

    @Test
    @DisplayName("Get rental by id - throws access denied for another customer")
    void getRentalById_AnotherCustomer_ThrowsAccessDenied() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);
        customer.setRoles(Set.of(TestEntityFactory.createRole(RoleName.CUSTOMER)));

        User owner = TestEntityFactory.createUser();
        owner.setId(2L);

        Rental rental = TestEntityFactory.createRental(owner, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(customer));
        when(rentalRepository.findById(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class,
                () -> rentalService.getRentalById(TEST_RENTAL_ID));
    }

    @Test
    @DisplayName("Get rental by id - throws not found when rental does not exist")
    void getRentalById_RentalNotFound_ThrowsException() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(customer));
        when(rentalRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.getRentalById(NON_EXISTENT_ID));
    }

    // ================= RETURN =================

    @Test
    @DisplayName("Return rental - success")
    void returnRental_Success() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);

        Car car = TestEntityFactory.createCar();
        car.setId(TEST_CAR_ID);
        car.setInventory(INVENTORY_AFTER_RETURN);

        Rental rental = TestEntityFactory.createRental(customer, car);
        rental.setId(TEST_RENTAL_ID);
        rental.setActualReturnDate(null);
        rental.setReturnDate(LocalDate.now().minusDays(2));

        RentalResponseDto expectedDto = new RentalResponseDto(
                TEST_RENTAL_ID, TEST_CAR_ID, TEST_USER_ID,
                rental.getRentalDate(), rental.getReturnDate(), LocalDate.now());

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(rentalRepository.findByIdForUpdate(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(expectedDto);

        RentalResponseDto result = rentalService.returnRental(TEST_RENTAL_ID);

        assertEquals(expectedDto, result);
        assertNotNull(rental.getActualReturnDate());

        verify(rentalRepository).findByIdForUpdate(TEST_RENTAL_ID);
        verify(rentalRepository).save(rental);
    }

    @Test
    @DisplayName("Return rental - throws exception when already returned")
    void returnRental_AlreadyReturned_ThrowsException() {
        User customer = TestEntityFactory.createUser();
        customer.setId(TEST_USER_ID);

        Rental rental = TestEntityFactory.createRental(customer, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);
        rental.setActualReturnDate(LocalDate.now());

        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(rentalRepository.findByIdForUpdate(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));

        assertThrows(IllegalStateException.class,
                () -> rentalService.returnRental(TEST_RENTAL_ID));

        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Return rental - throws access denied for another customer")
    void returnRental_AnotherCustomer_ThrowsAccessDenied() {
        User customer = TestEntityFactory.createUser();
        customer.setId(2L);

        User owner = TestEntityFactory.createUser();
        owner.setId(TEST_USER_ID);

        Rental rental = TestEntityFactory.createRental(owner, TestEntityFactory.createCar());
        rental.setId(TEST_RENTAL_ID);
        rental.setActualReturnDate(null);

        when(userService.getAuthenticatedUserId()).thenReturn(2L);
        when(rentalRepository.findByIdForUpdate(TEST_RENTAL_ID)).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class,
                () -> rentalService.returnRental(TEST_RENTAL_ID));

        verify(rentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("Return rental - throws not found when rental does not exist")
    void returnRental_RentalNotFound_ThrowsException() {
        when(userService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
        when(rentalRepository.findByIdForUpdate(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.returnRental(NON_EXISTENT_ID));
    }
}
