package sdjini.AirDMM.log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface SafeLevel {
    Level[] value() default {Level.STEP, Level.INFO};
}
