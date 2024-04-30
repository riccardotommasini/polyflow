package graph.jena.content;

import org.apache.jena.shacl.Shapes;
import org.streamreasoning.rsp4j.api.secret.content.Content;

public interface ValidatedContent<I, W, R> extends Content<I, W, R> {

    enum ValidationOption {
        STREAM_LEVEL,
        CONTENT_LEVEL
    }
    void setShapes(Shapes shapes);

    Shapes getShapes();

    void setValidationOption(ValidationOption vo);
    ValidationOption getValidationOption(ValidationOption vo);
}
