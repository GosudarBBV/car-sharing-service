package car.sharing.service.chs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import car.sharing.service.chs.dto.CarResponseDto;
import car.sharing.service.chs.dto.CreateCarRequestDto;
import car.sharing.service.chs.dto.UpdateCarRequestDto;
import car.sharing.service.chs.exception.CarInUseException;
import car.sharing.service.chs.exception.CarNotFoundException;
import car.sharing.service.chs.mapper.CarMapper;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.model.CarType;
import car.sharing.service.chs.repository.CarRepository;
import car.sharing.service.chs.repository.RentalRepository;
import car.sharing.service.chs.util.TestEntityFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
    private static final long EXISTING_CAR_ID = 1L;
    private static final long NON_EXISTENT_CAR_ID = 99L;
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String CAR_BRAND = "Tesla";
    private static final String CAR_MODEL = "Model Y";
    private static final String UPDATED_MODEL = "Updated";
    private static final int INVENTORY = 5;
    private static final int UPDATED_INVENTORY = 3;
    private static final BigDecimal DAILY_FEE = BigDecimal.valueOf(120);
    private static final BigDecimal UPDATED_FEE = BigDecimal.valueOf(100);

    @Mock
    private CarRepository carRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Create car - returns car response dto")
    void create_ReturnsCarResponseDto() {
        CreateCarRequestDto dto = TestEntityFactory.createCarRequest();
        Car car = TestEntityFactory.createCar();
        Car savedCar = TestEntityFactory.createCar();
        CarResponseDto expectedResponse = new CarResponseDto(
                EXISTING_CAR_ID, CAR_BRAND, CAR_MODEL, CarType.SEDAN, INVENTORY, DAILY_FEE
        );

        when(carMapper.toEntity(dto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(savedCar);
        when(carMapper.toDto(savedCar)).thenReturn(expectedResponse);

        CarResponseDto result = carService.create(dto);

        assertEquals(expectedResponse, result);

        verify(carMapper).toEntity(dto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(savedCar);
    }

    @Test
    @DisplayName("Get all cars - returns page of car response dto")
    void getAll_ReturnsPage() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
        Car car = TestEntityFactory.createCar();
        CarResponseDto expectedDto = new CarResponseDto(
                EXISTING_CAR_ID, CAR_BRAND, CAR_MODEL, CarType.SEDAN, INVENTORY, DAILY_FEE
        );
        Page<Car> page = new PageImpl<>(List.of(car));

        when(carRepository.findAllNotDeleted(pageable)).thenReturn(page);
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        Page<CarResponseDto> result = carService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(expectedDto, result.getContent().get(0));

        verify(carRepository).findAllNotDeleted(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Get car by id - returns car when exists")
    void getById_ReturnsCar() {
        Car car = TestEntityFactory.createCar();
        CarResponseDto expectedDto = new CarResponseDto(
                EXISTING_CAR_ID, CAR_BRAND, CAR_MODEL, CarType.SEDAN, INVENTORY, DAILY_FEE
        );

        when(carRepository.findByIdAndNotDeleted(EXISTING_CAR_ID)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(expectedDto);

        CarResponseDto result = carService.getById(EXISTING_CAR_ID);

        assertEquals(expectedDto, result);

        verify(carRepository).findByIdAndNotDeleted(EXISTING_CAR_ID);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Get car by id - throws exception when car not found")
    void getById_ThrowsException() {
        when(carRepository.findByIdAndNotDeleted(NON_EXISTENT_CAR_ID)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class,
                () -> carService.getById(NON_EXISTENT_CAR_ID));
    }

    @Test
    @DisplayName("Update car - returns updated car when exists")
    void update_ReturnsUpdatedCar() {
        Car car = TestEntityFactory.createCar();
        UpdateCarRequestDto dto = TestEntityFactory.updateCarRequest();
        CarResponseDto expectedResponse = new CarResponseDto(
                EXISTING_CAR_ID, CAR_BRAND, UPDATED_MODEL, CarType.SEDAN,
                UPDATED_INVENTORY, UPDATED_FEE
        );

        when(carRepository.findByIdForUpdateAndNotDeleted(EXISTING_CAR_ID)).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateCarFromDto(dto, car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(expectedResponse);

        CarResponseDto result = carService.update(EXISTING_CAR_ID, dto);

        assertEquals(expectedResponse, result);

        verify(carRepository).findByIdForUpdateAndNotDeleted(EXISTING_CAR_ID);
        verify(carMapper).updateCarFromDto(dto, car);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("Update car - throws exception when car not found")
    void update_ThrowsException() {
        UpdateCarRequestDto dto = TestEntityFactory.updateCarRequest();

        when(carRepository.findByIdForUpdateAndNotDeleted(EXISTING_CAR_ID)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class,
                () -> carService.update(EXISTING_CAR_ID, dto));

        verify(carRepository).findByIdForUpdateAndNotDeleted(EXISTING_CAR_ID);
    }

    @Test
    @DisplayName("Delete car - deletes car successfully when not in use")
    void delete_Success() {
        when(rentalRepository.existsByCarIdAndActualReturnDateIsNull(EXISTING_CAR_ID))
                .thenReturn(false);

        carService.delete(EXISTING_CAR_ID);

        verify(rentalRepository).existsByCarIdAndActualReturnDateIsNull(EXISTING_CAR_ID);
        verify(carRepository).deleteById(EXISTING_CAR_ID);
    }

    @Test
    @DisplayName("Delete car - throws exception when car is in use")
    void delete_InUse_ThrowsException() {
        when(rentalRepository.existsByCarIdAndActualReturnDateIsNull(EXISTING_CAR_ID))
                .thenReturn(true);

        assertThrows(CarInUseException.class,
                () -> carService.delete(EXISTING_CAR_ID));

        verify(rentalRepository).existsByCarIdAndActualReturnDateIsNull(EXISTING_CAR_ID);
        verifyNoInteractions(carRepository);
    }
}
