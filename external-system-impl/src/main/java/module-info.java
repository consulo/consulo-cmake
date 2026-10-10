/**
 * @author VISTALL
 */
module consulo.cmake.external.system.impl {
    requires transitive consulo.cmake;
    requires consulo.module.creation.api;
    requires consulo.external.system.api;
    requires consulo.execution.debugger.dap;
    requires consulo.nativeDev.debugger.api;
    requires consulo.nativeDev.profiler.api;
    requires consulo.compiler.api;

    requires com.google.gson;
    requires org.slf4j;

    exports consulo.cmake.externalSystem.impl;
    exports consulo.cmake.externalSystem.model;
    exports consulo.cmake.setting;
    exports consulo.cmake.importProvider;
    exports consulo.cmake.debug;
    exports consulo.cmake.debug.breakpoint;

    opens consulo.cmake.setting to consulo.util.xml.serializer;
}
