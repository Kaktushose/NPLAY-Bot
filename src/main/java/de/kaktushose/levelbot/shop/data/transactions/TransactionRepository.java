package de.kaktushose.levelbot.shop.data.transactions;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query(value = "SELECT * FROM transactions WHERE item_id = :itemId and user_id = :userId", nativeQuery = true)
    List<Transaction> findByUserIdAndItemId(@Param("userId") long userId, @Param("itemId") int itemId);

}
