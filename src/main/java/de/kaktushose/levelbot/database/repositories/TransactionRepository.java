package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query(value = "SELECT * FROM transactions WHERE item_id = :itemId and user_id = :userId", nativeQuery = true)
    List<Transaction> findByUserIdAndItemId(@Param("userId") long userId, @Param("itemId") int itemId);

}
