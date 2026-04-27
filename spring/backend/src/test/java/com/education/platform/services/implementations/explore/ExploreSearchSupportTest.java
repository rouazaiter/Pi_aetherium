package com.education.platform.services.implementations.explore;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExploreSearchSupportTest {

    @Test
    void matchesPrefixOrContainsSupportsPrefixAndFallbackContains() {
        assertThat(ExploreSearchSupport.matchesPrefixOrContains("smart portfolio", "s")).isTrue();
        assertThat(ExploreSearchSupport.matchesPrefixOrContains("sales app", "s")).isTrue();
        assertThat(ExploreSearchSupport.matchesPrefixOrContains("spring boot api", "s")).isTrue();
        assertThat(ExploreSearchSupport.matchesPrefixOrContains("task manager", "s")).isFalse();
        assertThat(ExploreSearchSupport.matchesPrefixOrContains("task manager", "man")).isTrue();
    }

    @Test
    void matchesAnyPrefixOrContainsChecksMultipleFields() {
        assertThat(ExploreSearchSupport.matchesAnyPrefixOrContains(
                "s",
                "portfolio owner",
                "software engineer",
                "java angular"
        )).isTrue();

        assertThat(ExploreSearchSupport.matchesAnyPrefixOrContains(
                "s",
                "portfolio owner",
                "backend engineer",
                "java angular"
        )).isFalse();
    }

    @Test
    void prefixOrContainsScorePrefersPrefixMatches() {
        assertThat(ExploreSearchSupport.prefixOrContainsScore("s", "spring boot api")).isEqualTo(2);
        assertThat(ExploreSearchSupport.prefixOrContainsScore("man", "task manager")).isEqualTo(1);
        assertThat(ExploreSearchSupport.prefixOrContainsScore("zzz", "task manager")).isZero();
    }
}
