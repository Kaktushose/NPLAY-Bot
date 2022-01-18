package de.kaktushose.levelbot.shop.data.items;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findByCategoryId(int categoryId);

    @Query(value = "SELECT * FROM items where items.item_id IN (SELECT transactions.item_id FROM transactions WHERE user_id = :userId)", nativeQuery = true)
    List<Item> findItemsByUserId(@Param("userId") long userId);

}
