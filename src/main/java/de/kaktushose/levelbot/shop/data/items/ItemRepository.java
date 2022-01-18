package de.kaktushose.levelbot.shop.data.items;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemRepository extends CrudRepository<Item, Integer> {

    List<Item> findByCategoryId(int categoryId);

}
