package pe.com.unifiedgo.sales;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

@ApplicationScoped
@ComponentProvider.Qualifier("SSA_MODULE_PROVIDER")
public class SSAComponentProvider extends BaseComponentProvider {

  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources
        .add(createStaticResource("web/pe.com.unifiedgo.sales/js/execBOEMarkAsReceived.js", true));
    globalResources.add(
        createStaticResource("web/pe.com.unifiedgo.sales/js/execBOEUnmarkAsReceived.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.sales/js/execProjPropContractLines_VoidLine.js", true));
    globalResources.add(
        createStaticResource("web/pe.com.unifiedgo.sales/js/execApproveContractDiscLine.js", true));
    return globalResources;
  }

}