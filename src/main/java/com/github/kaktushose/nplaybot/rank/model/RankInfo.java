package com.github.kaktushose.nplaybot.rank.model;

public record RankInfo(int rankId, long roleId, String name, String color, int xpBound, boolean lootboxReward, int itemRewardId) {
}
