package com.github.kaktushose.nplaybot.rank.model;

public record UserInfo(int xp, Rank currentRank, Rank nextRank, int messageCount, int xpGain) { }
