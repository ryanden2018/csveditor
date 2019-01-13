/* csveditor.java
 *
 * Usage: java csveditor
 *
 * A Java Swing-based utility which allows editing of CSV files.
 * 
 * Inspired by csv2html.py from chapter 2 of
 * Mark Summerfield's book "Programming in Python 3" 2/e.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;

class AugString {
    String str;
    boolean lastLine;
}

class Lines {
    // read a line from fin.
    public static AugString readLine(FileInputStream fin) {
        AugString str = new AugString();
        int c = -1;

        str.str = "";
        str.lastLine = false;

        try {
            do {
                c = fin.read();
                if((c != -1) && (c != '\n')) {
                    str.str = str.str + Character.toString(c);
                }
            } while((c != -1) && (c != '\n'));
        } catch (IOException exc) {
            System.out.println("I/O error");
        }

        if(c == -1) {
            str.lastLine = true;
        }

        return(str);
    }
    
    // write a line to fout
    public static void writeLine(FileOutputStream fout, String str) {
        try {
            for(int i=0; i < str.length();i++) {
                fout.write(str.charAt(i));
            }
            fout.write('\n');
        } catch (IOException exc) {
            System.out.println("I/O error");
        }
    }
}


class NotifBox implements ActionListener {
    JFrame frame;
    JPanel buttonPanel;
    JButton okButton;
    JLabel msgLabel;
    
    NotifBox(String msg) {
        frame = new JFrame("Message");
        buttonPanel = new JPanel();
        okButton = new JButton("OK");
        msgLabel = new JLabel(msg,SwingConstants.CENTER);

        okButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.setLayout(new FlowLayout());

        frame.add(msgLabel,BorderLayout.CENTER);
        frame.add(buttonPanel,BorderLayout.SOUTH);
        frame.setSize(400,150);
        frame.setVisible(true);
        frame.setLocation(300,300);
    }
 
    public void actionPerformed(ActionEvent e) {
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }
}

class InputPrompt implements ActionListener {
    JFrame frame;
    JButton okButton;
    JTextField field;
    JLabel queryLabel;
    JPanel buttonPanel;
    JPanel textFieldPanel;

    InputPrompt(String msg) {
        frame = new JFrame("Prompt");
        buttonPanel = new JPanel();
        okButton = new JButton("OK");
        textFieldPanel = new JPanel();
        field = new JTextField(null,"",25);
        queryLabel = new JLabel(msg,SwingConstants.CENTER);

        okButton.addActionListener(this);

        buttonPanel.add(okButton);
        buttonPanel.setLayout(new FlowLayout());

        textFieldPanel.add(field);
        textFieldPanel.setLayout(new FlowLayout());

        frame.add(queryLabel,BorderLayout.NORTH);
        frame.add(textFieldPanel,BorderLayout.CENTER);
        frame.add(buttonPanel,BorderLayout.SOUTH);
        frame.setSize(400,150);
        frame.setVisible(true);
        frame.setLocation(300,300);
    }

    public void actionPerformed(ActionEvent e) {
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }
}


class RenameColumnBox extends InputPrompt {
    int index;
    csveditor prog;

    RenameColumnBox(int index,csveditor prog) {
        super("Enter new column name");
        this.index = index;
        this.prog = prog;
    }

    public void actionPerformed(ActionEvent e) {
        String newname = field.getText();
        prog.setColName(index,newname);
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }
}


class YesNoPrompt implements ActionListener {
    JFrame frame;
    JPanel buttonPanel;
    JButton yesButton;
    JButton noButton;
    JLabel queryLabel;
    String answer;
    
    YesNoPrompt(String msg,String defaultAnswer) {
        answer = defaultAnswer;
        frame = new JFrame("Prompt");
        buttonPanel = new JPanel();
        yesButton = new JButton("Yes");
        noButton = new JButton("No");
        queryLabel = new JLabel(msg,SwingConstants.CENTER);

        yesButton.addActionListener(this);
        noButton.addActionListener(this);

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        buttonPanel.setLayout(new FlowLayout());

        frame.add(queryLabel,BorderLayout.CENTER);
        frame.add(buttonPanel,BorderLayout.SOUTH);
        frame.setSize(400,150);
        frame.setVisible(true);
        frame.setLocation(300,300);
    }
 
    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()) {
            case "Yes":
                answer = "Yes";
                break;
            case "No":
                answer = "No";
                break;
        }
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }

    public String getAnswer() {
        return answer;
    }
}


class ExitPrompt extends YesNoPrompt {
    ExitPrompt(String msg,String defaultAnswer) {
        super(msg,defaultAnswer);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand() == "Yes") {
            System.exit(0);
        }
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }
}



class CreateFilePrompt extends YesNoPrompt {
    csveditor prog;

    CreateFilePrompt(String msg,String defaultAnswer,csveditor prog) {
        super(msg,defaultAnswer);
        this.prog = prog;
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand() == "Yes") {
            prog.resetData();
        }
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
    }
}


class OpenFilePrompt extends YesNoPrompt {
    csveditor prog;

    OpenFilePrompt(String msg,String defaultAnswer,csveditor prog) {
        super(msg,defaultAnswer);
        this.prog = prog;
    }

    public void actionPerformed(ActionEvent e) {
        frame.dispatchEvent(new WindowEvent(frame,WindowEvent.WINDOW_CLOSING));
        if(e.getActionCommand() == "Yes") {
            prog.openFile();
        }
    }
}


class csveditor implements ActionListener {
    FileInputStream fin;
    FileOutputStream fout;
    JFrame frame;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, helpMenu;
    JMenuItem newItem, openItem, saveItem, saveAsItem, quitItem;
    JMenuItem addRowItem, addRowAboveItem, addRowBelowItem,
         addColItem, renameColumnItem,removeRowItem,
         removeColumnItem,moveRowUpItem,moveRowDownItem,
         moveColLeftItem,moveColRightItem;
    JMenuItem aboutItem;
    JLabel status;
    JTable tbl;
    JScrollPane scroll;
    AugString line;
    String[] defaultColumnNames = {"A","B","C","D","E"};
    String defaultNewColumnName = "New Column";
    Object[][] defaultData = {{"","","","",""},{"","","","",""},
            {"","","","",""},{"","","","",""},{"","","","",""}};
    DefaultTableModel model;

    csveditor() {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        helpMenu = new JMenu("Help");
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        saveAsItem = new JMenuItem("Save As...");
        quitItem = new JMenuItem("Exit");
        addRowItem = new JMenuItem("Add Row at End");
        addRowAboveItem = new JMenuItem("Add Row Above");
        addRowBelowItem = new JMenuItem("Add Row Below");
        addColItem = new JMenuItem("Add Column");
        renameColumnItem = new JMenuItem("Rename Column");
        removeRowItem = new JMenuItem("Remove Row");
        removeColumnItem = new JMenuItem("Remove Column");
        moveRowUpItem = new JMenuItem("Move Row Up");
        moveRowDownItem = new JMenuItem("Move Row Down");
        moveColLeftItem = new JMenuItem("Move Column Left");
        moveColRightItem = new JMenuItem("Move Column Right");
        aboutItem = new JMenuItem("About");
        status = new JLabel(" ");
        model = new DefaultTableModel(defaultData,defaultColumnNames);
        tbl = new JTable(model);
        scroll = new JScrollPane(tbl);
        frame = new JFrame("csveditor"); // BorderLayout

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getTableHeader().setReorderingAllowed(false);

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        quitItem.addActionListener(this);
        addRowItem.addActionListener(this);
        addRowAboveItem.addActionListener(this);
        addRowBelowItem.addActionListener(this);
        addColItem.addActionListener(this);
        renameColumnItem.addActionListener(this);
        removeRowItem.addActionListener(this);
        removeColumnItem.addActionListener(this);
        moveRowUpItem.addActionListener(this);
        moveRowDownItem.addActionListener(this);
        moveColLeftItem.addActionListener(this);
        moveColRightItem.addActionListener(this);
        aboutItem.addActionListener(this);

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(quitItem);

        editMenu.add(addRowItem);
        editMenu.add(addRowAboveItem);
        editMenu.add(addRowBelowItem);
        editMenu.add(addColItem);
        editMenu.add(renameColumnItem);
        editMenu.add(removeRowItem);
        editMenu.add(removeColumnItem);
        editMenu.add(moveRowUpItem);
        editMenu.add(moveRowDownItem);
        editMenu.add(moveColLeftItem);
        editMenu.add(moveColRightItem);

        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        frame.addWindowListener(new TopFrameListener());
        frame.add(menuBar,BorderLayout.NORTH);
        frame.add(scroll,BorderLayout.CENTER);
        frame.add(status,BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(600,600);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        switch(e.getActionCommand()) {
            case "New":
                new CreateFilePrompt(
                  "Create new file? Any unsaved changes will be lost.","No",this);
                break;
            case "Open":
                new OpenFilePrompt(
                   "Open a file? Any unsaved changes will be lost.","No",this);
                break;
            case "Save":
                // TODO
                break;
            case "Save As...":
                // TODO
                break;
            case "Exit":
                new ExitPrompt(
                    "Exit the program? Any unsaved changes will be lost.","No");
                break;
            case "Add Row at End":
                addRow();
                break;
            case "Add Row Above":
                if (tbl.getSelectedRow() != -1) {
                    insertEmptyRow(tbl.getSelectedRow());
                } else {
                    new NotifBox("No row selected");
                }
                break;
            case "Add Row Below":
                if (tbl.getSelectedRow() != -1) {
                    insertEmptyRow(tbl.getSelectedRow()+1);
                } else {
                    new NotifBox("No row selected");
                }
                break;
            case "Add Column":
                addEmptyColumn();
                break;
            case "Rename Column":
                if (tbl.getSelectedColumn() != -1) {
                    new RenameColumnBox(tbl.getSelectedColumn(),this);
                } else {
                    new NotifBox("No column selected");
                }
                break;
            case "Remove Row":
                if (tbl.getSelectedRow() != -1) {
                    model.removeRow(tbl.getSelectedRow());
                } else {
                    new NotifBox("No row selected");
                }
                break;
            case "Remove Column":
                if(tbl.getSelectedColumn() != -1) {
                    removeCol(tbl.getSelectedColumn());
                } else {
                    new NotifBox("No column selected");
                }
                break;
            case "Move Row Up":
                if (tbl.getSelectedRow() != -1) {
                    moveRowUp(tbl.getSelectedRow());
                } else {
                    new NotifBox("No row selected");
                }
                break;
            case "Move Row Down":
                if (tbl.getSelectedRow() != -1) {
                    moveRowUp(tbl.getSelectedRow()+1);
                } else {
                    new NotifBox("No row selected");
                }
                break;
            case "Move Column Left":
                if(tbl.getSelectedColumn() != -1) {
                    moveColumnLeft(tbl.getSelectedColumn());
                } else {
                    new NotifBox("No column selected");
                }
                break;
            case "Move Column Right":
                if(tbl.getSelectedColumn() != -1) {
                    moveColumnLeft(tbl.getSelectedColumn()+1);
                } else {
                    new NotifBox("No column selected");
                }
                break;
            case "About":
                new NotifBox("csveditor - a simple GUI utility for editing CSV files");
                break;
            default:
                new NotifBox("Unexpected error");
                break;
        }
    }

    public void resetData() {
        model.setDataVector(defaultData,defaultColumnNames);
    }

    public void setStatus(String str) {
        status.setText(str);
    }

    public void insertEmptyRow(int index) {
        int numCols = model.getColumnCount();
        Object[] emptyRow = new Object[numCols];
        for(int i = 0;i<numCols;i++) {
            emptyRow[i] = "";
        }
        model.insertRow(index,emptyRow);
    }

    public void addEmptyColumn() {
        int numRows = model.getRowCount();
        Object[] emptyCol = new Object[numRows];
        for(int i = 0;i<numRows;i++) {
            emptyCol[i] = "";
        }
        model.addColumn(defaultNewColumnName,emptyCol);
    }

    public void moveRowUp(int index) {
        try {
            model.moveRow(index,index,index-1);
        } catch (ArrayIndexOutOfBoundsException exc) {
            new NotifBox("Invalid move");
        }
    }

    public void moveColumnLeft(int index) {
        int numCols = model.getColumnCount();
        int numRows = model.getRowCount();
        Object[] colNames = new String[numCols];

        for(int i=0; i<numCols; i++) {
            colNames[i] = model.getColumnName(i);
        }
        
        try {
            Object tmp;
            
            // switch column names
            tmp = colNames[index-1];
            colNames[index-1] = colNames[index];
            colNames[index] = tmp;
            model.setColumnIdentifiers(colNames);

            // switch data
            for(int i=0; i<numRows; i++) {
                tmp = model.getValueAt(i,index-1);
                model.setValueAt(model.getValueAt(i,index),i,index-1);
                model.setValueAt(tmp,i,index);
            }
        } catch (ArrayIndexOutOfBoundsException exc) {
            new NotifBox("Invalid move");
        }
    }

    public void addRow() {
        int numCols = model.getColumnCount();
        Object[] emptyRow = new Object[numCols];
        for(int i = 0;i<numCols;i++) {
            emptyRow[i] = "";
        }
        model.addRow(emptyRow);
    }


    public void setColName(int index,String newname) {
        int numCols = model.getColumnCount();
        int numRows = model.getRowCount();
        Object[] colNames = new String[numCols];

        for(int i=0; i<numCols; i++) {
            colNames[i] = model.getColumnName(i);
        }

        colNames[index] = newname;
        model.setColumnIdentifiers(colNames);
    }

    public void removeCol(int index) {
        int numCols = model.getColumnCount();

        for(int i=index+1;i<numCols; i++) {
            moveColumnLeft(i);
        }

        model.setColumnCount(numCols-1);
    }

    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        int returnValue = chooser.showOpenDialog(null);
        String filepath;

        if(returnValue == JFileChooser.APPROVE_OPTION) {
            filepath = chooser.getSelectedFile().getPath();
        } else {
            return;
        }
        
        // open the file
        try {
            fin = new FileInputStream(filepath);
        } catch (FileNotFoundException exc) {
            new NotifBox("Error: file not found");
            return;
        }

        // read the file
        readFile();

        // close the file
        try {
            if (fin != null) {
                fin.close();
            }
        } catch (IOException exc) {
            new NotifBox("IO Error");
        }
    }
        
    private void readFile() {
        AugString line;
        String[] fields;
        boolean first_line = true;
        int numCols = 0;

        while(true) {
            line = Lines.readLine(fin);

            fields = extract_fields(line.str);
            
            // is it the first line?
            if(first_line == true) {
                numCols = fields.length;
                model.setColumnCount(numCols);
                model.setDataVector(new Object[0][numCols], fields);
                first_line = false;
            } else {
                String[] new_row = new String[numCols];
                for(int i=0;i<new_row.length && i<fields.length;i++) {
                    new_row[i] = fields[i];
                }
                if(line.str != "") {
                    model.addRow(new_row);
                }
            }

            if(line.lastLine == true) {
                break;
            }
        }
    }

        // open the file
        /*try {
            fin = new FileInputStream(args[0]);
            fout = new FileOutputStream(args[1]);
        } catch (FileNotFoundException exc) {
            System.out.println("Error: file not found");
            return;
        }*/


        // read and process line-by-line
        //while(true) {
          //  line = Lines.readLine(fin);
            
		/*
            if (count == 0) {
                color = "lightgreen";
            } else if (count%2 == 0) {
                color = "white";
            } else {
                color = "lightyellow";
            } */
            
             // line_html = makeline(line.str,color);

           // if( line_html != "" ) {
                //Lines.writeLine(fout,line_html);
            // }
            
            //count += 1;
            //if (line.lastLine == true) {
            //    break;
            //}
        //}

        // print closing string
        //Lines.writeLine(fout,close_str);

        // close the file
        //try {
        //    if (fin != null) {
        //        fin.close();
        //    }
        //    if (fout != null) {
        //        fout.close();
        //    }
        //} catch (IOException exc) {
        //    System.out.println("Error closing files");
        //}
   // }




    public static String[] extract_fields(String line) {
        String[] fields = new String[0];
        String field = "";
        int quote = -1;

        for( int i = 0; i < line.length(); i++) {
            int c = line.charAt(i);

            // add a character unless it is a quote
            if ((c == '\'') || (c == '\"')) {
                if (quote == -1) {
                    quote = c;
                } else if (quote == c) {
                    quote = -1;
                } else {
                    field += Character.toString(c);
               }
                continue;
            }

            if ((quote == -1) && (c == ',')) { // end of a field
                fields = addString(fields,field);
                field = "";
            } else {
                field += Character.toString(c);
            }
        }
        if( field != "") {
            fields = addString(fields,field);
        }

        return fields;
    }

    public static String[] addString(String[] fields, String field) {
        String[] fields2 = new String[fields.length+1];

        for(int i = 0;i<fields.length; i++) {
            fields2[i] = fields[i];
        }

        fields2[fields.length] = field;

        return fields2;
    }

    public static void main(String args[]) {
        new csveditor();
    }
}


class TopFrameListener extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
        new ExitPrompt(
            "Exit the program? Any unsaved changes will be lost.","No");
    }
}

