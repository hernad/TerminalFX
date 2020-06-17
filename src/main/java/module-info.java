open module com.kodedu.terminalfx {


    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires transitive pty4j;
    requires org.apache.commons.lang3;

    exports com.kodedu.terminalfx;
    exports com.kodedu.terminalfx.config;
    exports com.kodedu.terminalfx.helper;

}