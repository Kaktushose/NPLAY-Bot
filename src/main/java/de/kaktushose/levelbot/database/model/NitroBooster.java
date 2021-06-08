package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nitro_boosters")
public class NitroBooster {

    @Id
    private Long userId;
    private long boostStart;
    private boolean active;

    public NitroBooster() {

    }

    public NitroBooster(Long userId, long boostStart, boolean active) {
        this.userId = userId;
        this.boostStart = boostStart;
        this.active = active;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getBoostStart() {
        return boostStart;
    }

    public void setBoostStart(long boostStart) {
        this.boostStart = boostStart;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
