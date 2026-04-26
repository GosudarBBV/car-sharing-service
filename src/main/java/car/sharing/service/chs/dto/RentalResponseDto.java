package car.sharing.service.chs.dto;

import java.time.LocalDate;

public record RentalResponseDto(
        Long id,
        Long carId,
        Long userId,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate
) {
}
