package io.alapierre.freshmail.model;

import lombok.Getter;

/**
 * @author Adrian Lapierre {@literal <adrian.lapierre@sidgroup.pl>}
 * created 13.06.18
 */
public enum SubscriberStatus {

    ACTIVE(1),
    TO_ACTIVATE(2),
    INACTIVE(3),
    UNSUBSCRIBED(4),
    SOFT_BACK(5),
    HARD_BACK(6);

    SubscriberStatus(int code) {
        this.code = code;
    }

    @Getter
    private int code;
}
