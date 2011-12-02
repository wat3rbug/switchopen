// Created by: Douglas Gardiner
// Creation Date: Sun Oct 17 09:15:33 CDT 2010
// Update Date: Fri Nov 12 20:01:56 CST 2010
//
import javax.swing.*;
import java.awt.BorderLayout;
import net.sourceforge.napkinlaf.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;

/** The File takes care of all debug functions.  It logs debug
 *  output to a file in append mode.  It also opens a debug
 *  window for output so that you can see what is happening.
 * @author Douglas Gardiner
 */

public class DebugWindow {
    
    // attributes

    JFrame frame;
	JTextArea outputWindow = null;
    
    // constructors

    /**
     * Creates a debug object complete with debug window.  Uses Napkin 
	 * Look and Feel to give the 'under development' look. It also logs
	 * information to a debug log file.
     */

    public DebugWindow() {

        frame = new JFrame("Debug output");
        outputWindow = new JTextArea(15, 60);
        JPanel outputPanel = new JPanel();
        JScrollPane messageScroller = new JScrollPane(outputWindow);
        String looknfeelName = "net.sourceforge.napkinlaf.NapkinLookAndFeel";
        try {
            UIManager.setLookAndFeel(looknfeelName);
        } catch (ClassNotFoundException es) {
            es.printStackTrace();
        } catch (InstantiationException et) {
            System.out.println("cannot initialize what I cannot find");
        } catch (IllegalAccessException er) {
            System.out.println("Now sure what to say anymore");
        } catch (UnsupportedLookAndFeelException ev) {
            System.out.println("cannot do " + looknfeelName);
            ev.printStackTrace();
        }
        // build the screen
        
        outputPanel.add(messageScroller);
        outputPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(), "Debug Messages"));
        messageScroller.setHorizontalScrollBarPolicy(
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messageScroller.setVerticalScrollBarPolicy(
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        outputWindow.setLineWrap(true);
        outputWindow.setWrapStyleWord(true);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        JPanel contentPanel = new JPanel();
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(outputPanel);
        background.add(contentPanel);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(frame);
    
        // ugly hack, getBounds result in issues with both windows?
        frame.setLocation(0, 130);
        frame.pack();
        frame.setVisible(true);
        this.update(" ########################\n ### start of new log ###"
			+ "\n ########################\n");
    }
    // methods

    /**
     * Adds new message to the debug log and the debugging window.
     * @param newMessage String of the message that you want to output to 
	 * the debug window and the logging file.
     */

    public void update(String newMessage) {

        BufferedWriter writer = null;
		Throwable t = new Throwable();
		String title = "";
		try {
			StackTraceElement[] elements = t.getStackTrace();
			title = elements[1].getClassName();
		} finally {
			t = null;
		}
		String message = title + ": " + newMessage + "\n";
        try {       
            writer = new BufferedWriter(new FileWriter("debug.txt", true));
            writer.write(message);
            writer.flush();
        } catch (IOException ioe) {
             ioe.printStackTrace();
        } finally {                       // always close the file
             if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe2) {    
                    // just ignore it
                }
            }
        }   
        outputWindow.append(message);
    }
}
