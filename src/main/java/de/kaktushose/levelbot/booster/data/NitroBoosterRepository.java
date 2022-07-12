package de.kaktushose.levelbot.booster.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NitroBoosterRepository extends CrudRepository<NitroBooster, Long> {

    @Query(value = "SELECT user_id FROM nitro_boosters WHERE `active` = true", nativeQuery = true)
    List<Long> getActiveNitroBoosters();

    @Query(value = "SELECT user_id FROM nitro_boosters WHERE (UNIX_TIMESTAMP(NOW()) - boost_start / 1000) % (30 * 24 * 60 * 60) < 24 * 60 * 60 AND `active` = true;", nativeQuery = true)
    List<Long> getRewardableBoosters();

}
