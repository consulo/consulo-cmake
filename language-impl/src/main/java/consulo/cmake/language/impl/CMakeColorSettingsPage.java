package consulo.cmake.language.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.localize.CMakeLocalize;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtensionImpl
public class CMakeColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] LEXER_DESCRIPTORS =
        new AttributesDescriptor[]{
            new AttributesDescriptor(CMakeLocalize.colorSettingsComment(), CMakeSyntaxHighlighter.COMMENT),
            new AttributesDescriptor(CMakeLocalize.colorSettingsBraces(), CMakeSyntaxHighlighter.BRACES),
            new AttributesDescriptor(CMakeLocalize.colorSettingsSeparator(), CMakeSyntaxHighlighter.SEPARATOR),
            new AttributesDescriptor(CMakeLocalize.colorSettingsBadChar(), CMakeSyntaxHighlighter.BADCHAR),
            new AttributesDescriptor(CMakeLocalize.colorSettingsKeyword(), CMakeSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor(CMakeLocalize.colorSettingsBracketArgument(), CMakeSyntaxHighlighter.BRACKET_ARGUMENT),
            new AttributesDescriptor(CMakeLocalize.colorSettingsQuotedArgument(), CMakeSyntaxHighlighter.STRING)
        };

    private static final AttributesDescriptor[] ANNOTATOR_DESCRIPTORS =
        new AttributesDescriptor[]{
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeCommand(), CMakeSyntaxHighlighter.CMAKE_COMMAND),
            new AttributesDescriptor(CMakeLocalize.colorSettingsUnquotedLegacy(), CMakeSyntaxHighlighter.UNQUOTED_LEGACY),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeVarRef(), CMakeSyntaxHighlighter.CMAKE_VAR_REF),
            new AttributesDescriptor(CMakeLocalize.colorSettingsVarRef(), CMakeSyntaxHighlighter.VAR_REF),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeVarDef(), CMakeSyntaxHighlighter.CMAKE_VAR_DEF),
            new AttributesDescriptor(CMakeLocalize.colorSettingsVarDef(), CMakeSyntaxHighlighter.VAR_DEF),
            new AttributesDescriptor(CMakeLocalize.colorSettingsPathUrl(), CMakeSyntaxHighlighter.CMAKE_PATH_URL),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeOperator(), CMakeSyntaxHighlighter.CMAKE_OPERATOR),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeProperty(), CMakeSyntaxHighlighter.CMAKE_PROPERTY),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeBoolean(), CMakeSyntaxHighlighter.CMAKE_BOOLEAN),
            new AttributesDescriptor(CMakeLocalize.colorSettingsCmakeModule(), CMakeSyntaxHighlighter.CMAKE_MODULE),
            new AttributesDescriptor(CMakeLocalize.colorSettingsFunction(), CMakeSyntaxHighlighter.FUNCTION),
            new AttributesDescriptor(CMakeLocalize.colorSettingsMacros(), CMakeSyntaxHighlighter.MACROS)
        };

    @Nonnull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new CMakeSyntaxHighlighter();
    }

    @Nonnull
    @Override
    public String getDemoText() {
        return "# Line Comment\n"
            + "#[[This is a bracket comment.\n"
            + "It runs until the close bracket.]]\n"
            + "<c>include</c>(<mod>CheckCXXCompilerFlag</mod>)\n"
            + "if(<b>TRUE</b>)\n"
            + "\tunknown_command(\n"
            + "\t\tunquoted_argument=<vr>${outer_${inner_variable}_variable}</vr><u>/followed/by/path</u>\n"
            + "\t\t<cvd>CMAKE_CXX_FLAGS</cvd> BadChar=\\d\\g\\j)\n"
            + "endif()\n"
            + "<c>set</c>(<vd>variable_definition</vd> arg1;arg2;arg3\n"
            + "\t<p>PUBLIC</p> with_known_CMake_Property\n"
            + "\t#[=[with Bracket Comment]=] <o>AND</o> with_known_CMake_Operator\n"
            + "\t<cvr>${CMAKE_CXX_FLAGS}</cvr> with_known_CMake_Variable_reference\n"
            + "\t<l>UnquotedLegacy\"fff\"ghg -Da=\"b c\"\" \" -Da=$(v) a\" \"b\"c\"f$$$ </l>)\n"
            + "function(<f>fun1</f>)\n"
            + "\t<c>message</c>( <-this_is_known_CMake_Command \"This is a quoted argument containing multiple lines.\n"
            + "\tThis is always one argument even though it contains a ; character.\n"
            + "\tBoth \\\\-escape sequences and <vr>${variable}</vr> references are evaluated.\n"
            + "\tThe text does not end on an escaped double-quote like \\\".\n"
            + "\tIt does end in an unescaped double quote.\")\n"
            + "endfunction()\n"
            + "macro(<m>macros1</m>)\n"
            + "\t<c>message</c>( [=[\n"
            + "\tThis is the first line in a bracket argument with bracket length 1.\n"
            + "\tNo \\-escape sequences or ${variable} references are evaluated.\n"
            + "\tThis is always one argument even though it contains a ; character.\n"
            + "\tThe text does not end on a closing bracket of length 0 like ]].\n"
            + "\tIt does end in a closing bracket of length 1. ]=])"
            + "endmacro()\n";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        List<String> keys = Arrays.asList("c", "o", "u", "p", "b", "l", "cvr", "vr", "cvd", "vd", "f", "m", "mod");
        List<TextAttributesKey> commands = Arrays.asList(
            CMakeSyntaxHighlighter.CMAKE_COMMAND,
            CMakeSyntaxHighlighter.CMAKE_OPERATOR,
            CMakeSyntaxHighlighter.CMAKE_PATH_URL,
            CMakeSyntaxHighlighter.CMAKE_PROPERTY,
            CMakeSyntaxHighlighter.CMAKE_BOOLEAN,
            CMakeSyntaxHighlighter.UNQUOTED_LEGACY,
            CMakeSyntaxHighlighter.CMAKE_VAR_REF,
            CMakeSyntaxHighlighter.VAR_REF,
            CMakeSyntaxHighlighter.CMAKE_VAR_DEF,
            CMakeSyntaxHighlighter.VAR_DEF,
            CMakeSyntaxHighlighter.FUNCTION,
            CMakeSyntaxHighlighter.MACROS,
            CMakeSyntaxHighlighter.CMAKE_MODULE
        );

        Map<String, TextAttributesKey> map = new LinkedHashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i), commands.get(i));
        }
        return map;
    }

    @Nonnull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return Stream.concat(Arrays.stream(LEXER_DESCRIPTORS), Arrays.stream(ANNOTATOR_DESCRIPTORS))
            .toArray(AttributesDescriptor[]::new);
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return CMakeLanguage.INSTANCE.getDisplayName();
    }
}
