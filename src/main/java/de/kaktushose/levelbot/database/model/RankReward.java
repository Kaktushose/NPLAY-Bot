package de.kaktushose.levelbot.database.model;

import javax.persistence.*;

@Entity
@Table(name = "rank_rewards")
public class RankReward {

    @Id
    private Integer rewardId;
    private int coins;
    private int xp;
    private int diamonds;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "itemId", referencedColumnName = "itemId")
    private Item item;

}
