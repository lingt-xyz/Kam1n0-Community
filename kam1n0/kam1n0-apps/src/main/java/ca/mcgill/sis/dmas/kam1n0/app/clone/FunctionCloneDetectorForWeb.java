/*******************************************************************************
 * Copyright 2017 McGill University All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ca.mcgill.sis.dmas.kam1n0.app.clone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.mcgill.sis.dmas.env.LocalJobProgress;
import ca.mcgill.sis.dmas.env.StringResources;
import ca.mcgill.sis.dmas.env.LocalJobProgress.StageInfo;
import ca.mcgill.sis.dmas.io.collection.Counter;
import ca.mcgill.sis.dmas.kam1n0.app.adata.FunctionDataUnit;
import ca.mcgill.sis.dmas.kam1n0.app.clone.adata.FunctionCloneDetectionResultForWeb;
import ca.mcgill.sis.dmas.kam1n0.app.clone.adata.FunctionCloneEntryForWeb;
import ca.mcgill.sis.dmas.kam1n0.framework.disassembly.BinarySurrogate;
import ca.mcgill.sis.dmas.kam1n0.framework.storage.Binary;
import ca.mcgill.sis.dmas.kam1n0.framework.storage.Function;
import ca.mcgill.sis.dmas.kam1n0.problem.clone.FunctionCloneDetector;
import ca.mcgill.sis.dmas.kam1n0.utils.executor.SparkInstance;

public class FunctionCloneDetectorForWeb {

	public FunctionCloneDetector detector = null;
	private static Logger logger = LoggerFactory.getLogger(FunctionCloneDetectorForWeb.class);

	public FunctionCloneDetectorForWeb(FunctionCloneDetector detector) {
		this.detector = detector;
	}

	/**
	 *
	 * @param rid Application Id
	 * @param binary
	 * @param threshold Similarity score threshold
	 * @param topK
	 * @param avoidSameBinary
	 * @param progress
	 * @return
	 * @throws Exception
	 */
	public ArrayList<FunctionCloneDetectionResultForWeb> detectClones(long rid, BinarySurrogate binary,
			double threshold, int topK, boolean avoidSameBinary, LocalJobProgress progress) throws Exception {

		StageInfo stage = progress.nextStage(FunctionCloneDetectorForWeb.class, "Detecting clones [" + binary.functions.size() + " funcs]");

		List<Function> funcs = binary.toFunctions();
		ArrayList<FunctionCloneDetectionResultForWeb> fullResults = new ArrayList<>();

		Counter counter = Counter.zero();
		long start = System.currentTimeMillis();
		String omString = stage.msg;
		Counter gate = new Counter();
		gate.inc(100);
//		ForkJoinPool pool = new ForkJoinPool(8);
//		pool.submit(() -> {

			List<FunctionCloneDetectionResultForWeb> ls = IntStream.range(0, funcs.size()).parallel()
					.mapToObj(ind -> {
						Function func = funcs.get(ind);

						if (progress.interrupted)
							return null;
						
//						try {
//							SparkInstance.checkAndWait();
//						} catch (Exception e1) {
//							logger.warn("Failed to check spark status.", e1);
//						}
						/**
						 * Visualization purpose
						 */
						counter.inc();
						stage.progress = counter.getVal() * 1.0 / funcs.size();
						logger.info("{} queued {} bks named {}", StringResources.FORMAT_AR4D.format(stage.progress),
								func.blocks.size(), func.functionName);
						if(counter.getVal() > gate.getVal()) {
							gate.inc(100);
							double eta = (System.currentTimeMillis() - start) / stage.progress / 1000 / 60;
							double taken =  (System.currentTimeMillis() - start) / 1000 / 60;
							stage.msg = omString + " Taken " + StringResources.FORMAT_AR2D.format(taken) + " mins. Finishing in " + StringResources.FORMAT_AR2D.format(eta - taken) + " mins.";
						}

						try {
							return this.detectClones(rid, func, threshold, topK, avoidSameBinary);
						} catch (Exception e) {
							logger.error("Failed to detect clone for " + func.functionName, e);
							return null;
						}
					}).filter(re -> re != null).collect(Collectors.toList());
			fullResults.addAll(ls);
//		}).get();
//		pool.shutdownNow();

		stage.complete();

		if (progress.interrupted)
			throw new Exception("This job is being interrupted.. cancelling job.");

		//
		// for (Function function : binary.toFunctions()) {
		// FunctionCloneDetectionResultForWeb res = this.detectClones(function,
		// threashold);
		// if (res != null)
		// fullResults.add(res);
		// progress.currentProgress = count * 1.0 / total;
		// count++;
		// }
		this.printResult(fullResults);

		return fullResults;
	}

	// precision and recall
	// https://en.wikipedia.org/wiki/Precision_and_recall
	// precision = true positive / (true positives + false positives)
	// precision is the fraction of retrieved documents that are relevant to the query
	// For example, for a text search on a set of documents, precision is the number of correct results divided by the number of all returned results.
	// recall = true positive / (true positives + false negatives)
	// recall is the fraction of the relevant documents that are successfully retrieved
	// For example, for a text search on a set of documents, recall is the number of correct results divided by the number of results that should have been returned.
	private void printResult(ArrayList<FunctionCloneDetectionResultForWeb> fullResults){
		List<String> results = new ArrayList<>();

		AtomicInteger noneMatch = new AtomicInteger();
		List<String> noneMatch_results = new ArrayList<>();
		noneMatch_results.add("----------------------------------------------------------------did not find a match------------------------------------------------------------------");
		AtomicInteger matched = new AtomicInteger();
		List<String> matched_results = new ArrayList<>();
		matched_results.add("----------------------------------------------------------------matched expected------------------------------------------------------------------");
		AtomicInteger matched_more_than_05 = new AtomicInteger();
		List<String> matched_more_than_05__results = new ArrayList<>();
		matched_more_than_05__results.add("----------------------------------------------------------------number of blocks >= 1.5------------------------------------------------------------------");
		AtomicInteger notMatched = new AtomicInteger();
		List<String> not_matched_results = new ArrayList<>();
		not_matched_results.add("----------------------------------------------------------------matched unexpected------------------------------------------------------------------");
		fullResults.stream().forEach(r -> {
			String functionName = r.function.functionName;
			if(r.clones.isEmpty()){
				noneMatch.getAndIncrement();
				noneMatch_results.add(functionName);
			}else{
				FunctionCloneEntryForWeb f = r.clones.get(0);
				if(f.functionName.equalsIgnoreCase(functionName)){
					matched.getAndIncrement();
					matched_results.add(f.functionName + " " + f.similarity);
					if(r.function.blockSize >= f.numBbs * 1.5){
						matched_more_than_05.getAndIncrement();
						matched_more_than_05__results.add(f.functionName + " " + f.similarity);
					}
				}else{
					notMatched.getAndIncrement();
					not_matched_results.add("expected " + functionName + ", matched " + f.functionName);
				}
			}

		});
		results.add("did not find a match: " + noneMatch);
		results.add("matched expected: " + matched);
		results.add("matched_more_than_05: " + matched_more_than_05);
		results.add("matched unexpected: " + notMatched);
		results.addAll(noneMatch_results);
		results.addAll(matched_results);
		results.addAll(matched_more_than_05__results);
		results.addAll(not_matched_results);
		ca.concordia.Printer.PrintStatisticsResults("_statistical", results);
	}

	public FunctionCloneDetectionResultForWeb detectClones(long rid, Function function, double threshold, int topK,
			boolean avoidSameBinary) throws Exception {
		FunctionCloneDetectionResultForWeb reslt = new FunctionCloneDetectionResultForWeb();
		detector.detectClonesForFunc(rid, function, threshold, topK, avoidSameBinary).stream()
				.map(entry -> new FunctionCloneEntryForWeb(entry)).filter(entry -> entry.similarity >= threshold)
				.forEach(reslt.clones::add);
		reslt.function = new FunctionDataUnit(function);
		return reslt;
	}

	public void indexFuncs(long rid, Binary bianry, LocalJobProgress progress) throws Exception {
		detector.index(rid, progress, bianry);
	}

	public String params() {
		return StringResources.JOINER_TOKEN_CSV.join(detector.params(), "detectorWrapper=",
				this.getClass().getSimpleName());
	}

	public void init() throws Exception {
		detector.init();
	}

	public void close() throws Exception {
		detector.close();
	}

	public void clear(long rid) {
		this.detector.clear(rid);
	}

}
