package io.github.abradee.simplyQuests.Models;

import io.github.abradee.simplyQuests.Models.QuestType;
import io.github.abradee.simplyQuests.Models.Season;

public record Quest(
        String id,
        String name,
        QuestType type,
        String target, // e.g., "DIAMOND_ORE" or "ZOMBIE"
        int requiredAmount,
        Season season
) {}