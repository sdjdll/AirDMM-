package sdjini.AirDMM.log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface UnsafeLevel {
    Level[] value() default {Level.INFO, Level.ERROR, Level.FATAL};
}
