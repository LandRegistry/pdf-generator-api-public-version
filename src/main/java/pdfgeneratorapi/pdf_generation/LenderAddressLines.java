package pdfgeneratorapi.pdf_generation;

import java.util.Arrays;

public class LenderAddressLines {

    private String[] lines;

    public String[] getLines() {
        return lines;
    }

    public void setLines(final String[] lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        final StringBuilder formatted = new StringBuilder();
        formatted.append(Arrays.toString(lines));
        return formatted.toString();
    }
}