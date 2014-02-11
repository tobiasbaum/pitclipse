package org.pitest.pitclipse.pitrunner;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Integer.valueOf;

import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;

import org.pitest.mutationtest.commandline.MutationCoverageReport;
import org.pitest.pitclipse.pitrunner.client.PitClient;

@ThreadSafe
public class PitRunner {

	public static void main(String[] args) {
		validateArgs(args);
		int port = valueOf(args[0]);
		PitClient client = new PitClient(port);
		try {
			client.connect();
			System.out.println("Connected");
			PitRequest request = client.readRequest();
			System.out.println("Received request: " + request);
			PitResults results = new PitRunner().runPIT(request);
			System.out.println("Sending results: " + results);
			client.sendResults(results);
		} finally {
			try {
				System.out.println("Closing server");
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Closed");
		}
	}

	public PitResults runPIT(PitRequest request) {
		String[] cliArgs = PitCliArguments.from(request.getOptions());
		// EntryPoint e = new EntryPoint();
		// ReportOptions data = new ReportOptions();
		// data.addOutputFormats(asList("XML", "HTML"));
		// data.setClassPathElements(cliArgs.)
		// e.execute(null, data);
		MutationCoverageReport.main(cliArgs);
		File reportDir = request.getReportDirectory();
		File htmlResultFile = findResultFile(reportDir, "index.html");
		File xmlResultFile = findResultFile(reportDir, "mutations.xml");
		return PitResults.builder().withHtmlResults(htmlResultFile).withXmlResults(xmlResultFile)
				.withProjects(request.getProjects()).build();
	}

	private static void validateArgs(String[] args) {
		checkArgument(args.length == 1);
	}

	private static File findResultFile(File reportDir, String fileName) {
		for (File file : reportDir.listFiles()) {
			if (fileName.equals(file.getName())) {
				return file;
			}
		}
		for (File file : reportDir.listFiles()) {
			if (file.isDirectory()) {
				File result = findResultFile(file, fileName);
				if (null != result) {
					return result;
				}
			}
		}
		return null;
	}

}
