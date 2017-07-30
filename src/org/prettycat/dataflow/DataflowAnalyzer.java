package org.prettycat.dataflow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.graalvm.compiler.api.test.Graal;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.java.GraphBuilderPhase;
import org.graalvm.compiler.nodeinfo.Verbosity;
import org.graalvm.compiler.nodes.AbstractBeginNode;
import org.graalvm.compiler.nodes.PhiNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;
import org.graalvm.compiler.nodes.cfg.Block;
import org.graalvm.compiler.nodes.cfg.ControlFlowGraph;
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
import org.prettycat.examples.test.TestClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class DataflowAnalyzer {
	public static void main(String[] args) {
		AnalysisContext context = new AnalysisContext();
		ResolvedJavaMethod seed_method = context.findMethod(TestClass.class, "call");
		
		Scanner scanner = new Scanner(context);
		scanner.includePackage("org.prettycat.examples");
		scanner.excludePackage("org.prettycat");

		Path path = Paths.get("./out.xml");
		
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.setXmlStandalone(true);
			
			Element root = XMLProtocol.createGraphElement(doc);
			doc.appendChild(root);

			for (ResolvedJavaMethod method: scanner.findAllMethods(seed_method)) {
				MethodHandler handler = new MethodHandler(context, method);
				root.appendChild(handler.newXMLGenerator().makeElement(doc));
				// System.out.println(method.getDeclaringClass().toClassName() + "." + method.getName());
			}
			
			try (BufferedWriter backend_writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(backend_writer));
			} catch (IOException e) {
				System.err.format("failed to write to %s: %s\n", path, e);
			}
		} catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println("written!");
		
		/*toVisit.add(seed_method);

		StructuredGraph graph = new StructuredGraph.Builder(options, AllowAssumptions.YES).method(seed_method).build();
		graphBuilderSuite.apply(graph, context);
		graph.getNodes().filter(PhiNode.class).forEach(PhiNode::inferStamp);

		for (Node node : graph.getNodes()) {
			//System.out.println(node);
			if (node instanceof MethodCallTargetNode) {
				MethodCallTargetNode target = (MethodCallTargetNode) node;
				toVisit.add(target.targetMethod());
			}
		}

		while (!toVisit.isEmpty()) {
			ResolvedJavaMethod method = toVisit.pop();
			System.out.println("=== BEGIN OF METHOD " + method.getName() + " ===");
			if (!method.getDeclaringClass().toJavaName().startsWith("org.rosi.ccc.")) {
				System.out.println("not analysing outside org.rosi.ccc...");
				continue;
			}
			
			graph = new StructuredGraph.Builder(options, AllowAssumptions.YES).method(method)
					.build();
			graphBuilderSuite.apply(graph, context);
			graph.getNodes().filter(PhiNode.class).forEach(PhiNode::inferStamp);
			
			ControlFlowGraph cfg = ControlFlowGraph.compute(graph, true, true, true, true);
			
			writeCDFG2(method, graph, cfg);
			
			for (Node node : graph.getNodes()) {
				// System.out.println(node);
				if (node instanceof MethodCallTargetNode) {
					MethodCallTargetNode target = (MethodCallTargetNode) node;
					toVisit.add(target.targetMethod());
				}
			}
		}*/

	}

}
