package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
}
