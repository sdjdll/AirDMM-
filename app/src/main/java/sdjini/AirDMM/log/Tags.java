package sdjini.AirDMM.log;

import android.content.Context;

import androidx.annotation.NonNull;

import javax.xml.parsers.SAXParser;

public final class Tags {
    public static final class MainTag{
        public static final class Default implements Tag{
            @NonNull
            @Override
            public String toString() {
                return "MainTag.Default";
            }
        }

    }

    public static final class Service{
        public static final class ServiceAction implements Tag {
            private final String context;
            public ServiceAction(Context context) {
                this.context = context.getClass().getName();
            }

            public ServiceAction() {
                context = "Service";
            }

            @NonNull
            @Override
            public String toString() {
                return context + ".ServiceAction";
            }
        }
    }
}
