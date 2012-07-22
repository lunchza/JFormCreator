/**
 * @author Peter Pretorius
 * The aim of this program is to allow users to generate simple, user-created GUI's using 
 * a simple interface. Once the GUI is created, the user can generate a target source file
 * that houses all of the code necessary for implementation of the GUI. The program also
 * allows templates to be saved and loaded. (Loaded templates are fully modifiable)
 * 
 * Note: The GUI's generated with this program are not flexible AT ALL. The purpose of this
 * program is to quickly deploy static GUI's, the functionality of which can then be
 * added. This program also currently only supports the creation of single-frame
 * interfaces.
 */

package jGuiMaker;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.*;
import java.util.ArrayList;
import java.awt.*;
import java.io.File;

import jTools.JStatusBar;

public class JFormCreator extends JFrame implements ActionListener, MouseListener,
		MouseMotionListener, KeyListener {

	/**
	 * This ArrayList of <tt>Component</tt>s contains the details for user every
	 * created component, and ultimately will provide the necessary parameters
	 * for code generation.
	 */
	private ArrayList<Component> components, 
	
	/**
	 * This is the list of currently selected components
	 */
	selectedComponents, 
	
	/**
	 * This is the clipboard, which stores (copies of) copied components
	 */
	clipboard;
	
	/**
	 * This list returns moved components to their original position after attempting to move
	 * them out of bounds
	 */
	private ArrayList<Point> originalComponentLocations;
	

	private static final long serialVersionUID = -3655550468500441772L;
	
	private JLabel widthLabel, heightLabel, xLabel, yLabel, textLabel, actionListenerLabel;
	
	private JTextField widthField, heightField, xField, yField, textField,
						frameWidthField, frameHeightField; 

	private JButton createButton_button, createLabel_button,
			createTextField_button, createTextArea_button, createCheckBox_button,
			createComboBox_button;
	
	private JButton updateButton, deleteButton, maximumButton, okButton;
	
	private JFrame sizeFrame;

	private static JPanel mainPanel;

	private JPanel toolPanel, utilityPanel, propertiesPanel;

	private JStatusBar statusBar;
	
	private TemplateManager templateManager;
	
	private JMenuBar menuBar;
	
	//The context menu of the program
	private JPopupMenu contextMenu;
	
	//The items in the context menu
	private JMenuItem copy_context, cut_context, paste_context, delete_context;
	
	
	private JMenu fileMenu, editMenu, optionsMenu, helpMenu;
	
	private JMenuItem newMenuItem, createTemplateItem, loadTemplateItem, exportMenuItem, exitMenuItem,
			copyItem, cutItem, pasteItem, deleteItem,frameSizeItem, forceEventCodeItem, aboutMenuItem;
	
	private JCheckBoxMenuItem disableTraceItem;
	
	private JCheckBox actionListenerBox;

	private int currentStatus;
	
	//private int componentMaximumSize;
	
	private boolean drawTraceLines = true, forcedEventCode = false;
	
	/*	X and Y values are recorded whenever:
	 *  The mouse is pressed
	 *  The mouse is dragged
	 *  The mouse is released
	 *  
	 *  These parameters are necessary for controlling component behaviour
	 */
	private int pressedX, pressedY, currentX, currentY, releasedX, releasedY;

	/*
	 * These coordinates are used for single-click placement of components
	 */
	private int mouseLocationX, mouseLocationY;
	
	/*
	 * These coordinates assist with component dragging calculations
	 */
	private int initialClickedComponentX, initialClickedComponentY;
	
	/*
	 * Introduced out of necessity due to the way that MouseEvents generate coordinates
	 * relative to the clicked component
	 */
	private boolean firstTick = true;
	
	/*
	 * This boolean saves a lot of redundant code by allowing a simple check to determine the
	 * difference between copy and cut
	 */
	private boolean cut = false;
	
	/*
	 * The initial size of the panel, before any resizing is done
	 */
	private Dimension originalPanelSize;
	
	/*
	 * The absolute center of the program frame
	 */
	private Point frameCenter;
	
	/*
	 * The variable representation of the dragged rectangle that allows for multiple
	 * component selection
	 */
	private Rectangle draggedRect;

	//The currently selected component and the top-most component used for assistance with pasting calculations
	private static Component selectedComponent, topMostComponent;

	/*
	 * CONSTANTS
	 */
	static final String version = "1.25b";
	
	static final int NORMAL_STATUS = 0;
	static final int CREATE_BUTTON_STATUS = 1;
	static final int CREATE_LABEL_STATUS = 2;
	static final int CREATE_TEXT_FIELD_STATUS = 3;
	static final int CREATE_TEXT_AREA_STATUS = 4;
	static final int CREATE_CHECK_BOX_STATUS = 5;
	static final int CREATE_COMBO_BOX_STATUS = 6;
	static final int DRAG_MODE_STATUS = 8;
	static final int COMPONENT_SELECTED_STATUS = 10;
	static final int MULTIPLE_COMPONENTS_SELECTED_STATUS = 11;
	//The minimum width and height allowed for any component
	static final int COMPONENT_MINIMUM_SIZE = 25;

	public JFormCreator() {
		super("JFormCreator v" + version + " by Peter Pretorius");
		initialise();
	}

	/**
	 * Initialises the interface and initial variable values for JFormCreator
	 */
	public void initialise() {
		/*
		 * VARIABLE INITIALISATION
		 */
		
		menuBar = new JMenuBar();
		
		fileMenu = new JMenu("File");
		editMenu = new JMenu("Edit");
		optionsMenu = new JMenu("Options");
		helpMenu = new JMenu("Help");
		
		newMenuItem = new JMenuItem("New");
		createTemplateItem = new JMenuItem("Save template");
		loadTemplateItem = new JMenuItem("Load template");
		exportMenuItem = new JMenuItem("Generate code");
		exitMenuItem = new JMenuItem("Exit");
		copyItem = new JMenuItem("Copy");
		cutItem = new JMenuItem("Cut");
		pasteItem = new JMenuItem("Paste");
		deleteItem = new JMenuItem("Delete");
		frameSizeItem = new JMenuItem("Change frame size");
		disableTraceItem = new JCheckBoxMenuItem("Hide trace lines");
		forceEventCodeItem = new JCheckBoxMenuItem("Force event code generation");
		aboutMenuItem = new JMenuItem("About");
		
		contextMenu = new JPopupMenu();
		copy_context = new JMenuItem("Copy");
		cut_context = new JMenuItem("Cut");
		paste_context = new JMenuItem("Paste");
		delete_context = new JMenuItem("Delete");
		
		components = new ArrayList<Component>();
		selectedComponents = new ArrayList<Component>();
		clipboard = new ArrayList<Component>();
		createButton_button = new JButton("Create button");
		createLabel_button = new JButton("Create label");
		createTextField_button = new JButton("Create text field");
		createTextArea_button = new JButton("Create text area");
		createCheckBox_button = new JButton("Create check box");
		createComboBox_button = new JButton("Create combo box");
		
		originalPanelSize = new Dimension();
			
		//Property fields
		xLabel = new JLabel(" x:");
		xField = new JTextField();
		xField.setEditable(false);
		yLabel = new JLabel(" y:");
		yField = new JTextField();
		yField.setEditable(false);
		widthLabel = new JLabel(" width:");
		widthField = new JTextField();
		widthField.setEditable(false);
		heightLabel = new JLabel(" height:");
		heightField = new JTextField();
		heightField.setEditable(false);
		textLabel = new JLabel(" text/values:");
		textField = new JTextField();
		textField.setEditable(false);
		textField.setColumns(5);
		actionListenerLabel = new JLabel(" Action Listener");
		actionListenerBox = new JCheckBox();
		actionListenerBox.setEnabled(false);
		
		updateButton = new JButton("update");
		updateButton.setEnabled(false);
		
		deleteButton = new JButton("Delete");
		statusBar = new JStatusBar("Program running", JStatusBar.LEFT_ORIENTATION);
		resetPoints();
		currentStatus = NORMAL_STATUS;

		/*
		 * GUI INITIALISATION
		 */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLayout(null);

		toolPanel = new JPanel(new FlowLayout()); // The panel that houses the
													// toolbar
		toolPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		toolPanel.setPreferredSize(new Dimension(200, 768));
		toolPanel.setLayout(new GridLayout(8, 2));

		mainPanel = new DisplayPanel(); // The center panel, which essentially
										// represents the screen area of the
										// target machine
		mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		mainPanel.setLayout(null);
		
		propertiesPanel = new JPanel(new GridLayout(8, 2)); //The panel that holds the properties of the selected component
		
		propertiesPanel.add(xLabel);
		propertiesPanel.add(xField);
		propertiesPanel.add(yLabel);
		propertiesPanel.add(yField);
		propertiesPanel.add(widthLabel);
		propertiesPanel.add(widthField);
		propertiesPanel.add(heightLabel);
		propertiesPanel.add(heightField);
		propertiesPanel.add(textLabel);
		propertiesPanel.add(textField);
		propertiesPanel.add(actionListenerLabel);
		JPanel tempPanel = new JPanel(new BorderLayout());
		tempPanel.add(actionListenerBox, BorderLayout.EAST);
		propertiesPanel.add(tempPanel);
		propertiesPanel.add(new JPanel());
		propertiesPanel.add(updateButton);

		utilityPanel = new JPanel(new BorderLayout());
		
		utilityPanel.add(propertiesPanel, BorderLayout.NORTH);
		utilityPanel.add(deleteButton, BorderLayout.SOUTH);
		utilityPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		//Set the frame size to the current screen resolution
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		screenSize.setSize(screenSize.width, screenSize.height-50);
		
		setSize(screenSize); // set frame size
		setLocationRelativeTo(null); // center frame

		setLayout(new BorderLayout());

		toolPanel.add(createButton_button);
		toolPanel.add(createLabel_button);
		toolPanel.add(createTextField_button);
		toolPanel.add(createTextArea_button);
		toolPanel.add(createCheckBox_button);
		toolPanel.add(createComboBox_button);
		
		fileMenu.add(newMenuItem);
		fileMenu.add(createTemplateItem);
		fileMenu.add(loadTemplateItem);
		fileMenu.add(exportMenuItem);
		fileMenu.add(exitMenuItem);
		
		editMenu.add(copyItem);
		editMenu.add(cutItem);
		editMenu.add(pasteItem);
		editMenu.add(deleteItem);
		
		optionsMenu.add(frameSizeItem);
		optionsMenu.add(disableTraceItem);
		optionsMenu.add(forceEventCodeItem);
		
		helpMenu.add(aboutMenuItem);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(optionsMenu);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
		
		contextMenu.add(copy_context);
		contextMenu.add(cut_context);
		contextMenu.add(paste_context);
		contextMenu.add(delete_context);

		add(toolPanel, BorderLayout.WEST);
		add(mainPanel, BorderLayout.CENTER);
		add(utilityPanel, BorderLayout.EAST);
		add(statusBar, BorderLayout.SOUTH);

		/*
		 * EVENT-CONTROL INITIALISATION
		 */
		createButton_button.addActionListener(this);
		createLabel_button.addActionListener(this);
		createTextField_button.addActionListener(this);
		createTextArea_button.addActionListener(this);
		createCheckBox_button.addActionListener(this);
		createComboBox_button.addActionListener(this);
		newMenuItem.addActionListener(this);
		createTemplateItem.addActionListener(this);
		loadTemplateItem.addActionListener(this);
		exportMenuItem.addActionListener(this);
		exitMenuItem.addActionListener(this);
		copyItem.addActionListener(this);
		cutItem.addActionListener(this);
		pasteItem.addActionListener(this);
		deleteItem.addActionListener(this);
		frameSizeItem.addActionListener(this);
		disableTraceItem.addActionListener(this);
		forceEventCodeItem.addActionListener(this);
		aboutMenuItem.addActionListener(this);
		xField.addKeyListener(this);
		yField.addKeyListener(this);
		widthField.addKeyListener(this);
		heightField.addKeyListener(this);
		textField.addKeyListener(this);
		updateButton.addActionListener(this);
		deleteButton.addActionListener(this);
		mainPanel.addMouseListener(this);
		mainPanel.addMouseMotionListener(this);
		copy_context.addActionListener(this);
		cut_context.addActionListener(this);
		paste_context.addActionListener(this);
		delete_context.addActionListener(this);
		
		mainPanel.setComponentPopupMenu(contextMenu);
		
		//Initial repaint is key for initialisation of certain variables
		mainPanel.repaint();
		
		setVisible(true);
		frameCenter = new Point(mainPanel.getX()+mainPanel.getWidth()/2, mainPanel.getY() + mainPanel.getHeight()/2);
		}

	public static void main(String[] args) {
		new JFormCreator();

	}
	
	public static Dimension getFrameSize()
	{
		return new Dimension(mainPanel.getSize());
	}
	
	public void showContext(MouseEvent e)
	{
		contextMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	/**
	 * Updates the current status of the program
	 * 
	 * @param s The new program status
	 */
	public void setStatus(String s) {
		statusBar.setStatus(s);
	}
	/**
	 * Resets the status of the program and reinitialises key variables, putting them in
	 * their initial states
	 */
	public void resetStatus() {
		selectedComponent = null;
		selectedComponents.clear();
		resetPoints();
		cut = false;
		setStatus("Program running");
		currentStatus = NORMAL_STATUS;
		mainPanel.repaint();
	}

	public void actionPerformed(ActionEvent e) {
		
		//This code handles all component updates that are set by the properties panel
		if (e.getSource() == updateButton && selectedComponent != null)
		{
			String oldText = "";
			String[] oldValues = null;
			try
			{
				//prevents code injection and other forms of abuse
				if(textField.getText().contains("\""))
					throw new IllegalArgumentException("Quotation marks not allowed and were therefore be removed.");
				
				if(textField.getText().contains("\'"))
					throw new IllegalArgumentException("Apostrophe's not allowed and were therefore be removed.");
				
				if(textField.getText().contains("\\"))
					throw new IllegalArgumentException("Backslashes not allowed and were therefore be removed.");
				
				//The following lines of code are responsible for checking component bounds after updating
				int x = Integer.parseInt(xField.getText());
				int y = Integer.parseInt(yField.getText());
				
				if (!mainPanel.contains(x, y))
					throw new IllegalArgumentException("Specified Coordinates are out of bounds.");

				int width = Integer.parseInt(widthField.getText());
				
				if (width < COMPONENT_MINIMUM_SIZE)
					throw new IllegalArgumentException("Minimum width is " + COMPONENT_MINIMUM_SIZE);

				if (width > (mainPanel.getWidth()-selectedComponent.getX()-5))
					throw new IllegalArgumentException("Specified width is out of bounds. Maximum allowed width at these coordinates is " + (mainPanel.getWidth()-selectedComponent.getX()-5));

				int height = Integer.parseInt(heightField.getText());
				
				if (height < COMPONENT_MINIMUM_SIZE)
					throw new IllegalArgumentException("Minimum height is " + COMPONENT_MINIMUM_SIZE);

				
				if (height > (mainPanel.getHeight()-selectedComponent.getY()-5))
					throw new IllegalArgumentException("Specified height is out of bounds. Maximum allowed height at these coordinates is " + (mainPanel.getHeight()-selectedComponent.getY()-5));

				
				//Change the selected component's parameters to match the values in the property fields
				selectedComponent.setBounds(x, y, width, height);
							
				//Update selected component's text if it's a button
				if (selectedComponent instanceof JButton)
				{
					oldText = ((JButton) selectedComponent).getText();
					((JButton) selectedComponent).setText(textField.getText());
				}
			
				//Update selected component's text if it's a label, and adjust its width to accommodate any change
				if (selectedComponent instanceof JLabel)
				{
					if (textField.getText().equals(""))
					{
						((JLabel) selectedComponent).setText("_");
						setPropertiesFields();
						return;
					}
					
					oldText = ((JLabel) selectedComponent).getText();
					((JLabel) selectedComponent).setText(textField.getText());
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());
				}
				
				//Update selected component's text if it's a checkbox, and adjust its width to accommodate any change
				if (selectedComponent instanceof JCheckBox)
				{
					oldText = ((JCheckBox) selectedComponent).getText();
					((JCheckBox) selectedComponent).setText(textField.getText());
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());
				}
				
				//Update selected component's values if it's a combobox, and adjust its width to accommodate any change
				if (selectedComponent instanceof JComboBox)
				{
					oldValues = new String[((JComboBox) selectedComponent).getItemCount()];
					for (int i = 0; i < oldValues.length; i++)
					{
						oldValues[i] = (String) ((JComboBox) selectedComponent).getItemAt(i);
					}
					String input = textField.getText();
					String[] values = input.split(",");
					((JComboBox) selectedComponent).removeAllItems();
					for (int i = 0; i < values.length; i++)
					{
						((JComboBox) selectedComponent).addItem(values[i]);
					}
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());
				}
				
				Point lowerRightCorner = new Point(selectedComponent.getX()+selectedComponent.getWidth(), selectedComponent.getY() + selectedComponent.getHeight());
				//text change has put the component out of bounds
				if (!mainPanel.contains(lowerRightCorner))
				{
					throw new StringIndexOutOfBoundsException("Text length has put the component out of bounds. Old text restored");
				}
			}
			
			//invalid text in property field
			catch (NumberFormatException N)
			{
				JOptionPane.showMessageDialog(null, "Invalid input");
				
				//reset property fields to selected componen't current values
				setPropertiesFields();
			}
			
			//Quotation marks detected in name field (used by check boxes
			catch (IllegalArgumentException E)
			{
				JOptionPane.showMessageDialog(null, E.getMessage());
				
				//reset property fields to selected componen't current values
				setPropertiesFields();
			}
			
			//If the component's new text has put it out of bounds, the old component text is restored
			catch (StringIndexOutOfBoundsException S)
			{
				JOptionPane.showMessageDialog(null, S.getMessage());
				if (selectedComponent instanceof JButton)
					((JButton) selectedComponent).setText(oldText);
			
				if (selectedComponent instanceof JLabel)
				{
					((JLabel) selectedComponent).setText(oldText);
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());
				}
				
				if (selectedComponent instanceof JCheckBox)
				{
					((JCheckBox) selectedComponent).setText(oldText);
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());
				}
				
				if (selectedComponent instanceof JComboBox)
				{
					((JComboBox) selectedComponent).removeAllItems();
					for (int i = 0; i < oldValues.length; i++)
						((JComboBox) selectedComponent).addItem(oldValues[i]);
					
					selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getPreferredSize().width, selectedComponent.getHeight());

				}
				
				setPropertiesFields();
			}
			
			//Check if the new component width is greater than the minimum component width
			if (selectedComponent.getWidth() < COMPONENT_MINIMUM_SIZE)
			{
				selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), COMPONENT_MINIMUM_SIZE, selectedComponent.getHeight());
				widthField.setText(""+COMPONENT_MINIMUM_SIZE);
			}
			//Check if the new component height is greater than the minimum component height
			if (selectedComponent.getHeight() < COMPONENT_MINIMUM_SIZE)
			{
				selectedComponent.setBounds(selectedComponent.getX(), selectedComponent.getY(), selectedComponent.getWidth() , COMPONENT_MINIMUM_SIZE);
				heightField.setText(""+COMPONENT_MINIMUM_SIZE);
			}
			
			//Indicates that the selected component has an actionListener, if necessary
			if (actionListenerBox.isSelected())
			{
				//only buttons are allowed actionListeners
				if(selectedComponent instanceof JButton || selectedComponent instanceof JCheckBox)
					selectedComponent.setName("HAS_ACTION_LISTENER");
				else
				{
					actionListenerBox.setSelected(false);
					JOptionPane.showMessageDialog(null, "Cannot add an ActionListener to this component!");
				}
			}
			//remove action listener if so desired by the user
			if (!actionListenerBox.isSelected() && selectedComponent.getName() != null)
			{
				selectedComponent.setName(null);
			}
			
			setStatus("Component properties updated");
			mainPanel.repaint();
		}
		
		/*	Deselects the currently selected component if the user clicks 
		 * 	anywhere on the main Panel or any of the component creation buttons
		 */
		if (selectedComponent != null && e.getSource() != deleteButton && e.getSource() != updateButton && e.getSource() != delete_context)
		{
			selectedComponent = null;
			clearPropertiesFields();
			mainPanel.repaint();
		}

		//create Button clicked
		if (e.getSource() == createButton_button) {
			setStatus("Click and drag the rectangle to the desired dimensions of the component");
			currentStatus = CREATE_BUTTON_STATUS;
		}

		//create Label clicked
		if (e.getSource() == createLabel_button) {
			setStatus("Click on the point where you'd like to place the label");
			currentStatus = CREATE_LABEL_STATUS;
		}

		//create Text Field clicked
		if (e.getSource() == createTextField_button) {
			setStatus("Click and drag the rectangle to the desired dimensions of the component");
			currentStatus = CREATE_TEXT_FIELD_STATUS;
		}
		
		//create Text Area clicked
		if (e.getSource() == createTextArea_button) {
			setStatus("Click and drag the rectangle to the desired dimensions of the component");
			currentStatus = CREATE_TEXT_AREA_STATUS;
		}
		
		//create Check Box clicked
		if (e.getSource() == createCheckBox_button) {
			setStatus("Click on the point where you'd like to place the checkbox");
			currentStatus = CREATE_CHECK_BOX_STATUS;
		}
		
		//create Combo Box clicked
		if (e.getSource() == createComboBox_button) {
			setStatus("Click on the point where you'd like to place the combo box");
			currentStatus = CREATE_COMBO_BOX_STATUS;
		}
		
		/*
		 * If a component is selected, 'deletes' said component by calling the deleteComponent method
		 */
		if (e.getSource() == deleteButton)
		{
			if (currentStatus == MULTIPLE_COMPONENTS_SELECTED_STATUS)
			{
				for (Component comp: selectedComponents)
				{
					deleteComponent(comp);			
				}
				selectedComponents.clear();
			}
			
			if (selectedComponent != null)
			{
				deleteComponent(selectedComponent);
			}
			
			resetStatus();
		}
		
		//Clears all user-created components from the screen by destroying them
		if (e.getSource() == newMenuItem)
		{
			int result = JOptionPane.showConfirmDialog(null, "This will clear the current template. Continue?", "Warning", JOptionPane.YES_NO_OPTION);
			if (result == 0)
			{
				/*
				 * Iterates through every user-created component, selects them then
				 * removes them. Note that this doesn't call the deleteComponent method
				 * as this results in a ConcurrentModificationException
				 */
				for (Component comp : components)
				{	
					comp.setVisible(false);
					comp.setEnabled(false);
					comp.removeNotify();
					comp.setLocation(-999, -999);
				}
				components.clear();
				mainPanel.setSize(originalPanelSize);
				Point center = new Point(getX() + toolPanel.getWidth(), 0);
				//Point center = new Point(getWidth()/2, getHeight()/2);
				mainPanel.setLocation(center);
				resetStatus();
				clearPropertiesFields();
			components.clear();
			selectedComponent = null;
			}
			resetStatus();
		}
		/*
		 * Generates a .tmp file that stores the properties of every component on screen
		 */
		if (e.getSource() == createTemplateItem)
		{
			if (components.size() == 0)
				JOptionPane.showMessageDialog(null, "Template is blank!");
			else
			{
			templateManager = new TemplateManager(components);
			if (templateManager.createTemplate())
				JOptionPane.showMessageDialog(null, "Template saved");
			//else
				//JOptionPane.showMessageDialog(null, "Template not saved");
			
			resetStatus();
			}
		}
		
		/*
		 * Clears the current template and loads component information from a target .tmp file
		 */
		if (e.getSource() == loadTemplateItem)
		{
			templateManager = new TemplateManager();

			File templateFile = null;
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open file");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setAcceptAllFileFilterUsed(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"java files(.tmp)", "tmp");

			fileChooser.setFileFilter(filter);

			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				templateFile = fileChooser.getSelectedFile();

			if (templateFile != null) {
				int result = 0;
				if(components.size() != 0)
				result = JOptionPane.showConfirmDialog(null,
						"This will clear the current template. Continue? ", "Warning", JOptionPane.YES_NO_OPTION);
				if (result == 0) {
					// clear the current template
					for (Component comp : components)
					{	
						comp.setVisible(false);
						comp.setEnabled(false);
						comp.removeNotify();
						comp.setLocation(-999, -999);
					}
					
					components.clear();
					selectedComponent = null;

					components = templateManager.loadTemplate(templateFile);
					if (components.size() == 0)
					{
						components = new ArrayList<Component>();
						resetStatus();
						return;
					}
					
					mainPanel.setSize(templateManager.getSize());

					//This recentering is done in 2 parts for various reasons
					mainPanel.setLocation(frameCenter);
					
					mainPanel.setLocation(mainPanel.getX()-mainPanel.getWidth()/2, mainPanel.getY()-mainPanel.getHeight()/2);
					
					setLayout(null);
					for (Component comp : components) {
						comp.validate();
						//without this code, certain components become editable after creation 
						if (comp instanceof JComboBox || comp instanceof JTextField || comp instanceof JTextArea || comp instanceof JCheckBox)
							comp.setEnabled(false);
						
						mainPanel.add(comp);
						setListeners(comp);
					}
					mainPanel.repaint();
				}
			}
			resetStatus();
		}
		
		/*
		 * Exports source code that recreates the current user interface using SourceGenerator
		 */
		
		if (e.getSource() == exportMenuItem)
		{
			if (components.size() == 0)
				JOptionPane.showMessageDialog(null, "Template is blank!");
			else
			{
				boolean generateEventCode = false;
				for (Component comp: components)
				{
					if (comp.getName() != null)
						generateEventCode = true;
				}
				
				if (generateEventCode)
					new SourceGenerator(components, true).generate();
				else
					if(forcedEventCode)
						new SourceGenerator(components, true).generate();
					else
						new SourceGenerator(components, false).generate();
			}
		}
		
		//Exit program
		if (e.getSource() == exitMenuItem)
		{
			System.exit(0);
		}
		
		/*
		 * The functions of the edit menu simply fire their corresponding equivalents in the
		 * context menu
		 */
		if (e.getSource() == copyItem)
		{
			actionPerformed(new ActionEvent(copy_context, 1, null));
		}
		
		if (e.getSource() == cutItem)
		{
			actionPerformed(new ActionEvent(cut_context, 1, null));
		}
		
		if (e.getSource() == pasteItem)
		{
			actionPerformed(new ActionEvent(paste_context, 1, null));
		}
		
		if (e.getSource() == deleteItem)
		{
			actionPerformed(new ActionEvent(delete_context, 1, null));
		}
		
		/*
		 * Creates and displays the frame responsible for changing the main panel size
		 * In a psuedo-form of bootstrapping, this code was mostly generated by this very program
		 */
		if (e.getSource() == frameSizeItem)
		{
			resetStatus();
			sizeFrame = new JFrame("Change frame size");
			sizeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			sizeFrame.setSize(280, 125);
			JLabel frameWidthLabel = new JLabel("width:");
	        JLabel frameHeightLabel = new JLabel("height:");
	        frameWidthField = new JTextField(""+mainPanel.getWidth());
	        frameHeightField = new JTextField(""+mainPanel.getHeight());
	        okButton = new JButton("Ok");
	        maximumButton = new JButton("Maximum");
	        
	        okButton.addActionListener(this);
	        maximumButton.addActionListener(this);
	        
	        sizeFrame.addWindowListener(new WindowListener(){

				public void windowActivated(WindowEvent arg0) {
					
				}

				public void windowClosed(WindowEvent arg0) {
					
				}

				public void windowClosing(WindowEvent arg0) {
					setEnabled(true);
					
				}

				public void windowDeactivated(WindowEvent arg0) {
					
				}

				public void windowDeiconified(WindowEvent arg0) {
					
				}

				public void windowIconified(WindowEvent arg0) {
					
				}

				public void windowOpened(WindowEvent arg0) {
					
				}
	        	
	        });
	        
	        frameWidthLabel.setBounds(31, 28, 34, 25);
	        frameHeightLabel.setBounds(170, 28, 38, 25);
	        frameWidthField.setBounds(76, 28, 50, 25);
	        frameHeightField.setBounds(218, 28, 50, 25);
	        okButton.setBounds(218, 65, 50, 25);
	        maximumButton.setBounds(31, 65, 100, 25);
	        
	        sizeFrame.setLayout(null);
	        
	        sizeFrame.add(frameWidthLabel);
	        sizeFrame.add(frameHeightLabel);
	        sizeFrame.add(frameWidthField);
	        sizeFrame.add(frameHeightField);
	        sizeFrame.add(okButton);
	        sizeFrame.add(maximumButton);
	        
	        sizeFrame.setLocationRelativeTo(null);
	        sizeFrame.setResizable(false);
	        sizeFrame.setVisible(true);
	        sizeFrame.requestFocus();
	        setEnabled(false);
		}
		
		/*
		 * Turns trace lines on or off
		 */
		if (e.getSource() == disableTraceItem)
		{
			if (drawTraceLines)
				drawTraceLines = false;
			else
				drawTraceLines = true;
			
			mainPanel.repaint();
		}
		
		/*
		 * Always force event code generation
		 */
		if (e.getSource() == forceEventCodeItem)
		{
			if(forcedEventCode)
				forcedEventCode = false;
			else
				forcedEventCode = true;
		}
		
		//About menu
		if (e.getSource() == aboutMenuItem)
		{
			JOptionPane.showMessageDialog(null, "JFormCreator v" + version + "\ncoded by Peter Pretorius");
		}
		
		/*
		 * Attempts to change the size of the main panel
		 */
		if (e.getSource() == okButton)
		{
			int result = 0;
			if (components.size() != 0)
				result = JOptionPane.showConfirmDialog(null, "Template isn't blank, hence resizing may lead to unpredictable behaviour. Continue?", "Warning", JOptionPane.YES_NO_OPTION);
			
			if (result == 0) {
				try {
					//these are the current frame bounds
					int newWidth = Integer.parseInt(frameWidthField.getText());
					
					int newHeight = Integer.parseInt(frameHeightField.getText());
					
					//minimum size check
					if (newWidth < 100 || newHeight < 100)
					{
						throw new IllegalArgumentException("Error: Minimum frame bounds are (100,100)");
					}

					//maximum size check
					if (newWidth > originalPanelSize.width
							|| newHeight > originalPanelSize.height)
						throw new IllegalArgumentException("Error: Frame bounds are ("
								+ originalPanelSize.width + ","
								+ originalPanelSize.height + ")");

					//center of the panel, used for centering the new frame in the event of a rectangular
					//frame
					Point center = new Point(mainPanel.getX()
							+ mainPanel.getWidth() / 2 - newWidth / 2,
							mainPanel.getY() + mainPanel.getHeight() / 2
									- newHeight / 2);

					mainPanel.setSize(newWidth, newHeight);
					setLayout(null);
					mainPanel.setLocation(center);

					sizeFrame.dispose();
					setEnabled(true);
					requestFocus();
				} catch (NumberFormatException nfe) {
					JOptionPane.showMessageDialog(null, "Invalid input");
				}

				catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(null,iae.getMessage());
				}
			}
			
			mainPanel.repaint();
		}
		
		/*
		 * Sets the main panel size to its maximum allowed size
		 */
		if (e.getSource() == maximumButton)
		{
			frameWidthField.setText(""+originalPanelSize.width);
			frameHeightField.setText(""+originalPanelSize.height);
		}
		
		/*
		 * Handles copying components to clipboard via the context menu
		 */
		if (e.getSource() == copy_context)
		{
			//clipboard not empty
			if (clipboard.size() != 0)
				clipboard.clear();
			
			//No selection
			if (selectedComponents.size() == 0)
				JOptionPane.showMessageDialog(null, "Nothing selected!");
			
			else
			{
				//Initially the top-most component is randomly selected
				//A copy is used instead of the original in case the original is deleted
				//before the paste takes place
				topMostComponent = copyComponent(selectedComponents.get(0));
				

				for (int i = 0; i < selectedComponents.size(); i++)
				{
					Component comp = selectedComponents.get(i);
					//Replace the current top-most component if this component is higher up
					if(comp.getY() < topMostComponent.getY())
						topMostComponent = copyComponent(comp);
					
					//Copies are added to the clipboard instead of the original so that even
					//if the originals are deleted, copies can still be pasted
					clipboard.add(copyComponent(comp));
						
					if (cut)
						deleteComponent(comp);
						
					
				}
				resetStatus(); 
			}
		}
		
		/*
		 * Changes the cut flag from false to true then generates a copy event
		 */
		if (e.getSource() == cut_context)
		{
			cut = true;
			actionPerformed(new ActionEvent(copy_context, 1, null));
		}
		
		/*
		 * Handles pasting copied components from the clipboard via the context menu
		 */
		if (e.getSource() == paste_context)
		{
			if (clipboard.size() == 0)
				JOptionPane.showMessageDialog(null, "Nothing to paste!");
			
			else
			{
				if (topMostComponent != null)
				{	
				/* The difference in location between the copied top-most component and the
				 * point at which the context menu was generated is used as a basis for the
				 * relative placement of copied items
				 */
				int xDiff = pressedX - topMostComponent.getX();
				int yDiff = pressedY - topMostComponent.getY();
												
				//Clipboard contents are all offset by the above calculated difference
				//The new top-most component is calculated
				for (Component comp: clipboard)
				{
					comp.setLocation(comp.getX()+xDiff, comp.getY()+yDiff);
					
					//Recalculated top-most component used as the basis for the next paste
					if(comp.getY() < topMostComponent.getY())
						topMostComponent = comp;
					
					setListeners(comp);
					comp.setVisible(true);
					mainPanel.add(comp);
					selectedComponents.add(comp);
					
					components.add(comp);
				}
							
				//Ensures that pasted components are selected after paste
				if (selectedComponents.size() == 1)
				{
					currentStatus = COMPONENT_SELECTED_STATUS;
					selectedComponents.get(0).dispatchEvent(new MouseEvent(selectedComponents.get(0), MouseEvent.MOUSE_CLICKED, 0, MouseEvent.BUTTON1_MASK, selectedComponents.get(0).getX(), selectedComponents.get(0).getY(), 0, false));
					setStatus("Component selected. Click and drag to reposition");
				}
				
				else
				{
				currentStatus = MULTIPLE_COMPONENTS_SELECTED_STATUS;
				setStatus("Multiple components selected. Click and drag to move or right-click for context menu");
				}
				
				mainPanel.repaint();
				
				}
				//A temporary buffer that holds the contents of the clipboard while it's
				//repopulated
				ArrayList<Component> buffer = new ArrayList<Component>();

				
				//Flush the clipboard and add new copies for subsequent pastes
				buffer.addAll(clipboard);
				clipboard.clear();
				
				//Choose another random component as the top-most component
				topMostComponent = buffer.get(0);
				for (Component comp: buffer)
				{
					//Recalculate the top-most component. This is necessary for multiple
					//pastes based on a single copy
					if(comp.getY() < topMostComponent.getY())
						topMostComponent = comp;
					
					clipboard.add(copyComponent(comp));
					
				}
				//Flush the buffer
				buffer.clear();
				
				mainPanel.repaint();
				//Makes text fields and text areas uneditable
				createButton_button.requestFocus();
			}
		}
		
		/*
		 * Handles deleting a component via the context menu
		 */
		if (e.getSource() == delete_context)
		{
			actionPerformed(new ActionEvent(deleteButton, 0, null));
		}
	}

	public void mouseClicked(MouseEvent m) {
		if (m.getButton() == MouseEvent.BUTTON3)
			return;
		
		
		/*	Deselects the currently selected component(s) if the user clicks 
		 * 	anywhere on the main Panel
		 */
		if(currentStatus == COMPONENT_SELECTED_STATUS || currentStatus == MULTIPLE_COMPONENTS_SELECTED_STATUS)
		{
			selectedComponents.clear();
			resetStatus();
			clearPropertiesFields();
			updateButton.setEnabled(false);
		}

		/*
		 * Calls the component creation method when a user just clicks on the main panel,
		 * as opposed to clicking and dragging
		 */
		if (currentStatus != NORMAL_STATUS) {
			pressedX = m.getX();
			pressedY = m.getY();
			createComponent();
		}
	}

	public void mouseEntered(MouseEvent m) {

	}

	public void mouseExited(MouseEvent m) {

	}

	public void mousePressed(MouseEvent m) {
		if (currentStatus == NORMAL_STATUS && m.getSource() == mainPanel && m.getButton() == MouseEvent.BUTTON1)
			currentStatus = DRAG_MODE_STATUS;
		
		/*
		 * When the mouse is pressed with the intention of dragging, the initial coordinates
		 * of the press are recorded and stored in pressedX and pressedY
		 */
		pressedX = m.getX();
		pressedY = m.getY();
		
		currentX = pressedX;
		currentY = pressedY;

	}

	public void mouseReleased(MouseEvent m) {
		
		if (currentStatus == NORMAL_STATUS || m.getSource() != mainPanel || currentStatus == COMPONENT_SELECTED_STATUS)
			return;
		
		/*
		 * When the mouse is released after dragging, the final coordinates
		 * of the release are recorded and stored in releasedX and releasedY
		 */
		releasedX = m.getX();
		releasedY = m.getY();
		
		//Mouse drag finished, call component creation method
		if( currentStatus != DRAG_MODE_STATUS && currentStatus != MULTIPLE_COMPONENTS_SELECTED_STATUS)
			createComponent();

		/*
		 * This is for selecting one or more components by dragging a selection box on the main frame.
		 * The rectangle bounds are known (and drawn) so determining if any components are within the
		 * rectangle is trivial
		 */
		if (currentStatus == DRAG_MODE_STATUS)
		{
			for (Component comp: components)
			{
				
				if (draggedRect.contains(comp.getX(), comp.getY()) || draggedRect.contains(comp.getX()+comp.getWidth(),comp.getY()+comp.getHeight()))
				{
					selectedComponents.add(comp);
				}
			}
			
			if (selectedComponents.size() == 0)
				resetStatus();
			
			else if (selectedComponents.size() == 1)
			{
				selectedComponents.get(0).dispatchEvent(new MouseEvent(selectedComponents.get(0), MouseEvent.MOUSE_CLICKED, 0, MouseEvent.BUTTON1_MASK, selectedComponents.get(0).getX(), selectedComponents.get(0).getY(), 0, false));
				//selectedComponents.clear();
			}
			
			else
			{
				currentStatus = MULTIPLE_COMPONENTS_SELECTED_STATUS;
				setStatus("Multiple components selected. Click and drag to move or right-click for context menu");
			}
		}
	}

	public void mouseDragged(MouseEvent m) {
		
		/* 
		 * The following 2 if statements disallow the creation of components with
		 * negative dimensions
		 */
		if (currentStatus != DRAG_MODE_STATUS) {
			if (m.getX() < pressedX) {
				currentX = pressedX;
				return;
			}

			if (m.getY() < pressedY) {
				currentY = pressedY;
				return;
			}
		}
		
		/*
		 * Constantly update currentX and currentY with the current mouse coordinates
		 * while dragging
		 */
		currentX = m.getX();
		currentY = m.getY();
		mainPanel.repaint();
	}

	public void mouseMoved(MouseEvent m) {
		/*
		 * Keep track of where the mouse currently is so that the placement crosshair can be drawn
		 */
		mouseLocationX = m.getX();
		mouseLocationY = m.getY();
		mainPanel.repaint();
	}

	/**
	 * Resets key variables to put the program in more of an initial state
	 */
	public void resetPoints() {
	pressedX = 0;
	pressedY = 0;
	currentX = 0;
	currentY = 0;
	releasedX = 0;
	releasedY = 0;
	
	draggedRect = new Rectangle(0,0,0,0);
	}
	
	
	/**
	 * If a component is selected, 'deletes' said component by doing the following:
	 * -Making it invisible
	 * -Disabling it
	 * -Calling its removeNotify() method, which destroys its native screen resource
	 * -Moving it far off screen so that it doesn't interfere with normal program flow
	 * 
	 * Note: dispose() is not available for components
	 */
	public void deleteComponent(Component comp)
	{
		comp.setVisible(false);
		comp.setEnabled(false);
		comp.removeNotify();
		comp.setLocation(-999, -999);
		components.remove(comp);
		clearPropertiesFields();
		//resetStatus();
		return;
	}
	
	/**
	 * Clears and disables every property field
	 */
	public void clearPropertiesFields()
	{	
	xField.setText("");
	xField.setEditable(false);
	yField.setText("");
	yField.setEditable(false);
	widthField.setText("");
	widthField.setEditable(false);
	heightField.setText("");
	heightField.setEditable(false);
	textField.setText("");
	textField.setEditable(false);
	actionListenerBox.setSelected(false);
	actionListenerBox.setEnabled(false);
	}
	
	/**
	 * Updates every property field to reflect the properties of the currently
	 * selected component
	 */
	public void setPropertiesFields()
	{
		if (selectedComponent != null)
		{
		xField.setText(""+selectedComponent.getX());
		yField.setText(""+selectedComponent.getY());;
		widthField.setText(""+selectedComponent.getWidth());
		heightField.setText(""+selectedComponent.getHeight());
		
		if (selectedComponent instanceof JButton)
		{
		textField.setText(((JButton) selectedComponent).getText());
		}
		
		if (selectedComponent instanceof JLabel)
		{
		textField.setText(((JLabel) selectedComponent).getText());
		}
		
		if (selectedComponent instanceof JCheckBox)
		{
		textField.setText(((JCheckBox) selectedComponent).getText());
		}
		
		if (selectedComponent instanceof JTextField || selectedComponent instanceof JTextArea)
		{
			textField.setText("");
		}
		
		if (selectedComponent instanceof JComboBox)
		{
			textField.setText("");
			for (int i = 0; i < ((JComboBox) selectedComponent).getItemCount(); i++)
			{
				textField.setText(textField.getText() + ((JComboBox) selectedComponent).getItemAt(i) + ",");
			}
			
			// Remove the final comma from the text field
			if (textField.getText().endsWith(","))
				textField.setText(textField.getText().substring(0, textField.getText().length()-1));
		}
		
		
		if (selectedComponent.getName() != null)
			actionListenerBox.setSelected(true);
		}
	}
	
	public void setListeners(Component comp)
	{
		/**
		 * The component's MouseListeners tell the program when the component is selected
		 * and all the code that handles component manipulation is housed here
		 */
		comp.addMouseListener(new MouseListener() {
			
			public void mouseClicked(MouseEvent m2) {
				
				//Right-mouse button was clicked, therefore show context menu
				if (m2.getButton() == MouseEvent.BUTTON3)
				{
					showContext(m2);
					return;
				}
				//component was clicked
				if (selectedComponent == null || (selectedComponent != m2.getComponent()) && m2.getButton() == MouseEvent.BUTTON1)
				{
						selectedComponent = m2.getComponent();
						selectedComponents.add(m2.getComponent());
						setStatus("Component selected. Click and drag to reposition");
						currentStatus = COMPONENT_SELECTED_STATUS;
						setPropertiesFields();
						xField.setEditable(true);
						yField.setEditable(true);
						widthField.setEditable(true);
						heightField.setEditable(true);
						actionListenerBox.setEnabled(true);
						updateButton.setEnabled(true);

						if (m2.getComponent() instanceof JButton) {
							textField.setEditable(true);
							textField.setEnabled(true);
							textField.setText(((JButton) m2.getComponent())
									.getText());
						}

						if (m2.getComponent() instanceof JLabel) {
							textField.setEditable(true);
							textField.setEnabled(true);
							textField.setText(((JLabel) m2.getComponent())
									.getText());
						}
						
						if (m2.getComponent() instanceof JCheckBox) {
							textField.setEditable(true);
							textField.setEnabled(true);
							textField.setText(((JCheckBox) m2.getComponent())
									.getText());
						}
						
						if (m2.getComponent() instanceof JComboBox)
						{
							textField.setEditable(true);
							textField.setEnabled(true);
						}
					
					createButton_button.requestFocus();
				}
								
				//The component was selected and then clicked again, which deselects it
				else if (m2.getButton() == MouseEvent.BUTTON1)
				{
					selectedComponent = null;
					clearPropertiesFields();
					updateButton.setEnabled(false);
					resetStatus();
				}
				mainPanel.repaint();
			}

			public void mouseEntered(MouseEvent m2) {

			}

			public void mouseExited(MouseEvent m2) {

			}

			public void mousePressed(MouseEvent m2) {
				//When about to drag one or more components, its/their location(s) are stored
				//in case a reset is needed
				originalComponentLocations = new ArrayList<Point>();
				
				for (int i = 0; i < selectedComponents.size(); i++)
					originalComponentLocations.add(selectedComponents.get(i).getLocation());
				 
				/*
				  * The coordinates of the mouse cursor relative to the source component
				  * (i.e where exactly on the component you clicked) are stored the first
				  * time that the component is clicked. These 2 coordinates assist with
				  * component dragging. Once the mouse is dragged, firstTick is set to false
				  * and these coordinates don't get updated again until mouse release.
				  */
				
				 if (firstTick)
				 {
				 initialClickedComponentX = m2.getX();
				 initialClickedComponentY = m2.getY();
				 }
				 /*//TODO: find out why this isn't working!
				 if (selectedComponent != m2.getComponent())
					{
						if(currentStatus != DELETE_MODE_STATUS)
						{
						selectedComponent = m2.getComponent();
						setStatus("Component selected. Click and drag to reposition");
						currentStatus = COMPONENT_SELECTED_STATUS;
						setPropertiesFields();
						xField.setEditable(true);
						yField.setEditable(true);
						widthField.setEditable(true);
						heightField.setEditable(true);
						actionListenerBox.setEnabled(true);
						updateButton.setEnabled(true);
						
						if (m2.getComponent() instanceof JButton)
						{
						textField.setEditable(true);
						textField.setEnabled(true);
						textField.setText(((JButton) m2.getComponent()).getText());
						}
						
						if (m2.getComponent() instanceof JLabel)
						{
						textField.setEditable(true);
						textField.setEnabled(true);
						textField.setText(((JLabel) m2.getComponent()).getText());
						}
						}
					}
					*/
			}

			public void mouseReleased(MouseEvent m2) {
				
				/*
				 * When the mouse is released, a check is done to determine if all components are
				 * now within the bounds of the main panel. If not, their positions are reset to where
				 * they were before they were dragged.
				 */
				
				for (int i = 0; i < selectedComponents.size(); i++)
				{
				Component comp = selectedComponents.get(i);
				//Check if component is within bounds
				if (!mainPanel.contains(comp.getLocation()) || !mainPanel.contains(comp.getX()+comp.getWidth(), comp.getY()+comp.getHeight()))
				{
					//If any component is out of bounds, all component locations are reset
					for (int j = 0; j < selectedComponents.size(); j++)
						selectedComponents.get(j).setLocation(originalComponentLocations.get(j).getLocation());
					
					
					//prevents frame tearing from the JOptionPane
					mainPanel.repaint();
					JOptionPane.showMessageDialog(null, "One or more components were out of bounds! Component locations reset.");
					break;
				}
				}
				
				if(selectedComponent != null)
				{
					setPropertiesFields();
				}
				mainPanel.repaint();
			}
			
			
		});
		
		/**
		 * This is the code that handles repositioning the component by dragging the mouse.
		 * The calculation is done by calculating relative X and Y coordinates.
		 */
		comp.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent m2) {
				
				Component clickedComponent = m2.getComponent();
				
				/*
				 * This code is responsible for dragging components when more than 1 is
				 * selected. It does this by calculating how the clicked component
				 * moved and applies this differential to the x- and y-coordinates of
				 * all the other selected components
				 */
				if (currentStatus == MULTIPLE_COMPONENTS_SELECTED_STATUS)
				{
				int xDiff, yDiff;
				
				int clickedCompX = clickedComponent.getX();
				int clickedCompY = clickedComponent.getY();
				
				clickedComponent.setLocation(m2.getXOnScreen()-toolPanel.getWidth()-(mainPanel.getX()-(toolPanel.getX()+toolPanel.getWidth())) - initialClickedComponentX, m2.getYOnScreen()-menuBar.getHeight()-30-(mainPanel.getY()-toolPanel.getY()) - initialClickedComponentY);
				
				xDiff = clickedComponent.getX()-clickedCompX;
				yDiff = clickedComponent.getY()-clickedCompY;
				
				for (Component comp: selectedComponents)
				{
					if (comp != clickedComponent)
						comp.setLocation(comp.getX()+xDiff, comp.getY()+yDiff);
				}

				}
				
				if (selectedComponent == clickedComponent)
				{
					m2.getComponent().setBounds(m2.getXOnScreen()-toolPanel.getWidth()-(mainPanel.getX()-(toolPanel.getX()+toolPanel.getWidth())) - initialClickedComponentX, m2.getYOnScreen()-menuBar.getHeight()-30-(mainPanel.getY()-toolPanel.getY()) - initialClickedComponentY, clickedComponent.getWidth(), clickedComponent.getHeight());
					
					setPropertiesFields();
				}

				mainPanel.repaint();
			}

			public void mouseMoved(MouseEvent m2) {

			}

		});
		
	}
	
	/*
	 * The maximum allowed component size is set to the width and height of the main panel
	 * minus 10 pixels 
	 * 
	 */
	public int getMaximumComponentWidth()
	{
		return (int)mainPanel.getWidth()-10;
	}
	
	public int getMaximumComponentHeight()
	{
		return mainPanel.getHeight()-10;
	}
	
	public Dimension getMaximumComponentSize()
	{
		return new Dimension(getMaximumComponentWidth(), getMaximumComponentHeight());
	}
	

	/**
	 * This method handles component creation. The process is as follows:
	 * -Component width and height is calculated from the rectangle that was created
	 * 		by dragging the mouse. If the mouse was just clicked as opposed to dragged,
	 * 		the width and height are set to the minimum allowed dimensions
	 * -The type of component is determined by the current status of the program, which
	 * 		is dependant on which button was clicked
	 * -The component is added to the main panel
	 * -A MouseListener and MouseMotionListener are added to the component
	 * -The component information is stored in an ArrayList 
	 */
	public void createComponent() {
		Component comp = null;
		
		/*
		 * Calculate component width and height from the dimensions of the dragged rectangle 
		 */
		int width = releasedX-pressedX;
		int height = releasedY-pressedY;
		
		/*
		 * Ensure that the component meets both minimum and maximum size standards
		 */
		if (width < COMPONENT_MINIMUM_SIZE)
			width = COMPONENT_MINIMUM_SIZE;
		
		else if (width > getMaximumComponentWidth())
			width = getMaximumComponentWidth();
		
		if (height < COMPONENT_MINIMUM_SIZE)
			height = COMPONENT_MINIMUM_SIZE;
		
		else if (height > getMaximumComponentHeight())
			height = getMaximumComponentHeight();
		
		/*
		 * Determine the component type by looking at which button was pressed, then
		 * set the bounds of the component. Note that some component's width is dynamically 
		 * determined by the length of its text
		 */
		switch (currentStatus) {
		case CREATE_BUTTON_STATUS:
			comp = createButton();
			comp.setBounds(pressedX, pressedY, width, height);
			break;
			
		case CREATE_LABEL_STATUS:
			comp = createLabel();
			comp.setBounds(pressedX, pressedY, Math.max(comp.getPreferredSize().width, COMPONENT_MINIMUM_SIZE), COMPONENT_MINIMUM_SIZE);
			break;

		case CREATE_TEXT_FIELD_STATUS:
			comp = createTextField();
			comp.setBounds(pressedX, pressedY, width, COMPONENT_MINIMUM_SIZE);//comp.getPreferredSize().height);
			break;
			
		case CREATE_TEXT_AREA_STATUS:
			comp = createTextArea();
			comp.setBounds(pressedX, pressedY, width, height);
			((JTextArea) comp).setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			break;
			
		case CREATE_CHECK_BOX_STATUS:
			comp = createCheckBox();
			comp.setEnabled(false);
			comp.setBounds(pressedX, pressedY, comp.getPreferredSize().width, Math.max(comp.getPreferredSize().height, COMPONENT_MINIMUM_SIZE));
			break;
			
		case CREATE_COMBO_BOX_STATUS:
			comp = createComboBox();
			comp.setEnabled(false);
			comp.setBounds(pressedX, pressedY, comp.getPreferredSize().width, comp.getPreferredSize().height);
			break;
		}
		
		//Was component created within the bounds of the main panel? If not, destroy it
		Point lowerRightCorner = new Point(comp.getX()+comp.getWidth(), comp.getY() + comp.getHeight());
		if (!mainPanel.contains(lowerRightCorner))
		{
			JOptionPane.showMessageDialog(null, "Component creation is out of bounds (Possibly due to text width). Component will be destroyed");
			deleteComponent(comp);
			return;
		}
		
		comp.setFocusable(false);
		//comp.setEnabled(false);
		
		//add the component to the main panel
		mainPanel.add(comp);
		mainPanel.repaint();
	
		setListeners(comp);
		
		//((JComponent) comp).setComponentPopupMenu(contextMenu);
		
		//Store component information in an ArrayList
		components.add(comp);

		resetPoints();
		resetStatus();
	}
	
	/**
	 * Creates a copy of the specified component. This is useful for instances like copy-pasting, as it
	 * avoids problems that arise from certain situations e.g pasting after having deleted the source
	 * components.
	 */
	public Component copyComponent(Component comp)
	{
		Component component = null;
		
		if (comp instanceof JButton)
		{
			component = new JButton(((JButton) comp).getText());
			if (comp.getName() != null)
				component.setName(comp.getName());
		}
		
		else if (comp instanceof JLabel)
		{
			component = new JLabel(((JLabel) comp).getText());
		}
		
		else if (comp instanceof JTextField)
		{
			component = new JTextField();
			component.setEnabled(false);
			((JTextField) component).setBorder(BorderFactory.createLineBorder(Color.BLACK));

		}
		
		else if (comp instanceof JTextArea)
		{
			component = new JTextArea();
			((JTextArea) component).setBorder(BorderFactory.createLineBorder(Color.BLACK));
			component.setEnabled(false);
		}
		
		else if (comp instanceof JCheckBox)
		{
			component = new JCheckBox(((JCheckBox) comp).getText());
			component.setEnabled(false);
		}
		
		else if (comp instanceof JComboBox)
		{
			component = new JComboBox();
			component.setEnabled(false);

			String[] items = new String[((JComboBox) comp).getItemCount()];
			for (int i = 0; i < items.length; i++)
				items[i] = (String) ((JComboBox) comp).getItemAt(i);
			
			for (int i = 0; i < items.length; i++)
				((JComboBox) component).addItem(items[i]);
	
		}
		
		component.setBounds(comp.getBounds());
		return component;
	}

	/**
	 * Returns a new JButton object, the text of which is determined by a JOptionPane
	 */
	public Component createButton() {
		String name = JOptionPane.showInputDialog(null, "Enter button text");
		if (name != null && name.contains("\""))
		{
			JOptionPane.showMessageDialog(null, "Quotation marks not allowed and were therefore removed.");
		
		name = name.replace("\"", "");
		}
		if (name == null || name.equals(""))
			name = "Unlabelled";
		return new JButton(name);
	}
	
	/**
	 * Returns a new JLabel object, the text of which is determined by a JOptionPane. If the 
	 * label has no associated text, the text is set to "unlabelled"
	 * 
	 */
	public Component createLabel() {
		String labelText = JOptionPane.showInputDialog(null, "Enter text for the label");
		
		if (labelText != null && labelText.contains("\""))
		{
			JOptionPane.showMessageDialog(null, "Quotation marks not allowed and were therefore removed.");
		
		labelText = labelText.replace("\"", "");
		}
		
		if (labelText == null || labelText.equals(""))
			labelText = "Unlabelled";
		
		return new JLabel(labelText);
	}

	/**
	 * Returns a new JTextField object
	 */
	public Component createTextField() {

		return new JTextField();
	}
	
	/**
	 * Returns a new JTextArea object
	 */
	public Component createTextArea()
	{
		return new JTextArea();
	}
	
	/**
	 * Returns a new JCheckBox object
	 */
	public Component createCheckBox()
	{
		String labelText = JOptionPane.showInputDialog(null, "Enter text for the checkbox");
		
		if (labelText != null && labelText.contains("\""))
		{
			JOptionPane.showMessageDialog(null, "Quotation marks not allowed and were therefore removed.");
		
		labelText = labelText.replace("\"", "");
		}
		
		if (labelText == null || labelText.equals(""))
			labelText = "Unlabelled";
		
		return new JCheckBox(labelText);
	}
	
	/**
	 * Returns a new JComboBox object with values that are comma-separated. An empty check box will
	 * be returned if no values are specified
	 */
	public Component createComboBox()
	{
		String[] items = null;
		String input = JOptionPane.showInputDialog(null, "Enter a comma-separated list of values to populate the combo box:");
		
		while (input != null && input.contains("\""))
			input = JOptionPane.showInputDialog(null, "Quotation marks not allowed. Please remove them.", input);
		
		if(input != null)
		{
			items = input.split(",");
			return new JComboBox(items);

		}
		return new JComboBox();
	}
	
	public static String getVersion()
	{
		return version;
	}
	
	public void keyPressed(KeyEvent k) {
		
	}

	public void keyReleased(KeyEvent k) {
		
	}

	/**
	 * Allows the use of "enter" to commit changes to the property fields. All it does is simulate
	 * clicking on the update button
	 */
	public void keyTyped(KeyEvent k) {
		if (selectedComponent == null)
			return;
	
		if (k.getKeyChar() == KeyEvent.VK_ENTER)
			actionPerformed(new ActionEvent(updateButton, 1, null));	
	}

	/**
	 * 
	 * @author Peter Pretorius
	 * This nested class is a simple extension of JPanel that makes drawing to the main panel
	 * easier. The parameters for all graphical operations are determined by the status of the
	 * main program
	 *
	 */
	@SuppressWarnings("serial")
	private class DisplayPanel extends JPanel{
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			//It's convenient for this variable to be updated whenever the panel repaints,
			//hence why it's placed here.
			originalPanelSize.setSize(getParent().getWidth()-toolPanel.getWidth()-propertiesPanel.getWidth(), getParent().getHeight()-statusBar.getHeight());
			
			if (currentStatus == NORMAL_STATUS && selectedComponent == null)
				return;
			
			//Controls the rectangle that's drawn when in drag mode
			if (currentStatus == DRAG_MODE_STATUS)
			{
				//controls drawing the rectangle from right to left, top to bottom
				if (currentX < pressedX && currentY > pressedY)
				{
					draggedRect = new Rectangle(currentX, pressedY, pressedX-currentX, currentY-pressedY);
					g.drawRect(draggedRect.x, draggedRect.y, draggedRect.width, draggedRect.height);
				}

				//controls drawing the rectangle from left to right, bottom to top
				if (currentX > pressedX && currentY < pressedY)
				{
					draggedRect = new Rectangle(pressedX, currentY, currentX-pressedX, pressedY-currentY);
					g.drawRect(draggedRect.x, draggedRect.y, draggedRect.width, draggedRect.height);
				}
					
				//controls drawing the rectangle from left to right, top to bottom
				if (currentX > pressedX && currentY > pressedY)
				{
					draggedRect = new Rectangle(pressedX, pressedY, currentX-pressedX, currentY-pressedY);
					g.drawRect(draggedRect.x, draggedRect.y, draggedRect.width, draggedRect.height);
				}
				
				//controls drawing the rectangle from right to left, bottom to top
				if (currentX < pressedX && currentY < pressedY)
				{
					draggedRect = new Rectangle(currentX, currentY, pressedX-currentX, pressedY-currentY);
					g.drawRect(draggedRect.x, draggedRect.y, draggedRect.width, draggedRect.height);
				}

			}
			
			if (currentStatus == MULTIPLE_COMPONENTS_SELECTED_STATUS)
			{
				for (Component comp: selectedComponents)
				{
					if (comp.isVisible())
					{
					g.setColor(Color.red);
					g.drawRect(comp.getX()-1, comp.getY()-1, comp.getWidth()+1, comp.getHeight()+1);
					}
				}
			}
			
			/*
			 * Depending on the status of the program, draws the placeholder rectangle that
			 * indicates what the final dimensions of the created component will be . The width
			 * of this rectangle is determined by currentX-pressedX and the height is determined
			 * by currentY-pressedY. The height of the rectangle when creating a text field is limited 
			 * for obvious reasons
			 * 
			 * Also draws a crosshair at the mouse position that assists with component positioning
			 */
			switch (currentStatus)
			{
			case CREATE_TEXT_FIELD_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);
				g.drawRect(pressedX, pressedY, currentX - pressedX, COMPONENT_MINIMUM_SIZE);
				break;
				
			case CREATE_LABEL_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);
				break;
				
			case CREATE_BUTTON_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);
				g.drawRect(pressedX, pressedY, currentX - pressedX, currentY - pressedY);
				break;
				
			case CREATE_TEXT_AREA_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);
				g.drawRect(pressedX, pressedY, currentX - pressedX, currentY - pressedY);
				break;
				
			case CREATE_CHECK_BOX_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);

				break;
			case CREATE_COMBO_BOX_STATUS:
				g.drawLine(mouseLocationX-5, mouseLocationY, mouseLocationX+5, mouseLocationY);
				g.drawLine(mouseLocationX, mouseLocationY-5, mouseLocationX, mouseLocationY+5);
				break;
			}
			
			//Are trace lines enabled? (Yes by default)
			if (drawTraceLines) {
				//These trace lines are drawn during component creation
				if (currentStatus != NORMAL_STATUS && currentStatus != DRAG_MODE_STATUS && currentStatus != MULTIPLE_COMPONENTS_SELECTED_STATUS && selectedComponent == null)
				{
					g.setColor(Color.blue);
					for (Component comp: components)
					{
						//If the mouse cursor is in line with another component on the Y axis
						if (mouseLocationX == comp.getX() || mouseLocationX == comp.getX() + comp.getWidth())
							g.drawLine(mouseLocationX, 0, mouseLocationX, 1000);
						
						//If the mouse cursor is in line with another component on the X axis
						if (mouseLocationY == comp.getY() || mouseLocationY == comp.getY() + comp.getHeight())
							g.drawLine(0, mouseLocationY, 1000, mouseLocationY);
						
						//If when dragging the mouse cursor is in line with another component on the Y axis
						if (currentX == comp.getX() || currentX == comp.getX() + comp.getWidth())
							g.drawLine(currentX, 0, currentX, 1000);
						
						//If when dragging the mouse cursor is in line with another component on the X axis
						if (currentY == comp.getY() || currentY == comp.getY() + comp.getHeight())
							g.drawLine(0, currentY, 1000, currentY);
						
					}
				
				}
				
				/*
				 * If a component is selected, this draws the standard trace lines that
				 * are helpful for component positioning
				 */
				if (selectedComponent != null) {
					g.setColor(Color.red);

					g.drawLine(selectedComponent.getX(),
							selectedComponent.getY() - 1000,
							selectedComponent.getX(),
							selectedComponent.getY() + 1000);
					g.drawLine(selectedComponent.getX() - 1000,
							selectedComponent.getY(),
							selectedComponent.getX() + 1000,
							selectedComponent.getY());
					g.drawLine(
							selectedComponent.getX() + 1000,
							selectedComponent.getY()
									+ selectedComponent.getHeight() - 1,
							selectedComponent.getX() - 1000,
							selectedComponent.getY()
									+ selectedComponent.getHeight() - 1); // bottom
																			// horizontal
																			// trace
																			// line
					g.drawLine(
							selectedComponent.getX()
									+ selectedComponent.getWidth() - 1,
							selectedComponent.getY() + 1000,
							selectedComponent.getX()
									+ selectedComponent.getWidth() - 1,
							selectedComponent.getY() - 1000); // right vertical
																// trace line

				}
				/*
				 * Drawing of additional trace lines to help with component
				 * positioning. When components line up, the trace line changes
				 * color depending on orientation
				 */
				g.setColor(Color.green);
				if (selectedComponent != null)
					for (Component comp : components) {
						if (comp == selectedComponent)
							continue;

						// components line up vertically on the left side
						if (comp.getX() == selectedComponent.getX())
							g.drawLine(comp.getX(), 0, comp.getX(), 1000);

						// components line up vertically on the right side
						if (comp.getX() + comp.getWidth() == selectedComponent
								.getX() + selectedComponent.getWidth())
							g.drawLine(comp.getX() + comp.getWidth() - 1, 0,
									selectedComponent.getX()
											+ selectedComponent.getWidth() - 1,
									1000);

						g.setColor(Color.blue);

						// components line up horizontally on the top side
						if (comp.getY() == selectedComponent.getY())
							g.drawLine(0, comp.getY(), 1000,
									selectedComponent.getY());

						// components line up horizontally on the bottom side
						if (comp.getY() + comp.getHeight() == selectedComponent
								.getY() + selectedComponent.getHeight())
							g.drawLine(0, comp.getY() + comp.getHeight() - 1,
									1000, selectedComponent.getY()
											+ selectedComponent.getHeight() - 1);

						g.setColor(Color.magenta);

						// components line up vertically right-side to left-side
						if (selectedComponent.getX()
								+ selectedComponent.getWidth() == comp.getX())
							g.drawLine(selectedComponent.getX()
									+ selectedComponent.getWidth() - 1, 0,
									comp.getX() - 1, 1000);

						// components line up vertically left-side to right-side
						if (selectedComponent.getX() == comp.getX()
								+ comp.getWidth())
							g.drawLine(selectedComponent.getX(), 0, comp.getX()
									+ comp.getWidth(), 1000);

						// components line up horizontally top-side to
						// bottom-side
						if (selectedComponent.getY() == comp.getY()
								+ comp.getHeight())
							g.drawLine(0, selectedComponent.getY(), 1000,
									comp.getY() + comp.getHeight());

						// components line up horizontally bottom-side to
						// top-side
						if (selectedComponent.getY()
								+ selectedComponent.getHeight() == comp.getY())
							g.drawLine(0, selectedComponent.getY()
									+ selectedComponent.getHeight(), 1000,
									comp.getY());
					}
			}
		}
	}
}