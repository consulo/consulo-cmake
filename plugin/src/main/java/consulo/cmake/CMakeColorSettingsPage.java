package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ExtensionImpl
public class CMakeColorSettingsPage implements ColorSettingsPage {
  //  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
  private static final AttributesDescriptor[] LEXER_DESCRIPTORS =
      new AttributesDescriptor[] {
        new AttributesDescriptor("Line or Brackets comment", CMakeSyntaxHighlighter.COMMENT),
        new AttributesDescriptor("Braces \"(\"", CMakeSyntaxHighlighter.BRACES),
        new AttributesDescriptor(
            "Commands and arguments separator", CMakeSyntaxHighlighter.SEPARATOR),
        new AttributesDescriptor("Bad char", CMakeSyntaxHighlighter.BADCHAR),
        new AttributesDescriptor("Keyword", CMakeSyntaxHighlighter.KEYWORD),
        new AttributesDescriptor("Bracket argument", CMakeSyntaxHighlighter.BRACKET_ARGUMENT),
        new AttributesDescriptor("Quoted argument", CMakeSyntaxHighlighter.STRING)
      };

  private static final AttributesDescriptor[] ANNOTATOR_DESCRIPTORS =
      new AttributesDescriptor[] {
        new AttributesDescriptor("Predefined literal//CMake command", CMakeSyntaxHighlighter.CMAKE_COMMAND),
        new AttributesDescriptor(
            "Unquoted legacy argument", CMakeSyntaxHighlighter.UNQUOTED_LEGACY),
        new AttributesDescriptor("Variable//CMake variable reference", CMakeSyntaxHighlighter.CMAKE_VAR_REF),
        new AttributesDescriptor("Variable//Local variable reference", CMakeSyntaxHighlighter.VAR_REF),
        new AttributesDescriptor("Variable//CMake variable definition", CMakeSyntaxHighlighter.CMAKE_VAR_DEF),
        new AttributesDescriptor("Variable//Local variable definition", CMakeSyntaxHighlighter.VAR_DEF),
        new AttributesDescriptor("Path or URL reference", CMakeSyntaxHighlighter.CMAKE_PATH_URL),
        new AttributesDescriptor("Predefined literal//CMake operator", CMakeSyntaxHighlighter.CMAKE_OPERATOR),
        new AttributesDescriptor("Predefined literal//CMake property", CMakeSyntaxHighlighter.CMAKE_PROPERTY),
        new AttributesDescriptor("Predefined literal//CMake boolean", CMakeSyntaxHighlighter.CMAKE_BOOLEAN),
        new AttributesDescriptor("Predefined literal//CMake module", CMakeSyntaxHighlighter.CMAKE_MODULE),
        new AttributesDescriptor("Function name", CMakeSyntaxHighlighter.FUNCTION),
        new AttributesDescriptor("Macros name", CMakeSyntaxHighlighter.MACROS)
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

      List<String> keys =  Arrays.asList("c", "o", "u", "p", "b", "l", "cvr", "vr", "cvd", "vd", "f", "m", "mod");
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
        .toArray(AttributesDescriptor[]::new); // LEXER_DESCRIPTORS + ANNOTATOR_DESCRIPTORS
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "CMake";
  }
}
