package car.sharing.service.chs.service.stripe;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public interface PaymentProvider {
    Session createSession(SessionCreateParams params, String idempotencyKey);
}
