package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.ItemCategory;
import org.springframework.data.repository.CrudRepository;

public interface ItemCategoryRepository extends CrudRepository<ItemCategory, Integer> {
}
