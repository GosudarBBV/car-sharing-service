package car.sharing.service.chs.service;

import car.sharing.service.chs.dto.CreateRentalRequestDto;
import car.sharing.service.chs.dto.RentalResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {

    RentalResponseDto createRental(CreateRentalRequestDto dto);

    Page<RentalResponseDto> getRentals(Boolean isActive, Pageable pageable);

    RentalResponseDto getRentalById(Long rentalId);

    RentalResponseDto returnRental(Long rentalId);
}
