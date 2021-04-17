package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Item;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findByCategoryId(int categoryId);

}
