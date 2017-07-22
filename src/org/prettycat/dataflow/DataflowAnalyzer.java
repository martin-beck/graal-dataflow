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
	
	private static void writeCDFG(
			ResolvedJavaMethod method,
			StructuredGraph graph
			)
	{
		String fqdn = method.getDeclaringClass().toClassName() + "." + method.getName();
		System.err.println("writing analysis of "+fqdn+" ...");
		Path path = Paths.get("./"+fqdn+".dot");
		try (BufferedWriter backend_writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
			PrintWriter out = new PrintWriter(backend_writer);
			out.format("digraph %s {\n", method.getName());
			/*for (Block block : cfg.getBlocks()) {
				out.format("b%d [label=\"block %d\"];\n", block.getId(), block.getId());
				/*System.out.println("  nodes:");
				for (Node node : block.getNodes()) {
					System.out.println("    " + node);
				}
				System.out.println("  successors:");*//*
				for (Node successor : block.getEndNode().cfgSuccessors()) {
					Block successor_block = cfg.blockFor(successor);
					out.format("b%d -> b%d;\n", block.getId(), successor_block.getId());
					/*System.out.println("    block " + successor_block.getId() + " / " + successor);*//*
				}
			}*/
			for (Node node: graph.getNodes()) {
				out.format("n%s [label=\"%s\"]\n", node.toString(Verbosity.Id), node.toString(Verbosity.Name));
				for (Node successor: node.successors()) {
					out.format("n%s -> n%s\n", node.toString(Verbosity.Id), successor.toString(Verbosity.Id));
				}
			}
			out.println("}");
		} catch (IOException e) {
			System.err.format("failed to write to %s: %s\n", path, e);
		}
	}
	
	private static void writeCDFG2(
			ResolvedJavaMethod method,
			StructuredGraph graph,
			ControlFlowGraph cfg
			)
	{
		String fqdn = method.getDeclaringClass().toClassName() + "." + method.getName();
		System.err.println("writing analysis of "+fqdn+" ...");
		Path path = Paths.get("./"+fqdn+".dot");
		try (BufferedWriter backend_writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
			PrintWriter out = new PrintWriter(backend_writer);
			out.format("digraph %s {\n", method.getName());
			/*for (Block block : cfg.getBlocks()) {
				out.format("b%d [label=\"block %d\"];\n", block.getId(), block.getId());
				/*System.out.println("  nodes:");
				for (Node node : block.getNodes()) {
					System.out.println("    " + node);
				}
				System.out.println("  successors:");*//*
				for (Node successor : block.getEndNode().cfgSuccessors()) {
					Block successor_block = cfg.blockFor(successor);
					out.format("b%d -> b%d;\n", block.getId(), successor_block.getId());
					/*System.out.println("    block " + successor_block.getId() + " / " + successor);*//*
				}
			}*/
			for (Node node: graph.getNodes()) {
				Block block = cfg.blockFor(node);
				int block_id = -1;
				if (block != null) {
					block_id = block.getId();
				}
				out.format("n%s [label=\"%d/%s:%s\"]\n", node.toString(Verbosity.Id), block_id, node.toString(Verbosity.Id), node.toString(Verbosity.Name));
				
				int ninput = 0;
				for (Node input: node.inputs()) { 
					out.format("n%s -> n%s [style=dashed,headlabel=\"in %d\"]\n", input.toString(Verbosity.Id), node.toString(Verbosity.Id), ninput);
					ninput += 1;
				}
			}
			for (Block block: cfg.getBlocks()) {
				for (Node node: block.getNodes()) {
					int nbranch = 0;
					for (Node successor: node.cfgSuccessors()) {
					    out.format("n%s -> n%s [taillabel=\"branch %d\"]\n", node.toString(Verbosity.Id), successor.toString(Verbosity.Id), nbranch);
					    nbranch += 1;
					}
				}
			}
			out.println("}");
		} catch (IOException e) {
			System.err.format("failed to write to %s: %s\n", path, e);
		}
	}

	public static void main(String[] args) {
		AnalysisContext context = new AnalysisContext();
		ResolvedJavaMethod seed_method = context.findMethod(TestClass.class, "call");

		Path path = Paths.get("./out.xml");
		
		MethodHandler handler = new MethodHandler(context, seed_method);
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			doc.setXmlStandalone(true);
		
			Element el = handler.newXMLGenerator().makeElement(doc);
			doc.appendChild(el);

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
