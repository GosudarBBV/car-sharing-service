package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.car.CarResponseDto;
import car.sharing.service.chs.dto.car.CreateCarRequestDto;
import car.sharing.service.chs.dto.car.UpdateCarRequestDto;
import car.sharing.service.chs.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Endpoints for managing cars")
public class CarController {

    private final CarService carService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @Operation(summary = "Get all cars", description = "Returns a paginated list of all cars")
    public Page<CarResponseDto> getAll(Pageable pageable) {
        return carService.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @Operation(summary = "Get car by ID", description = "Returns details of a car by its ID")
    public CarResponseDto getById(@PathVariable Long id) {
        return carService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new car", description = "Adds a new car to the system")
    public CarResponseDto create(@Valid @RequestBody CreateCarRequestDto dto) {
        return carService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update car", description = "Updates details of an existing car by ID")
    public CarResponseDto update(@PathVariable Long id,
                                 @Valid @RequestBody UpdateCarRequestDto dto) {
        return carService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete car", description = "Soft-deletes a car by ID")
    public void delete(@PathVariable Long id) {
        carService.delete(id);
    }
}
