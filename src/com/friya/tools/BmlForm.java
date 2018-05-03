// 
// Decompiled by Procyon v0.5.30
// 

package com.friya.tools;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BmlForm
{
    private static Logger logger;
    private final StringBuffer buf;
    private static final String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
    private int openBorders;
    private int openCenters;
    private int openVarrays;
    private int openScrolls;
    private int openHarrays;
    private int openTrees;
    private int openRows;
    private int openColumns;
    private int openTables;
    private int indentNum;
    private boolean beautify;
    private boolean closeDefault;
    
    static {
        BmlForm.logger = Logger.getLogger(BmlForm.class.getName());
    }
    
    public BmlForm() {
        this.buf = new StringBuffer();
        this.openBorders = 0;
        this.openCenters = 0;
        this.openVarrays = 0;
        this.openScrolls = 0;
        this.openHarrays = 0;
        this.openTrees = 0;
        this.openRows = 0;
        this.openColumns = 0;
        this.openTables = 0;
        this.indentNum = 0;
        this.beautify = false;
        this.closeDefault = false;
    }
    
    public BmlForm(final String formTitle) {
        this.buf = new StringBuffer();
        this.openBorders = 0;
        this.openCenters = 0;
        this.openVarrays = 0;
        this.openScrolls = 0;
        this.openHarrays = 0;
        this.openTrees = 0;
        this.openRows = 0;
        this.openColumns = 0;
        this.openTables = 0;
        this.indentNum = 0;
        this.beautify = false;
        this.closeDefault = false;
        this.addDefaultHeader(formTitle);
    }
    
    public void addDefaultHeader(final String formTitle) {
        if (this.closeDefault) {
            return;
        }
        this.beginBorder();
        this.beginCenter();
        this.addBoldText(formTitle);
        this.endCenter();
        this.beginScroll();
        this.beginVerticalFlow();
        this.closeDefault = true;
    }
    
    public void beginBorder() {
        this.buf.append(this.indent("border{"));
        ++this.indentNum;
        ++this.openBorders;
    }
    
    public void endBorder() {
        --this.indentNum;
        this.buf.append(this.indent("}"));
        --this.openBorders;
    }
    
    public void beginCenter() {
        this.buf.append(this.indent("center{"));
        ++this.indentNum;
        ++this.openCenters;
    }
    
    public void endCenter() {
        --this.indentNum;
        this.buf.append(this.indent("};null;"));
        --this.openCenters;
    }
    
    public void beginVerticalFlow() {
        this.buf.append(this.indent("varray{rescale=\"true\";"));
        ++this.indentNum;
        ++this.openVarrays;
    }
    
    public void endVerticalFlow() {
        --this.indentNum;
        this.buf.append(this.indent("}"));
        --this.openVarrays;
    }
    
    public void beginScroll() {
        this.buf.append(this.indent("scroll{vertical=\"true\";horizontal=\"false\";"));
        ++this.indentNum;
        ++this.openScrolls;
    }
    
    public void endScroll() {
        --this.indentNum;
        this.buf.append(this.indent("};null;null;"));
        --this.openScrolls;
    }
    
    public void beginHorizontalFlow() {
        this.buf.append(this.indent("harray {"));
        ++this.indentNum;
        ++this.openHarrays;
    }
    
    public void endHorizontalFlow() {
        --this.indentNum;
        this.buf.append(this.indent("}"));
        --this.openHarrays;
    }
    
    public void beginTable(final int rowCount, final String[] columns) {
        this.buf.append(this.indent("table {rows=\"" + rowCount + "\"; cols=\"" + columns.length + "\";"));
        ++this.indentNum;
        for (final String c : columns) {
            this.addLabel(c);
        }
        --this.indentNum;
        ++this.indentNum;
        ++this.openTables;
    }
    
    public void endTable() {
        --this.indentNum;
        this.buf.append(this.indent("}"));
        --this.openTables;
    }
    
    public void addBoldText(final String text, final String... args) {
        this.addText(text, "bold", args);
    }
    
    public void addHidden(final String name, final String val) {
        this.buf.append(this.indent("passthrough{id=\"" + name + "\";text=\"" + val + "\"}"));
    }
    
    public void addText(final String text, final String... args) {
        this.addText(text, "", args);
    }
    
    private String indent(final String s) {
        return this.beautify ? (String.valueOf(this.getIndentation()) + s + "\r\n") : s;
    }
    
    private String getIndentation() {
        if (this.indentNum > 0) {
            return "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t".substring(0, this.indentNum);
        }
        return "";
    }
    
    public void addRaw(final String s) {
        this.buf.append(s);
    }
    
    public void addImage(final String url, final int height, final int width) {
        this.addImage(url, height, width, "");
    }
    
    public void addImage(final String url, final int height, final int width, final String tooltip) {
        this.buf.append("image{src=\"");
        this.buf.append(url);
        this.buf.append("\";size=\"");
        this.buf.append(String.valueOf(height) + "," + width);
        this.buf.append("\";text=\"" + tooltip + "\"}");
    }
    
    public void addLabel(final String text) {
        this.buf.append("label{text='" + text + "'};");
    }
    
    public void addInput(final String id, final int maxChars, final String defaultText) {
        this.buf.append("input{id='" + id + "';maxchars='" + maxChars + "';text=\"" + defaultText + "\"};");
    }
    
    private void addText(final String text, final String type, final String... args) {
        final String[] lines = text.split("\n");
        String[] array;
        for (int length = (array = lines).length, i = 0; i < length; ++i) {
            final String l = array[i];
            if (this.beautify) {
                this.buf.append(this.getIndentation());
            }
            this.buf.append("text{");
            if (!type.equals("")) {
                this.buf.append("type='" + type + "';");
            }
            this.buf.append("text=\"");
            this.buf.append(String.format(l, (Object[])args));
            this.buf.append("\"}");
            if (this.beautify) {
                this.buf.append("\r\n");
            }
        }
    }
    
    public void addButton(final String name, final String id) {
        this.buf.append(this.indent("button{text='  " + name + "  ';id='" + id + "'}"));
    }
    
    @Override
    public String toString() {
        if (this.closeDefault) {
            this.endVerticalFlow();
            this.endScroll();
            this.endBorder();
            this.closeDefault = false;
        }
        if (this.openCenters != 0 || this.openVarrays != 0 || this.openScrolls != 0 || this.openHarrays != 0 || this.openBorders != 0 || this.openTrees != 0 || this.openRows != 0 || this.openColumns != 0 || this.openTables != 0) {
            BmlForm.logger.log(Level.SEVERE, "While finalizing BML unclosed (or too many closed) blocks were found (this will likely mean the BML will not work!): center: " + this.openCenters + " vert-flows: " + this.openVarrays + " scroll: " + this.openScrolls + " horiz-flows: " + this.openHarrays + " border: " + this.openBorders + " trees: " + this.openTrees + " rows: " + this.openRows + " columns: " + this.openColumns + " tables: " + this.openTables);
        }
        return this.buf.toString();
    }
}
