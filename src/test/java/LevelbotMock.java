import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.booster.data.BoosterService;
import de.kaktushose.levelbot.bot.data.SettingsService;
import de.kaktushose.levelbot.account.data.UserService;

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
