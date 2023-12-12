package com.github.kaktushose.nplaybot.rank.model;

import java.util.Optional;

public record UserInfo(int xp, RankInfo currentRank, Optional<RankInfo> nextRank, int messageCount, int xpGain) {
}
