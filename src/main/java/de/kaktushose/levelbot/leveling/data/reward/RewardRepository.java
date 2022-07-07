package de.kaktushose.levelbot.leveling.data.reward;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RewardRepository extends CrudRepository<Reward, Integer> {

    @Query(value = "SELECT * FROM rewards WHERE reward_id = 12", nativeQuery = true)
    Reward getMonthlyNitroBoosterReward();

    @Query(value = "SELECT * FROM rewards WHERE reward_id = 11", nativeQuery = true)
    Reward getOneTimeNitroBoosterReward();

    @Query(value = "SELECT * FROM rewards WHERE reward_id > 15", nativeQuery = true)
    List<Reward> getDailyRewards();

}
