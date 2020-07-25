package contextconfidentiality.externaljavaactions;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.palladiosimulator.pcm.confidentiality.context.model.Direction;
import org.palladiosimulator.pcm.confidentiality.context.model.HierachicalContext;
import org.palladiosimulator.pcm.confidentiality.context.model.impl.HierachicalContextImpl;

/** 
 * External Java Action that enables the setting of the direction property of a Hierarchical Context Attribute 
 * over a dialog window.
 * 
 * @author Cynthia Diedrich
 * 
 */
public class SetDirection implements IExternalJavaAction {

	public SetDirection() { }

	@Override
	public boolean canExecute(Collection<? extends EObject> arg0) {
		return true;
	}
	
	/** 
	 * Opens a dialog window for the selection of a direction.
	 * 
	 * @param parameters Following variables must be specified: element
	 * 
	 */
	@Override
	public void execute(Collection<? extends EObject> arg0, Map<String, Object> parameters) {
		Object object = (Object) parameters.get("element");

		// Direction can only be set for Hierarchical Contexts
		if (object instanceof HierachicalContextImpl) {

			HierachicalContext semantic = (HierachicalContext)
					parameters.get("element");								
			Direction direction = loadDirection();
			
			if (direction != null) { 
				semantic.setIncludeDirection(direction); 
			}
		}
		
	}
	
	private static Direction loadDirection() {
		
		Shell shell = Display.getCurrent().getActiveShell(); 
		shell = getShell();
		shell.open();
		 	
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());

		dialog.setTitle("Set Direction");
		dialog.setMessage("Select a Direction (* = any string, ? = any char):");
		dialog.setElements(new Object[] { "TOPDOWN", "BOTTOMUP" });
		
		if (dialog.open() == Dialog.OK) {
			Object[] selection = dialog.getResult();
			String selectionName = selection[0].toString();
			
			if (selectionName.contentEquals("BOTTOMUP")) {
				Direction direction = Direction.BOTTOMUP;
				return direction;
			} else if (selectionName.contentEquals("TOPDOWN")) {
				Direction direction = Direction.TOPDOWN;
				return direction;
			}
		}		
		return null;
	}
	
	private static Shell getShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		  if (window == null) {
		    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		    if (windows.length > 0) {
		       return windows[0].getShell();
		    }
		  } else {
		    return window.getShell();
		  }
		  return null;
	}

}
