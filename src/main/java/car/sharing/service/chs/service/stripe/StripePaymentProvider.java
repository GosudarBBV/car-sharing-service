package car.sharing.service.chs.service.stripe;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StripePaymentProvider {
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe initialized in {} mode",
                stripeSecretKey.startsWith("sk_test_") ? "TEST" : "LIVE");
    }

    public Session createSession(Long rentalId, Long userId, BigDecimal amount,
                                 String type, String productName,
                                 String successUrl, String cancelUrl) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .putAllMetadata(java.util.Map.of(
                            "rental_id", rentalId.toString(),
                            "user_id", userId.toString(),
                            "payment_type", type
                    ))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amount
                                                            .multiply(BigDecimal
                                                                    .valueOf(100))
                                                            .longValue())
                                                    .setProductData(
                                                            SessionCreateParams
                                                                    .LineItem
                                                                    .PriceData
                                                                    .ProductData
                                                                    .builder()
                                                                    .setName(productName)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("payment_" + rentalId + "_" + System.currentTimeMillis())
                    .build();

            Session session = Session.create(params, options);
            log.info("Session created: {}", session.getId());
            return session;

        } catch (Exception e) {
            throw new RuntimeException("Stripe error: " + e.getMessage(), e);
        }
    }

    public Session validateAndGetSessionFromWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            if (!"checkout.session.completed".equals(event.getType())) {
                throw new RuntimeException("Unsupported event type: " + event.getType());
            }

            return (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new RuntimeException("No session in webhook"));

        } catch (Exception e) {
            throw new RuntimeException("Webhook validation failed: " + e.getMessage(), e);
        }
    }
}
