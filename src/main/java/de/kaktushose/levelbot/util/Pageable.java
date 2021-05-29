package de.kaktushose.levelbot.util;

public interface Pageable {

    Long getUserId();

    long getCount(Pagination.CurrencyType currencyType);

}
