package consulo.cmake;

import com.cmakeplugin.psi.CMakeTypes;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributes;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple highlighter based on the lexer output. Annotator provides the psi-aware
 * highlights.
 */
public class CMakeSyntaxHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> keys1;
    private static final Map<IElementType, TextAttributesKey> keys2;

    @Nonnull
    @Override
    public Lexer getHighlightingLexer() {
        return new CMakeLexerAdapter();
    }

    @Deprecated
    private static final int BOLD = 1;// TODO swing constant from Font

    // Highlighting styles
    public static final TextAttributesKey COMMENT = TextAttributesKey.of("CMAKE.LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);

    public static final TextAttributesKey STRING =
        TextAttributesKey.of("CMAKE.STRING_LITERAL", DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey BRACES =
        TextAttributesKey.of("CMAKE.BRACES", DefaultLanguageHighlighterColors.BRACES);

    public static final TextAttributesKey SEPARATOR =
        TextAttributesKey.of("CMAKE.SEPARATOR", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey BADCHAR =
        TextAttributesKey.of("CMAKE.BADCHAR", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

    public static final TextAttributesKey KEYWORD =
        TextAttributesKey.of("CMAKE.KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey CMAKE_COMMAND =
        TextAttributesKey.of("CMAKE.COMMAND", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    public static final TextAttributesKey FUNCTION =
        createTextAttributesKey("CMAKE.FUNCTION", BOLD, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    public static final TextAttributesKey MACROS =
        createTextAttributesKey("CMAKE.MACROS", BOLD, DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);

    public static final TextAttributesKey UNQUOTED_LEGACY =
        TextAttributesKey.of("CMAKE.UNQUOTED_LEGACY", DefaultLanguageHighlighterColors.DOC_COMMENT_TAG);

    public static final TextAttributesKey CMAKE_VAR_REF =
        TextAttributesKey.of(
            "CMAKE.CMAKE_VAR_REF", DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey CMAKE_VAR_DEF =
        createTextAttributesKey("CMAKE.CMAKE_VAR_DEF", BOLD, DefaultLanguageHighlighterColors.CONSTANT);

    public static final TextAttributesKey VAR_REF =
        TextAttributesKey.of("CMAKE.VAR_REF", DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey VAR_DEF =
        createTextAttributesKey("CMAKE.VAR_DEF", BOLD, DefaultLanguageHighlighterColors.INSTANCE_FIELD);

    public static final TextAttributesKey BRACKET_ARGUMENT =
        TextAttributesKey.of(
            "BRACKET_ARGUMENT", DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey CMAKE_OPERATOR =
        TextAttributesKey.of(
            "CMAKE.OPERATOR", DefaultLanguageHighlighterColors.METADATA);

    public static final TextAttributesKey CMAKE_MODULE =
        TextAttributesKey.of(
            "CMAKE.MODULE", DefaultLanguageHighlighterColors.METADATA);

    public static final TextAttributesKey CMAKE_PATH_URL =
        TextAttributesKey.of(
            "CMAKE.PATH_URL", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    public static final TextAttributesKey CMAKE_PROPERTY =
        TextAttributesKey.of(
            "CMAKE.PROPERTY", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey CMAKE_BOOLEAN =
        TextAttributesKey.of(
            "CMAKE.BOOLEAN", DefaultLanguageHighlighterColors.NUMBER);

    @Nonnull
    private static TextAttributesKey createTextAttributesKey(
        String externalName, int newFontType, TextAttributesKey baseTextAttributesKey) {
        TextAttributes textAttributes = baseTextAttributesKey.getDefaultAttributes().clone();
        int fontType = textAttributes.getFontType() + newFontType;
        if (fontType >= 0 && fontType <= 3) {
            textAttributes.setFontType(fontType);
        }
        return TextAttributesKey.createTextAttributesKey(externalName, textAttributes);
    }

    //  private static TextAttributesKey createBoldTextAttributesKey (String externalName,
    // TextAttributesKey baseTextAttributesKey) {
    //    TextAttributes textAttributes = baseTextAttributesKey.getDefaultAttributes().clone();
    //    textAttributes.setFontType(Font.BOLD);
    //    return TextAttributesKey.createTextAttributesKey( externalName, textAttributes);
    //  }

    static {
        keys1 = new HashMap<IElementType, TextAttributesKey>();
        keys2 = new HashMap<IElementType, TextAttributesKey>();
        keys1.put(TokenType.BAD_CHARACTER, BADCHAR);
        keys1.put(TokenType.WHITE_SPACE, SEPARATOR);
        // TODO: Populate maps here
        keys1.put(CMakeTypes.LINE_COMMENT, COMMENT);
        keys1.put(CMakeTypes.BRACKET_COMMENT, COMMENT);
        keys1.put(CMakeTypes.QUOTED_ARGUMENT, STRING);
        keys1.put(CMakeTypes.LPAR, BRACES);
        keys1.put(CMakeTypes.RPAR, BRACES);
        keys1.put(CMakeTypes.CMAKE_COMMAND, CMAKE_COMMAND);
        keys1.put(CMakeTypes.BRACKET_ARGUMENT, BRACKET_ARGUMENT);
        // IF keywords highlight
        keys1.put(CMakeTypes.IF, KEYWORD);
        keys1.put(CMakeTypes.ELSEIF, KEYWORD);
        keys1.put(CMakeTypes.ENDIF, KEYWORD);
        keys1.put(CMakeTypes.ELSE, KEYWORD);
        // FOR keywords highlight
        keys1.put(CMakeTypes.FOREACH, KEYWORD);
        keys1.put(CMakeTypes.ENDFOREACH, KEYWORD);
        // WHILE keywords highlight
        keys1.put(CMakeTypes.WHILE, KEYWORD);
        keys1.put(CMakeTypes.ENDWHILE, KEYWORD);
        // MACRO keywords highlight
        keys1.put(CMakeTypes.MACRO, KEYWORD);
        keys1.put(CMakeTypes.ENDMACRO, KEYWORD);
        // FUNCTION keywords highlight
        keys1.put(CMakeTypes.FUNCTION, KEYWORD);
        keys1.put(CMakeTypes.ENDFUNCTION, KEYWORD);
    }

    @Nonnull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType iElementType) {
        return SyntaxHighlighterBase.pack(keys1.get(iElementType), keys2.get(iElementType));
    }
}
