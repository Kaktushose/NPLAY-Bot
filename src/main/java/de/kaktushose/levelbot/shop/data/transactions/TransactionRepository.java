package de.kaktushose.levelbot.shop.data.transactions;

import de.kaktushose.levelbot.shop.data.items.Item;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    @Query(value = "SELECT new java.lang.Boolean(count(*) > 0) FROM Transaction WHERE user_id = :userId and item_id = :itemId")
    boolean existsByUserIdAndItemId(@Param("userId") long userId, @Param("itemId") int itemId);

    @Query(value = "SELECT * from transactions WHERE user_id = :userId and item_id = :itemId", nativeQuery = true)
    List<Transaction> findByUserIdAndItemId(@Param("userId") long userId, @Param("itemId") int itemId);

    @Modifying
    @Transactional
    @Query(value = "DELETE from transactions WHERE user_id = :userId and item_id = :itemId", nativeQuery = true)
    void deleteByUserIdAndItemId(@Param("userId") long userId, @Param("itemId") int itemId);

    @Query("SELECT new java.lang.Boolean(count(*) > 0) FROM Transaction t WHERE t.userId = :userId AND t.item.itemId in (SELECT i.itemId from Item i where i.categoryId = :categoryId)")
    boolean existsByUserIdAndCategoryId(@Param("userId") long userId, @Param("categoryId") int categoryId);

    @Query(value = "SELECT * FROM transactions INNER JOIN items ON items.item_id=transactions.item_id WHERE duration - ((UNIX_TIMESTAMP(NOW()) * 1000) - buy_time) < 86400000 AND transactions.item_id <> 3", nativeQuery = true)
    List<Transaction> findExpiringTransactions();
}
