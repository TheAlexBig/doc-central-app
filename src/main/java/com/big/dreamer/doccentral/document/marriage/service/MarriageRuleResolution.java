package com.big.dreamer.doccentral.document.marriage.service;

import java.util.List;

public record MarriageRuleResolution(
        String effectivePropertyRegime,
        List<Issue> issues,
        List<String> postCelebrationActions) {

    public enum Severity { ERROR, REQUIRED, WARNING }

    public record Issue(Severity severity, String field, String code, String message) {
    }
}
