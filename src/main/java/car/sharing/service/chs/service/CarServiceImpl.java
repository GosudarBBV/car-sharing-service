package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import car.sharing.service.chs.exception.CarInUseException;
import car.sharing.service.chs.mapper.CarMapper;
import car.sharing.service.chs.model.Car;
import car.sharing.service.chs.repository.CarRepository;
import car.sharing.service.chs.repository.RentalRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    private final RentalRepository rentalRepository;

    @Override
    @Transactional
    public CarResponseDto create(CreateCarRequestDto dto) {
        Car car = carMapper.toEntity(dto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarResponseDto> getAll(Pageable pageable) {
        return carRepository.findAllNotDeleted(pageable)
                .map(carMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CarResponseDto getById(Long id) {
        Car car = carRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
        return carMapper.toDto(car);
    }

    @Override
    @Transactional
    public CarResponseDto update(Long id, UpdateCarRequestDto dto) {
        Car car = carRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));

        carMapper.updateCarFromDto(dto, car);

        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (rentalRepository.existsByCarIdAndActualReturnDateIsNull(id)) {
            throw new CarInUseException(id);
        }
        carRepository.deleteById(id);
    }
}
