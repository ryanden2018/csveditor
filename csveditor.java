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


class csveditor implements ActionListener {
    FileInputStream fin;
    FileOutputStream fout;
    JFrame frame;
    JMenuBar menuBar;
    JMenu fileMenu, editMenu, helpMenu;
    JMenuItem newItem, openItem, saveItem, saveAsItem, quitItem;
    JMenuItem addRowAboveItem, addRowBelowItem,
         addColLeftItem, addColRightItem,renameColumnItem,removeRowItem,
         removeColumnItem,moveRowUpItem,moveRowDownItem;
    JMenuItem aboutItem;
    JLabel status;
    JTable tbl;
    JScrollPane scroll;
    AugString line;
    String[] defaultColumnNames = {"A","B","C","D","E"};
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
        addRowAboveItem = new JMenuItem("Add Row Above");
        addRowBelowItem = new JMenuItem("Add Row Below");
        addColLeftItem = new JMenuItem("Add Column Left");
        addColRightItem = new JMenuItem("Add Column Right");
        renameColumnItem = new JMenuItem("Rename Column");
        removeRowItem = new JMenuItem("Remove Row");
        removeColumnItem = new JMenuItem("Remove Column");
        moveRowUpItem = new JMenuItem("Move Row Up");
        moveRowDownItem = new JMenuItem("Move Row Down");
        aboutItem = new JMenuItem("About");
        status = new JLabel(" ");
        model = new DefaultTableModel(defaultData,defaultColumnNames);
        tbl = new JTable(model);
        scroll = new JScrollPane(tbl);
        frame = new JFrame("csveditor"); // BorderLayout

        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        quitItem.addActionListener(this);
        addRowAboveItem.addActionListener(this);
        addRowBelowItem.addActionListener(this);
        addColLeftItem.addActionListener(this);
        addColRightItem.addActionListener(this);
        renameColumnItem.addActionListener(this);
        removeRowItem.addActionListener(this);
        removeColumnItem.addActionListener(this);
        moveRowUpItem.addActionListener(this);
        moveRowDownItem.addActionListener(this);
        aboutItem.addActionListener(this);

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.add(quitItem);

        editMenu.add(addRowAboveItem);
        editMenu.add(addRowBelowItem);
        editMenu.add(addColLeftItem);
        editMenu.add(addColRightItem);
        editMenu.add(renameColumnItem);
        editMenu.add(removeRowItem);
        editMenu.add(removeColumnItem);
        editMenu.add(moveRowUpItem);
        editMenu.add(moveRowDownItem);

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
                  "Create new file? Any unsaved work will be lost.","No",this);
                break;
            case "Open":
                break;
            case "Save":
                break;
            case "Save As...":
                break;
            case "Exit":
                new ExitPrompt(
                    "Exit the program? Any unsaved work will be lost.","No");
                break;
            case "Add Row Above":
                break;
            case "Add Row Below":
                break;
            case "Add Column Left":
                break;
            case "Add Column Right":
                break;
            case "Rename Column":
                break;
            case "Remove Row":
                break;
            case "Remove Column":
                break;
            case "Move Row Up":
                break;
            case "Move Row Down":
                break;
            case "About":
                break;
        }
    }

    public void resetData() {
        model.setDataVector(defaultData,defaultColumnNames);
    }

    public void setStatus(String str) {
        status.setText(str);
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

	/*
    public static String makeline(String line, String color) {
        String str = "";
        String strpre = "";
        String strpost = "";
        String[] fields;

        strpre += "<tr bgcolor=\'";
        strpre += color;
        strpre += "\'>\n";
        strpost += "</tr>\n";

        fields = extract_fields(line);

        for(int i=0; i<fields.length; i++) {
            if (fields[i] == "") {
                str += "<td></td>\n";
            } else {
                str += "<td>";
                str += fields[i];
                str += "</td>\n";
            }
        }

        if( str == "") {
            return "";
        }

        return (strpre + str + strpost);
    }
	*/

    /*public static String escape_html(int c) {
        String str = "";
        if( c == '&') {
            str += "&amp;";
        } else if (c == '<') {
            str += "&lt;";
        } else if (c == '>') {
            str += "&gt;";
        } else {
            str += Character.toString(c);
        }
        return str;
    }*/

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
            "Exit the program? Any unsaved work will be lost.","No");
    }
}

