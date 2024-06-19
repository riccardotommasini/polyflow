package org.streamreasoning.rsp4j.api.sds.timevarying;

import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAG;
import org.streamreasoning.rsp4j.api.operators.r2r.DAG.DAGNode;
import org.streamreasoning.rsp4j.api.sds.SDS;

public class LazyTimeVarying<R extends Iterable<?>> implements TimeVarying<R> {

    DAGNode<R> dag;
    R content;

    String name;

    public LazyTimeVarying(DAGNode<R> dag, String name){
        this.dag = dag;
        this.name = name;
    }
    public LazyTimeVarying(DAGNode<R> dag, String name){
        this.dag = dag;
        this.name = name;
    }

    @Override
    public void materialize(long ts) {
        content = dag.eval(ts);
        if(content == null){
            throw new RuntimeException("Result of DAG computation is null");
        }
    }

    @Override
    public R get() {
        return content;
    }

    @Override
    public String iri() {
        return name;
    }

    @Override
    public void setIri(String name) {
        this.name = name;
    }

    @Override
    public boolean named() {
        return true;
    }
}
