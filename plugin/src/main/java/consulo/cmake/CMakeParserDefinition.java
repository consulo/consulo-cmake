package consulo.cmake;

import com.cmakeplugin.parsing.CMakeParser;
import com.cmakeplugin.psi.CMakeTypes;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.psi.CMakeFile;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import jakarta.annotation.Nonnull;

/**
 * Created by alex on 12/21/14.
 */
@ExtensionImpl
public class CMakeParserDefinition implements ParserDefinition {
    private static TokenSet WHITE_SPACES;
    private static TokenSet COMMENTS;
    private static TokenSet STRINGS;
    private static IFileElementType FILE =
        new IFileElementType(CMakeLanguage.INSTANCE);

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public Lexer createLexer(@Nonnull LanguageVersion version) {
        return new CMakeLexerAdapter();
    }

    @Nonnull
    @Override
    public PsiParser createParser(@Nonnull LanguageVersion version) {
        return new CMakeParser();
    }

    @Nonnull
    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Nonnull
    @Override
    public TokenSet getWhitespaceTokens(@Nonnull LanguageVersion version) {
        if (WHITE_SPACES == null) {
            WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
        }
        return WHITE_SPACES;
    }

    @Nonnull
    @Override
    public TokenSet getCommentTokens(@Nonnull LanguageVersion version) {
        if (COMMENTS == null) {
            COMMENTS = TokenSet.create(CMakeTypes.LINE_COMMENT, CMakeTypes.BRACKET_COMMENT);
        }
        return COMMENTS;
    }

    @Nonnull
    @Override
    public TokenSet getStringLiteralElements(@Nonnull LanguageVersion version) {
        if (STRINGS == null) {
            STRINGS = TokenSet.create(CMakeTypes.QUOTED_ARGUMENT);
        }
        return STRINGS;
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public PsiElement createElement(@Nonnull ASTNode astNode) {
        return CMakeTypes.Factory.createElement(astNode);
    }

    @Nonnull
    @Override
    public PsiFile createFile(@Nonnull FileViewProvider fileViewProvider) {
        return new CMakeFile(fileViewProvider);
    }

    @Nonnull
    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode astNode, ASTNode astNode1) {
        // Tune the separator behavior between the tokens
    /*    if( (astNode.getElementType()  == CMakeTypes.FILE_ELEMENT
    //            ||astNode.getElementType() == CMakeTypes.COMPOUND_EXPR
    //            ||astNode.getElementType() == CMakeTypes.COMMAND_EXPR
                ||astNode.getElementType() == CMakeTypes.LINE_COMMENT)
                && !getCommentTokens().contains(astNode1.getElementType())  )
          return SpaceRequirements.MUST_LINE_BREAK;

        if( (astNode.getElementType() == CMakeTypes.ARGUMENT
                && astNode1.getElementType() == CMakeTypes.ARGUMENT))
          return SpaceRequirements.MUST;
        if( astNode.getElementType() == CMakeTypes.UNQUOTED_ARGUMENT )
          return SpaceRequirements.MUST;
    //    if( (astNode.getElementType() == CMakeTypes.COMMAND_NAME))
    //      return SpaceRequirements.MUST_NOT;*/
        return SpaceRequirements.MAY;
    }
}
