package DigitalClock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JFrame;

import java.awt.AWTException;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.PlainDocument;


import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class DigitalClock extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> alarmList;
	private List<String> noteList;
	private JList<String> list;
	private DefaultListModel<String> dlm;
	private TrayIcon trayIcon;
	private SystemTray tray;
	private Calendar cal;
	private static final String DEFAULT_NOTE = "Note:";
	private JTextField textField;
	private JTextArea noteArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					DigitalClock frame = new DigitalClock();
					frame.setVisible(true);
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DigitalClock() {

		TimeZone timeZone = TimeZone.getDefault();
		Timer timerForRefresh = new Timer();

		setResizable(false);
		setTitle("Digital Clock");
		setBackground(Color.WHITE);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 550);
		setSize(490, 380);
		getContentPane().setLayout(new BorderLayout(0, 0));
		setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
		
		//Making popup menu for tray icon
		PopupMenu popupMenu = new PopupMenu();
		MenuItem openItem = new MenuItem("Show Digital Clock");
        MenuItem exitItem = new MenuItem("Exit");
        popupMenu.add(openItem);     
        popupMenu.addSeparator();
        popupMenu.add(exitItem);
        
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setVisible(true);
            }
        });
        
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
		//Making tray icon
		tray = SystemTray.getSystemTray();
		trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("icon.png"));
		trayIcon.setPopupMenu(popupMenu);
		trayIcon.setToolTip("Digital Clock");
        trayIcon.setImageAutoSize(true);
        try {
			tray.add(trayIcon);
		} catch (AWTException e1) {
		}
        
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setBackground(Color.WHITE);

		alarmList = readAlarmList("list.data");
		noteList = readNoteList("note.data");
		// Making alarms display list
		dlm = readFileDefaultListModel("list.data");

		list = new JList<>(dlm);
		list.setVisibleRowCount(-1);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (!dlm.isEmpty() && !alarmList.isEmpty() && !noteList.isEmpty()) {
					int[] selectedIndices = list.getSelectedIndices();
					if (selectedIndices.length != 0) {
						if (selectedIndices.length == 1) {
							noteArea.setText(noteList.get(list.getSelectedIndex()));
						} else {
							noteArea.setText(""); // Show nothing in group selection
						}
						// show the latest alarm
						textField.setText(dlm.getElementAt(selectedIndices[0]).substring(0, 8));
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent arg0) {// Not use

			}

			@Override
			public void mouseExited(MouseEvent arg0) {// Not use

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {// Not use
			}

			@Override
			public void mouseClicked(MouseEvent e) {// Not use

			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removeAlarm();
					// reset note area
					noteArea.setText(DEFAULT_NOTE);
				}
			}
		});

		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(200, 130));

		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setBounds(10, 8, 260, 40);
		listScrollPane.setPreferredSize(new Dimension(210, 40));
		panel.add(listScrollPane);

		textField = new JTextField();
		textField.setBounds(294, 24, 86, 24);
		textField.setPreferredSize(new Dimension(20, 22));
		textField.setText("00:00 AM");
		textField.setColumns(10);
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				//
				noteArea.setText(DEFAULT_NOTE);
				//
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					addAlarm();
			}
		});
		// Add alarm filter
		PlainDocument docTextField = (PlainDocument) textField.getDocument();
		docTextField.setDocumentFilter(new AlarmTimeFilter());
		panel.add(textField);

		noteArea = new JTextArea();
		noteArea.setBorder(UIManager.getBorder("TextField.border"));
		noteArea.setBounds(10, 56, 370, 63);
		panel.add(noteArea);
		noteArea.setColumns(10);
		noteArea.setFont(textField.getFont());
		noteArea.setText(DEFAULT_NOTE);
		noteArea.setToolTipText("Your note goes here");
		noteArea.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				if (noteArea.getText().equals(DEFAULT_NOTE) || noteArea.getText().equals("Nothing here"))
					noteArea.setText(""); // clear noteArea when there is no note
			}

			@Override
			public void mouseExited(MouseEvent arg0) {

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {

			}
		});

		JButton btnAddAlarm = new JButton("Add");
		btnAddAlarm.setBounds(399, 25, 65, 24);
		btnAddAlarm.setPreferredSize(new Dimension(65, 24));
		btnAddAlarm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addAlarm();
			}
		});
		panel.add(btnAddAlarm);

		JButton btnDeleteAlarm = new JButton("Delete");
		btnDeleteAlarm.setBounds(399, 95, 65, 24);
		btnDeleteAlarm.setPreferredSize(new Dimension(65, 24));
		btnDeleteAlarm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeAlarm();
				// reset note area
				noteArea.setText(DEFAULT_NOTE);
			}
		});
		panel.add(btnDeleteAlarm);

		JButton btnEdit = new JButton("Edit");
		btnEdit.setActionCommand("Edit");
		btnEdit.setPreferredSize(new Dimension(65, 24));
		btnEdit.setBounds(399, 60, 65, 24);
		btnEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editAlarm();
			}
		});
		panel.add(btnEdit);

		timerForRefresh.schedule(new TimerTask() {

			@Override
			public void run() {
				cal = Calendar.getInstance(timeZone);
				repaint();
				SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
				if (!alarmList.isEmpty())
					for (int i = 0; i < alarmList.size(); i++) {
						// can't use equal because there is 2 space at the end of each
						// alarmList element
						if (alarmList.get(i).contains(sdf.format(cal.getTime()))) {
							// Use another thread for sound effect
							SoundPlay soundThread = new SoundPlay("alarm-clock-01.wav");
							soundThread.start();
							// Show alarm note
							if (!noteList.get(i).equals("Nothing here"))
								JOptionPane.showMessageDialog(getContentPane(),
										noteList.get(i).replaceAll("\\\n", "\n"), "Note " + dlm.getElementAt(i),
										JOptionPane.INFORMATION_MESSAGE);
							break;
						}
					}
			}

		}, 0, 1000);

	}

	@Override
	public void paint(Graphics g) {
		int numberSize = (450 + 300) / 10;
		Font f = new Font("Digital-7", Font.PLAIN, numberSize * 2);

		super.paint(g);

		// Keep time updating
		SimpleDateFormat s1 = new SimpleDateFormat("hh:mm"); // hour and minute
		SimpleDateFormat s2 = new SimpleDateFormat("ss"); // second
		SimpleDateFormat s31 = new SimpleDateFormat("MMMM   dd,"); // month and date
		SimpleDateFormat s32 = new SimpleDateFormat("yyyy"); // year
		SimpleDateFormat s4 = new SimpleDateFormat("a"); // AM and PM
		SimpleDateFormat s5 = new SimpleDateFormat("EEE"); // date of week

		g.setColor(Color.BLACK);
		g.setFont(f);
		g.drawString(s1.format(cal.getTime()), 15, numberSize / 2 + 120);

		g.setFont(new Font(f.getFontName(), f.getStyle(), numberSize / 2));
		g.drawString(s2.format(cal.getTime()), numberSize + 270, numberSize / 2 + 120);

		g.setFont(new Font(f.getFontName(), f.getStyle(), f.getSize() / 3));
		g.drawString(s31.format(cal.getTime()), 15, numberSize / 2 + 180);

		g.setFont(new Font(f.getFontName(), f.getStyle(), f.getSize() / 3 + 30));
		g.drawString(s32.format(cal.getTime()), numberSize + 270, numberSize / 2 + 180);

		g.setFont(new Font(f.getFontName(), f.getStyle(), numberSize / 2));
		g.drawString(s4.format(cal.getTime()), numberSize + 270, numberSize / 2 + 55);

		g.setFont(new Font(f.getFontName(), Font.BOLD, f.getSize() / 3));
		g.drawString(s5.format(cal.getTime()), numberSize + 320, numberSize / 2 + 120);
	}

	boolean addAlarm() {
		String alarm = textField.getText();

		// validate hour
		try {
			Integer.parseInt(alarm.substring(0, 2));
		} catch (NumberFormatException e) {
			alarm = "0" + alarm; // add "0" if user just entered 1 number for hour
			try {
				Integer.parseInt(alarm.substring(0, 2));
			} catch (NumberFormatException e1) {
				alarm = "0" + alarm;// add another "0" if user didnt enter any number for hour
			}
		}
		// validate minute
		try {
			Integer.parseInt(alarm.substring(3, 5));
		} catch (NumberFormatException e) {
			alarm = alarm.replaceFirst(":", ":0");
			try {
				Integer.parseInt(alarm.substring(3, 5));
			} catch (NumberFormatException e1) {
				alarm = alarm.replaceFirst(":", ":0");
			}
		}
		alarm = alarm.toUpperCase() + "  ";

		// add alarm into list
		// comparing minute value
		if (!dlm.contains(alarm)) {
			if (dlm.getSize() != 0) {
				int alarm_value = Integer.parseInt(alarm.substring(0, 2)) * 60
						+ Integer.parseInt(alarm.substring(3, 5));
				if (alarm.substring(6, 8).compareToIgnoreCase("pm") == 0)
					alarm_value += 12 * 60;
				for (int i = 0; i < dlm.getSize(); i++) {
					int tmp = Integer.parseInt(dlm.getElementAt(i).substring(0, 2)) * 60
							+ Integer.parseInt(dlm.getElementAt(i).substring(3, 5));

					if (dlm.getElementAt(i).substring(6, 8).compareToIgnoreCase("pm") == 0)
						tmp += 12 * 60;
					if (alarm_value < tmp) {
						dlm.insertElementAt(alarm, i);
						list.setSelectedIndex(i);
						break;
					}
					if (i == dlm.getSize() - 1) {
						dlm.addElement(alarm);
						list.setSelectedIndex(dlm.getSize() - 1);
						break;
					}
				}
			} else {
				dlm.addElement(alarm);
				list.setSelectedIndex(dlm.getSize() - 1);
			}
		} else
			return false; // False to add new alarm

		// ignore if the alarm has been added
		alarm = alarm.replaceFirst(" ", ":00 ");
		if (!alarmList.contains(alarm)) {
			alarmList.add(alarm);
			if (!noteArea.getText().equals(DEFAULT_NOTE)) {
				noteList.add(noteArea.getText());
			} else
				noteList.add("Nothing here");
		} else
			return false; // False to add new alarm

		return true;
	}

	boolean removeAlarm() {
		if (!dlm.isEmpty() && !alarmList.isEmpty() && !noteList.isEmpty()) {
			int deleteTimes = 0;
			int[] selectedIndices = list.getSelectedIndices();
			for (int i = 0; i < selectedIndices.length; i++, deleteTimes++) {
				// remove alarms
				alarmList.remove(selectedIndices[i] - deleteTimes);
				noteList.remove(selectedIndices[i] - deleteTimes);
				dlm.removeElementAt(selectedIndices[i] - deleteTimes);
			}
		} else
			return false;// False to remove alarm

		return true;
	}

	boolean editAlarm() {
		// add the new then delete the previous alarms
		if (!dlm.isEmpty() && !alarmList.isEmpty() && !noteList.isEmpty()) {
			int[] selectedIndices = list.getSelectedIndices();
			if (addAlarm()) {
				int deleteTimes = 0;
				for (int i = 0; i < selectedIndices.length; i++, deleteTimes++) {
					// remove alarm
					alarmList.remove(selectedIndices[i] - deleteTimes);
					noteList.remove(selectedIndices[i] - deleteTimes);
					dlm.removeElementAt(selectedIndices[i] - deleteTimes);
				}
			} else {
				// remove noteList of the selected alarm then insert new on
				if (selectedIndices.length == 1) {
					noteList.remove(selectedIndices[0]);
					noteList.add(selectedIndices[0], noteArea.getText());
				}
			}
		} else
			return false; // False to edit existing alarm
		return true;
	}

	@SuppressWarnings("resource")
	void writeFile(String fileName, Vector<String> alarmList_, DefaultListModel<String> dlm_) {

		try {
			PrintWriter writer = new PrintWriter(fileName);
			if (alarmList_ == null || dlm_ == null) {
				// Overwrite the exist file
				File fnew = new File(fileName);
				fnew.createNewFile();
				writer = new PrintWriter(fnew);
				writer.println("*");
				writer.println("*");
				return;
			}
			String line = null;
			for (int i = 0; i < alarmList_.size(); i++) {
				line = alarmList_.elementAt(i);
				writer.println(line);
			}
			writer.println("*");

			for (int i = 0; i < dlm_.getSize(); i++) {
				line = dlm_.elementAt(i);
				writer.println(line);
			}
			writer.println("*");
			writer.flush();
			writer.close();
		} catch (IOException e) {

		}
	}

	@SuppressWarnings("resource")
	void writeNoteList(String fileName, Vector<String> noteList_) {
		try {
			PrintWriter writer = new PrintWriter(fileName);
			if (noteList_ == null) {
				// Overwrite the exist file
				File fnew = new File(fileName);
				fnew.createNewFile();
				writer = new PrintWriter(fnew);
				writer.println("*");
				return;
			}
			String line = null;
			for (int i = 0; i < noteList_.size(); i++) {
				line = noteList_.elementAt(i);
				writer.println(line.replaceAll("\n", "/n"));
			}
			writer.println("*");

			writer.flush();
			writer.close();
		} catch (IOException e) {

		}
	}

	List<String> readAlarmList(String fileName) {
		List<String> records = new Vector<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.equals("*"))
					records.add(line);
				else
					break;
			}
			reader.close();
			return records;
		} catch (Exception e) {
			// No file found, create new List.
			return new Vector<String>();
		}

	}

	List<String> readNoteList(String fileName) {
		List<String> records = new Vector<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.equals("*"))
					records.add(line.replaceAll("/n", "\n"));
				else
					break;
			}
			reader.close();
			return records;
		} catch (Exception e) {
			// No file found, create new List.
			return new Vector<String>();
		}

	}

	DefaultListModel<String> readFileDefaultListModel(String fileName) {
		DefaultListModel<String> records = new DefaultListModel<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null)
				if (line.equals("*"))
					break;

			while ((line = reader.readLine()) != null) {
				if (!line.equals("*"))
					records.addElement(line);
				else
					break;
			}
			reader.close();
			return records;
		} catch (Exception e) {
			// Do nothing
			return new DefaultListModel<String>();
		}

	}

	@Override
	public void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			writeNoteList("note.data", (Vector<String>) noteList);
			writeFile("list.data", (Vector<String>) alarmList, dlm);
			setVisible(false);
			/*dispose();
			System.exit(0);*/
		}
	}

}
