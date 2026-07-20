package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarResponseDto create(CreateCarRequestDto dto);

    Page<CarResponseDto> getAll(Pageable pageable);

    CarResponseDto getById(Long id);

    CarResponseDto update(Long id, UpdateCarRequestDto dto);

    void delete(Long id);
}
