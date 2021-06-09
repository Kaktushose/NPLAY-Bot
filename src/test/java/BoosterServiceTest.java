import de.kaktushose.levelbot.Bootstrapper;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.BoosterService;
import de.kaktushose.levelbot.database.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootConfiguration
@SpringBootTest(classes = Bootstrapper.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "classpath:truncate_all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BoosterServiceTest {

    private BoosterService boosterService;
    private UserService userService;

    @BeforeAll
    public void setup() {
        LevelbotMock levelbot = new LevelbotMock();
        boosterService = levelbot.getBoosterService();
        userService = levelbot.getUserService();
    }

    @AfterEach
    public void cleanUp() {
        System.out.println(userService.getAllUsers().size());
    }

    @Test
    public void getAllNitroBoosters_WithoutUsers_ShouldReturnEmptyList() {
        assertTrue( boosterService.getAllNitroBoosters().isEmpty());
    }

    @Test
    public void getAllNitroBoosters_WithUsers_ShouldReturnNotEmptyList() {
        boosterService.createNewNitroBooster(0);
        boosterService.createNewNitroBooster(1);
        boosterService.createNewNitroBooster(2);
        assertEquals(3, boosterService.getAllNitroBoosters().size());
    }

    @Test
    public void createNewNitroBooster_ShouldBeActiveBooster() {
        boosterService.createNewNitroBooster(0);
        assertTrue(boosterService.isNitroBooster(0));
    }

    @Test
    public void isNitroBooster_WithoutUsers_ShouldReturnFalse() {
        assertFalse(boosterService.isNitroBooster(0));
    }

    @Test
    public void isNitroBooster_WithUsers_ShouldReturnTrue() {
        boosterService.createNewNitroBooster(0);
        assertTrue(boosterService.isNitroBooster(0));
    }

    @Test
    public void changeNitroBoosterStatus_WithTrue_ShouldBeActiveBooster() {
        boosterService.createNewNitroBooster(0);
        boosterService.changeNitroBoosterStatus(0, true);
        assertTrue(boosterService.isNitroBooster(0));
    }

    @Test
    public void changeNitroBoosterStatus_WithoutPresentUsers_ShouldThrow() {
        assertThrows(NoSuchElementException.class, () -> boosterService.changeNitroBoosterStatus(0, true));
        assertThrows(NoSuchElementException.class, () -> boosterService.changeNitroBoosterStatus(0, false));
    }

    @Test
    public void addMonthlyReward_ShouldAddCurrencies() {
        userService.createUser(0);
        String rewardText = boosterService.addMonthlyReward(0);
        BotUser botUser = userService.getUserById(0);
        assertEquals(50, botUser.getXp());
        assertEquals(50, botUser.getCoins());
        assertEquals(5, botUser.getDiamonds());
        assertFalse(rewardText.isEmpty());
    }

    @Test
    public void addMonthlyReward_WithoutUsers_ShouldThrow() {
        assertThrows(NoSuchElementException.class, () -> boosterService.addMonthlyReward(0));
    }

    @Test
    public void addOneTime_ShouldAddCurrencies() {
        userService.createUser(0);
        String rewardText = boosterService.addOneTimeReward(0);
        BotUser botUser = userService.getUserById(0);
        assertEquals(250, botUser.getXp());
        assertEquals(250, botUser.getCoins());
        assertEquals(25, botUser.getDiamonds());
        assertFalse(rewardText.isEmpty());
    }

    @Test
    public void addOneTimeReward_WithoutUsers_ShouldThrow() {
        assertThrows(NoSuchElementException.class, () -> boosterService.addOneTimeReward(0));
        userService.createUser(0);
        userService.createUser(1);
    }
}
