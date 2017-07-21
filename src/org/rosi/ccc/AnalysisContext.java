package org.rosi.ccc;

import java.lang.reflect.Method;

import org.graalvm.compiler.api.test.Graal;
import org.graalvm.compiler.java.GraphBuilderPhase;
import org.graalvm.compiler.nodes.PhiNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugins;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration.Plugins;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.PhaseSuite;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.phases.util.Providers;
import org.graalvm.compiler.runtime.RuntimeProvider;

import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class AnalysisContext {
	private final PhaseSuite<HighTierContext> m_suite;
	private final OptionValues m_options;
	private final HighTierContext m_context;
	private final MetaAccessProvider m_metaaccess;
	
	public AnalysisContext()
	{
		m_suite = new PhaseSuite<>();

		RuntimeProvider rt = Graal.getRequiredCapability(RuntimeProvider.class);
		Providers providers = rt.getHostBackend().getProviders();
		m_metaaccess = providers.getMetaAccess();

		Plugins plugins = new Plugins(new InvocationPlugins());
		GraphBuilderConfiguration config = GraphBuilderConfiguration.getDefault(plugins).withEagerResolving(true);
		m_suite.appendPhase(new GraphBuilderPhase(config));
		
		m_options = Graal.getRequiredCapability(OptionValues.class);
		m_context = new HighTierContext(providers, m_suite, OptimisticOptimizations.NONE);
	}
	
	public PhaseSuite<HighTierContext> getGraphBuilderSuite()
	{
		return m_suite;
	}
	
	public OptionValues getOptions()
	{
		return m_options;
	}
	
	public HighTierContext getContext()
	{
		return m_context;
	}
	
	public StructuredGraph getStructuredGraphFor(ResolvedJavaMethod method)
	{
		StructuredGraph graph = new StructuredGraph.Builder(
				m_options, 
				AllowAssumptions.YES
		).method(method).build();
		m_suite.apply(graph, m_context);
		graph.getNodes().filter(PhiNode.class).forEach(PhiNode::inferStamp);
		return graph;
	}

	public ResolvedJavaMethod findMethod(Class<?> declaringClass, String methodName) {
		Method reflectionMethod = null;
		for (Method m : declaringClass.getDeclaredMethods()) {
			if (m.getName().equals(methodName)) {
				assert reflectionMethod == null : "More than one method with name " + methodName + " in class "
						+ declaringClass.getName();
				reflectionMethod = m;
			}
		}
		assert reflectionMethod != null : "No method with name " + methodName + " in class " + declaringClass.getName();
		return m_metaaccess.lookupJavaMethod(reflectionMethod);
	}
}
