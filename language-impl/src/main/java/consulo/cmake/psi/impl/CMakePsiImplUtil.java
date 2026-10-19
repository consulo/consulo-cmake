package consulo.cmake.psi.impl;

import com.cmakeplugin.psi.CMakeQuotedArgumentContainer;
import com.cmakeplugin.psi.CMakeUnquotedArgumentContainer;
import com.cmakeplugin.psi.CMakeUnquotedArgumentMaybeVariableContainer;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.util.CachedValueProvider;
import consulo.cmake.psi.CMakePsiElementFactory;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.psi.*;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static consulo.cmake.utils.CMakeIFWHILEcheck.getInnerVars;

public class CMakePsiImplUtil {
    @RequiredReadAction
    public static String getName(CMakeUnquotedArgumentMaybeVariableContainer o) {
        return o.getText();
    }

    @Nonnull
    @RequiredWriteAction
    public static CMakeUnquotedArgumentMaybeVariableContainer setName(CMakeUnquotedArgumentMaybeVariableContainer o, String newName) {
        Objects.requireNonNull(o.getUnquotedArgumentMaybeVarDef())
            .replace(CMakePsiElementFactory.createArgumentFromText(o, newName, CMakeUnquotedArgumentMaybeVariableContainer.class));
        return o;
    }

    @Nullable
    public static PsiElement getNameIdentifier(CMakeUnquotedArgumentMaybeVariableContainer o) {
        return (CMakePSITreeSearch.existReferenceTo(o))
            ? o.getUnquotedArgumentMaybeVarDef()
            : null;
    }

    public static ItemPresentation getPresentation(CMakeUnquotedArgumentMaybeVariableContainer o) {
        return new ItemPresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                PsiFile file = o.getContainingFile();
                Document document = file.getViewProvider().getDocument();
                if (document == null) {
                    return LocalizeValue.of(o.getText());
                }

                final String filePath = file.getContainingDirectory() + File.separator + file.getName();
                final String shortFilePath = (filePath.length() > 30) ? filePath.substring(filePath.length() - 30) : filePath;
                final int lineNumber = document.getLineNumber(o.getTextOffset());
                final TextRange lineRange = new TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber));
                final String lineText = document.getText(lineRange).replaceAll(" {2,}", " ");

                return LocalizeValue.of(String.format("%30.30s:%4d  %s", shortFilePath, lineNumber, lineText));
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Image getIcon() {
                return null;
            }
        };
    }
//    @NotNull
//    public static PsiReference getReference(CMakeVariableContainer o) {
//        return new PsiReferenceBase<CMakeVariableContainer>(o, TextRange.from(0, o.getTextRange().getLength())) {
//            @Nullable
//            @Override
//            public PsiElement resolve() {
//                String name = getElement().getVariable().getText();
//                CommonProcessors.FindFirstProcessor<CMakeUnquotedArgumentContainer> processor =
//                    new CommonProcessors.FindFirstProcessor<CMakeUnquotedArgumentContainer>() {
//                        @Override
//                        protected boolean accept(CMakeUnquotedArgumentContainer o) {
//                            return Comparing.equal(o.getName(), name);
//                        }
//                    };
//                processUnquotedArgumentVariants(getElement(), processor);
//                return processor.getFoundValue();
//            }
//
//            @NotNull
//            @Override
//            public Object[] getVariants() {
//                CommonProcessors.CollectProcessor<CMakeUnquotedArgumentContainer> processor =
//                    new CommonProcessors.CollectProcessor<>();
//                processUnquotedArgumentVariants(getElement(), processor);
//                return ArrayUtil.toObjectArray(processor.getResults());
//            }
//
//            @Override
//            public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
//                return getElement().getVariable()
//                    .replace(CMakePsiElementFactory.createVariableFromText(getElement().getProject(), newElementName));
//            }
//        };
//  }

    private static class MyPsiPolyVariantReferenceBase<T extends PsiElement> extends PsiPolyVariantReferenceBase<T> {
        /**
         * @param element        PSI element
         * @param rangeInElement range relatively to the element's start offset
         */

        MyPsiPolyVariantReferenceBase(T element, TextRange rangeInElement) {
            super(element, rangeInElement);
        }

        @Nonnull
        @Override
        public ResolveResult[] multiResolve(boolean b) {
//            String name = getValue();
//            CommonProcessors.FindFirstProcessor<CMakeUnquotedArgumentContainer> processor =
//                new CommonProcessors.FindFirstProcessor<CMakeUnquotedArgumentContainer>() {
//                    @Override
//                    protected boolean accept(CMakeUnquotedArgumentContainer o) {
//                      return Comparing.equal(o.getName(), name);
//                    }
//                };
//            processUnquotedArgumentVariants(getElement(), processor);
//            return PsiElementResolveResult.createResults(processor.getFoundValue());
            return PsiElementResolveResult.createResults(
                CMakePSITreeSearch.findVariableDefinitions(getElement(), getValue()));
        }

        @Nonnull
        @Override
        public Object[] getVariants() {
            // TODO
//            CommonProcessors.CollectProcessor<CMakeUnquotedArgumentContainer> processor =
//                new CommonProcessors.CollectProcessor<>();
//            processUnquotedArgumentVariants(getElement(), processor);
//            return ArrayUtil.toObjectArray(processor.getResults());

            //return CMakePSITreeSearch.getAllPossibleVarDefs(getElement()).toArray();
            return EMPTY_ARRAY;
        }

        @Override
        public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
            if (getElement() instanceof CMakeUnquotedArgumentContainer) {
                return getElement().getFirstChild().replace(CMakePsiElementFactory.createArgumentFromText(getElement(),
                    getRangeInElement().replace(getElement().getText(), newElementName),
                    CMakeUnquotedArgumentContainer.class));
            }
            else if (getElement() instanceof CMakeQuotedArgumentContainer) {
                return getElement().getFirstChild().replace(CMakePsiElementFactory.createArgumentFromText(getElement(),
                    "\"" + getRangeInElement().replace(getElement().getText(), newElementName) + "\"",
                    CMakeQuotedArgumentContainer.class));
            }
            else {
                throw new IncorrectOperationException("Unknown type of Argument to replace: " + getElement().getClass());
            }
        }

    }

    @Nonnull
    public static PsiReference[] getReferences(PsiElement o) {
        List<TextRange> innerVars = getInnerVars(o);
        if (innerVars.isEmpty()) {
            return PsiReference.EMPTY_ARRAY;
        }
        PsiReference[] result = new PsiReference[innerVars.size()];
        for (int i = 0; i < innerVars.size(); i++) {
            TextRange innerVar = innerVars.get(i);
            result[i] = new MyPsiPolyVariantReferenceBase<>(o, innerVar);
        }
        return result;
    }

    private static boolean processUnquotedArgumentVariants(PsiElement context, Predicate<CMakeUnquotedArgumentContainer> processor) {
        final PsiFile containingFile = context.getContainingFile();
        List<CMakeUnquotedArgumentContainer> UnquotedArguments = LanguageCachedValueUtil.getCachedValue(
            containingFile,
            () -> CachedValueProvider.Result.create(
                computeElementsOfClass(containingFile, CMakeUnquotedArgumentContainer.class),
                containingFile
            )
        );
        return ContainerUtil.process(UnquotedArguments, processor);
    }

    public static <T> List<T> computeElementsOfClass(PsiFile psiFile, final Class<T> clazz) {
        final List<T> result = new ArrayList<>();
        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            @SuppressWarnings("unchecked")
            public void visitElement(PsiElement element) {
                if (clazz.isInstance(element)) {
                    result.add((T) element);
                }
                else //if (!(element instanceof JFlexLexicalRulesSection) && !(element instanceof JFlexUserCodeSection))
                {
                    super.visitElement(element);
                }
            }
        });
        return result;
    }
}
