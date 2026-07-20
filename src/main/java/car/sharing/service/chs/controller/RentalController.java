package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.rental.CreateRentalRequestDto;
import car.sharing.service.chs.dto.rental.RentalResponseDto;
import car.sharing.service.chs.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
@Tag(name = "Rentals",
        description = "Endpoints for managing car rentals (Customer & Manager)")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create a new rental",
            description = "Allows a customer to create a new rental")
    public RentalResponseDto createRental(@Valid @RequestBody CreateRentalRequestDto dto) {
        return rentalService.createRental(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "Get rentals",
            description = "Returns a paginated list of rentals. "
                    + "Customers see their own; managers can see all")
    public Page<RentalResponseDto> getRentals(
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rentalDate") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        return rentalService.getRentals(isActive, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','MANAGER')")
    @Operation(summary = "Get rental by ID",
            description = "Returns details of a specific rental "
                    + "for the current user (Customer or Manager)")
    public RentalResponseDto getRental(@PathVariable Long id) {
        return rentalService.getRentalById(id);
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Return a rental",
            description = "Allows a customer to mark their rental as returned")
    public RentalResponseDto returnRental(@PathVariable Long id) {
        return rentalService.returnRental(id);
    }
}
