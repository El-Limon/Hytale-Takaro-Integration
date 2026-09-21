package dev.takaro.hytale.handlers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import dev.takaro.hytale.util.Responses;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link CommandSender} that keeps everything the command writes back to it.
 *
 * <p>Using this instead of subscribing to the global server logger means the output of a
 * Takaro-issued command is exactly that command's output - no other player's or console
 * user's log lines can leak into it (F7).
 */
public class OutputCapturingCommandSender implements CommandSender {
    private final List<String> capturedMessages = new CopyOnWriteArrayList<>();
    private final UUID uuid = new UUID(0L, 0L);

    @Override
    public void sendMessage(@Nonnull Message message) {
        String text = message.getAnsiMessage();
        if (text == null || text.isEmpty()) {
            text = message.getRawText();
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        capturedMessages.add(stripAnsi(text));
    }

    static String stripAnsi(String text) {
        return Responses.stripAnsi(text);
    }

    @Nonnull
    @Override
    public String getUsername() {
        return "Takaro Console";
    }

    @Nonnull
    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean hasPermission(@Nonnull String id) {
        return true;
    }

    @Override
    public boolean hasPermission(@Nonnull String id, boolean def) {
        return true;
    }

    public List<String> getCapturedMessages() {
        return capturedMessages;
    }

    public String getCapturedOutput() {
        return String.join("\n", capturedMessages);
    }
}
