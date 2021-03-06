package org.robiul.toolbox.plugins.linesorganizer;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SortAction
 *
 * @author <a href="mailto:md.robiul@gmail.com">Robiul Robi</a>
 * @version $Id$
 */
public class SortAction extends TextComponentEditorAction {
  protected SortAction() {
    super(new Handler());
  }

  private static class Handler extends EditorWriteActionHandler {
    public void executeWriteAction(Editor editor, DataContext dataContext) {
      final Document doc = editor.getDocument();

      int startLine;
      int endLine;

      boolean hasSelection = editor.getSelectionModel().hasSelection();
      if (hasSelection) {
        startLine = doc.getLineNumber(editor.getSelectionModel().getSelectionStart());
        endLine = doc.getLineNumber(editor.getSelectionModel().getSelectionEnd());
        if (doc.getLineStartOffset(endLine) == editor.getSelectionModel().getSelectionEnd()) {
          endLine--;
        }
      } else {
        startLine = 0;
        endLine = doc.getLineCount() - 1;
      }

      // Ignore last lines (usually one) which are only '\n'
      endLine = ignoreLastEmptyLines(doc, endLine);

      if (startLine >= endLine) {
        return;
      }

      // Extract text as a list of lines
      String[] lines = extractLines(doc, startLine, endLine);

      String output = sortLines(lines);

      StringBuilder sortedText = new StringBuilder();

      sortedText.append(output);

      // Remove last \n is sort has been applied on whole file and the file did not end with \n
      if (!hasSelection) {
        CharSequence charsSequence = doc.getCharsSequence();
        if (charsSequence.charAt(charsSequence.length() - 1) != '\n') {
          sortedText.deleteCharAt(sortedText.length() - 1);
        }
      }

      // Replace text
      int startOffset = doc.getLineStartOffset(startLine);
      int endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine);

      editor.getDocument().replaceString(startOffset, endOffset, sortedText);
    }

    private String sortLines(String[] lines) {

      String output = "";

      String currentAncestor;

      String previousAncestor = null;

      Arrays.sort(lines);

      for (String line : lines) {

        line = line.trim();

        String ancestor[] = line.split("[\\\\]");

        String tempArray[] = Arrays.copyOf(ancestor, ancestor.length - 1);

        currentAncestor = String.join("_", tempArray).trim();

        if (currentAncestor.equals(previousAncestor) == false) {

          output += "\n";
        }

        output += line + "\n";

        previousAncestor = currentAncestor;
      }

      return output;
    }

    private int ignoreLastEmptyLines(Document doc, int endLine)
    {
      while (endLine >= 0)
      {
        if (doc.getLineEndOffset(endLine) > doc.getLineStartOffset(endLine))
        {
          return endLine;
        }

        endLine--;
      }

      return -1;
    }

    private String[] extractLines(Document doc, int startLine, int endLine)
    {
      List<String> lines = new ArrayList<String>(endLine - startLine);
      for (int i = startLine; i <= endLine; i++)
      {
        String line = extractLine(doc, i);

        lines.add(line);
      }

      String[] stringArray = lines.toArray(ArrayUtils.EMPTY_STRING_ARRAY);

      return stringArray;
    }

    private String extractLine(Document doc, int lineNumber)
    {
      int lineSeparatorLength = doc.getLineSeparatorLength(lineNumber);
      int startOffset = doc.getLineStartOffset(lineNumber);
      int endOffset = doc.getLineEndOffset(lineNumber) + lineSeparatorLength;

      String line = doc.getCharsSequence().subSequence(startOffset, endOffset).toString();

      // If last line has no \n, add it one
      // This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
      // with \n... This is fixed after.
      if (lineSeparatorLength == 0)
      {
        line += "\n";
      }

      return line;
    }
  }
}
