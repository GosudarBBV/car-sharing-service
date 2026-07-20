package car.sharing.service.chs.controller;

import car.sharing.service.chs.dto.payment.PaymentRequestDto;
import car.sharing.service.chs.dto.payment.PaymentResponseDto;
import car.sharing.service.chs.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER')")
    @Operation(summary = "Get user payments")
    public Page<PaymentResponseDto> getPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentService.getPayments(userDetails.getUsername(), pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create Stripe payment session")
    public PaymentResponseDto createPayment(
            @Valid @RequestBody PaymentRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Creating payment for user: {}", userDetails.getUsername());
        return paymentService.createPaymentSession(dto, userDetails.getUsername());
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("Payment processed successfully");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok("Payment was canceled");
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel pending payment")
    public ResponseEntity<String> cancelPayment(
            @RequestParam("session_id") String sessionId
    ) {
        paymentService.handleCancel(sessionId);
        return ResponseEntity.ok("Payment canceled");
    }
}
