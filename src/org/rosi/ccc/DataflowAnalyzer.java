package org.rosi.ccc;

import java.lang.reflect.Method;
import java.util.ArrayDeque;

import org.graalvm.compiler.api.test.Graal;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.java.GraphBuilderPhase;
import org.graalvm.compiler.nodes.PhiNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration.Plugins;
import org.graalvm.compiler.nodes.graphbuilderconf.InvocationPlugins;
import org.graalvm.compiler.nodes.java.MethodCallTargetNode;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.PhaseSuite;
import org.graalvm.compiler.phases.tiers.HighTierContext;
import org.graalvm.compiler.phases.util.Providers;
import org.graalvm.compiler.runtime.RuntimeProvider;

import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class DataflowAnalyzer {

	static MetaAccessProvider metaAccess;
	static ArrayDeque<MethodCallTargetNode> toVisit;

	public static void main(String[] args) {

		toVisit = new ArrayDeque<>();

		RuntimeProvider rt = Graal.getRequiredCapability(RuntimeProvider.class);
		Providers providers = rt.getHostBackend().getProviders();
		metaAccess = providers.getMetaAccess();

		OptionValues options = Graal.getRequiredCapability(OptionValues.class);

		ResolvedJavaMethod method = findMethod(TestClass.class, "call");

		PhaseSuite<HighTierContext> graphBuilderSuite = new PhaseSuite<>();
		Plugins plugins = new Plugins(new InvocationPlugins());
		GraphBuilderConfiguration config = GraphBuilderConfiguration.getDefault(plugins).withEagerResolving(true);
		graphBuilderSuite.appendPhase(new GraphBuilderPhase(config));
		HighTierContext context = new HighTierContext(providers, graphBuilderSuite, OptimisticOptimizations.NONE);

		StructuredGraph graph = new StructuredGraph.Builder(options, AllowAssumptions.YES).method(method).build();
		graphBuilderSuite.apply(graph, context);
		graph.getNodes().filter(PhiNode.class).forEach(PhiNode::inferStamp);

		for (Node node : graph.getNodes()) {
			if (node instanceof MethodCallTargetNode) {
				MethodCallTargetNode target = (MethodCallTargetNode) node;
				toVisit.add(target);
			}
		}

		while (!toVisit.isEmpty()) {
			MethodCallTargetNode mctn = toVisit.pop();
			System.out.println(mctn.targetName());
			graph = new StructuredGraph.Builder(options, AllowAssumptions.YES).method(mctn.targetMethod())
					.build();
			graphBuilderSuite.apply(graph, context);
			graph.getNodes().filter(PhiNode.class).forEach(PhiNode::inferStamp);
			
			for (Node node : graph.getNodes()) {
				if (node instanceof MethodCallTargetNode) {
					MethodCallTargetNode target = (MethodCallTargetNode) node;
					toVisit.add(target);
				}
			}
		}

	}

	private static ResolvedJavaMethod findMethod(Class<?> declaringClass, String methodName) {
		Method reflectionMethod = null;
		for (Method m : declaringClass.getDeclaredMethods()) {
			if (m.getName().equals(methodName)) {
				assert reflectionMethod == null : "More than one method with name " + methodName + " in class "
						+ declaringClass.getName();
				reflectionMethod = m;
			}
		}
		assert reflectionMethod != null : "No method with name " + methodName + " in class " + declaringClass.getName();
		return metaAccess.lookupJavaMethod(reflectionMethod);
	}

}
