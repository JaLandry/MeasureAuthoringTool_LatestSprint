package mat.client.clause.clauseworkspace.view;

import mat.client.clause.clauseworkspace.model.CellTreeNode;
import mat.client.clause.clauseworkspace.presenter.XmlConversionlHelper;
import mat.client.clause.clauseworkspace.presenter.XmlTreeDisplay;
import mat.client.shared.MatContext;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
// TODO: Auto-generated Javadoc
/**
 * The Class PopulationWorkSpaceContextMenu.
 *
 * @author jnarang
 */
public class PopulationWorkSpaceContextMenu extends ClauseWorkspaceContextMenu {
	/**
	 * Stratum Node name.
	 */
	private static final String STRATUM = "Stratum";
	
	/** The Constant MEASURE_OBSERVATION. */
	private static final String MEASURE_OBSERVATION = "Measure Observation";
	/**
	 * Stratification Node name.
	 */
	private static final String STRATIFICATION = "Stratification";
	
	private static final String MEASURE_OBSERVATIONS = "Measure Observations";
	MenuItem viewHumanReadableMenu;
	/**
	 * Instantiates a new population work space context menu.
	 *
	 * @param treeDisplay - XmlTreeDisplay.
	 * @param popPanel - PopupPanel.
	 */
	
	public PopulationWorkSpaceContextMenu(XmlTreeDisplay treeDisplay, PopupPanel popPanel) {
		super(treeDisplay, popPanel);
		Command viewHumanReadableCmd = new Command() {
			@Override
			public void execute() {
				System.out.println("View Human Readable clicked...");
				popupPanel.hide();
				CellTreeNode selectedNode = xmlTreeDisplay.getSelectedNode();
				if ((selectedNode.getNodeType() == CellTreeNode.CLAUSE_NODE)
						|| (selectedNode.getNodeType() == CellTreeNode.SUBTREE_NODE)) {
					String xmlForPopulationNode = XmlConversionlHelper.createXmlFromTree(selectedNode);
					final String populationName = selectedNode.getName();
					String measureId = MatContext.get().getCurrentMeasureId();
					//					String url = GWT.getModuleBaseURL() + "export?id=" +measureId+ "&xml=" + xmlForPopulationNode+ "&format=subtreeHTML";
					//					Window.open(url + "&type=open", "_blank", "");
					xmlTreeDisplay.validatePopulationCellTreeNodes(xmlTreeDisplay.getSelectedNode());
					if (xmlTreeDisplay.isValidHumanReadable()) {
						MatContext.get().getMeasureService().getHumanReadableForNode(measureId, xmlForPopulationNode, new AsyncCallback<String>() {
							@Override
							public void onSuccess(String result) {
								showHumanReadableDialogBox(result, populationName);
							}
							@Override
							public void onFailure(Throwable caught) {
							}
						});
					} else {
						xmlTreeDisplay.getErrorMessageDisplay().setMessage(MatContext.get()
								.getMessageDelegate().getINVALID_LOGIC_POPULATION_WORK_SPACE());
					}
				}
			}
		};
		viewHumanReadableMenu = new MenuItem(template.menuTable("View Human Readable", ""), viewHumanReadableCmd);
	}
	/* (non-Javadoc)
	 * @see mat.client.clause.clauseworkspace.view.ClauseWorkspaceContextMenu#displayMenuItems(com.google.gwt.user.client.ui.PopupPanel)
	 */
	@Override
	public void displayMenuItems(final PopupPanel popupPanel) {
		popupMenuBar.clearItems();
		popupMenuBar.setFocusOnHoverEnabled(true);
		popupMenuBar.focus();
		popupPanel.clear();
		copyMenu.setEnabled(false);
		deleteMenu.setEnabled(false);
		pasteMenu.setEnabled(false);
		cutMenu.setEnabled(false);
		/*
		 * POC Global Copy Paste.
		 * copyToClipBoardMenu.setEnabled(false);
		pasteFromClipboardMenu.setEnabled(false);
		CellTreeNode copiedNode = MatContext.get().getCopiedNode();*/
		viewHumanReadableMenu.setEnabled(false);
		showHideExpandMenu();
		
		switch (xmlTreeDisplay.getSelectedNode().getNodeType()) {
			case CellTreeNode.MASTER_ROOT_NODE:
				if (xmlTreeDisplay.getSelectedNode().getName().equalsIgnoreCase(STRATIFICATION)) {
					Command addNodeCmd = new Command() {
						@Override
						public void execute() {
							xmlTreeDisplay.setDirty(true);
							popupPanel.hide();
							addMasterRootNodeTypeItem();
						}
					};
					addMenu = new MenuItem(getAddMenuName(xmlTreeDisplay.getSelectedNode().getChilds().get(0))
							, true, addNodeCmd);
					popupMenuBar.addItem(addMenu);
					popupMenuBar.addSeparator(separator);
					if ((xmlTreeDisplay.getCopiedNode() != null)
							&& (xmlTreeDisplay.getCopiedNode().getParent()
									.equals(xmlTreeDisplay.getSelectedNode()))) {
						pasteMenu.setEnabled(true);
					}
					/*
					 * POC Global Copy Paste.
					 * if ((copiedNode != null)
							&& (copiedNode.getParent()
									.equals(xmlTreeDisplay.getSelectedNode()))) {
						pasteMenu.setEnabled(true);
						pasteFromClipboardMenu.setEnabled(true);
					}*/
				}
				addCommonMenus();
				break;
			case CellTreeNode.ROOT_NODE:
				Command addNodeCmd = new Command() {
					@Override
					public void execute() {
						xmlTreeDisplay.setDirty(true);
						popupPanel.hide();
						addRootNodeTypeItem();
					}
				};
				
				//TODO by Ravi
				addMenu = new MenuItem(getAddMenuName(xmlTreeDisplay.getSelectedNode().getChilds().get(0))
						, true, addNodeCmd);
				popupMenuBar.addItem(addMenu);
				popupMenuBar.addSeparator(separator);
				addCommonMenus();
				addMenu.setEnabled(true);
				copyMenu.setEnabled(false);
				deleteMenu.setEnabled(false);
				if ((xmlTreeDisplay.getCopiedNode() != null)
						&& xmlTreeDisplay.getCopiedNode().getParent().equals(xmlTreeDisplay.getSelectedNode())) {
					pasteMenu.setEnabled(true);
				}
				/*
				 * POC Global Copy Paste.
				 * if ((copiedNode != null)
						&& copiedNode.getParent().getName().equals(xmlTreeDisplay.getSelectedNode().getName())) {
					pasteMenu.setEnabled(true);
					pasteFromClipboardMenu.setEnabled(true);
				}*/
				cutMenu.setEnabled(false);
				if (xmlTreeDisplay.getSelectedNode().getName().contains(STRATIFICATION)) {
					copyMenu.setEnabled(true);
					/*
					 * POC Global Copy Paste.
					 * copyToClipBoardMenu.setEnabled(true);*/
				}
				if ((xmlTreeDisplay.getSelectedNode().getName().contains(STRATIFICATION)
						|| xmlTreeDisplay.getSelectedNode().getName().contains(MEASURE_OBSERVATIONS))
						&& (xmlTreeDisplay.getSelectedNode().getParent().getChilds().size() > 1)) {
					deleteMenu.setEnabled(true);
				}
				break;
			case CellTreeNode.CLAUSE_NODE:
				if (xmlTreeDisplay.getSelectedNode().getName().contains(STRATUM)) {
					subMenuBar = new MenuBar(true);
					popupMenuBar.setAutoOpen(true);
					subMenuBar.setAutoOpen(true);
					createAddMenus(MatContext.get().logicalOps, CellTreeNode.LOGICAL_OP_NODE
							, subMenuBar); // creating logical Operators Menu 2nd level
					createAddClauseMenuItem(subMenuBar);
					addMenu = new MenuItem("Add", subMenuBar); // 1st level menu
					popupMenuBar.addItem(addMenu);
					if ((xmlTreeDisplay.getCopiedNode() != null)
							&& xmlTreeDisplay.getCopiedNode().getParent().
							equals(xmlTreeDisplay.getSelectedNode())) {
						pasteMenu.setEnabled(true);
					}
					/*
					 * POC Global Copy Paste.
					 * if ((copiedNode != null)
							&& copiedNode.getParent().
							equals(xmlTreeDisplay.getSelectedNode())) {
						pasteMenu.setEnabled(true);
						pasteFromClipboardMenu.setEnabled(true);
					}*/
				}
				//show the fist level menu to add clause
				if(xmlTreeDisplay.getSelectedNode().getName().contains(MEASURE_OBSERVATION)
						&& !xmlTreeDisplay.getSelectedNode().hasChildren()){
					subMenuBar = new MenuBar(true);
					popupMenuBar.setAutoOpen(true);
					subMenuBar.setAutoOpen(true);
					createAddClauseMenuItem(subMenuBar);
					addMenu = new MenuItem("Add", subMenuBar);
					popupMenuBar.addItem(addMenu);
				}
				addCommonMenus();
				//Add "View Human Readable" right click option for all populations: Start
				popupMenuBar.addItem(viewHumanReadableMenu);
				viewHumanReadableMenu.setEnabled(true);
				//Add "View Human Readable" right click option for all populations: End
				copyMenu.setEnabled(true);
				/*
				 * POC Global Copy Paste.
				 * copyToClipBoardMenu.setEnabled(true);*/
				//pasteMenu.setEnabled(false);
				if (xmlTreeDisplay.getSelectedNode().getParent().getChilds().size() > 1) {
					deleteMenu.setEnabled(true);
				}
				cutMenu.setEnabled(false);
				break;
			case CellTreeNode.LOGICAL_OP_NODE:
				subMenuBar = new MenuBar(true);
				popupMenuBar.setAutoOpen(true);
				subMenuBar.setAutoOpen(true);
				createAddMenus(MatContext.get().logicalOps, CellTreeNode.LOGICAL_OP_NODE
						, subMenuBar); // creating logical Operators Menu 2nd level
				createAddClauseMenuItem(subMenuBar);
				addMenu = new MenuItem("Add", subMenuBar); // 1st level menu
				popupMenuBar.addItem(addMenu);
				addCommonMenus();
				copyMenu.setEnabled(true);
				if ((xmlTreeDisplay.getCopiedNode() != null)
						&& (xmlTreeDisplay.getCopiedNode().getNodeType() != CellTreeNode.CLAUSE_NODE)
						&& (xmlTreeDisplay.getCopiedNode().getNodeType() != CellTreeNode.ROOT_NODE)) {
					pasteMenu.setEnabled(true);
				}
				/*
				 * POC Global Copy Paste.
				 * copyToClipBoardMenu.setEnabled(true);
				//can paste LOGOP,RELOP, QDM, TIMING & FUNCS
				if ((copiedNode != null)
						&& (copiedNode.getNodeType() != CellTreeNode.CLAUSE_NODE)
						&& (copiedNode.getNodeType() != CellTreeNode.ROOT_NODE)) {
					pasteMenu.setEnabled(true);
					pasteFromClipboardMenu.setEnabled(true);
				}*/
				if ((xmlTreeDisplay.getSelectedNode().getParent().getNodeType() != CellTreeNode.CLAUSE_NODE)
						|| (xmlTreeDisplay.getSelectedNode().getParent().getName().contains(STRATUM))
						|| (xmlTreeDisplay.getSelectedNode().getParent().getName().contains(MEASURE_OBSERVATION))) {
					deleteMenu.setEnabled(true);
				}
				if (((xmlTreeDisplay.getSelectedNode().getParent().getNodeType() != CellTreeNode.CLAUSE_NODE)
						&& (xmlTreeDisplay.getSelectedNode().getNodeType() == CellTreeNode.LOGICAL_OP_NODE))
						|| (xmlTreeDisplay.getSelectedNode().getParent().getName().contains(STRATUM))) {
					cutMenu.setEnabled(true);
					addMoveUpMenu(popupPanel);
					popupMenuBar.addItem(moveUpMenu);
					moveUpMenu.setEnabled(checkIfTopChildNode());
					addMoveDownMenu(popupPanel);
					popupMenuBar.addItem(moveDownMenu);
					moveDownMenu.setEnabled(checkIfLastChildNode());
					subMenuBar = new MenuBar(true);
					createEditMenus(MatContext.get().logicalOps, subMenuBar);
					editMenu = new MenuItem("Edit", true, subMenuBar);
					popupMenuBar.addItem(editMenu);
				}
				break;
			case CellTreeNode.TIMING_NODE:
				addCommonMenus();
				//commonMenuEnableState(false, false, true, false, true);
				copyMenu.setEnabled(false);
				cutMenu.setEnabled(false);
				deleteMenu.setEnabled(true);
				pasteMenu.setEnabled(false);
				expandMenu.setEnabled(true);
				break;
			case CellTreeNode.ELEMENT_REF_NODE:
				addCommonMenus();
				//commonMenuEnableState(false, false, true, false, true);
				copyMenu.setEnabled(false);
				cutMenu.setEnabled(false);
				deleteMenu.setEnabled(true);
				pasteMenu.setEnabled(false);
				expandMenu.setEnabled(true);
				break;
			case CellTreeNode.FUNCTIONS_NODE:
				addCommonMenus();
				//commonMenuEnableState(false, false, true, false, true);
				copyMenu.setEnabled(false);
				cutMenu.setEnabled(false);
				deleteMenu.setEnabled(true);
				pasteMenu.setEnabled(false);
				expandMenu.setEnabled(true);
				break;
			case CellTreeNode.RELATIONSHIP_NODE:
				addCommonMenus();
				//commonMenuEnableState(false, false, true, false, true);
				copyMenu.setEnabled(false);
				cutMenu.setEnabled(false);
				deleteMenu.setEnabled(true);
				pasteMenu.setEnabled(false);
				expandMenu.setEnabled(true);
				break;
			case CellTreeNode.SUBTREE_REF_NODE:
				subMenuBar = new MenuBar(true);
				popupMenuBar.setAutoOpen(true);
				subMenuBar.setAutoOpen(true);
				Command showClauseLogic = new Command() {
					@Override
					public void execute() {
						popupPanel.hide();
						ShowSubTreeLogicDialogBox.showSubTreeLogicDialogBox(xmlTreeDisplay, false);
					}
				};
				addMenu = new MenuItem("View Clause Logic", true, showClauseLogic);
				popupMenuBar.addItem(addMenu);
				popupMenuBar.addSeparator(separator);
				addCommonMenus();
				addMenu.setEnabled(true);
				addMoveUpMenu(popupPanel);
				popupMenuBar.addItem(moveUpMenu);
				moveUpMenu.setEnabled(checkIfTopChildNode());
				addMoveDownMenu(popupPanel);
				popupMenuBar.addItem(moveDownMenu);
				moveDownMenu.setEnabled(checkIfLastChildNode());
				deleteMenu.setEnabled(true);
				/*
				 * POC Global Copy Paste.
				 * copyToClipBoardMenu.setEnabled(true);*/
				Command editClauseCmd = new Command() {
					@Override
					public void execute() {
						popupPanel.hide();
						//To edit the Clause element
						SubTreeDialogBox.showSubTreeDialogBox(xmlTreeDisplay, false);
					}
				};
				editMenu = new MenuItem("Edit", true, editClauseCmd);
				popupMenuBar.addItem(editMenu);
				break;
			default:
				break;
		}
	}
	/**
	 * Common Menu's ie : (Cut/Paste/Delete/Copy/Expand) Enable/Disabled state.
	 * @param isCopyEnabled - boolean.
	 * @param isCutEnabled - boolean
	 * @param isDeleteEnabled - boolean
	 * @param isPasteEnabled - boolean
	 * @param isExpandEnabled - boolean
	 */
	@SuppressWarnings("unused")
	private void commonMenuEnableState(boolean isCopyEnabled , boolean isCutEnabled, boolean isDeleteEnabled
			, boolean isPasteEnabled , boolean isExpandEnabled) {
		copyMenu.setEnabled(isCopyEnabled);
		cutMenu.setEnabled(isCutEnabled);
		deleteMenu.setEnabled(isDeleteEnabled);
		pasteMenu.setEnabled(isPasteEnabled);
		expandMenu.setEnabled(isExpandEnabled);
	}
	/**
	 * Creates the add clause menu item.
	 *
	 * @param menuBar - MenuBar.
	 */
	private void createAddClauseMenuItem(MenuBar menuBar) {
		Command addClauseCmd = new Command() {
			@Override
			public void execute() {
				popupPanel.hide();
				//To show the Clause Logic on Population Workspace
				SubTreeDialogBox.showSubTreeDialogBox(xmlTreeDisplay, true);
			}
		};
		MenuItem item = new MenuItem("Clause", true, addClauseCmd);
		menuBar.addItem(item);
	}
	
}
