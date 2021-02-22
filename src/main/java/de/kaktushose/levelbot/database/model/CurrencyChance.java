package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "currency_chances")
public class CurrencyChance {

    @Id
    private Integer id;
    private int amount;
    private int chance;
    private int type;

    public CurrencyChance() {
    }

    public CurrencyChance(int id, int amount, int chance, int type) {
        this.id = id;
        this.amount = amount;
        this.chance = chance;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "CurrencyChance{" +
                "id=" + id +
                ", amount=" + amount +
                ", chance=" + chance +
                ", type=" + type +
                '}';
    }
}
