package car.sharing.service.chs.config;

import car.sharing.service.chs.service.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = false)
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot bot) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
            log.info("Telegram bot registered successfully: {}", bot.getBotUsername());
            return api;
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot", e);
            throw new RuntimeException("Failed to register Telegram bot", e);
        }
    }
}
