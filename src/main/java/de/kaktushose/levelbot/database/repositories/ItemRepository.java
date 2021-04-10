package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findByCategoryId(int categoryId);

}
