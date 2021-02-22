package de.kaktushose.levelbot.database.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "item_categories")
public class ItemCategory {

    @Id
    private Integer categoryId;
    private String name;
    private String description;
    private String emote;
    private boolean visible;

    public ItemCategory() {
    }

    public ItemCategory(int categoryId, String name, String description, String emote, boolean visible) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.emote = emote;
        this.visible = visible;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmote() {
        return emote;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
