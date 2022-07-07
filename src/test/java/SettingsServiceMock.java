import de.kaktushose.levelbot.leveling.data.reward.Reward;
import de.kaktushose.levelbot.bot.data.SettingsService;

public class SettingsServiceMock extends SettingsService {

    @Override
    public Reward getMonthlyNitroBoosterReward() {
        return new Reward(0, 50, 50, 5, null, "Reward");
    }

    @Override
    public Reward getOneTimeNitroBoosterReward() {
        return new Reward(1, 250, 250, 25, null, "Reward");
    }

}
