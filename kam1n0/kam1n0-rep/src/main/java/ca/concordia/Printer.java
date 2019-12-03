package ca.concordia;

import ca.mcgill.sis.dmas.kam1n0.framework.storage.Function;
import ca.mcgill.sis.dmas.nlp.model.astyle._1_original.FuncTokenized;
import scala.util.parsing.combinator.testing.Str;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Printer {
    private final static String Root = "D:/asm2vec/log/";

    private final static List<String> functionNames = Arrays.asList(
            "BN_options",
            "X509_STORE_CTX_new",
            "CRYPTO_new_ex_data",
            "CRYPTO_realloc",
            "CRYPTO_get_new_lockid",
            "CRYPTO_realloc_clean",
            "int_dup_ex_data",
            "SSLeay_version",
            "int_dup_ex_data",
            "SHA512",
            "old_hmac_decode",
            "CAST_set_key",
            "call_weak_fn",
            "CONF_modules_load",
            "_statistical"
    ).stream().map(String::toLowerCase).collect(Collectors.toList());

    public static void PrintOriginal(Function function) {
        String functionName = function.functionName;
        List<List<String>> asmLines = function.blocks.stream().map(block -> block.getAsmLines()).flatMap(List::stream).collect(Collectors.toList());
        List<String> input = asmLines.stream().map(in -> String.join(" ", in)).collect(Collectors.toList());
        Print(functionName, input, "original");
    }

    public static void PrintOriginal(String functionName, List<String> input) {
        Print(functionName, input, "original");
    }

    public static void PrintTokenize(String functionName, List<String> input) {
        Print(functionName, input, "tokenize");
    }

    public static void PrintTokenize(FuncTokenized functionTokenized) {
        String functionName = functionTokenized.name;

        List<List<String>> asmLines = functionTokenized.blks.stream().map(block -> block.ins).flatMap(List::stream).collect(Collectors.toList());
        List<String> input = asmLines.stream().map(in -> String.join(" ", in)).collect(Collectors.toList());
        Print(functionName, input, "tokenize");
    }

    public static void PrintInline(String functionName, List<String> input) {
        Print(functionName, input, "inline");
    }

    public static void PrintRandomWalk(String functionName, List<String> input) {
        Print(functionName, input, "randomwalk");
    }

    public static void PrintAfterFiltration(String functionName, List<String> input) {
        Print(functionName, input, "filtration");
    }

    public static void PrintAfterFiltration(String functionName, String appendName, List<String> input) {
        Print(functionName, input, "filtration" + "_" + appendName);
    }

    public static void PrintResults(String functionName, List<String> input) {
        Print(functionName, input, "result");
    }


    public static void PrintStatisticsResults(String functionName, List<String> input) {
        Print(functionName, input, "result");
    }

    private static void Print(String functionName, List<String> input, String type) {
        if(functionName == null){
            return;
        }
        if (!functionNames.contains(functionName.toLowerCase())) {
            return;
        }
        Stream<String> lines = input.stream();
        final FileWriter fw;
        try {
            fw = new FileWriter(Root + functionName + "_" + type + "_" + new Date().getTime());
            lines.forEach(str -> {
                try {
                    fw.write(str);
                    fw.write(System.getProperty("line.separator"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            lines.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
