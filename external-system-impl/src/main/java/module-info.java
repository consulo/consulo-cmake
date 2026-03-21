/**
 * @author VISTALL
 */
module consulo.cmake.external.system.impl {
    requires transitive consulo.cmake;
    requires consulo.external.system.api;

    requires com.google.gson;
    requires org.slf4j;

    exports consulo.cmake.externalSystem.impl;
    exports consulo.cmake.setting;
    exports consulo.cmake.importProvider;

    opens consulo.cmake.setting to consulo.util.xml.serializer;
}
