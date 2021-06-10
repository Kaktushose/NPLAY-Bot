import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.services.BoosterService;
import de.kaktushose.levelbot.database.services.SettingsService;
import de.kaktushose.levelbot.database.services.UserService;

public class LevelbotMock extends Levelbot {

    private final UserService userService;
    private final SettingsService settingsService;
    private final BoosterService boosterService;

    public LevelbotMock() {
        this.userService = new UserService();
        this.settingsService = new SettingsServiceMock();
        this.boosterService = new BoosterService(this);
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public SettingsService getSettingsService() {
        return settingsService;
    }

    @Override
    public BoosterService getBoosterService() {
        return boosterService;
    }
}
