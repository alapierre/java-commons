package io.alapierre.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Adrian Lapierre {@literal al@alapierre.io}
 * Copyrights by original author 2021.12.09
 */
class ReflectionUtilTest {

    @Test
    void getValue() {

        Boolean res = ReflectionUtil.getValue(Boolean.class, "true");
        System.out.println(res);

    }
}
