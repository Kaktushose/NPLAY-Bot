package de.kaktushose.levelbot.account;

import com.github.kaktushose.jda.commands.annotations.Component;
import com.github.kaktushose.jda.commands.dispatching.CommandContext;
import com.github.kaktushose.jda.commands.dispatching.adapter.TypeAdapter;
import de.kaktushose.levelbot.util.Pagination;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Component
public class CurrencyTypeAdapter implements TypeAdapter<Pagination.CurrencyType> {

    @Override
    public Optional<Pagination.CurrencyType> parse(@NotNull String raw, @NotNull CommandContext context) {
        return switch (raw) {
            case "xp" -> Optional.of(Pagination.CurrencyType.XP);
            case "münzen" -> Optional.of(Pagination.CurrencyType.COINS);
            case "diamanten" -> Optional.of(Pagination.CurrencyType.DIAMONDS);
            default -> Optional.empty();
        };
    }
}
