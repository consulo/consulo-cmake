package com.cmakeplugin.annotator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class CMakeKeywords {
  private CMakeKeywords() {}
  private static String varRegexp = "[A-Za-z0-9/_.+-]+";
  private static String varNumberRegexp = "[0-9]";
  static final Set<String> commands_Scripting = new HashSet<>();
  static final Set<String> commands_Project = new HashSet<>();
  static final Set<String> commands_Test = new HashSet<>();
  static final Set<String> commands_Deprecated = new HashSet<>();

  static final Set<String> variables_All = new HashSet<>();
  static final Set<String> variables_ENV = new HashSet<>();

  static final Set<String> properties_All = new HashSet<>();
  static final Set<String> properties_Deprecated = new HashSet<>();
  static final Set<String> operators = new HashSet<>();

  static {
// https://cmake.org/cmake/help/latest/manual/cmake-commands.7.html
    Collections.addAll(commands_Scripting,"break","cmake_host_system_information","cmake_minimum_required","cmake_parse_arguments","cmake_policy","configure_file","continue","elseif","else","endforeach","endfunction","endif","endmacro","endwhile","execute_process","file","find_file","find_library","find_package","find_path","find_program","foreach","function","get_cmake_property","get_directory_property","get_filename_component","get_property","if","include","include_guard","list","macro","mark_as_advanced","math","message","option","return","separate_arguments","set_directory_properties","set_property","set","site_name","string","unset","variable_watch","while" );
    Collections.addAll(commands_Project,"add_compile_options","add_custom_command","add_custom_target","add_definitions","add_dependencies","add_executable","add_library","add_subdirectory","add_test","aux_source_directory","build_command","create_test_sourcelist","define_property","enable_language","enable_testing","export","fltk_wrap_ui","get_source_file_property","get_target_property","get_test_property","include_directories","include_external_msproject","include_regular_expression","install","link_directories","link_libraries","load_cache","project","qt_wrap_cpp","qt_wrap_ui","remove_definitions","set_source_files_properties","set_target_properties","set_tests_properties","source_group","target_compile_definitions","target_compile_features","target_compile_options","target_include_directories","target_link_libraries","target_sources","try_compile","try_run" );
    Collections.addAll(commands_Test,"ctest_build","ctest_configure","ctest_coverage","ctest_empty_binary_directory","ctest_memcheck","ctest_read_custom_files","ctest_run_script","ctest_sleep","ctest_start","ctest_submit","ctest_test","ctest_update","ctest_upload" );
    Collections.addAll(commands_Deprecated,"build_name","exec_program","export_library_dependencies","install_files","install_programs","install_targets","load_command","make_directory","output_required_files","remove","subdir_depends","subdirs","use_mangled_mesa","utility_source","variable_requires","write_file" );

    //cmake.org/cmake/help/latest/manual/cmake-variables.7.html
    Collections.addAll(variables_All,
//    CMAKE_Variables_that_Provide_Information=
            "CMAKE_AR","CMAKE_ARGC","CMAKE_ARGV0","CMAKE_BINARY_DIR","CMAKE_BUILD_TOOL","CMAKE_CACHEFILE_DIR","CMAKE_CACHE_MAJOR_VERSION","CMAKE_CACHE_MINOR_VERSION","CMAKE_CACHE_PATCH_VERSION","CMAKE_CFG_INTDIR","CMAKE_COMMAND","CMAKE_CROSSCOMPILING","CMAKE_CROSSCOMPILING_EMULATOR","CMAKE_CTEST_COMMAND","CMAKE_CURRENT_BINARY_DIR","CMAKE_CURRENT_LIST_DIR","CMAKE_CURRENT_LIST_FILE","CMAKE_CURRENT_LIST_LINE","CMAKE_CURRENT_SOURCE_DIR","CMAKE_DL_LIBS","CMAKE_EDIT_COMMAND","CMAKE_EXECUTABLE_SUFFIX","CMAKE_EXTRA_GENERATOR","CMAKE_EXTRA_SHARED_LIBRARY_SUFFIXES","CMAKE_FIND_PACKAGE_NAME","CMAKE_FIND_PACKAGE_SORT_DIRECTION","CMAKE_FIND_PACKAGE_SORT_ORDER","CMAKE_GENERATOR","CMAKE_GENERATOR_PLATFORM","CMAKE_GENERATOR_TOOLSET","CMAKE_HOME_DIRECTORY","CMAKE_IMPORT_LIBRARY_PREFIX","CMAKE_IMPORT_LIBRARY_SUFFIX","CMAKE_JOB_POOL_COMPILE","CMAKE_JOB_POOL_LINK","CMAKE_"+varRegexp+"_COMPILER_AR","CMAKE_"+varRegexp+"_COMPILER_RANLIB","CMAKE_LINK_LIBRARY_SUFFIX","CMAKE_LINK_SEARCH_END_STATIC","CMAKE_LINK_SEARCH_START_STATIC","CMAKE_MAJOR_VERSION","CMAKE_MAKE_PROGRAM","CMAKE_MATCH_COUNT","CMAKE_MATCH_"+varNumberRegexp,"CMAKE_MINIMUM_REQUIRED_VERSION","CMAKE_MINOR_VERSION","CMAKE_PARENT_LIST_FILE","CMAKE_PATCH_VERSION","CMAKE_PROJECT_DESCRIPTION","CMAKE_PROJECT_NAME","CMAKE_RANLIB","CMAKE_ROOT","CMAKE_SCRIPT_MODE_FILE","CMAKE_SHARED_LIBRARY_PREFIX","CMAKE_SHARED_LIBRARY_SUFFIX","CMAKE_SHARED_MODULE_PREFIX","CMAKE_SHARED_MODULE_SUFFIX","CMAKE_SIZEOF_VOID_P","CMAKE_SKIP_INSTALL_RULES","CMAKE_SKIP_RPATH","CMAKE_SOURCE_DIR","CMAKE_STATIC_LIBRARY_PREFIX","CMAKE_STATIC_LIBRARY_SUFFIX","CMAKE_TOOLCHAIN_FILE","CMAKE_TWEAK_VERSION","CMAKE_VERBOSE_MAKEFILE","CMAKE_VERSION","CMAKE_VS_DEVENV_COMMAND","CMAKE_VS_INTEL_Fortran_PROJECT_VERSION","CMAKE_VS_MSBUILD_COMMAND","CMAKE_VS_NsightTegra_VERSION","CMAKE_VS_PLATFORM_NAME","CMAKE_VS_PLATFORM_TOOLSET","CMAKE_VS_PLATFORM_TOOLSET_CUDA","CMAKE_VS_PLATFORM_TOOLSET_HOST_ARCHITECTURE","CMAKE_VS_WINDOWS_TARGET_PLATFORM_VERSION","CMAKE_XCODE_GENERATE_SCHEME","CMAKE_XCODE_PLATFORM_TOOLSET",varRegexp+"_BINARY_DIR",varRegexp+"_SOURCE_DIR",varRegexp+"_VERSION",varRegexp+"_VERSION_MAJOR",varRegexp+"_VERSION_MINOR",varRegexp+"_VERSION_PATCH",varRegexp+"_VERSION_TWEAK","PROJECT_BINARY_DIR","PROJECT_DESCRIPTION","PROJECT_NAME","PROJECT_SOURCE_DIR","PROJECT_VERSION","PROJECT_VERSION_MAJOR","PROJECT_VERSION_MINOR","PROJECT_VERSION_PATCH","PROJECT_VERSION_TWEAK",
//    CMAKE_Variables_that_Change_Behavior=
            "BUILD_SHARED_LIBS","CMAKE_ABSOLUTE_DESTINATION_FILES","CMAKE_APPBUNDLE_PATH","CMAKE_AUTOMOC_RELAXED_MODE","CMAKE_BACKWARDS_COMPATIBILITY","CMAKE_BUILD_TYPE","CMAKE_CODELITE_USE_TARGETS","CMAKE_COLOR_MAKEFILE","CMAKE_CONFIGURATION_TYPES","CMAKE_DEBUG_TARGET_PROPERTIES","CMAKE_DEPENDS_IN_PROJECT_ONLY","CMAKE_DISABLE_FIND_PACKAGE_"+varRegexp+"","CMAKE_ECLIPSE_GENERATE_LINKED_RESOURCES","CMAKE_ECLIPSE_GENERATE_SOURCE_PROJECT","CMAKE_ECLIPSE_MAKE_ARGUMENTS","CMAKE_ECLIPSE_VERSION","CMAKE_ERROR_DEPRECATED","CMAKE_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION","CMAKE_EXPORT_COMPILE_COMMANDS","CMAKE_EXPORT_NO_PACKAGE_REGISTRY","CMAKE_FIND_APPBUNDLE","CMAKE_FIND_FRAMEWORK","CMAKE_FIND_LIBRARY_CUSTOM_LIB_SUFFIX","CMAKE_FIND_LIBRARY_PREFIXES","CMAKE_FIND_LIBRARY_SUFFIXES","CMAKE_FIND_NO_INSTALL_PREFIX","CMAKE_FIND_PACKAGE_NO_PACKAGE_REGISTRY","CMAKE_FIND_PACKAGE_NO_SYSTEM_PACKAGE_REGISTRY","CMAKE_FIND_PACKAGE_WARN_NO_MODULE","CMAKE_FIND_ROOT_PATH","CMAKE_FIND_ROOT_PATH_MODE_INCLUDE","CMAKE_FIND_ROOT_PATH_MODE_LIBRARY","CMAKE_FIND_ROOT_PATH_MODE_PACKAGE","CMAKE_FIND_ROOT_PATH_MODE_PROGRAM","CMAKE_FRAMEWORK_PATH","CMAKE_IGNORE_PATH","CMAKE_INCLUDE_DIRECTORIES_BEFORE","CMAKE_INCLUDE_DIRECTORIES_PROJECT_BEFORE","CMAKE_INCLUDE_PATH","CMAKE_INSTALL_DEFAULT_COMPONENT_NAME","CMAKE_INSTALL_MESSAGE","CMAKE_INSTALL_PREFIX","CMAKE_INSTALL_PREFIX_INITIALIZED_TO_DEFAULT","CMAKE_LIBRARY_PATH","CMAKE_MFC_FLAG","CMAKE_MODULE_PATH","CMAKE_NOT_USING_CONFIG_FLAGS","CMAKE_POLICY_DEFAULT_CMP"+varNumberRegexp+"{4}","CMAKE_POLICY_WARNING_CMP"+varNumberRegexp+"{4}","CMAKE_PREFIX_PATH","CMAKE_PROGRAM_PATH","CMAKE_PROJECT_"+varRegexp+"_INCLUDE","CMAKE_SKIP_INSTALL_ALL_DEPENDENCY","CMAKE_STAGING_PREFIX","CMAKE_SUBLIME_TEXT_2_ENV_SETTINGS","CMAKE_SUBLIME_TEXT_2_EXCLUDE_BUILD_TREE","CMAKE_SYSROOT","CMAKE_SYSROOT_COMPILE","CMAKE_SYSROOT_LINK","CMAKE_SYSTEM_APPBUNDLE_PATH","CMAKE_SYSTEM_FRAMEWORK_PATH","CMAKE_SYSTEM_IGNORE_PATH","CMAKE_SYSTEM_INCLUDE_PATH","CMAKE_SYSTEM_LIBRARY_PATH","CMAKE_SYSTEM_PREFIX_PATH","CMAKE_SYSTEM_PROGRAM_PATH","CMAKE_USER_MAKE_RULES_OVERRIDE","CMAKE_WARN_DEPRECATED","CMAKE_WARN_ON_ABSOLUTE_INSTALL_DESTINATION",
//    CMAKE_Variables_that_Describe_the_System=
            "ANDROID","APPLE","BORLAND","CMAKE_CL_64","CMAKE_COMPILER_2005","CMAKE_HOST_APPLE","CMAKE_HOST_SOLARIS","CMAKE_HOST_SYSTEM","CMAKE_HOST_SYSTEM_NAME","CMAKE_HOST_SYSTEM_PROCESSOR","CMAKE_HOST_SYSTEM_VERSION","CMAKE_HOST_UNIX","CMAKE_HOST_WIN32","CMAKE_LIBRARY_ARCHITECTURE","CMAKE_LIBRARY_ARCHITECTURE_REGEX","CMAKE_OBJECT_PATH_MAX","CMAKE_SYSTEM","CMAKE_SYSTEM_NAME","CMAKE_SYSTEM_PROCESSOR","CMAKE_SYSTEM_VERSION","CYGWIN","ENV","GHS-MULTI","MINGW","MSVC","MSVC10","MSVC11","MSVC12","MSVC14","MSVC60","MSVC70","MSVC71","MSVC80","MSVC90","MSVC_IDE","MSVC_VERSION","UNIX","WIN32","WINCE","WINDOWS_PHONE","WINDOWS_STORE","XCODE","XCODE_VERSION",
//    CMAKE_Variables_that_Control_the_Build=
            "CMAKE_ANDROID_ANT_ADDITIONAL_OPTIONS","CMAKE_ANDROID_API","CMAKE_ANDROID_API_MIN","CMAKE_ANDROID_ARCH","CMAKE_ANDROID_ARCH_ABI","CMAKE_ANDROID_ARM_MODE","CMAKE_ANDROID_ARM_NEON","CMAKE_ANDROID_ASSETS_DIRECTORIES","CMAKE_ANDROID_GUI","CMAKE_ANDROID_JAR_DEPENDENCIES","CMAKE_ANDROID_JAR_DIRECTORIES","CMAKE_ANDROID_JAVA_SOURCE_DIR","CMAKE_ANDROID_NATIVE_LIB_DEPENDENCIES","CMAKE_ANDROID_NATIVE_LIB_DIRECTORIES","CMAKE_ANDROID_NDK","CMAKE_ANDROID_NDK_DEPRECATED_HEADERS","CMAKE_ANDROID_NDK_TOOLCHAIN_HOST_TAG","CMAKE_ANDROID_NDK_TOOLCHAIN_VERSION","CMAKE_ANDROID_PROCESS_MAX","CMAKE_ANDROID_PROGUARD","CMAKE_ANDROID_PROGUARD_CONFIG_PATH","CMAKE_ANDROID_SECURE_PROPS_PATH","CMAKE_ANDROID_SKIP_ANT_STEP","CMAKE_ANDROID_STANDALONE_TOOLCHAIN","CMAKE_ANDROID_STL_TYPE","CMAKE_ARCHIVE_OUTPUT_DIRECTORY","CMAKE_ARCHIVE_OUTPUT_DIRECTORY_"+varRegexp+"","CMAKE_AUTOMOC","CMAKE_AUTOMOC_DEPEND_FILTERS","CMAKE_AUTOMOC_MOC_OPTIONS","CMAKE_AUTORCC","CMAKE_AUTORCC_OPTIONS","CMAKE_AUTOUIC","CMAKE_AUTOUIC_OPTIONS","CMAKE_AUTOUIC_SEARCH_PATHS","CMAKE_BUILD_RPATH","CMAKE_BUILD_WITH_INSTALL_NAME_DIR","CMAKE_BUILD_WITH_INSTALL_RPATH","CMAKE_COMPILE_PDB_OUTPUT_DIRECTORY","CMAKE_COMPILE_PDB_OUTPUT_DIRECTORY_"+varRegexp+"","CMAKE_"+varRegexp+"_POSTFIX","CMAKE_DEBUG_POSTFIX","CMAKE_ENABLE_EXPORTS","CMAKE_EXE_LINKER_FLAGS","CMAKE_EXE_LINKER_FLAGS_"+varRegexp+"","CMAKE_EXE_LINKER_FLAGS_"+varRegexp+"_INIT","CMAKE_EXE_LINKER_FLAGS_INIT","CMAKE_Fortran_FORMAT","CMAKE_Fortran_MODULE_DIRECTORY","CMAKE_GNUtoMS","CMAKE_INCLUDE_CURRENT_DIR","CMAKE_INCLUDE_CURRENT_DIR_IN_INTERFACE","CMAKE_INSTALL_NAME_DIR","CMAKE_INSTALL_RPATH","CMAKE_INSTALL_RPATH_USE_LINK_PATH","CMAKE_INTERPROCEDURAL_OPTIMIZATION","CMAKE_INTERPROCEDURAL_OPTIMIZATION_"+varRegexp+"","CMAKE_IOS_INSTALL_COMBINED","CMAKE_"+varRegexp+"_CLANG_TIDY","CMAKE_"+varRegexp+"_COMPILER_LAUNCHER","CMAKE_"+varRegexp+"_CPPLINT","CMAKE_"+varRegexp+"_INCLUDE_WHAT_YOU_USE","CMAKE_"+varRegexp+"_VISIBILITY_PRESET","CMAKE_LIBRARY_OUTPUT_DIRECTORY","CMAKE_LIBRARY_OUTPUT_DIRECTORY_"+varRegexp+"","CMAKE_LIBRARY_PATH_FLAG","CMAKE_LINK_DEF_FILE_FLAG","CMAKE_LINK_DEPENDS_NO_SHARED","CMAKE_LINK_INTERFACE_LIBRARIES","CMAKE_LINK_LIBRARY_FILE_FLAG","CMAKE_LINK_LIBRARY_FLAG","CMAKE_LINK_WHAT_YOU_USE","CMAKE_MACOSX_BUNDLE","CMAKE_MACOSX_RPATH","CMAKE_MAP_IMPORTED_CONFIG_"+varRegexp+"","CMAKE_MODULE_LINKER_FLAGS","CMAKE_MODULE_LINKER_FLAGS_"+varRegexp+"","CMAKE_MODULE_LINKER_FLAGS_"+varRegexp+"_INIT","CMAKE_MODULE_LINKER_FLAGS_INIT","CMAKE_NINJA_OUTPUT_PATH_PREFIX","CMAKE_NO_BUILTIN_CHRPATH","CMAKE_NO_SYSTEM_FROM_IMPORTED","CMAKE_OSX_ARCHITECTURES","CMAKE_OSX_DEPLOYMENT_TARGET","CMAKE_OSX_SYSROOT","CMAKE_PDB_OUTPUT_DIRECTORY","CMAKE_PDB_OUTPUT_DIRECTORY_"+varRegexp+"","CMAKE_POSITION_INDEPENDENT_CODE","CMAKE_RUNTIME_OUTPUT_DIRECTORY","CMAKE_RUNTIME_OUTPUT_DIRECTORY_"+varRegexp+"","CMAKE_SHARED_LINKER_FLAGS","CMAKE_SHARED_LINKER_FLAGS_"+varRegexp+"","CMAKE_SHARED_LINKER_FLAGS_"+varRegexp+"_INIT","CMAKE_SHARED_LINKER_FLAGS_INIT","CMAKE_SKIP_BUILD_RPATH","CMAKE_SKIP_INSTALL_RPATH","CMAKE_STATIC_LINKER_FLAGS","CMAKE_STATIC_LINKER_FLAGS_"+varRegexp+"","CMAKE_STATIC_LINKER_FLAGS_"+varRegexp+"_INIT","CMAKE_STATIC_LINKER_FLAGS_INIT","CMAKE_TRY_COMPILE_CONFIGURATION","CMAKE_TRY_COMPILE_PLATFORM_VARIABLES","CMAKE_TRY_COMPILE_TARGET_TYPE","CMAKE_USE_RELATIVE_PATHS","CMAKE_VISIBILITY_INLINES_HIDDEN","CMAKE_VS_INCLUDE_INSTALL_TO_DEFAULT_BUILD","CMAKE_VS_INCLUDE_PACKAGE_TO_DEFAULT_BUILD","CMAKE_WIN32_EXECUTABLE","CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS","CMAKE_XCODE_ATTRIBUTE_"+varRegexp+"","EXECUTABLE_OUTPUT_PATH","LIBRARY_OUTPUT_PATH",
//    CMAKE_Variables_for_Languages=
            "CMAKE_COMPILER_IS_GNUCC","CMAKE_COMPILER_IS_GNUCXX","CMAKE_COMPILER_IS_GNUG77","CMAKE_CUDA_EXTENSIONS","CMAKE_CUDA_STANDARD","CMAKE_CUDA_STANDARD_REQUIRED","CMAKE_CUDA_TOOLKIT_INCLUDE_DIRECTORIES","CMAKE_CXX_COMPILE_FEATURES","CMAKE_CXX_EXTENSIONS","CMAKE_CXX_STANDARD","CMAKE_CXX_STANDARD_REQUIRED","CMAKE_C_COMPILE_FEATURES","CMAKE_C_EXTENSIONS","CMAKE_C_STANDARD","CMAKE_C_STANDARD_REQUIRED","CMAKE_Fortran_MODDIR_DEFAULT","CMAKE_Fortran_MODDIR_FLAG","CMAKE_Fortran_MODOUT_FLAG","CMAKE_INTERNAL_PLATFORM_ABI","CMAKE_"+varRegexp+"_ANDROID_TOOLCHAIN_MACHINE","CMAKE_"+varRegexp+"_ANDROID_TOOLCHAIN_PREFIX","CMAKE_"+varRegexp+"_ANDROID_TOOLCHAIN_SUFFIX","CMAKE_"+varRegexp+"_ARCHIVE_APPEND","CMAKE_"+varRegexp+"_ARCHIVE_CREATE","CMAKE_"+varRegexp+"_ARCHIVE_FINISH","CMAKE_"+varRegexp+"_COMPILER","CMAKE_"+varRegexp+"_COMPILER_ABI","CMAKE_"+varRegexp+"_COMPILER_EXTERNAL_TOOLCHAIN","CMAKE_"+varRegexp+"_COMPILER_ID","CMAKE_"+varRegexp+"_COMPILER_LOADED","CMAKE_"+varRegexp+"_COMPILER_TARGET","CMAKE_"+varRegexp+"_COMPILER_VERSION","CMAKE_"+varRegexp+"_COMPILE_OBJECT","CMAKE_"+varRegexp+"_CREATE_SHARED_LIBRARY","CMAKE_"+varRegexp+"_CREATE_SHARED_MODULE","CMAKE_"+varRegexp+"_CREATE_STATIC_LIBRARY","CMAKE_"+varRegexp+"_FLAGS","CMAKE_"+varRegexp+"_FLAGS_DEBUG","CMAKE_"+varRegexp+"_FLAGS_DEBUG_INIT","CMAKE_"+varRegexp+"_FLAGS_INIT","CMAKE_"+varRegexp+"_FLAGS_MINSIZEREL","CMAKE_"+varRegexp+"_FLAGS_MINSIZEREL_INIT","CMAKE_"+varRegexp+"_FLAGS_RELEASE","CMAKE_"+varRegexp+"_FLAGS_RELEASE_INIT","CMAKE_"+varRegexp+"_FLAGS_RELWITHDEBINFO","CMAKE_"+varRegexp+"_FLAGS_RELWITHDEBINFO_INIT","CMAKE_"+varRegexp+"_GHS_KERNEL_FLAGS_DEBUG","CMAKE_"+varRegexp+"_GHS_KERNEL_FLAGS_MINSIZEREL","CMAKE_"+varRegexp+"_GHS_KERNEL_FLAGS_RELEASE","CMAKE_"+varRegexp+"_GHS_KERNEL_FLAGS_RELWITHDEBINFO","CMAKE_"+varRegexp+"_IGNORE_EXTENSIONS","CMAKE_"+varRegexp+"_IMPLICIT_INCLUDE_DIRECTORIES","CMAKE_"+varRegexp+"_IMPLICIT_LINK_DIRECTORIES","CMAKE_"+varRegexp+"_IMPLICIT_LINK_FRAMEWORK_DIRECTORIES","CMAKE_"+varRegexp+"_IMPLICIT_LINK_LIBRARIES","CMAKE_"+varRegexp+"_LIBRARY_ARCHITECTURE","CMAKE_"+varRegexp+"_LINKER_PREFERENCE","CMAKE_"+varRegexp+"_LINKER_PREFERENCE_PROPAGATES","CMAKE_"+varRegexp+"_LINK_EXECUTABLE","CMAKE_"+varRegexp+"_OUTPUT_EXTENSION","CMAKE_"+varRegexp+"_PLATFORM_ID","CMAKE_"+varRegexp+"_SIMULATE_ID","CMAKE_"+varRegexp+"_SIMULATE_VERSION","CMAKE_"+varRegexp+"_SIZEOF_DATA_PTR","CMAKE_"+varRegexp+"_SOURCE_FILE_EXTENSIONS","CMAKE_"+varRegexp+"_STANDARD_INCLUDE_DIRECTORIES","CMAKE_"+varRegexp+"_STANDARD_LIBRARIES","CMAKE_Swift_LANGUAGE_VERSION","CMAKE_USER_MAKE_RULES_OVERRIDE_"+varRegexp+"",
//    CMAKE_Variables_for_CTest=
            "CTEST_BINARY_DIRECTORY","CTEST_BUILD_COMMAND","CTEST_BUILD_NAME","CTEST_BZR_COMMAND","CTEST_BZR_UPDATE_OPTIONS","CTEST_CHANGE_ID","CTEST_CHECKOUT_COMMAND","CTEST_CONFIGURATION_TYPE","CTEST_CONFIGURE_COMMAND","CTEST_COVERAGE_COMMAND","CTEST_COVERAGE_EXTRA_FLAGS","CTEST_CURL_OPTIONS","CTEST_CUSTOM_COVERAGE_EXCLUDE","CTEST_CUSTOM_ERROR_EXCEPTION","CTEST_CUSTOM_ERROR_MATCH","CTEST_CUSTOM_ERROR_POST_CONTEXT","CTEST_CUSTOM_ERROR_PRE_CONTEXT","CTEST_CUSTOM_MAXIMUM_FAILED_TEST_OUTPUT_SIZE","CTEST_CUSTOM_MAXIMUM_NUMBER_OF_ERRORS","CTEST_CUSTOM_MAXIMUM_NUMBER_OF_WARNINGS","CTEST_CUSTOM_MAXIMUM_PASSED_TEST_OUTPUT_SIZE","CTEST_CUSTOM_MEMCHECK_IGNORE","CTEST_CUSTOM_POST_MEMCHECK","CTEST_CUSTOM_POST_TEST","CTEST_CUSTOM_PRE_MEMCHECK","CTEST_CUSTOM_PRE_TEST","CTEST_CUSTOM_TEST_IGNORE","CTEST_CUSTOM_WARNING_EXCEPTION","CTEST_CUSTOM_WARNING_MATCH","CTEST_CVS_CHECKOUT","CTEST_CVS_COMMAND","CTEST_CVS_UPDATE_OPTIONS","CTEST_DROP_LOCATION","CTEST_DROP_METHOD","CTEST_DROP_SITE","CTEST_DROP_SITE_CDASH","CTEST_DROP_SITE_PASSWORD","CTEST_DROP_SITE_USER","CTEST_EXTRA_COVERAGE_GLOB","CTEST_GIT_COMMAND","CTEST_GIT_INIT_SUBMODULES","CTEST_GIT_UPDATE_CUSTOM","CTEST_GIT_UPDATE_OPTIONS","CTEST_HG_COMMAND","CTEST_HG_UPDATE_OPTIONS","CTEST_MEMORYCHECK_COMMAND","CTEST_MEMORYCHECK_COMMAND_OPTIONS","CTEST_MEMORYCHECK_SANITIZER_OPTIONS","CTEST_MEMORYCHECK_SUPPRESSIONS_FILE","CTEST_MEMORYCHECK_TYPE","CTEST_NIGHTLY_START_TIME","CTEST_P4_CLIENT","CTEST_P4_COMMAND","CTEST_P4_OPTIONS","CTEST_P4_UPDATE_OPTIONS","CTEST_SCP_COMMAND","CTEST_SITE","CTEST_SOURCE_DIRECTORY","CTEST_SVN_COMMAND","CTEST_SVN_OPTIONS","CTEST_SVN_UPDATE_OPTIONS","CTEST_TEST_LOAD","CTEST_TEST_TIMEOUT","CTEST_TRIGGER_SITE","CTEST_UPDATE_COMMAND","CTEST_UPDATE_OPTIONS","CTEST_UPDATE_VERSION_ONLY","CTEST_USE_LAUNCHERS",
//    CMAKE_Variables_for_CPack=
            "CPACK_ABSOLUTE_DESTINATION_FILES","CPACK_COMPONENT_INCLUDE_TOPLEVEL_DIRECTORY","CPACK_ERROR_ON_ABSOLUTE_INSTALL_DESTINATION","CPACK_INCLUDE_TOPLEVEL_DIRECTORY","CPACK_INSTALL_SCRIPT","CPACK_PACKAGING_INSTALL_PREFIX","CPACK_SET_DESTDIR","CPACK_WARN_ON_ABSOLUTE_INSTALL_DESTINATION"
    );

    // https://cmake.org/cmake/help/latest/manual/cmake-env-variables.7.html
    Collections.addAll(variables_ENV,
// Environment Variables that Control the Build
            "CMAKE_CONFIG_TYPE","CMAKE_MSVCIDE_RUN_PATH","CMAKE_OSX_ARCHITECTURES","LDFLAGS","MACOSX_DEPLOYMENT_TARGET",
// Environment Variables for Languages
            "ASM"+varRegexp+"","ASM"+varRegexp+"FLAGS","CC","CFLAGS","CSFLAGS","CUDACXX","CUDAFLAGS","CUDAHOSTCXX","CXX","CXXFLAGS","FC","FFLAGS","RC","RCFLAGS",
// Environment Variables for CTest
            "CMAKE_CONFIG_TYPE","CTEST_INTERACTIVE_DEBUG_MODE","CTEST_OUTPUT_ON_FAILURE","CTEST_PARALLEL_LEVEL","CTEST_USE_LAUNCHERS_DEFAULT","DASHBOARD_TEST_FROM_CTEST"
    );

// https://cmake.org/cmake/help/latest/manual/cmake-properties.7.html
    Collections.addAll(properties_All,
//Properties of Global Scope
"ALLOW_DUPLICATE_CUSTOM_TARGETS","AUTOGEN_SOURCE_GROUP","AUTOGEN_TARGETS_FOLDER","AUTOMOC_SOURCE_GROUP","AUTOMOC_TARGETS_FOLDER","AUTORCC_SOURCE_GROUP","CMAKE_C_KNOWN_FEATURES","CMAKE_CXX_KNOWN_FEATURES","DEBUG_CONFIGURATIONS","DISABLED_FEATURES","ENABLED_FEATURES","ENABLED_LANGUAGES","FIND_LIBRARY_USE_LIB32_PATHS","FIND_LIBRARY_USE_LIB64_PATHS","FIND_LIBRARY_USE_LIBX32_PATHS","FIND_LIBRARY_USE_OPENBSD_VERSIONING","GENERATOR_IS_MULTI_CONFIG","GLOBAL_DEPENDS_DEBUG_MODE","GLOBAL_DEPENDS_NO_CYCLES","IN_TRY_COMPILE","PACKAGES_FOUND","PACKAGES_NOT_FOUND","JOB_POOLS","PREDEFINED_TARGETS_FOLDER","ECLIPSE_EXTRA_NATURES","REPORT_UNDEFINED_PROPERTIES","RULE_LAUNCH_COMPILE","RULE_LAUNCH_CUSTOM","RULE_LAUNCH_LINK","RULE_MESSAGES","TARGET_ARCHIVES_MAY_BE_SHARED_LIBS","TARGET_MESSAGES","TARGET_SUPPORTS_SHARED_LIBS","USE_FOLDERS","XCODE_EMIT_EFFECTIVE_PLATFORM_NAME",
//PropertiesonDirectories
"ADDITIONAL_MAKE_CLEAN_FILES","BINARY_DIR","BUILDSYSTEM_TARGETS","CACHE_VARIABLES","CLEAN_NO_CUSTOM","CMAKE_CONFIGURE_DEPENDS","COMPILE_DEFINITIONS","COMPILE_OPTIONS","DEFINITIONS","EXCLUDE_FROM_ALL","IMPLICIT_DEPENDS_INCLUDE_TRANSFORM","INCLUDE_DIRECTORIES","INCLUDE_REGULAR_EXPRESSION","INTERPROCEDURAL_OPTIMIZATION_"+varRegexp+"","INTERPROCEDURAL_OPTIMIZATION","LABELS","LINK_DIRECTORIES","LISTFILE_STACK","MACROS","PARENT_DIRECTORY","RULE_LAUNCH_COMPILE","RULE_LAUNCH_CUSTOM","RULE_LAUNCH_LINK","SOURCE_DIR","SUBDIRECTORIES","TEST_INCLUDE_FILES","VARIABLES","VS_GLOBAL_SECTION_POST_"+varRegexp+"","VS_GLOBAL_SECTION_PRE_"+varRegexp+"","VS_STARTUP_PROJECT",
//PropertiesonTargets
"ALIASED_TARGET","ANDROID_ANT_ADDITIONAL_OPTIONS","ANDROID_API","ANDROID_API_MIN","ANDROID_ARCH","ANDROID_ASSETS_DIRECTORIES","ANDROID_GUI","ANDROID_JAR_DEPENDENCIES","ANDROID_JAR_DIRECTORIES","ANDROID_JAVA_SOURCE_DIR","ANDROID_NATIVE_LIB_DEPENDENCIES","ANDROID_NATIVE_LIB_DIRECTORIES","ANDROID_PROCESS_MAX","ANDROID_PROGUARD","ANDROID_PROGUARD_CONFIG_PATH","ANDROID_SECURE_PROPS_PATH","ANDROID_SKIP_ANT_STEP","ANDROID_STL_TYPE","ARCHIVE_OUTPUT_DIRECTORY_"+varRegexp+"","ARCHIVE_OUTPUT_DIRECTORY","ARCHIVE_OUTPUT_NAME_"+varRegexp+"","ARCHIVE_OUTPUT_NAME","AUTOGEN_BUILD_DIR","AUTOGEN_TARGET_DEPENDS","AUTOMOC_COMPILER_PREDEFINES","AUTOMOC_DEPEND_FILTERS","AUTOMOC_MACRO_NAMES","AUTOMOC_MOC_OPTIONS","AUTOMOC","AUTOUIC","AUTOUIC_OPTIONS","AUTOUIC_SEARCH_PATHS","AUTORCC","AUTORCC_OPTIONS","BINARY_DIR","BUILD_RPATH","BUILD_WITH_INSTALL_NAME_DIR","BUILD_WITH_INSTALL_RPATH","BUNDLE_EXTENSION","BUNDLE","C_EXTENSIONS","C_STANDARD","C_STANDARD_REQUIRED","COMPATIBLE_INTERFACE_BOOL","COMPATIBLE_INTERFACE_NUMBER_MAX","COMPATIBLE_INTERFACE_NUMBER_MIN","COMPATIBLE_INTERFACE_STRING","COMPILE_DEFINITIONS","COMPILE_FEATURES","COMPILE_FLAGS","COMPILE_OPTIONS","COMPILE_PDB_NAME","COMPILE_PDB_NAME_"+varRegexp+"","COMPILE_PDB_OUTPUT_DIRECTORY","COMPILE_PDB_OUTPUT_DIRECTORY_"+varRegexp+"",""+varRegexp+"_OUTPUT_NAME",""+varRegexp+"_POSTFIX","CROSSCOMPILING_EMULATOR","CUDA_PTX_COMPILATION","CUDA_SEPARABLE_COMPILATION","CUDA_RESOLVE_DEVICE_SYMBOLS","CUDA_EXTENSIONS","CUDA_STANDARD","CUDA_STANDARD_REQUIRED","CXX_EXTENSIONS","CXX_STANDARD","CXX_STANDARD_REQUIRED","DEBUG_POSTFIX","DEFINE_SYMBOL","DEPLOYMENT_REMOTE_DIRECTORY","EchoString","ENABLE_EXPORTS","EXCLUDE_FROM_ALL","EXCLUDE_FROM_DEFAULT_BUILD_"+varRegexp+"","EXCLUDE_FROM_DEFAULT_BUILD","EXPORT_NAME","FOLDER","Fortran_FORMAT","Fortran_MODULE_DIRECTORY","FRAMEWORK","FRAMEWORK_VERSION","GENERATOR_FILE_NAME","GNUtoMS","HAS_CXX","IMPLICIT_DEPENDS_INCLUDE_TRANSFORM","IMPORTED_CONFIGURATIONS","IMPORTED_IMPLIB_"+varRegexp+"","IMPORTED_IMPLIB","IMPORTED_LIBNAME_"+varRegexp+"","IMPORTED_LIBNAME","IMPORTED_LINK_DEPENDENT_LIBRARIES_"+varRegexp+"","IMPORTED_LINK_DEPENDENT_LIBRARIES","IMPORTED_LINK_INTERFACE_LANGUAGES_"+varRegexp+"","IMPORTED_LINK_INTERFACE_LANGUAGES","IMPORTED_LINK_INTERFACE_LIBRARIES_"+varRegexp+"","IMPORTED_LINK_INTERFACE_LIBRARIES","IMPORTED_LINK_INTERFACE_MULTIPLICITY_"+varRegexp+"","IMPORTED_LINK_INTERFACE_MULTIPLICITY","IMPORTED_LOCATION_"+varRegexp+"","IMPORTED_LOCATION","IMPORTED_NO_SONAME_"+varRegexp+"","IMPORTED_NO_SONAME","IMPORTED_OBJECTS_"+varRegexp+"","IMPORTED_OBJECTS","IMPORTED","IMPORTED_SONAME_"+varRegexp+"","IMPORTED_SONAME","IMPORT_PREFIX","IMPORT_SUFFIX","INCLUDE_DIRECTORIES","INSTALL_NAME_DIR","INSTALL_RPATH","INSTALL_RPATH_USE_LINK_PATH","INTERFACE_AUTOUIC_OPTIONS","INTERFACE_COMPILE_DEFINITIONS","INTERFACE_COMPILE_FEATURES","INTERFACE_COMPILE_OPTIONS","INTERFACE_INCLUDE_DIRECTORIES","INTERFACE_LINK_LIBRARIES","INTERFACE_POSITION_INDEPENDENT_CODE","INTERFACE_SOURCES","INTERFACE_SYSTEM_INCLUDE_DIRECTORIES","INTERPROCEDURAL_OPTIMIZATION_"+varRegexp+"","INTERPROCEDURAL_OPTIMIZATION","IOS_INSTALL_COMBINED","JOB_POOL_COMPILE","JOB_POOL_LINK","LABELS",""+varRegexp+"_CLANG_TIDY",""+varRegexp+"_COMPILER_LAUNCHER",""+varRegexp+"_CPPCHECK",""+varRegexp+"_CPPLINT",""+varRegexp+"_INCLUDE_WHAT_YOU_USE",""+varRegexp+"_VISIBILITY_PRESET","LIBRARY_OUTPUT_DIRECTORY_"+varRegexp+"","LIBRARY_OUTPUT_DIRECTORY","LIBRARY_OUTPUT_NAME_"+varRegexp+"","LIBRARY_OUTPUT_NAME","LINK_DEPENDS_NO_SHARED","LINK_DEPENDS","LINKER_LANGUAGE","LINK_FLAGS_"+varRegexp+"","LINK_FLAGS","LINK_INTERFACE_LIBRARIES_"+varRegexp+"","LINK_INTERFACE_LIBRARIES","LINK_INTERFACE_MULTIPLICITY_"+varRegexp+"","LINK_INTERFACE_MULTIPLICITY","LINK_LIBRARIES","LINK_SEARCH_END_STATIC","LINK_SEARCH_START_STATIC","LINK_WHAT_YOU_USE","LOCATION_"+varRegexp+"","LOCATION","MACOSX_BUNDLE_INFO_PLIST","MACOSX_BUNDLE","MACOSX_FRAMEWORK_INFO_PLIST","MACOSX_RPATH","MANUALLY_ADDED_DEPENDENCIES","MAP_IMPORTED_CONFIG_"+varRegexp+"","NAME","NO_SONAME","NO_SYSTEM_FROM_IMPORTED","OSX_ARCHITECTURES_"+varRegexp+"","OSX_ARCHITECTURES","OUTPUT_NAME_"+varRegexp+"","OUTPUT_NAME","PDB_NAME_"+varRegexp+"","PDB_NAME","PDB_OUTPUT_DIRECTORY_"+varRegexp+"","PDB_OUTPUT_DIRECTORY","POSITION_INDEPENDENT_CODE","PREFIX","PRIVATE_HEADER","PROJECT_LABEL","PUBLIC_HEADER","RESOURCE","RULE_LAUNCH_COMPILE","RULE_LAUNCH_CUSTOM","RULE_LAUNCH_LINK","RUNTIME_OUTPUT_DIRECTORY_"+varRegexp+"","RUNTIME_OUTPUT_DIRECTORY","RUNTIME_OUTPUT_NAME_"+varRegexp+"","RUNTIME_OUTPUT_NAME","SKIP_BUILD_RPATH","SOURCE_DIR","SOURCES","SOVERSION","STATIC_LIBRARY_FLAGS_"+varRegexp+"","STATIC_LIBRARY_FLAGS","SUFFIX","TYPE","VERSION","VISIBILITY_INLINES_HIDDEN","VS_CONFIGURATION_TYPE","VS_DEBUGGER_WORKING_DIRECTORY","VS_DESKTOP_EXTENSIONS_VERSION","VS_DOTNET_REFERENCE_"+varRegexp+"","VS_DOTNET_REFERENCEPROP_"+varRegexp+"_TAG_"+varRegexp+"","VS_DOTNET_REFERENCES","VS_DOTNET_REFERENCES_COPY_LOCAL","VS_DOTNET_TARGET_FRAMEWORK_VERSION","VS_GLOBAL_KEYWORD","VS_GLOBAL_PROJECT_TYPES","VS_GLOBAL_ROOTNAMESPACE","VS_GLOBAL_"+varRegexp+"","VS_IOT_EXTENSIONS_VERSION","VS_IOT_STARTUP_TASK","VS_KEYWORD","VS_MOBILE_EXTENSIONS_VERSION","VS_SCC_AUXPATH","VS_SCC_LOCALPATH","VS_SCC_PROJECTNAME","VS_SCC_PROVIDER","VS_SDK_REFERENCES","VS_USER_PROPS","VS_WINDOWS_TARGET_PLATFORM_MIN_VERSION","VS_WINRT_COMPONENT","VS_WINRT_EXTENSIONS","VS_WINRT_REFERENCES","WIN32_EXECUTABLE","WINDOWS_EXPORT_ALL_SYMBOLS","XCODE_ATTRIBUTE_"+varRegexp+"","XCODE_EXPLICIT_FILE_TYPE","XCODE_PRODUCT_TYPE","XCTEST",
//PropertiesonTests
"ATTACHED_FILES_ON_FAIL","ATTACHED_FILES","COST","DEPENDS","DISABLED","ENVIRONMENT","FAIL_REGULAR_EXPRESSION","FIXTURES_CLEANUP","FIXTURES_REQUIRED","FIXTURES_SETUP","LABELS","MEASUREMENT","PASS_REGULAR_EXPRESSION","PROCESSORS","REQUIRED_FILES","RESOURCE_LOCK","RUN_SERIAL","SKIP_RETURN_CODE","TIMEOUT","TIMEOUT_AFTER_MATCH","WILL_FAIL","WORKING_DIRECTORY",
//PropertiesonSourceFiles
"ABSTRACT","AUTOUIC_OPTIONS","AUTORCC_OPTIONS","COMPILE_DEFINITIONS","COMPILE_FLAGS","EXTERNAL_OBJECT","Fortran_FORMAT","GENERATED","HEADER_FILE_ONLY","KEEP_EXTENSION","LABELS","LANGUAGE","LOCATION","MACOSX_PACKAGE_LOCATION","OBJECT_DEPENDS","OBJECT_OUTPUTS","SKIP_AUTOGEN","SKIP_AUTOMOC","SKIP_AUTORCC","SKIP_AUTOUIC","SYMBOLIC","VS_COPY_TO_OUT_DIR","VS_CSHARP_"+varRegexp+"","VS_DEPLOYMENT_CONTENT","VS_DEPLOYMENT_LOCATION","VS_INCLUDE_IN_VSIX","VS_RESOURCE_GENERATOR","VS_SHADER_ENTRYPOINT","VS_SHADER_FLAGS","VS_SHADER_MODEL","VS_SHADER_OUTPUT_HEADER_FILE","VS_SHADER_TYPE","VS_SHADER_VARIABLE_NAME","VS_TOOL_OVERRIDE","VS_XAML_TYPE","WRAP_EXCLUDE","XCODE_EXPLICIT_FILE_TYPE","XCODE_FILE_ATTRIBUTES","XCODE_LAST_KNOWN_FILE_TYPE",
//PropertiesonCacheEntries
"ADVANCED","HELPSTRING","MODIFIED","STRINGS","TYPE","VALUE",
//PropertiesonInstalledFiles
"CPACK_DESKTOP_SHORTCUTS","CPACK_NEVER_OVERWRITE","CPACK_PERMANENT","CPACK_START_MENU_SHORTCUTS","CPACK_STARTUP_SHORTCUTS","CPACK_WIX_ACL",
// My addition
"SHARED","STATIC","MODULE","PRIVATE","PUBLIC","INTERFACE","TARGET","TARGETS","PROPERTY","PROPERTIES","REQUIRED","EXPORT","NAMESPACE","DESTINATION","FILES","CONFIG","REQUIRED","COMPONENTS","FATAL_ERROR","STATUS","WARNING","AUTHOR_WARNING","SEND_ERROR","DEPRECATION"
            );
    Collections.addAll(properties_Deprecated,
//DeprecatedPropertiesonDirectories
"COMPILE_DEFINITIONS_"+varRegexp,"TEST_INCLUDE_FILE",
//DeprecatedPropertiesonTargets
"COMPILE_DEFINITIONS_"+varRegexp,"POST_INSTALL_SCRIPT","PRE_INSTALL_SCRIPT",
//DeprecatedPropertiesonSourceFiles
"COMPILE_DEFINITIONS_"+varRegexp
            );

//CMAKE_Operators
    Collections.addAll(operators,
"ABSOLUTE","AND","BOOL","CACHE","COMMAND","DEFINED","DOC","EQUAL","EXISTS","EXT","FALSE","GREATER","INTERNAL","LESS","MATCHES","NAME","NAMES","NAME_WE","NOT","OFF","ON","OR","PATH","PATHS","PROGRAM","STREQUAL","STRGREATER","STRING","STRLESS","TRUE",
// My addition
"WRITE","REMOVE","REPLACE","APPEND"
    );
  }
}
