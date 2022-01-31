package com.danimaldan.redactor.applicator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class RedactionNullApplicator implements RedactionApplicator {
    @Override
    public void redact() {
        // do nothing
    }
}
