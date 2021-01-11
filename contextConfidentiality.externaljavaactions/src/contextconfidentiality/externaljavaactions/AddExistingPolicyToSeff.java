package contextconfidentiality.externaljavaactions;

import java.util.Collection;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.business.api.session.danalysis.DAnalysisSession;
import org.eclipse.sirius.diagram.DSemanticDiagram;
import org.eclipse.sirius.tools.api.ui.IExternalJavaAction;
import org.palladiosimulator.pcm.confidentiality.context.ConfidentialAccessSpecification;
import org.palladiosimulator.pcm.confidentiality.context.impl.ConfidentialAccessSpecificationImpl;
import org.palladiosimulator.pcm.confidentiality.context.specification.PolicySpecification.*;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import contextconfidentiality.service.ApplyProfilesStereotypes;
import contextconfidentiality.service.OpenPolicyDialog;
import contextconfidentiality.service.OpenResourceDialog;
import contextconfidentiality.service.PolicyVisibility;
import contextconfidentiality.service.OpenResourceDialog.ResourceObject;
import java.util.logging.Logger;

/** 
 * External Java Action that adds an existing Policy to a SEFF Diagram.
 * 
 * @author Cynthia Diedrich
 * 
 */
public class AddExistingPolicyToSeff implements IExternalJavaAction {
	private static Logger logger = Logger.getLogger(AddExistingPolicyToSeff.class.getName());
	
	public AddExistingPolicyToSeff()  { } 

	@Override
	public boolean canExecute(Collection<? extends EObject> arg0) {
		return true;
	}

	/** 
	 * Adds a selected Policy from a certain Resource to a SEFF Diagram.
	 * 
	 * @param parameters Following variables must be specified: container and containerView
	 * 
	 */
	@Override
	public void execute(Collection<? extends EObject> arg0, Map<String, Object> parameters) {
		ResourceDemandingSEFF seff = (ResourceDemandingSEFF) parameters.get("container");
		DSemanticDiagram seffDiagram = (DSemanticDiagram) parameters.get("containerView");

		ResourceObject confResourceObject = OpenResourceDialog.loadResource(seff, logger);
		ConfidentialAccessSpecification confRoot = (confResourceObject != null 
				&& confResourceObject.getRoot() instanceof ConfidentialAccessSpecificationImpl) 
				? (ConfidentialAccessSpecification) confResourceObject.getRoot() : null;
		
		EList <Policy> policyList = (confRoot != null) ? confRoot.getPolicyContainer().getPolicies() : null;
		Policy selectedPolicy = (policyList != null) 
				? (Policy) OpenPolicyDialog.loadPolicy(policyList) : null;
		
		if (selectedPolicy != null) {
			DAnalysisSession session = (DAnalysisSession) SessionManager.INSTANCE.getExistingSession(
					seffDiagram.eResource().getURI());	
						
			session.addSemanticResource(confResourceObject.getResource().getURI(), 
					new NullProgressMonitor());		
			session.save(new NullProgressMonitor());
			
			logger.info("Adding " + selectedPolicy.getEntityName() + " Policy to SEFF");			
			ApplyProfilesStereotypes.applyProfilesStereotypes(arg0, seff, selectedPolicy);
									
			DialectManager.INSTANCE.refresh(seffDiagram, new NullProgressMonitor());
			
			PolicyVisibility.showHideContainers(selectedPolicy, seffDiagram);
			
			session.save(new NullProgressMonitor());
			refreshProject(seffDiagram);
			
			logger.info("Successfully added Policy to SEFF");
		} else {
			logger.warning("Couldn't add Policy to SEFF - please check if the correct "
					+ "Resource was selected (Type: ConfidentialAccessSpecificationImpl)");
		}
	}
	
	public void refreshProject(DSemanticDiagram seffDiagram) {		
		try {
			String projectName = seffDiagram.eResource().getURI().segment(1);
			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = workspaceRoot.getProject(projectName);
			project.refreshLocal(IResource.DEPTH_ZERO, null);
	
		} catch (CoreException e) {
			logger.warning("Failed to refresh Project");
			e.printStackTrace();
		}
	}
}
