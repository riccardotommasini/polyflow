package graph.neo.seraph.syntax;

import graph.neo.seraph.R2R;
import graph.neo.seraph.R2S;
import graph.neo.seraph.S2R;
import graph.neo.seraph.SeraphQuery;
import org.antlr.v4.runtime.tree.ParseTree;

import java.time.Duration;
import java.util.*;

public class SeraphVisitorImpl extends SeraphBaseVisitor<SeraphQuery> {


    private Map<String, S2R> inputs = new HashMap<>();
    private Map<String, R2S> outputs = new HashMap<>();
    private Map<String, String> inputParameters = new HashMap<>();
    private Map<String, Object> outputParameters = new HashMap<>();
    private List<String> projections = new ArrayList<>();
    private Map<String, List<StringBuilder>> relationParameters = new HashMap<>();

    public SeraphVisitorImpl() {

        relationParameters.put("range", new ArrayList<>());
        relationParameters.put("r2r", new ArrayList<>(Collections.singletonList(new StringBuilder())));

    }


    @Override
    public SeraphQuery visitOC_Seraph(SeraphParser.OC_SeraphContext ctx) {

        // *S2R Part
        inputParameters.put("identifier", ctx.id.getText());

        String defaultInputStream = null;

        inputParameters.put("input", defaultInputStream);
        inputParameters.put("starting", ctx.oS_T0().getText());


        // *R2S Part
        Duration period = Duration.parse(ctx.range.getText().trim());
        String streamOperator = ctx.stream_op.getText();

        String defaultOutputStream = null;

        outputParameters.put("output", defaultOutputStream);
        outputParameters.put("period", period.toString());
        outputParameters.put("streamOperator", streamOperator);
        outputParameters.put("projections", projections);

        return super.visitOC_Seraph(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionBody(SeraphParser.OC_ProjectionBodyContext ctx) {

        return super.visitOC_ProjectionBody(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionItem(SeraphParser.OC_ProjectionItemContext ctx) {
        String text = "";
        if (ctx.children.size() > 1) {
            text = ctx.children.get(ctx.children.size() - 1).getText();
        } else
            text = ctx.children.get(0).getText();
        projections.add(text);
        return super.visitOC_ProjectionItem(ctx);
    }

    @Override
    public SeraphQuery visitOC_ProjectionItems(SeraphParser.OC_ProjectionItemsContext ctx) {

        return super.visitOC_ProjectionItems(ctx);
    }

    @Override
    public SeraphQuery visitOC_Return(SeraphParser.OC_ReturnContext ctx) {

        outputParameters.put("returnStatement", ctx.children.get(0).getText());
        String returnStatement = ctx.children.get(1).getText();


        for (StringBuilder r2r : relationParameters.get("r2r")) {
            r2r.append(" RETURN").append(returnStatement);
        }

        relationParameters.get("r2r").remove(relationParameters.get("r2r").size() - 1);

        return super.visitOC_Return(ctx);
    }


    @Override
    public SeraphQuery visitOC_Match(SeraphParser.OC_MatchContext ctx) {

        StringBuilder cypherMatch = new StringBuilder();

        for (ParseTree subTree : ctx.children) {
            if (subTree.getClass() == SeraphParser.OC_WithinContext.class) {
                String range = ((SeraphParser.OC_WithinContext) subTree).ISO8601_DURATION().getText().trim();

                relationParameters.get("range").add(new StringBuilder(range));
            } else {
                cypherMatch.append(subTree.getText());

            }
        }


        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(cypherMatch);
        relationParameters.get("r2r").add(new StringBuilder());

        return super.visitOC_Match(ctx);
    }


    @Override
    public SeraphQuery visitOC_With(SeraphParser.OC_WithContext ctx) {
        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(ctx.getText());

        return super.visitOC_With(ctx);
    }

    public SeraphQuery visitOC_Unwind(SeraphParser.OC_UnwindContext ctx) {
        relationParameters.get("r2r").get(relationParameters.get("r2r").size() - 1).append(ctx.getText());

        return super.visitOC_Unwind(ctx);
    }

    //returns the parsed seraph query
    public SeraphQuery getQuery() {

//        System.out.println("-----------------PARSING-----------------");
//
//        System.out.println("input:    " + inputParameters);
        System.out.println("relation: " + relationParameters);
//        System.out.println("output:   " + outputParameters);

        StringBuilder query = new StringBuilder();

        relationParameters.get("r2r").stream()
                .map(StringBuilder::toString)
                .forEach(q -> query.append(q + "\n"));

        String inputStream = inputParameters.get("input");
        String startingTime = inputParameters.get("starting");
        Duration range = Duration.parse(relationParameters.get("range").get(0));

        inputs.put(inputStream, new S2R(startingTime, range));

        String outputStream = (String) outputParameters.get("output");
        String streamOperator = (String) outputParameters.get("streamOperator");
        Duration period = Duration.parse((CharSequence) outputParameters.get("period"));

        outputs.put(outputStream, new R2S(streamOperator, period, null));

        System.out.println();

        return new SeraphQuery(inputParameters.get("identifier"), new R2R(query.toString()), inputs, outputs, projections);
    }


}
