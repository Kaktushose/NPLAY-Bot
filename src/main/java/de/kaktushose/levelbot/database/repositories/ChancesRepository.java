package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.CurrencyChance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/*
defined in table currency_chances
type 0 = xp
type 1 = coins
type 2 = diamonds
 */
public interface ChancesRepository extends Repository<CurrencyChance, Integer> {

    @Query(value = "SELECT * FROM currency_chances WHERE type = 0", nativeQuery = true)
    List<CurrencyChance> getXpChances();

    @Query(value = "SELECT * FROM currency_chances WHERE type = 1", nativeQuery = true)
    List<CurrencyChance> getCoinChances();

    @Query(value = "SELECT * FROM currency_chances WHERE type = 2", nativeQuery = true)
    List<CurrencyChance> getDiamondChances();

}
