// This is a generated file. Not intended for manual editing.
package com.goide.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GoForStatement extends GoStatement {

  @Nullable
  GoBlock getBlock();

  @Nullable
  GoForClause getForClause();

  @Nullable
  GoRangeClause getRangeClause();

  @NotNull
  PsiElement getFor();

}