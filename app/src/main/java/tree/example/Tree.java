package tree.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

public class Tree {
    public static void main(String[] args) throws IOException {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());
        
        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        var sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(Tree.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        var comp = sourceRoot.parse("", "App.java");

        Log.info("Positivizing!");

        var treeContent = new StringBuilder();

        comp.findFirst(MethodDeclaration.class).get()
            .findFirst(ExpressionStmt.class).get()
            .findFirst(MethodCallExpr.class).get()
            .walk(n -> treeContent.append(isComment(n) ? "" : n.getClass().getSimpleName() + 
                " : " + 
                n.getRange() + 
                " : " +
                n.toString() + " : " +
                (isMethodCallExpr(n) ? "" : n.getParentNode().get().toString() + " : ") +
                (isBinExpr(n) ? " : " + ((BinaryExpr)n).getOperator() : "") + "\n"
            )
        );

                
        Files.writeString(Path.of("tree_content.txt"), treeContent.toString(), StandardOpenOption.CREATE);
    }

    private static boolean isComment(Node n) {
        return n.getClass().getSimpleName().equals("LineComment");
    }

    private static boolean isBinExpr(Node n) {
        return n.getClass().getSimpleName().equals("BinaryExpr");
    }

    private static boolean isMethodCallExpr(Node n) {
        return n.getClass().getSimpleName().equals("MethodCallExpr");
    }
}
