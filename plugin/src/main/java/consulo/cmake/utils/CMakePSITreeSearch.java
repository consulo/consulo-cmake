package consulo.cmake.utils;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.*;
import consulo.language.psi.resolve.PsiElementProcessor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FileTypeIndex;
import consulo.language.psi.util.PsiElementFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static consulo.cmake.utils.CMakeIFWHILEcheck.couldBeVarDef;
import static consulo.cmake.utils.CMakeIFWHILEcheck.getInnerVars;

public class CMakePSITreeSearch {

    private static Set<Project> projects = ConcurrentHashMap.newKeySet();

    /**
     * Add File Listener to clear caches for file if it was changed.
     */
    private static void addFileListener(@Nonnull final Project project) {
        if (!projects.contains(project)) {
            PsiManager.getInstance(project)
                .addPsiTreeChangeListener(
                    new PsiTreeAnyChangeAbstractAdapter() {
                        @Override
                        protected void onChange(@Nullable PsiFile file) {
                            if (file != null) {
                                mapFilesToAllPossibleVarDefs.remove(file);
                                mapFilesToVarNameToVarDefs.remove(file);
                                mapFilesToAllVarRefs.remove(file);
                                mapFilesToAllCommandDefs.remove(file);
                            }
                        }
                    });
            projects.add(project);
        }
    }

    /**
     * Copy of {@link PsiTreeUtil#findChildrenOfAnyType(PsiElement, Class[])}
     */
    @Nonnull
    private static Collection<PsiElement> findChildrenByFilter(
        @Nonnull final PsiFile cmakeFile, @Nonnull final PsiElementFilter filter) {
        PsiElementProcessor.CollectFilteredElements<PsiElement> processor =
            new PsiElementProcessor.CollectFilteredElements<>(filter);
        PsiTreeUtil.processElements(cmakeFile, processor);
        return processor.getCollection();
    }

    @Nonnull
    @RequiredReadAction
    private static Collection<PsiFile> getCmakeFiles(@Nonnull PsiElement element) {
        Project project = element.getProject();
        Collection<VirtualFile> virtualFiles =
            FileTypeIndex.getFiles(CMakePDC.getCmakeFileType(), GlobalSearchScope.allScope(project));
        virtualFiles.add(element.getContainingFile().getVirtualFile());
        addFileListener(project);

        List<PsiFile> files = new ArrayList<>(virtualFiles.size());
        PsiManager manager = PsiManager.getInstance(project);
        for (VirtualFile virtualFile : virtualFiles) {
            PsiFile file = manager.findFile(virtualFile);
            if (file != null) {
                files.add(file);
            }
        }
        return files;
    }

    /**
     * looking ANY definitions of Variable in Project scope including current file.
     *
     * @param varReference PsiElement to start from
     * @param varName      Variable name to looking for
     * @return List of PsiElements with Variable definition or empty List
     */
    @Nonnull
    public static List<PsiElement> findVariableDefinitions(
        @Nonnull PsiElement varReference, final String varName) {
        List<PsiElement> result = new ArrayList<>();
        for (PsiFile cmakeFile : getCmakeFiles(varReference)) {
            result.addAll(findVarDefs(cmakeFile, varName));
        }
        return result;
    }

    private static Map<PsiFile, Map<String, Collection<PsiElement>>> mapFilesToVarNameToVarDefs =
        new ConcurrentHashMap<>();

    @Nonnull
    private static Collection<PsiElement> findVarDefs(
        @Nonnull final PsiFile cmakeFile, final String varName) {
        return mapFilesToVarNameToVarDefs
            .computeIfAbsent(cmakeFile, keyF -> new ConcurrentHashMap<>())
            .computeIfAbsent(varName, keyN -> doFindVarNameInVarDefs(cmakeFile, keyN));
    }

    @Nonnull
    private static Collection<PsiElement> doFindVarNameInVarDefs(
        @Nonnull final PsiFile cmakeFile, final String varName) {
        return getAllPossibleVarDefsInFile(cmakeFile)
            .filter(element -> element.textMatches(varName))
            .collect(Collectors.toList());
    }

    private static Map<PsiFile, Collection<PsiElement>> mapFilesToAllPossibleVarDefs =
        new ConcurrentHashMap<>();

    @Nonnull
    private static Stream<PsiElement> getAllPossibleVarDefsInFile(@Nonnull PsiFile cmakeFile) {
        return mapFilesToAllPossibleVarDefs
            .computeIfAbsent(
                cmakeFile, keyFile -> findChildrenByFilter(keyFile, CMakeIFWHILEcheck::couldBeVarDef))
            .stream();
    }

    /**
     * return ALL <b>possible</b> Variable definitions in Project scope including current file.
     *
     * @param startElement PsiElement to start from
     * @return Collection of Variable definitions or empty List
     */
    @Nonnull
    public static Stream<String> getAllPossibleVarDefs(@Nonnull PsiElement startElement) {
        return getCmakeFiles(startElement)
            .stream()
            .flatMap(CMakePSITreeSearch::getAllPossibleVarDefsInFile)
            .map(PsiElement::getText)
            .distinct();
    }

    final static Map<String, Collection<PsiElement>> EMPTY_MAP = new ConcurrentHashMap<>();

    /**
     * return ALL <b>referenced</b> Variable definitions in Project scope including current file.
     *
     * @param startElement PsiElement to start from
     * @return Collection of Variable definitions or empty List
     */
    @Nonnull
    public static Stream<String> getAllReferencedVarDefs(@Nonnull PsiElement startElement) {
        return getCmakeFiles(startElement)
            .stream()
//        .flatMap(CMakePSITreeSearch::getAllPossibleVarDefsInFile)
//        .filter(CMakePSITreeSearch::existReferenceTo)
//        .map(PsiElement::getText)
            .map(file -> mapFilesToVarNameToVarDefs.getOrDefault(file, EMPTY_MAP))
            .flatMap(mapNameToDefs -> mapNameToDefs.entrySet().stream())
            .filter(nameToDefs -> !nameToDefs.getValue().isEmpty())
            .map(Entry::getKey)
            .distinct();
    }

    /**
     * checking ANY reference of Variable in Project scope including current file.
     *
     * @param varDef PsiElement with potential Variable declaration
     * @return True if any reference found, False otherwise
     */
    public static boolean existReferenceTo(@Nonnull PsiElement varDef) {
        if (!couldBeVarDef(varDef)) {
            return false;
        }
        final String varDefText = varDef.getText();
        for (PsiFile cmakeFile : getCmakeFiles(varDef)) {
            if (hasVarRefToVarName(cmakeFile, varDefText)) {
                return true;
            }
        }
        return false;
    }

    private static Map<PsiFile, Map<String, Collection<PsiElement>>> mapFilesToAllVarRefs =
        new ConcurrentHashMap<>();

    private static boolean hasVarRefToVarName(PsiFile psiFile, String varName) {
        return mapFilesToAllVarRefs
            .computeIfAbsent(psiFile, CMakePSITreeSearch::createVarRefsForFileMap)
            .containsKey(varName);
    }

    private static Map<String, Collection<PsiElement>> createVarRefsForFileMap(PsiFile psiFile) {
        Map<String, Collection<PsiElement>> mapVarRefsToElements = new ConcurrentHashMap<>();
        for (PsiElement element : findChildrenByFilter(psiFile, CMakePDC::classCanHoldVarRef)) {
            for (String varRef : getAllVarRefs(element)) {
                mapVarRefsToElements.computeIfAbsent(varRef, keyVar -> new HashSet<>()).add(element);
            }
        }
        return mapVarRefsToElements;
    }

    @Nonnull
    private static Collection<String> getAllVarRefs(@Nonnull final PsiElement element) {
        return getInnerVars(element).stream()
            .map(range -> element.getText().substring(range.getStartOffset(), range.getEndOffset()))
            .collect(Collectors.toList());
    }

    /**
     * checking ANY definition of Variable in Project scope including current file.
     *
     * @param varRef  PsiElement with Variable reference
     * @param varName name of Variable
     * @return True if any definition found, False otherwise
     */
    public static boolean existDefinitionOf(@Nonnull PsiElement varRef, String varName) {
        for (PsiFile cmakeFile : getCmakeFiles(varRef)) {
            if (!findVarDefs(cmakeFile, varName).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * checking ANY definition of Function in Project scope including current file.
     *
     * @param command PsiElement with Function reference
     * @return True if any definition found, False otherwise
     */
    public static boolean existFunctionDefFor(@Nonnull PsiElement command) {
        return existCommandDefFor(command, CMakePDC.FUNCTION_CLASS);
    }

    /**
     * checking ANY definition of Macros in Project scope including current file.
     *
     * @param command PsiElement with Macros reference
     * @return True if any definition found, False otherwise
     */
    public static boolean existMacroDefFor(@Nonnull PsiElement command) {
        return existCommandDefFor(command, CMakePDC.MACRO_CLASS);
    }

    private static Map<PsiFile, Map<String, PsiElement>> mapFilesToAllCommandDefs =
        new ConcurrentHashMap<>();

    private static boolean existCommandDefFor(
        @Nonnull PsiElement commandRef, final Class<? extends PsiElement> clazz) {
        final String name = commandRef.getText().toLowerCase();
        PsiElement commandDef;
        for (PsiFile cmakeFile : getCmakeFiles(commandRef)) {
            if ((commandDef = getCommandDef(cmakeFile, name)) != null && clazz.isInstance(commandDef)) {
                return true;
            }
        }
        return false;
    }

    private static PsiElement getCommandDef(PsiFile psiFile, String commandName) {
        return mapFilesToAllCommandDefs
            .computeIfAbsent(psiFile, CMakePSITreeSearch::createCommandDefsForFileMap)
            .get(commandName);
    }

    private static Map<String, PsiElement> createCommandDefsForFileMap(PsiFile psiFile) {
        Map<String, PsiElement> mapCommandDefsToElement = new ConcurrentHashMap<>();
        final PsiElementFilter isFunMacroDefFilter =
            element -> PsiTreeUtil.instanceOf(element, CMakePDC.FUNCTION_CLASS, CMakePDC.MACRO_CLASS);
        for (PsiElement element : findChildrenByFilter(psiFile, isFunMacroDefFilter)) {
            mapCommandDefsToElement.put(getFunMacroName(element).toLowerCase(), element);
        }
        return mapCommandDefsToElement;
    }

    /**
     * -----------------------------------------------------------------------
     */
    public static String getFunMacroName(PsiElement rootOfFunMacro) {
        PsiElement name = getFunMacroNameElement(rootOfFunMacro);
        return name != null ? name.getText() : rootOfFunMacro.getText();
    }

    public static NavigatablePsiElement getFunMacroNameElement(PsiElement rootOfFunMacro) {
        if (rootOfFunMacro == null) {
            return null;
        }
        PsiElement arguments = PsiTreeUtil.findChildOfType(rootOfFunMacro, CMakePDC.ARGUMENTS_CLASS);
        PsiElement name = PsiTreeUtil.findChildOfAnyType(arguments, CMakePDC.FUN_MACRO_ARGUMENT_CLASSES);
        return (name instanceof NavigatablePsiElement) ? (NavigatablePsiElement) name : null;
    }

    public static String getFunMacroArgs(PsiElement element) {
        PsiElement arguments = PsiTreeUtil.findChildOfType(element, CMakePDC.ARGUMENTS_CLASS);
        return PsiTreeUtil.findChildrenOfAnyType(arguments, CMakePDC.FUN_MACRO_ARGUMENT_CLASSES).stream()
            .skip(1) // fun/macro name
            .map(PsiElement::getText)
            .collect(Collectors.joining(" "));
    }

    public static PsiElement getCommandNameElement(PsiElement argument) {
        PsiElement command = PsiTreeUtil.getParentOfType(argument, CMakePDC.COMMAND_CLASS);
        return PsiTreeUtil.getChildOfType(command, CMakePDC.COMMAND_NAME_CLASS);
    }

    public static PsiElement getFunMacroRootElement(PsiElement argument) {
        return PsiTreeUtil.getParentOfType(argument, CMakePDC.FUNCTION_CLASS, CMakePDC.MACRO_CLASS);
    }

    public static PsiElement getFunMacroEndElement(PsiElement argument) {
        return PsiTreeUtil.getParentOfType(argument, CMakePDC.FUN_MACRO_END_CLASSES);
    }

  /*  @NotNull
  private static List<PsiElement> findVarDefsFileScope(@NotNull PsiElement o, String name) {
    List<PsiElement> result = new ArrayList<>();
    PsiElement foundDeclaration = checkUpperSiblings(o.getParent(), name);
    while (foundDeclaration!=null) {
      result.add(foundDeclaration);
      foundDeclaration = (getScopeIfBody(foundDeclaration) == null)
              ? null
              : isSameIfScope(getScopeIfBody(foundDeclaration) , getScopeIfBody(o))
                ? null
                : checkUpperSiblings(foundDeclaration.getParent(), name);
    }
   return result;
  }

  private static boolean isSameIfScope(PsiElement declarationIfBody, PsiElement referenceIfBody){
    while (referenceIfBody != null) {
      if (declarationIfBody == referenceIfBody)
        return true;
      referenceIfBody = getScopeIfBody(referenceIfBody.getParent());
    }
    return false;
  }

  private static PsiElement getScopeIfBody(PsiElement element) {
    while (!(element instanceof PsiFile || element instanceof CMakeFunmacro)) { // stay in current Function scope if any
      if (element instanceof CMakeIfbody)
        return element;
      element = element.getParent();
    }
    return null;
  }

  @Nullable
  private static PsiElement checkUpperSiblings(PsiElement o, String name) {
    PsiElement result=null;
    if (o!=null && !(o instanceof PsiFile)) {
      while (o.getPrevSibling()!=null && !(o instanceof CMakeFunmacro)) { // ignore Function scopes. fixme: nested calls
        result=checkChildrens(o.getPrevSibling(), name);
        if (result!=null)
          break;
        else
          o=o.getPrevSibling();
      }
      result= (result==null) ? checkUpperSiblings(o.getParent(), name): result;
    }
    return result;
  }

  @Nullable
  private static PsiElement checkChildrens(@NotNull PsiElement o, String name) {
    PsiElement result=null, element=o.getLastChild();
    while (element!=null) {
      if (element.getText().equals(name) && element instanceof CMakeUnquotedArgumentContainer) {
        return element;
      } else {
        if (element.getLastChild()!=null) {
          result=checkChildrens(element, name);
          if (result!=null)
            return result;
        }
        element = element.getPrevSibling();
      }
    }
    return result;

  }*/
}
