package com.cmakeplugin;

import com.cmakeplugin.parsing.CMakeParser;
import com.cmakeplugin.psi.CMakeFile;
import com.cmakeplugin.psi.CMakeTypes;
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
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

/** Created by alex on 12/21/14. */
public class CMakeParserDefinition implements ParserDefinition {
  private static TokenSet WHITE_SPACES;
  private static TokenSet COMMENTS;
  private static TokenSet STRINGS;
  private static IFileElementType FILE =
      new IFileElementType(CMakeLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(LanguageVersion version) {
    return new CMakeLexerAdapter();
  }

  @Override
  public PsiParser createParser(LanguageVersion version) {
    return new CMakeParser();
  }

  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public TokenSet getWhitespaceTokens(LanguageVersion version) {
    if (WHITE_SPACES == null) WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    return WHITE_SPACES;
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens(LanguageVersion version) {
    if (COMMENTS == null)
      COMMENTS = TokenSet.create(CMakeTypes.LINE_COMMENT, CMakeTypes.BRACKET_COMMENT);
    return COMMENTS;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements(LanguageVersion version) {
    if (STRINGS == null) STRINGS = TokenSet.create(CMakeTypes.QUOTED_ARGUMENT);
    return STRINGS;
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode astNode) {
    return CMakeTypes.Factory.createElement(astNode);
  }

  @Override
  public PsiFile createFile(FileViewProvider fileViewProvider) {
    return new CMakeFile(fileViewProvider);
  }

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
