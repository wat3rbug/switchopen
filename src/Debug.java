import javax.swing.*;
import java.awt.*;
import net.sourceforge.napkinlaf.*;
import java.io.*;

public class Debug {

    // class variables

    JTextArea output = null;
    JFrame frame;
    // constructors

    public Debug() {

        frame = new JFrame("Debug output");
        output = new JTextArea(15,45);
        JPanel outputPanel = new JPanel();
        JScrollPane messageScroller = new JScrollPane(output);
        String destination = "net.sourceforge.napkinlaf.NapkinLookAndFeel";
        try {
            UIManager.setLookAndFeel(destination);
        } catch (ClassNotFoundException es) {
            es.printStackTrace();
        } catch (InstantiationException et) {
            System.out.println("cannot initialize what I cannot find");
        } catch (IllegalAccessException er) {
            System.out.println("Now sure what to say anymore");
        } catch (UnsupportedLookAndFeelException ev) {
            System.out.println("cannot do " + destination);
            ev.printStackTrace();
        }
        // build the screen
        
        outputPanel.add(messageScroller);
        outputPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Debug Messages"));
        messageScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messageScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        JPanel contents = new JPanel();
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        contents.add(outputPanel);
        background.add(contents);
        frame.getContentPane().add(background);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        SwingUtilities.updateComponentTreeUI(frame);
    
        // ugly hack, getBounds result in issues with both windows?
        frame.setLocation(0,130);
        frame.pack();
        frame.setVisible(true);
    }
    // methods

    public void update(String newMessage) {

		BufferedWriter writer = null;
		try {		
			writer = new BufferedWriter(new FileWriter("debug.txt", true));
			writer.write(newMessage + "\n" );
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
        output.append(newMessage + "\n");
    }
}
