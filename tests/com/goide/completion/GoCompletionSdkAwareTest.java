/*
 * Copyright 2013-2015 Sergey Ignatov, Alexander Zolotov, Mihai Toader, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.testFramework.LightProjectDescriptor;

import java.io.IOException;

public class GoCompletionSdkAwareTest extends GoCompletionTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    setUpProjectSdk();
  }

  @Override
  protected LightProjectDescriptor getProjectDescriptor() {
    return createMockProjectDescriptor();
  }

  public void testFormatter() {
    doTestInclude("package main; import . \"fmt\"; type alias <caret>", "Formatter");
  }

  public void testCamelHumpFunctionCompletion() {
    doCheckResult("package main;\n" +
                  "func test(){io.reWSC<caret>}",
                  "package main;\n" +
                  "import \"idea_io\"\n" +
                  "func test(){idea_io.ReadWriteSeekerCustom()}", Lookup.NORMAL_SELECT_CHAR);
  }

  
  public void testCamelHumpTypeCompletion() {
    doCheckResult("package main;\n" +
                  "func test(){io.reATC<caret>}",
                  "package main;\n" +
                  "import \"idea_io\"\n" +
                  "func test(){idea_io.ReadAtLeastCustom()}", Lookup.NORMAL_SELECT_CHAR);
  }

  public void testTypeAutoImport() {
    doCheckResult("package main; \n" +
                  "func test(){Templat<caret>}",
                  "package main;\n" +
                  "import \"text/template\"\n" +
                  "func test(){template.Template{<caret>}}");
  }

  public void testTypeAutoImportOnQualifiedName() {
    doCheckResult("package main; \n" +
                  "func test(){template.Templat<caret>}",
                  "package main;\n" +
                  "import \"text/template\"\n" +
                  "func test(){template.Template{<caret>}}");
  }

  public void testAutoImport() {
    doCheckResult("package main; \n" +
                  "func test(){Fprintl<caret>}",
                  "package main;\n" +
                  "import \"fmt\"\n" +
                  "func test(){fmt.Fprintln(<caret>)}");
  }

  public void testDuplicateAutoImport() {
    doCheckResult("package main; \n" +
                  "func test(){Fprintl<caret>}",
                  "package main;\n" +
                  "import \"fmt\"\n" +
                  "func test(){fmt.Fprintln(<caret>)}");
    myFixture.type(");Fprintl");
    myFixture.completeBasic();
    myFixture.checkResult("package main;\n" +
                          "import \"fmt\"\n" +
                          "func test(){fmt.Fprintln();fmt.Fprintln()}");
  }

  public void testForceAutoImportBlankImports() {
    doCheckResult("package main; \n" +
                  "import _ \"fmt\"\n" +
                  "func test(){Fprintl<caret>}",
                  "package main;\n" +
                  "import (\n" +
                  "\t_ \"fmt\"\n" +
                  "\t\"fmt\"\n" +
                  ")\n" +
                  "func test(){fmt.Fprintln()}");
  }

  public void testAutoImportWithAlias() {
    doCheckResult("package main; \n" +
                  "import alias `fmt`\n" +
                  "func test(){Fprintl<caret>}",
                  "package main; \n" +
                  "import alias `fmt`\n" +
                  "func test(){alias.Fprintln()}");
  }

  public void testAutoImportWithDotAlias() {
    doCheckResult("package main; \n" +
                  "import . `fmt`\n" +
                  "func test(){Fprintl<caret>}",
                  "package main; \n" +
                  "import . `fmt`\n" +
                  "func test(){Fprintln()}");
  }

  public void testUseImportPathInsteadOfPackageNameForAutoImport() {
    doCheckResult("package main\n" +
                  "\n" +
                  "import (\n" +
                  "    \"fmt\"\n" +
                  ")\n" +
                  "\n" +
                  "func main() {\n" +
                  "    fmt.Printf(FunctionInPackageThatDoesNotMatchDirectory<caret>);\n" +
                  "}",
                  "package main\n" +
                  "\n" +
                  "import (\n" +
                  "    \"fmt\"\n" +
                  "\t\"dirName\"\n" +
                  ")\n" +
                  "\n" +
                  "func main() {\n" +
                  "    fmt.Printf(otherPackage.FunctionInPackageThatDoesNotMatchDirectoryName());\n" +
                  "}");
  }

  public void testUsePackageNameInsteadOfImportPathIfPackageIsImported() {
    doCheckResult("package main\n" +
                  "\n" +
                  "import (\n" +
                  "    \"fmt\"\n" +
                  "    \"dirName\"\n" +
                  ")\n" +
                  "\n" +
                  "func main() {\n" +
                  "    fmt.Printf(FunctionInPackageThatDoesNotMatchDirectory<caret>);\n" +
                  "}",
                  "package main\n" +
                  "\n" +
                  "import (\n" +
                  "    \"fmt\"\n" +
                  "    \"dirName\"\n" +
                  ")\n" +
                  "\n" +
                  "func main() {\n" +
                  "    fmt.Printf(otherPackage.FunctionInPackageThatDoesNotMatchDirectoryName());\n" +
                  "}");
  }

  public void testDoNotImportLocallyImportedPackage() throws IOException {
    myFixture.getTempDirFixture().createFile("imported/imported.go", "package imported\n" +
                                                                     "func LocallyImported() {}");
    doCheckResult("package main; \n" +
                  "import `./imported`\n" +
                  "func test(){LocallyImport<caret>}", "package main; \n" +
                                                       "import `./imported`\n" +
                                                       "func test(){imported.LocallyImported()}");
  }

  public void testImportedFunctionsPriority() {
    myFixture.configureByText("a.go", "package main; \n" +
                                      "import `io`\n" +
                                      "func test(){ReadA<caret>}");
    failOnFileLoading();
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, "ReadAtLeast", "ReaderAt", "ReadAtLeastCustom", "ReaderAtCustom");
  }

  public void testImportedTypesPriority() {
    myFixture.configureByText("a.go", "package main; \n" +
                                      "import `io`\n" +
                                      "func test(ReadWriteSeeke<caret>){}");
    failOnFileLoading();
    myFixture.completeBasic();
    myFixture.assertPreferredCompletionItems(0, "ReadWriteSeeker", "ReadWriteSeekerCustom");
  }

  public void testDoNothingInsideSelector() {
    doTestVariants(
      "package main\n" +
      "import \"fmt\"\n" +
      "func test(){fmt.Sprintln().<caret>}", CompletionType.BASIC, 1, CheckType.EQUALS
    );
  }
  
  public void testDoNotRunAutoImportCompletionAfterDot() {
    doTestCompletion();
  }
  
  public void testDoNotRunAutoImportCompletionAfterDotAndSpace() {
    doTestCompletion();
  }

  public void testImports() {
    doTestInclude("package main; import \"<caret>", "fmt", "io");
  }

  public void testCaseInsensitiveTypeConversion() {
    doCheckResult("package main; import \"fmt\"; func test(){fmt.form<caret>}",
                  "package main; import \"fmt\"; func test(){fmt.Formatter(<caret>)}");
  }

  public void testCaseInsensitiveFunction() {
    doCheckResult("package main; import \"fmt\"; func test(){fmt.err<caret>}",
                  "package main; import \"fmt\"; func test(){fmt.Errorf(<caret>)}");
  }

  public void testCaseInsensitiveType() {
    doCheckResult("package main; import \"fmt\"; func test(fmt.form<caret>}",
                  "package main; import \"fmt\"; func test(fmt.Formatter<caret>}");
  }

  public void testPrintlnBuiltin() {
    doTestInclude("package main; func test(){pr<caret>}", "print", "println");
  }

  public void testNothingUnrelatedInsideSelector() {
    doTestEquals("package foo; type E struct {}; type B struct {E}; func (e E) foo() {}; func boo() {}" +
                 "func main() {B{}.E..<caret>foo()}", "foo");
  }

  public void testCompleteWithUnfinishedPackage() {
    doCheckResult("package main\n" +
                  "func main() {\n" +
                  "    f.fprintl<caret>\n" +
                  "}",
                  "package main\n" +
                  "import \"fmt\"\n" +
                  "func main() {\n" +
                  "    fmt.Fprintln()\n" +
                  "}");
  }
}
