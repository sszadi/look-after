package com.hfad.lookafter.content;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.List;

public class PageSplitter {
    private final int pageWidth;
    private final int pageHeight;
    private final float lineSpacingMultiplier;
    private final float lineSpacingExtra;
    private final List<CharSequence> pages = new ArrayList<CharSequence>();
    private SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

    public PageSplitter(int pageWidth, int pageHeight, float lineSpacingMultiplier, float lineSpacingExtra) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.lineSpacingMultiplier = lineSpacingMultiplier;
        this.lineSpacingExtra = lineSpacingExtra;
    }

    public void append(CharSequence charSequence) {
        pages.add("zero");
        spannableStringBuilder.append(charSequence);
        split();
    }

    public void split() {
        StaticLayout staticLayout = new StaticLayout(
                spannableStringBuilder,
                new TextPaint(),
                pageWidth,
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier,
                lineSpacingExtra,
                false
        );
        int startLine = 0;
        while (startLine < staticLayout.getLineCount()) {
            int startLineTop = staticLayout.getLineTop(startLine);
            int endLine = staticLayout.getLineForVertical(startLineTop + pageHeight);
            int endLineBottom = staticLayout.getLineBottom(endLine);
            int lastFullyVisibleLine;
            if (endLineBottom > startLineTop + pageHeight)
                lastFullyVisibleLine = endLine - 1;
            else
                lastFullyVisibleLine = endLine;
            int startOffset = staticLayout.getLineStart(startLine);
            int endOffset = staticLayout.getLineEnd(lastFullyVisibleLine);
            pages.add(spannableStringBuilder.subSequence(startOffset, endOffset));
            startLine = lastFullyVisibleLine + 1;
        }
    }

    public List<CharSequence> getPages() {
        return pages;
    }
}
